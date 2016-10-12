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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
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
public final class GetAttachmentTokenAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetAttachmentTokenAction}.
     *
     * @param services
     */
    public GetAttachmentTokenAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
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
                } catch (final NumberFormatException e) {
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
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
