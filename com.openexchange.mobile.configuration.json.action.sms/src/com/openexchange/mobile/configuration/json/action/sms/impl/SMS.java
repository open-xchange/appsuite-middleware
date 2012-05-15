package com.openexchange.mobile.configuration.json.action.sms.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;

public class SMS {

	private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MobilityProvisioningServlet.class));

	private String sipgateuser = "";
	private String sipgatepass = ""; // openexchange
	private String NUMBER = ""; // 4915112345678
	private String text = ""; // Please go to %u blalal
	private String serverUrl = ""; // https://samurai.sipgate.net/RPC2

	private final boolean replaceleadingzero = true;
	private final boolean replaceleadingzerozero = true;

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(final String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public String getSipgateuser() {
		return sipgateuser;
	}

	public void setSipgateuser(final String sipgateuser) {
		this.sipgateuser = sipgateuser;
	}

	public String getSipgatepass() {
		return sipgatepass;
	}

	public void setSipgatepass(final String sipgatepass) {
		this.sipgatepass = sipgatepass;
	}

	public String getSMSNumber() {
		return NUMBER;
	}

	/**
	 * Send number to send
	 * @param nUMBER
	 * @throws Exception if number is not correct
	 */
	public void setSMSNumber(String nUMBER) throws Exception {
		nUMBER = checkAndFormatRecipient(nUMBER);
		NUMBER = nUMBER;
	}

	public SMS() {

	}

	public boolean wasSuccessfull(){
		return this.wassendingsuccessfull;
	}
	private boolean wassendingsuccessfull = false;

	public String getErrorMessage(){
		return this.senderrormessage;
	}
	private String senderrormessage = null;


	public Map send() throws MalformedURLException, XmlRpcException {

		// setup xml rpc client config
		final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(this.getServerUrl()));
		config.setBasicUserName(this.getSipgateuser());
		config.setBasicPassword(this.getSipgatepass());
		final XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		// Identify our client to API call
		Vector params = new Vector();

		Hashtable table = new Hashtable();
		table.put("ClientName", "OX PROVISIONING SMS CLIENT");
		table.put("ClientVersion", "1.0");
		table.put("ClientVendor", "Open-Xchange");

		params.addElement(table);

		Map result = null;
		try {
			// execute test call
			result = (Map) client.execute("samurai.ClientIdentify", params);
			// System.err.println(result);
		} catch (final Exception e) {
			 wassendingsuccessfull = false;
			 this.senderrormessage = e.getMessage();
			 LOG.error("API error occured while executing samurai.ClientIdentify",e);
		}

		params = new Vector();
		result = null;

		// generate remote uri for sms as recipient
		final Vector remoteUris = new Vector();
		remoteUris.add("sip:" + this.getSMSNumber() + "@sipgate.net");

		// fill up data for rpc call
		table = new Hashtable();
		table.put("RemoteUri", remoteUris);
		table.put("TOS", "text");
		table.put("Content", this.getText());
		params.addElement(table);

		result = null;
		try {
			// execute SMS sending......
			result = (Map) client.execute("samurai.SessionInitiateMulti",params);
		} catch (final Exception e) {
			wassendingsuccessfull = false;
			this.senderrormessage = e.getMessage();
			LOG.error("API error occured while executing samurai.SessionInitiateMulti",e);
		}

		 //check if sending was OK
		 if(result!=null){
			 if(result.get("StatusCode").toString().trim().equalsIgnoreCase("200")){
				 wassendingsuccessfull = true;
			 }else{
				 wassendingsuccessfull = false;
				 this.senderrormessage = result.get("StatusString").toString();
			 }
		 }

		return result;
	}

	public String checkAndFormatRecipient(final String my_recipient) throws Exception {

		final String allowedCharsInNumber = "+0123456789";

		String to = my_recipient;

		int j = 0;
		while (j < to.length()) {
			boolean charIsAllowed = false;
			// each character in the entry has to be valid
			for (int k = 0; k < allowedCharsInNumber.length(); k++) {
				if (to.charAt(j) == allowedCharsInNumber.charAt(k)) {
                    charIsAllowed = true;
                }
			}

			if (charIsAllowed) {
				j++;
			} else {
				to = to.substring(0, j) + to.substring(j + 1, to.length());
			}
		}

		if (to.length() > 0 && to.charAt(0) == '+') {
			to = to.substring(1);
		} else {
			// remove leading 0 or 00
			if(replaceleadingzerozero && to.startsWith("00")){
				to = ""+to.substring(2);
			} else if (replaceleadingzero && (to.length() > 0 && to.charAt(0) == '0')){
				to = ""+to.substring(1);
			}
		}

		boolean validNumber = true;

		// number contains more than one "+"
		if (to.indexOf("+") >= 0) {
			validNumber = false;
		}

		// number is empty
		if (to.equalsIgnoreCase("")) {
			validNumber = false;
		}

		if (validNumber) {
			return to;
		} else {
			throw new Exception();
		}
	}

}
