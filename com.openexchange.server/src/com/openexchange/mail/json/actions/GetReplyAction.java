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

package com.openexchange.mail.json.actions;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetReplyAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetReplyAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetReplyAction}.
     *
     * @param services
     */
    public GetReplyAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final JSONArray paths = (JSONArray) req.getRequest().getData();
        if (null == paths) {
            return performGet(req);
        }
        return performPut(req, paths);
    }

    private AJAXRequestResult performPut(final MailRequest req, final JSONArray paths) throws OXException {
        try {
            int length = paths.length();
            if (length != 1) {
                throw new IllegalArgumentException("JSON array's length is not 1");
            }

            // Compile a new request instance...
            AJAXRequestData request = req.getRequest();
            AJAXRequestData requestData = new AJAXRequestData();
            requestData.setHostname(request.getHostname());
            requestData.setPrefix(request.getPrefix());
            requestData.setRoute(request.getRoute());
            requestData.setServerPort(request.getServerPort());
            for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                requestData.putParameter(entry.getKey(), entry.getValue());
            }
            for (int i = 0; i < length; i++) {
                JSONObject folderAndID = paths.getJSONObject(i);
                requestData.putParameter(AJAXServlet.PARAMETER_FOLDERID, folderAndID.getString(AJAXServlet.PARAMETER_FOLDERID));
                requestData.putParameter(AJAXServlet.PARAMETER_ID, folderAndID.get(AJAXServlet.PARAMETER_ID).toString());
            }
            requestData.setState(request.getState());

            // ... and fake a GET request
            return performGet(new MailRequest(requestData, req.getSession()));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performGet(final MailRequest req) throws OXException {
        try {
            ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            String view = req.getParameter(Mail.PARAMETER_VIEW);
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            detectDisplayMode(true, view, usmNoSave);
            if (AJAXRequestDataTools.parseBoolParameter(req.getParameter("dropPrefix"))) {
                usmNoSave.setDropReplyForwardPrefix(true);
            }
            if (AJAXRequestDataTools.parseBoolParameter(req.getParameter("attachOriginalMessage"))) {
                usmNoSave.setAttachOriginalMessage(true);
            }
            FromAddressProvider fromAddressProvider = FromAddressProvider.none();
            {
                boolean setFrom = AJAXRequestDataTools.parseBoolParameter(req.getParameter("setFrom"));
                if (setFrom) {
                    Dispatcher ox = getService(Dispatcher.class);
                    AJAXRequestData requestData = request().session(session).module(com.openexchange.mailaccount.Constants.getModule()).action(com.openexchange.mailaccount.json.actions.ResolveFolderAction.ACTION).params(AJAXServlet.PARAMETER_FOLDERID, folderPath).format("json").build(req.getRequest());
                    AJAXRequestResult requestResult = perform(requestData, ox, session);
                    JSONObject jResult = ((JSONObject) requestResult.getResultObject());
                    if (null != jResult && jResult.hasAndNotNull("from")) {
                        String address = jResult.optString("from");
                        fromAddressProvider = FromAddressProvider.providerFor(address);
                    } else {
                        fromAddressProvider = FromAddressProvider.byAccountId();
                    }
                }
            }
            /*
             * Get mail interface
             */
            MailServletInterface mailInterface = getMailInterface(req);
            MailMessage mail = mailInterface.getReplyMessageForDisplay(folderPath, uid, false, usmNoSave, fromAddressProvider);
            if (!mail.containsAccountId()) {
                mail.setAccountId(mailInterface.getAccountID());
            }

            if (null != csid) {
                CompositionSpace compositionSpace = CompositionSpace.getCompositionSpace(csid, session);
                compositionSpace.setReplyFor(new MailPath(folderPath, uid));

                AJAXRequestResult result = new AJAXRequestResult(mail, "mail");
                result.setParameter("csid", csid);
                return result;
            }

            return new AJAXRequestResult(mail, "mail");
        } catch (final OXException e) {
            final String uid = getUidFromException(e);
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e) && "undefined".equalsIgnoreCase(uid)) {
                throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
            }
            throw e;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult perform(AJAXRequestData requestData, Dispatcher ox, ServerSession session) throws OXException {
        AJAXRequestResult requestResult = null;
        Exception exc = null;
        try {
            requestResult = ox.perform(requestData, null, session);
            return requestResult;
        } catch (OXException x) {
            exc = x;
            throw x;
        } catch (RuntimeException x) {
            exc = x;
            throw MailExceptionCode.UNEXPECTED_ERROR.create(x, x.getMessage());
        } finally {
            Dispatchers.signalDone(requestResult, exc);
        }
    }

}
