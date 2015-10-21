package org.brownsocks.payments.gateways.paypal.std;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaypalIPNServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = LoggerFactory.getLogger(PaypalIPNServlet.class);  

	private PaypalBrowserSubmissionGateway _paypalGateway;
	
	/**
	 * Init param name for the name of the servlet context attribute containing a reference
	 * to the EnetsServerGateway.
	 */
	public static final String PAYPAL_GATEWAY_KEY = "paypal-gateway";
	
	public static final String DEFAULT_PAYPAL_GATEWAY_ATTRIBUTE_NAME = "paypalGateway";
	
	@Override
	public void init() throws ServletException {
		
		if (_paypalGateway != null)
			return; // already initialized, possibly through setEnetsServerGateway()
		
		String paypalGatewayAttributeName = getServletConfig().getInitParameter(PAYPAL_GATEWAY_KEY);
		
		if (StringUtils.isEmpty(paypalGatewayAttributeName))
			paypalGatewayAttributeName = DEFAULT_PAYPAL_GATEWAY_ATTRIBUTE_NAME;
		
		_paypalGateway = (PaypalBrowserSubmissionGateway) getServletContext().getAttribute(paypalGatewayAttributeName);
		if (_paypalGateway == null) {
			_log.warn("You need to instantiate and place a PaypalBrowserSubmissionGateway instance under the " + paypalGatewayAttributeName + " servlet context attribute.");
			throw new ServletException("No paypal gateway found. You need to instantiate and place a PaypalBrowserSubmissionGateway instance under the " + paypalGatewayAttributeName + " servlet context attribute.");
		}
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendError(405, "Not allowed");
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Allow", "GET, OPTIONS");
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendError(405, "Not allowed");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if (_paypalGateway == null) {
			_log.warn("PaypalBrowserSubmissionGateway was not set. Please refer to the documentation on how to use the PaypalIPNServlet.");
			resp.sendError(500, "Post servlet uninitialized.");
			return;
		}
		
		_paypalGateway.handleIPNRequest(req);
	}

	public void setPaypalGateway(PaypalBrowserSubmissionGateway paypalGateway) {
		_paypalGateway = paypalGateway;
	}
	
}
