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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
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

    private static final String URL = "https://api.sipgate.net/my/xmlrpcfacade/";
    private final int MAX_MESSAGE_LENGTH;

    private final HttpClient client;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link SipgateSMSService}.
     *
     * @throws OXException
     */
    public SipgateSMSService(ServiceLookup services) {
        this.services = services;
        ConfigurationService configService = services.getService(ConfigurationService.class);
        String sipgateUsername = configService.getProperty("com.openexchange.sms.sipgate.username");
        String sipgatePassword = configService.getProperty("com.openexchange.sms.sipgate.password");
        MAX_MESSAGE_LENGTH = configService.getIntProperty("com.openexchange.sms.sipgate.maxlength", 0);
        HttpClientParams params = new HttpClientParams();
        params.setAuthenticationPreemptive(true);
        params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, AuthPolicy.BASIC);
        Credentials credentials = new UsernamePasswordCredentials(sipgateUsername, sipgatePassword);
        HttpClient client = new HttpClient(params);
        client.getState().setCredentials(new AuthScope("api.sipgate.net", 443), credentials);
        this.client = client;
    }

    @Override
    public void sendMessage(String[] recipients, String message) throws OXException {
        if (MAX_MESSAGE_LENGTH > 0 && message.length() > MAX_MESSAGE_LENGTH) {
            throw SMSExceptionCode.MESSAGE_TOO_LONG.create(message.length(), MAX_MESSAGE_LENGTH);
        }
        JSONArray phoneNumbers = new JSONArray(recipients.length);
        try {
            for (int i = 0; i < recipients.length; i++) {
                phoneNumbers.put(i, checkAndFormatPhoneNumber(recipients[i], null));
            }
            JSONObject jsonObject = new JSONObject(3);
            jsonObject.put("TOS", "text");
            jsonObject.put("Content", message);
            jsonObject.put("RemoteUri", phoneNumbers);
            String encoded = URLEncoder.encode(jsonObject.toString(), StandardCharsets.UTF_8.toString());
            HttpMethod method = getHttpMethod("samurai.SessionInitiateMulti", encoded);
            execute(method);
        } catch (JSONException e) {
            throw SipgateSMSExceptionCode.UNKNOWN_ERROR.create(e, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw SipgateSMSExceptionCode.UNKNOWN_ERROR.create(e, e.getMessage());
        }
    }

    private String checkAndFormatPhoneNumber(String phoneNumber, Locale locale) throws OXException {
        PhoneNumberParserService parser = services.getService(PhoneNumberParserService.class);
        String parsedNumber = parser.parsePhoneNumber(phoneNumber, locale);
        StringBuilder sb = new StringBuilder(30);
        sb.append("sip:").append(parsedNumber).append("@sipgate.net");
        return sb.toString();
    }

    private HttpMethod getHttpMethod(String method, String parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL).append(method).append("/").append(parameters);
        return new GetMethod(sb.toString());
    }

    private void execute(HttpMethod method) throws OXException {
        try {
            int statusCode = client.executeMethod(method);
            if (HttpStatus.SC_OK == statusCode) {
                String response = method.getResponseBodyAsString();
                JSONObject resp = new JSONObject(response);
                if (resp.hasAndNotNull("error")) {
                    String errorMessage = resp.getString("error");
                    throw SMSExceptionCode.NOT_SENT.create(errorMessage);
                }
            } else {
                throw SipgateSMSExceptionCode.HTTP_ERROR.create(String.valueOf(statusCode), method.getStatusText());
            }
        } catch (IOException e) {
            throw SMSExceptionCode.SERVICE_UNAVAILABLE.create(e, e.getMessage());
        } catch (JSONException e) {
            throw SipgateSMSExceptionCode.UNKNOWN_ERROR.create(e, e.getMessage());
        } finally {
            if (null != method && method.hasBeenUsed()) {
                method.releaseConnection();
            }
        }
    }

}
