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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.structure.parser.MIMEStructureParser;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TransportMailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportMailAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(TransportMailAction.class);

    /**
     * Initializes a new {@link TransportMailAction}.
     *
     * @param services
     */
    public TransportMailAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final InternetAddress[] recipients;
            {
                final String recipientsStr = req.getParameter("recipients");
                recipients = null == recipientsStr ? null : QuotedInternetAddress.parseHeader(recipientsStr, false);
            }
            /*
             * Parse structured JSON mail object
             */
            final ComposedMailMessage composedMail = MIMEStructureParser.parseStructure((JSONObject) req.getRequest().requireData(), session);
            if (recipients != null && recipients.length > 0) {
                composedMail.addRecipients(recipients);
            }
            /*
             * Transport mail
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Determine account
             */
            int accountId;
            try {
                final InternetAddress[] fromAddrs = composedMail.getFrom();
                accountId = resolveFrom2Account(session, fromAddrs != null && fromAddrs.length > 0 ? fromAddrs[0] : null, true, true);
            } catch (final OXException e) {
                if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e) || MailExceptionCode.INVALID_SENDER.equals(e)) {
                    // Re-throw
                    throw e;
                }
                LOG.warn("{}. Using default account's transport.", e.getMessage());
                // Send with default account's transport provider
                accountId = MailAccount.DEFAULT_ID;
            }
            /*
             * User settings
             */
            final UserSettingMail usm = session.getUserSettingMail();
            usm.setNoSave(true);
            final boolean copy2Sent = AJAXRequestDataTools.parseBoolParameter("copy2Sent", req.getRequest(), !usm.isNoCopyIntoStandardSentFolder());
            usm.setNoCopyIntoStandardSentFolder(!copy2Sent);
            /*
             * Transport mail
             */
            final String id = mailInterface.sendMessage(composedMail, ComposeType.NEW, accountId, usm);
            if (null == id) {
                return new AJAXRequestResult(new JSONObject(1), "json");
            }
            final int pos = id.lastIndexOf(MailPath.SEPERATOR);
            if (-1 == pos) {
                throw MailExceptionCode.INVALID_MAIL_IDENTIFIER.create(id);
            }
            final JSONObject responseObj = new JSONObject(3);
            responseObj.put(FolderChildFields.FOLDER_ID, id.substring(0, pos));
            responseObj.put(DataFields.ID, id.substring(pos + 1));
            /*
             * Trigger contact collector
             */
            try {
                final ServerUserSetting setting = ServerUserSetting.getInstance();
                final int contextId = session.getContextId();
                final int userId = session.getUserId();
                if (setting.isContactCollectOnMailTransport(contextId, userId).booleanValue()) {
                    triggerContactCollector(session, composedMail, true);
                }
            } catch (final OXException e) {
                LOG.warn("Contact collector could not be triggered.", e);
            }
            return new AJAXRequestResult(responseObj, "json");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

}
