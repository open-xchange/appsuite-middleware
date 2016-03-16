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

package com.openexchange.messaging.sms.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.sms.osgi.MessagingSMSServiceRegistry;
import com.openexchange.messaging.sms.service.MessagingNewService;
import com.openexchange.messaging.sms.service.MessagingUserConfigurationInterface;
import com.openexchange.messaging.sms.service.SMSMessagingExceptionCodes;
import com.openexchange.messaging.sms.service.SMSMessagingMessage;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 */
public final class MessagingSMSServlet extends PermissionServlet {

    private static final long serialVersionUID = 1541427953784271108L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessagingSMSServlet.class);

    public MessagingSMSServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final Response response = new Response();

        JSONObject obj = new JSONObject();

        try {
            String action = JSONUtility.checkStringParameter(req, "action");
            if (action.equals("getconfig")) {
                final MessagingNewService service = MessagingSMSServiceRegistry.getServiceRegistry().getService(MessagingNewService.class);
                MessagingUserConfigurationInterface userConfig = service.getUserConfiguration(getSessionObject(req));

                obj.put("addresses", new JSONArray(userConfig.getAddresses()));
                obj.put("length", userConfig.getLength());
                obj.put("multisms", userConfig.getMultiSMS());
                obj.put("captcha", userConfig.isCaptcha());
                obj.put("mms", userConfig.isMMS());
                obj.put("signatureoption", userConfig.isSignatureOption());
                obj.put("smsLimit", userConfig.getSmsLimit());
                obj.put("recipientLimit", userConfig.getRecipientLimit());
                final String numCleanRegEx = userConfig.getNumCleanRegEx();
                if (null != numCleanRegEx) {
                    obj.put("numCleanRegEx", numCleanRegEx);
                }
                if (userConfig.getUpsellLink() != null) {
                    obj.put("upsell", userConfig.getUpsellLink());
                }
            } else if (action.equals("getstatus")) {
                final MessagingNewService service = MessagingSMSServiceRegistry.getServiceRegistry().getService(MessagingNewService.class);
                MessagingUserConfigurationInterface userConfig = service.getUserConfiguration(getSessionObject(req));

                obj.put("display_string", userConfig.getDisplayString());
            }
        } catch (final OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
        } catch (final RuntimeException e) {
            final OXException create = SMSMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            LOG.error("", create);
            response.setException(create);
        }

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(req)));
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final Response response = new Response();

        JSONObject obj = new JSONObject();

        final ServerSession session = getSessionObject(req);
        try {
            String action = JSONUtility.checkStringParameter(req, "action");
            if (action.equals("send")) {
                final String body = getBody(req);
                final JSONObject jsonObject = new JSONObject(body);
                final String from = jsonObject.getString("from");
                final String to = jsonObject.getString("to");
                final String smstext = jsonObject.getString("body");

                final MessagingNewService service = MessagingSMSServiceRegistry.getServiceRegistry().getService(MessagingNewService.class);

                final MessagingAccountTransport accountTransport = service.getAccountTransport(0, session);

                if (null != accountTransport) {
                    SMSMessagingMessage smsMessagingMessage = new SMSMessagingMessage(from, to, smstext);
                    parseCaptchaParameter(req, jsonObject, smsMessagingMessage);
                    parseAttachments(jsonObject, smsMessagingMessage);
                    try {
                        accountTransport.connect();
                        accountTransport.transport(smsMessagingMessage, null);
                        obj.put("message", "Message sent");
                    } finally {
                        accountTransport.close();
                    }
                    response.setData(obj);
                } else {
                    response.setException(SMSMessagingExceptionCodes.UNEXPECTED_ERROR.create("Error while getting MessagingAccountTransport"));
                }
            }
        } catch (OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
        }
        // Close response and flush print writer
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private void parseAttachments(JSONObject jsonObject, SMSMessagingMessage message) throws JSONException {
        if (!jsonObject.hasAndNotNull("attachments")) {
            return;
        }
        if (!(jsonObject.opt("attachments") instanceof JSONArray)) {
            return;
        }
        final JSONArray attachments = jsonObject.getJSONArray("attachments");
        for (int i = 0; i < attachments.length(); i++) {
            message.addAttachment(attachments.getString(i));
        }
    }

    private void parseCaptchaParameter(HttpServletRequest req, JSONObject body, SMSMessagingMessage message) throws JSONException {
        if (!body.has("captcha")) {
            return;
        }

        JSONObject captcha = body.getJSONObject("captcha");

        SMSMessagingMessage.CaptchaParams params = new SMSMessagingMessage.CaptchaParams();

        if (captcha.has("challenge")) {
            params.setChallenge(captcha.getString("challenge"));
        }
        if (captcha.has("response")) {
            params.setResponse(captcha.getString("response"));
        }
        params.setAddress(req.getRemoteAddr());

        message.setCaptchaParameters(params);
    }

}
