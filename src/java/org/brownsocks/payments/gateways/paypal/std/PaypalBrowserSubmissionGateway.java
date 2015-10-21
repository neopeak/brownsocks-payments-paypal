package org.brownsocks.payments.gateways.paypal.std;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.brownsocks.payments.PaymentRequest;
import org.brownsocks.payments.PaymentResult;
import org.brownsocks.payments.gateways.BrowserSubmissionGateway;
import org.brownsocks.payments.gateways.GatewayInitializationException;
import org.brownsocks.payments.gateways.PaymentsListenerSupport;
import org.brownsocks.payments.gateways.UnsupportedGatewayRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaypalBrowserSubmissionGateway extends PaymentsListenerSupport implements BrowserSubmissionGateway<PaypalStandardAccount> {

	private static final Logger logger = LoggerFactory.getLogger(PaypalBrowserSubmissionGateway.class); 
	
	// timeouts for IPN post-back to paypal
	public static final int SO_TIMEOUT = 60 * 5 * 1000; // 2 minutes
	public static final int CONNECTION_TIMEOUT = 60 * 1 * 1000; // 1 minute
	
	private PaypalStandardAccount _account;
	
	@Override
	public void setGatewayAccount(PaypalStandardAccount gatewayAccount) {
		_account = gatewayAccount;
	}

	@Override
	public void initialize() throws GatewayInitializationException {
		/* MAKE SURE MERCHANT ACCOUNT CFG IS SET */
		if (_account == null)
			throw new GatewayInitializationException("gatewayAccount was not set.");

	}

	@Override
	public PaymentResult lookup(String merchantTxnID)
			throws UnsupportedGatewayRequestException {
		throw new UnsupportedGatewayRequestException();
	}

	@Override
	public String getPaymentURL(PaymentRequest request) {
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		parameters.put("cmd", "_ext-enter");
        parameters.put("redirect_cmd", "_xclick");
        parameters.put("business", _account.getEmail());
        
        if (_account.getImageURL() != null)
        	parameters.put("image_url", _account.getImageURL());
        
        parameters.put("undefined_quantity", "0");
        parameters.put("no_note", "1");
        parameters.put("no_shipping", "1");
        parameters.put("amount", request.getCurrencyAmount().toString());
        parameters.put("currency_code", request.getCurrency());
        parameters.put("custom", request.getMerchantTxnID());
        parameters.put("item_name", request.getItemDescription());
        
        parameters.put("first_name", request.getCustomerInfo().getFirstName());
        parameters.put("last_name", request.getCustomerInfo().getLastName());
        parameters.put("address1", request.getCustomerInfo().getAddressLine1());
        parameters.put("address2", request.getCustomerInfo().getAddressLine2());
        parameters.put("city", request.getCustomerInfo().getCity());
        parameters.put("state", request.getCustomerInfo().getState());
        parameters.put("zip", request.getCustomerInfo().getZipcode());
        parameters.put("country", request.getCustomerInfo().getCountry());
        parameters.put("lc", request.getCustomerInfo().getLanguage().toString());
        
        if (_account.getIpnNotifyURL() != null)
        	parameters.put("notify_url", _account.getIpnNotifyURL());
		
        try {
        	return getURL(_account.getPaymentURL(), parameters);
        	
        } catch (UnsupportedEncodingException e) {
        	logger.warn("Failed to URL encode payment url", e);
        	throw new IllegalArgumentException(e.getMessage(), e);
        	
        }
	}
	
	private String getURL(String url, Map<String, String> params) throws UnsupportedEncodingException {
		
		StringBuilder builder = new StringBuilder(url);
		builder.append("?");
		
		boolean first = true;
		for (String name : params.keySet()) {
			String val = params.get(name);
			if (val == null)
				val = "";
			
			if (!first)
				builder.append("&");
			else
				first = false;
			
			builder.append(name);
			builder.append("=");
			builder.append(URLEncoder.encode(val, "UTF-8"));
			
		}
		
		return builder.toString();
		
	}
	
	/**
	 * Handles the IPN message from paypal.
	 * 
	 * Parse the message, do the verification and post the event to the application.
	 * 
	 * @param request
	 */
	void handleIPNRequest(HttpServletRequest request) {
		
		IOException verifyingException = null;
		String verifyResponse = null;
		
		try {
			verifyResponse = verifyWithPaypal(request);
			
		} catch (IOException e) {
			verifyingException = e;
			
		}
		
		PaypalIPNMessage ipnMessage = new PaypalIPNMessage(request, verifyResponse, verifyingException);
		PaymentResult result = ipnMessage.getPaymentResult();

		if (result == null)
			return; // not a valid IPN payment notification, ignore.
		
		sendPaymentReceivedEvent(result);
		
	}
	
	private String verifyWithPaypal(HttpServletRequest request) throws HttpException, IOException {
		//create a singular HttpClient object
        HttpClient client = getDefaultHttpClient();
        
        PostMethod method = new PostMethod(_account.getPaymentURL()); 
        
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
			String name = (String) paramNames.nextElement();
			String value = request.getParameter(name);
			method.addParameter(name, value);
		}
        
        // add the command parameter
        method.addParameter("cmd", "_notify-validate");

    	client.executeMethod(method);
    	return method.getResponseBodyAsString();
	}
	
	private HttpClient getDefaultHttpClient() {
		HttpClient httpclient = new HttpClient();
		httpclient.setConnectionTimeout(CONNECTION_TIMEOUT);
		httpclient.setTimeout(SO_TIMEOUT);
		return httpclient;
	}

}
