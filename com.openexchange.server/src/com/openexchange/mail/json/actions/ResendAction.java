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
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
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
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ResendAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "resend", description = "Re-sends a mail.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested Message."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "view", optional=true, description = "(available with SP6) - \"text\" forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. \"html\" to allow a possible HTML mail body being transferred as it is (but white-list filter applied).NOTE: if set, the corresponding gui config setting will be ignored.")
}, responseDescription = "(not IMAP: with timestamp): An object containing all data of the requested mail. The fields of the object are listed in Detailed mail data. The fields id and attachment are not included.")
public final class ResendAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResendAction.class);

    /**
     * Initializes a new {@link ResendAction}.
     *
     * @param services
     */
    public ResendAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException, JSONException {
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

    private MailMessage performBounce(final MailRequest req, final String folderPath, final String uid) throws OXException {
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
             * Check folder
             */
            final String sentFolder = mailInterface.getSentFolder(mailInterface.getAccountID());
            if (null != sentFolder && !sentFolder.equalsIgnoreCase(folderPath)) {
                final String fullName = MailFolderUtility.prepareMailFolderParam(sentFolder).getFullName();
                throw MailExceptionCode.RESEND_DENIED.create(fullName);
            }
            /*
             * Check from
             */
            resolveFrom2Account(session, message.getFrom()[0], true, true);
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
                    } catch (final javax.mail.IllegalWriteException e) {
                        readOnly = true;
                    } catch (final javax.mail.MessagingException e) {
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
                final MailMessage sentMail = transport.sendMailMessage(new ContentAwareComposedMailMessage(mimeMessage, session, null), ComposeType.NEW);
                if (!sentMail.containsAccountId()) {
                    sentMail.setAccountId(mailInterface.getAccountID());
                }
                return sentMail;
            } catch (final javax.mail.MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                transport.close();
            }
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
