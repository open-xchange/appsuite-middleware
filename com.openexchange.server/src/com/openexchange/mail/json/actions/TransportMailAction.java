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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
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
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.MailAccounts;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TransportMailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class TransportMailAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(TransportMailAction.class);

    /**
     * Initializes a new {@link TransportMailAction}.
     *
     * @param services
     */
    public TransportMailAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
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
            } catch (OXException e) {
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
            {
                final String paramName = "copy2Sent";
                String sCopy2Sent = req.getRequest().getParameter(paramName);
                if (null != sCopy2Sent) { // Provided as URL parameter
                    if (AJAXRequestDataTools.parseBoolParameter(sCopy2Sent)) {
                        usm.setNoCopyIntoStandardSentFolder(false);
                    } else if (Boolean.FALSE.equals(AJAXRequestDataTools.parseFalseBoolParameter(sCopy2Sent))) {
                        // Explicitly deny copy to sent folder
                        usm.setNoCopyIntoStandardSentFolder(true);
                    }
                } else {
                    MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                    if (mass != null && MailAccounts.isGmailTransport(mass.getTransportAccount(accountId, session.getUserId(), session.getContextId()))) {
                        // Deny copy to sent folder for Gmail
                        usm.setNoCopyIntoStandardSentFolder(true);
                    }
                }
            }
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
                boolean memorizeAddresses = ServerUserSetting.getInstance().isContactCollectOnMailTransport(session.getContextId(), session.getUserId()).booleanValue();
                triggerContactCollector(session, composedMail, memorizeAddresses, true);
            } catch (Exception e) {
                LOG.warn("Contact collector could not be triggered.", e);
            }
            return new AJAXRequestResult(responseObj, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

}
