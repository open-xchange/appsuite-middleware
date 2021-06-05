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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BounceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class BounceAction extends AbstractMailAction {

    /**
     * Initializes a new {@link BounceAction}.
     *
     * @param services
     */
    public BounceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        final JSONArray paths = (JSONArray) req.getRequest().getData();
        if (null == paths) {
            return new AJAXRequestResult(performBounce(req, req.checkParameter(AJAXServlet.PARAMETER_FOLDERID), req.checkParameter(AJAXServlet.PARAMETER_ID)), "mail");
        }
        final int length = paths.length();
        final List<MailMessage> ret = new ArrayList<MailMessage>(length);
        for (int i = 0; i < length; i++) {
            final JSONObject jo = paths.getJSONObject(i);
            ret.add(performBounce(req, jo.getString(AJAXServlet.PARAMETER_FOLDERID), jo.getString(AJAXServlet.PARAMETER_ID)));
        }
        return new AJAXRequestResult(ret, "mail");
    }

    private MailMessage performBounce(MailRequest req, String folderPath, String uid) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String view = req.getParameter(Mail.PARAMETER_VIEW);
            final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            detectDisplayMode(true, view, usmNoSave);
            if (Boolean.parseBoolean(req.getParameter("dropPrefix"))) {
                usmNoSave.setDropReplyForwardPrefix(true);
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final MailMessage message = mailInterface.getMessage(folderPath, uid, false);
            if (null == message) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
            }
            /*
             * Transport
             */
            final MailTransport transport = MailTransport.getInstance(session, mailInterface.getAccountID());
            try {
                final MimeMessage mimeMessage;
                if (message instanceof MimeMailMessage) {
                    final MimeMessage mm = ((MimeMailMessage) message).getMimeMessage();
                    boolean readOnly = false;
                    try {
                        mm.setHeader("X-Ignore", "Ignore");
                        mm.removeHeader("X-Ignore");
                    } catch (javax.mail.IllegalWriteException e) {
                        readOnly = true;
                    } catch (javax.mail.MessagingException e) {
                        throw MimeMailException.handleMessagingException(e);
                    }
                    if (readOnly) {
                        // Construct a new one
                        mimeMessage = MimeMessageUtility.mimeMessageFrom(mm);
                    } else {
                        mimeMessage = mm;
                    }
                } else {
                    mimeMessage = (MimeMessage) MimeMessageConverter.convertMailMessage(message, false);
                }
                mimeMessage.setHeader("Reply-To", session.getUser().getMail());
                final MailMessage sentMail = transport.sendMailMessage(new ContentAwareComposedMailMessage(mimeMessage, session, null), ComposeType.NEW);
                if (!sentMail.containsAccountId()) {
                    sentMail.setAccountId(mailInterface.getAccountID());
                }
                return sentMail;
            } catch (javax.mail.MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                transport.close();
            }
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
