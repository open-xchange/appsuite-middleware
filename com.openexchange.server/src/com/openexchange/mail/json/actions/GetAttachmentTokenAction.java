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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAttachmentTokenAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(allowPublicSession = true)
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetAttachmentTokenAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetAttachmentTokenAction}.
     *
     * @param services
     */
    public GetAttachmentTokenAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            ServerSession session = req.getSession();

            // Read in parameters
            String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            String sequenceId = req.getParameter(Mail.PARAMETER_MAILATTCHMENT);
            String imageContentId = req.getParameter(Mail.PARAMETER_MAILCID);

            if (sequenceId == null && imageContentId == null) {
                throw MailExceptionCode.MISSING_PARAM.create(new StringBuilder().append(Mail.PARAMETER_MAILATTCHMENT).append(" | ").append(Mail.PARAMETER_MAILCID).toString());
            }

            int ttlMillis;
            {
                final String tmp = req.getParameter("ttlMillis");
                try {
                    ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
                } catch (NumberFormatException e) {
                    ttlMillis = -1;
                }
            }

            boolean oneTime;
            {
                String tmp = req.getParameter("oneTime");
                oneTime = null == tmp ? false : Boolean.parseBoolean(tmp.trim());
            }

            boolean checkIp;
            {
                String tmp = req.getParameter("checkIp");
                checkIp = null == tmp ? false : Boolean.parseBoolean(tmp.trim());
            }

            // Check part existence if required
            if (AJAXRequestDataTools.parseBoolParameter("checkPart", req.getRequest())) {
                MailServletInterface mailInterface = getMailInterface(req);
                MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, true);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
            }

            AttachmentToken token = new AttachmentToken(ttlMillis <= 0 ? AttachmentTokenConstants.DEFAULT_TIMEOUT : ttlMillis);
            token.setAccessInfo(MailFolderUtility.prepareMailFolderParam(folderPath).getAccountId(), session);
            token.setAttachmentInfo(folderPath, uid, sequenceId);
            AttachmentTokenService service = ServerServiceRegistry.getInstance().getService(AttachmentTokenService.class, true);
            service.putToken(token.setOneTime(oneTime).setCheckIp(checkIp), session);

            JSONObject attachmentObject = new JSONObject(2);
            attachmentObject.put("id", token.getId());
            attachmentObject.put("jsessionid", token.getJSessionId());

            // Return result
            return new AJAXRequestResult(attachmentObject, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
