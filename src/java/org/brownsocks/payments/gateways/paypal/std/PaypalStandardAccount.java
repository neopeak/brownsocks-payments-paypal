package org.brownsocks.payments.gateways.paypal.std;

import java.io.Serializable;

import org.brownsocks.payments.gateways.GatewayAccount;

public class PaypalStandardAccount implements GatewayAccount, Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PRODUCTION_PAYMENT_URL = "https://www.paypal.com/cgi-bin/webscr";
	public static final String SANDBOX_PAYMENT_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr";
	
	private String _paymentURL = PRODUCTION_PAYMENT_URL;
	
	private String _email;
	
	private String _imageURL;
	
	private String _ipnNotifyURL;

	@Override
	public boolean isTestAccount() {
		return !(PRODUCTION_PAYMENT_URL.equalsIgnoreCase(_paymentURL));
	}
	
	public String getPaymentURL() {
		return _paymentURL;
	}

	public void setPaymentURL(String paymentURL) {
		_paymentURL = paymentURL;
	}

	/**
	 * The 'business' parameter, where money will be sent.
	 * @return
	 */
	public String getEmail() {
		return _email;
	}

	public void setEmail(String email) {
		_email = email;
	}

	/**
	 * Optional. URL to an image (logo) to display on payment page.
	 * @return
	 */
	public String getImageURL() {
		return _imageURL;
	}

	public void setImageURL(String imageURL) {
		_imageURL = imageURL;
	}

	public String getIpnNotifyURL() {
		return _ipnNotifyURL;
	}

	public void setIpnNotifyURL(String ipnNotifyURL) {
		_ipnNotifyURL = ipnNotifyURL;
	}

}
