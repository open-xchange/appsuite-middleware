/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mobile.configuration.json.action.sms.impl;

import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.container.ProvisioningInformation;
import com.openexchange.mobile.configuration.json.container.ProvisioningResponse;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public class ActionSMS implements ActionService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningServlet.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ActionSMS}.
     */
    public ActionSMS(ServiceLookup services) {
        super();
        this.services = services;

    }

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

		} catch (Exception e) {
			LOG.error("Invalid recipient detected. SMS to recipient {} (unformatted nr:{}) could not be send for user {} in context {}", to_formatted, to, I(userid), I(cid), e);
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
			smssend.send();
			if (smssend.wasSuccessfull()){
				provisioningResponse.setMessage("SMS sent successfully...");
				provisioningResponse.setSuccess(true);
				LOG.info("SMS to recipient {} (unformatted nr:{}) sent successfully for user {} in context {}", to_formatted, to, I(userid), I(cid));
			}else{
				provisioningResponse.setMessage("SMS could not be sent. Details: "+smssend.getErrorMessage());
				provisioningResponse.setSuccess(false);
				LOG.error("API error occured while sending sms to recipient {} (unformatted nr:{})  for user {} in context {}", to_formatted, to, I(userid), I(cid));
			}
		} catch (MalformedURLException e) {
			LOG.error("internal error occured while sending sms to recipient {} (unformatted nr:{})  for user {} in context {}", to_formatted, to, I(userid), I(cid), e);
			provisioningResponse.setMessage("Internal error occured while sending SMS...");
			provisioningResponse.setSuccess(false);
		}

		return provisioningResponse;
	}

	protected String getFromConfig(final String key) {
        ConfigurationService configservice;
		String retval = null;
		try {
			configservice = services.getService(ConfigurationService.class);
			if (null == configservice) {
                throw ServiceExceptionCode.absentService(ConfigurationService.class);
            }
			retval = configservice.getProperty(key);
		} catch (OXException e) {
			LOG.error("value for key {} was not found for ACTIONSMS configuration", key);
		}
		return retval;
	}

}
