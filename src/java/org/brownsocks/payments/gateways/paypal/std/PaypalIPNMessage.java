package org.brownsocks.payments.gateways.paypal.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.brownsocks.payments.PaymentOperation;
import org.brownsocks.payments.PaymentResult;
import org.brownsocks.payments.PaymentResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to parse the IPN message from Paypal. 
 * 
 * Only useful parameters for payment processing are parsed here. 
 * 
 * @author cveilleux
 */
public class PaypalIPNMessage {
	
	private static final Logger logger = LoggerFactory.getLogger(PaypalIPNMessage.class); 
	
	private String txnType;
	
	private String business;
	
	private String custom;
	
	private String receiverEmail;
	
	private String txnId;
	
	private String mcCurrency;
	
	private String mcGross;
	
	private String paymentStatus;
	
	private PaymentResult result;
	
	public PaypalIPNMessage() {
		super();
	}

	public PaypalIPNMessage(HttpServletRequest request, String paypalVerificationResponse, IOException verificationException) {
		txnType = request.getParameter("txn_type");
		business = request.getParameter("business");
		custom = request.getParameter("custom");
		receiverEmail = request.getParameter("receiver_email");
		txnId = request.getParameter("txn_id");
		mcCurrency = request.getParameter("mc_currency");
		mcGross = request.getParameter("mc_gross");
		paymentStatus = request.getParameter("payment_status");
		
		result = convertToPaymentResult(request, paypalVerificationResponse, verificationException);
	}
	
	private PaymentResult convertToPaymentResult(HttpServletRequest request, String paypalVerificationResponse, IOException verificationException) {
		PaymentResult result = new PaymentResult(PaymentOperation.SALE);
		
		result.setAmount(getAmount());
		result.setConfirmationCode(getTxnId());
		result.setMerchantTxnID(getCustom());
		result.setCurrency(getMcCurrency());
		
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String val = request.getParameter(name);
			result.addMeta(name, val);
		}
		
		
		if (verificationException != null) {
			result.setResultType(PaymentResultType.ERROR_NETWORK);
			result.setErrorMessage(verificationException.getClass().getSimpleName() + ": " + verificationException.getMessage());
			
		} else {
		
			if ("VERIFIED".equals(paypalVerificationResponse)) {
				
				if (isWebAcceptTxnType() && isCompletedPaymentStatus()) {
					result.setResultType(PaymentResultType.SUCCESS);
					
					
				} else {
					logger.warn("IPN message txn_type is not 'web_accept' or payment_status is not 'Completed'. IPN will be ignored.");
					logger.warn("txn_type was : " + getTxnType());
					logger.warn("payment_status was : " + getPaymentStatus());
					
					return null;
				}
				
				
			} else {
				result.setResultType(PaymentResultType.ERROR_SUSPECT);
				result.setErrorMessage("IPN verification failed with message: " + paypalVerificationResponse);
			}
		}
		
		
		result.markCompletion();
		
		return result;
	}
	
	public PaymentResult getPaymentResult() {
		return result;
	}

	public int getAmount() {
		if (StringUtils.isEmpty(mcCurrency))
			return 0;
		
		Currency currency = Currency.getInstance(mcCurrency);
		
		BigDecimal amount = new BigDecimal(mcGross);

		amount = amount.movePointRight(currency.getDefaultFractionDigits());
		return amount.intValue();
	}

	public String getTxnType() {
		return txnType;
	}
	
	public boolean isWebAcceptTxnType() {
		return ("web_accept".equals(getTxnType()));
	}
	
	public boolean isCompletedPaymentStatus() {
		return ("Completed".equals(getPaymentStatus()));
	}

	public String getBusiness() {
		return business;
	}

	public String getCustom() {
		return custom;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public String getTxnId() {
		return txnId;
	}

	public String getMcCurrency() {
		return mcCurrency;
	}

	public String getMcGross() {
		return mcGross;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}
	
}
