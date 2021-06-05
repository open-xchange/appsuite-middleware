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

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetStructureAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetStructureAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetStructureAction.class);

    /**
     * Initializes a new {@link GetStructureAction}.
     *
     * @param services
     */
    public GetStructureAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            AJAXRequestResult data = getJSONNullResult();
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final boolean unseen;
            {
                final String tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
                unseen = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            }
            final long maxSize;
            {
                final String tmp = req.getParameter("max_size");
                if (null == tmp) {
                    maxSize = -1;
                } else {
                    long l = -1;
                    try {
                        l = Long.parseLong(tmp.trim());
                    } catch (NumberFormatException e) {
                        l = -1;
                    }
                    maxSize = l;
                }
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final String uid;
            {
                String tmp2 = req.getParameter(AJAXServlet.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = req.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            if (isEmpty(uid)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
            }
            /*
             * Get message
             */
            final MailMessage mail = mailInterface.getMessage(folderPath, uid, !unseen);
            if (mail == null) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
            }
            if (!mail.containsAccountId()) {
                mail.setAccountId(mailInterface.getAccountID());
            }
            final boolean wasUnseen = (unseen ? !mail.isSeen() : mail.containsPrevSeen() && !mail.isPrevSeen());
            if (wasUnseen) {
                try {
                    boolean memorizeAddresses = ServerUserSetting.getInstance().isContactCollectOnMailAccess(session.getContextId(), session.getUserId()).booleanValue();
                    if (memorizeAddresses) {
                        triggerContactCollector(session, mail, true, false);
                    }
                } catch (Exception e) {
                    LOG.warn("Contact collector could not be triggered.", e);
                }
            }
            data = new AJAXRequestResult(MessageWriter.writeStructure(mailInterface.getAccountID(), mail, maxSize), "json");
            return data;
        } catch (OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn("Requested mail could not be found. Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.",
                    e);
                try {
                    final String uid = getUidFromException(e);
                    if ("undefined".equalsIgnoreCase(uid)) {
                        throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
                    }
                } catch (Exception x) {
                    // ignore
                }
            } else {
                LOG.error("", e);
            }
            throw e;
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
