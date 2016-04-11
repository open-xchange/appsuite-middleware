/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mobile.configuration.json.action.sms.impl;

import java.net.MalformedURLException;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.sms.osgi.ActionServiceRegistry;
import com.openexchange.mobile.configuration.json.container.ProvisioningInformation;
import com.openexchange.mobile.configuration.json.container.ProvisioningResponse;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;

/**
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public class ActionSMS implements ActionService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningServlet.class);

	@Override
    public ProvisioningResponse handleAction(
			final ProvisioningInformation provisioningInformation){
		final ProvisioningResponse provisioningResponse = new ProvisioningResponse();
		final int cid = provisioningInformation.getCtx().getContextId();
		final int userid = provisioningInformation.getUser().getId();
		// init sms
		final SMS smssend = new SMS();

		// fix up number/remove leading 00, if number is incorrect
		final String to = provisioningInformation.getTarget(); // initial number
		String to_formatted = null;
		try {
			// check number and set it to SMS object
			to_formatted = smssend.checkAndFormatRecipient(to);
			smssend.setSMSNumber(to_formatted);

		} catch (final Exception e) {
			LOG.error("Invalid recipient detected. SMS to recipient {} (unformatted nr:{}) could not be send for user {} in context {}", to_formatted, to, userid, cid, e);
			provisioningResponse.setMessage("Invalid recipient number detected.");
			provisioningResponse.setSuccess(false);
		}

		// set sipgate setting
		smssend.setServerUrl(getFromConfig("com.openexchange.mobile.configuration.json.action.sms.sipgat.api.url"));
		smssend.setSipgateuser(getFromConfig("com.openexchange.mobile.configuration.json.action.sms.sipgat.api.username"));
		smssend.setSipgatepass(getFromConfig("com.openexchange.mobile.configuration.json.action.sms.sipgat.api.password"));

		LOG.debug("Using API URL: {} ", new Object() { @Override public String toString() { return getFromConfig("com.openexchange.mobile.configuration.json.action.sms.sipgat.api.url");}});
		LOG.debug("Using API Username: {} ", new Object() { @Override public String toString() { return getFromConfig("com.openexchange.mobile.configuration.json.action.sms.sipgat.api.username");}});

		// set prov. URL in SMS
		smssend.setText(provisioningInformation.getUrl());

		// send sms and check response code
		try {
			final Map map = smssend.send();
			if(smssend.wasSuccessfull()){
				provisioningResponse.setMessage("SMS sent successfully...");
				provisioningResponse.setSuccess(true);
				LOG.info("SMS to recipient {} (unformatted nr:{}) sent successfully for user {} in context {}", to_formatted, to, userid, cid);
			}else{
				provisioningResponse.setMessage("SMS could not be sent. Details: "+smssend.getErrorMessage());
				provisioningResponse.setSuccess(false);
				LOG.error("API error occured while sending sms to recipient {} (unformatted nr:{})  for user {} in context {}", to_formatted, to, userid, cid);
			}
		} catch (final MalformedURLException e) {
			LOG.error("internal error occured while sending sms to recipient {} (unformatted nr:{})  for user {} in context {}", to_formatted, to, userid, cid,e);
			provisioningResponse.setMessage("Internal error occured while sending SMS...");
			provisioningResponse.setSuccess(false);
		} catch (final XmlRpcException e) {
			LOG.error("internal error occured while sending sms to recipient {} (unformatted nr:{})  for user {} in context {}", to_formatted, to, userid, cid,e);
			provisioningResponse.setMessage("Internal error occured while sending SMS...");
			provisioningResponse.setSuccess(false);
		}


		return provisioningResponse;
	}

	protected String getFromConfig(final String key) {
		ConfigurationService configservice;
		String retval = null;
		try {
			configservice = ActionServiceRegistry.getServiceRegistry().getService(ConfigurationService.class, true);
			retval = configservice.getProperty(key);
		} catch (final OXException e) {
			LOG.error("value for key {} was not found for ACTIONSMS configuration", key);
		}
		return retval;
	}

}
