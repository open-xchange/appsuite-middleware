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

package com.openexchange.sms.sipgate;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Locale;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sms.PhoneNumberParserService;
import com.openexchange.sms.SMSExceptionCode;
import com.openexchange.sms.SMSServiceSPI;

/**
 * {@link SipgateSMSService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class SipgateSMSService implements SMSServiceSPI {

    private static final Logger LOG = LoggerFactory.getLogger(SipgateSMSService.class);
    private static final String URI = "https://api.sipgate.com/v2/sessions/sms";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SipgateSMSService}.
     *
     * @throws OXException
     */
    public SipgateSMSService(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public void sendMessage(String[] recipients, String message, int userId, int contextId) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        ConfigView view = configViewFactory.getView(userId, contextId);
        String sipgateUsername = view.get("com.openexchange.sms.sipgate.username", String.class);
        String sipgatePassword = view.get("com.openexchange.sms.sipgate.password", String.class);
        Integer maxLength = view.get("com.openexchange.sms.sipgate.maxlength", Integer.class);
        if (Strings.isEmpty(sipgateUsername) || Strings.isEmpty(sipgatePassword)) {
            throw SipgateSMSExceptionCode.NOT_CONFIGURED.create(I(userId), I(contextId));
        }
        if (null == maxLength) {
            LOG.debug("Property \"com.openexchange.sms.sipgate.maxlength\" is not set, using default value 460.");
            maxLength = I(460);
        }
        if (i(maxLength) > 0 && message.length() > i(maxLength)) {
            throw SMSExceptionCode.MESSAGE_TOO_LONG.create(I(message.length()), maxLength);
        }
        HttpClientParams params = new HttpClientParams();
        params.setAuthenticationPreemptive(true);
        params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, AuthPolicy.BASIC);
        Credentials credentials = new UsernamePasswordCredentials(sipgateUsername, sipgatePassword);
        HttpClient client = new HttpClient(params);
        client.getState().setCredentials(new AuthScope("api.sipgate.com", 443), credentials);
        try {
            for (int i = 0; i < recipients.length; i++) {
                JSONObject jsonObject = new JSONObject(3);
                jsonObject.put("smsId", "s0");
                jsonObject.put("recipient", checkAndFormatPhoneNumber(recipients[i], null));
                jsonObject.put("message", message);
                sendMessage(client, jsonObject);
            }
        } catch (JSONException e) {
            // will not happen
        }
    }

    private String checkAndFormatPhoneNumber(String phoneNumber, Locale locale) throws OXException {
        PhoneNumberParserService parser = services.getService(PhoneNumberParserService.class);
        String parsedNumber = parser.parsePhoneNumber(phoneNumber, locale);
        StringBuilder sb = new StringBuilder(30);
        sb.append("+").append(parsedNumber);
        return sb.toString();
    }

    private void sendMessage(HttpClient client, JSONObject message) throws OXException {
        PostMethod request = new PostMethod(URI);
        request.setRequestEntity(new InputStreamRequestEntity(new JSONInputStream(message, Charsets.UTF_8_NAME), -1L, "application/json"));
        try {
            int status = client.executeMethod(request);
            if (status != HttpStatus.SC_NO_CONTENT) {
                String response = request.getResponseBodyAsString();
                throw SipgateSMSExceptionCode.HTTP_ERROR.create(status, response);
            }
        } catch (Exception e) {
            throw SipgateSMSExceptionCode.UNKNOWN_ERROR.create(e);
        }
    }

}
