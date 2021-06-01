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
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.compose.old.OldCompositionSpace;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetReplyAllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetReplyAllAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetReplyAllAction}.
     *
     * @param services
     */
    public GetReplyAllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        final JSONArray paths = (JSONArray) req.getRequest().getData();
        if (null == paths) {
            return performGet(req);
        }
        return performPut(req, paths);
    }

    private AJAXRequestResult performPut(MailRequest req, JSONArray paths) throws OXException {
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
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performGet(MailRequest req) throws OXException {
        try {
            ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            String view = req.getParameter(Mail.PARAMETER_VIEW);
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            UserSettingMail userSettingMail = session.getUserSettingMail();
            if (userSettingMail == null) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Unable to retrieve the mail settings.");
            }
            UserSettingMail usmNoSave = userSettingMail.clone();
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
            MailMessage mail = mailInterface.getReplyMessageForDisplay(folderPath, uid, true, usmNoSave, fromAddressProvider);
            if (!mail.containsAccountId()) {
                mail.setAccountId(mailInterface.getAccountID());
            }

            if (null != csid) {
                OldCompositionSpace oldCompositionSpace = OldCompositionSpace.getCompositionSpace(csid, session);
                oldCompositionSpace.setReplyFor(new MailPath(folderPath, uid));


                AJAXRequestResult result = new AJAXRequestResult(mail, "mail");
                result.setParameter("csid", csid);
                return result;
            }

            return new AJAXRequestResult(mail, "mail");
        } catch (OXException e) {
            final String uid = getUidFromException(e);
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e) && "undefined".equalsIgnoreCase(uid)) {
                throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
            }
            throw e;
        } catch (RuntimeException e) {
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
