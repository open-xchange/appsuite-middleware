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
import java.io.IOException;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
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
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
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

    private static final AuthScope AUTHSCOPE = new AuthScope("api.sipgate.com", -1, null, "https");
    private static final String USERNAME = "com.openexchange.sms.sipgate.username";
    private static final String PASSWORD = "com.openexchange.sms.sipgate.password";

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
        String sipgateUsername = view.get(USERNAME, String.class);
        String sipgatePassword = view.get(PASSWORD, String.class);
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

        try {
            HttpClient client = getHttpClient();
            for (int i = 0; i < recipients.length; i++) {
                JSONObject jsonObject = new JSONObject(3);
                jsonObject.put("smsId", "s0");
                jsonObject.put("recipient", checkAndFormatPhoneNumber(recipients[i], null));
                jsonObject.put("message", message);
                sendMessage(client, sipgateUsername, sipgatePassword, jsonObject);
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

    private void sendMessage(HttpClient client, String username, String password, JSONObject message) throws OXException {
        HttpPost request = new HttpPost(URI);
        request.setEntity(new InputStreamEntity(new JSONInputStream(message, Charsets.UTF_8_NAME), -1L, ContentType.APPLICATION_JSON));
        HttpResponse response = null;

        HttpContext context = new BasicHttpContext();
        {
            /*
             * Configure HTTP context
             */
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AUTHSCOPE, new UsernamePasswordCredentials(username, password));
            context.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
        }

        try {
            response = client.execute(request, context);
            StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_NO_CONTENT != statusLine.getStatusCode()) {
                HttpEntity entity = response.getEntity();
                String body = null;
                if (null != entity && entity.getContentLength() > 0) {
                    body = EntityUtils.toString(entity, Charsets.UTF_8);
                }
                throw SipgateSMSExceptionCode.HTTP_ERROR.create(String.valueOf(statusLine.getStatusCode()), Strings.isEmpty(body) ? statusLine.getReasonPhrase() : body);
            }
        } catch (IOException e) {
            throw SipgateSMSExceptionCode.UNKNOWN_ERROR.create(e, e.getMessage());
        } finally {
            HttpClients.close(request, response);
        }
    }

    private HttpClient getHttpClient() throws OXException {
        return services.getServiceSafe(HttpClientService.class).getHttpClient("sipgate").getHttpClient();
    }

}
