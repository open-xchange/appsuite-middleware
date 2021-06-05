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

package com.openexchange.mailaccount.json.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDefaultFolderAware;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.Tools;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.MailAccountFields;
import com.openexchange.mailaccount.json.parser.DefaultMailAccountParser;
import com.openexchange.mailaccount.json.writer.DefaultMailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAccountAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class NewAction extends AbstractMailAccountAction implements MailAccountFields {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NewAction.class);

    public static final String ACTION = AJAXServlet.ACTION_NEW;

    /**
     * Initializes a new {@link NewAction}.
     */
    public NewAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    @Override
    protected AJAXRequestResult innerPerform(AJAXRequestData requestData, ServerSession session, JSONValue jData) throws OXException, JSONException {
        if (!session.getUserPermissionBits().isMultipleMailAccounts()) {
            throw MailAccountExceptionCodes.NOT_ENABLED.create(Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        if (null == jData) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        MailAccountDescription accountDescription = new MailAccountDescription();
        List<OXException> warnings = new LinkedList<OXException>();
        Set<Attribute> availableAttributes = DefaultMailAccountParser.getInstance().parse(accountDescription, jData.toObject(), warnings);

        if (!availableAttributes.contains(Attribute.TRANSPORT_AUTH_LITERAL)) {
            accountDescription.setTransportAuth(TransportAuth.MAIL);
            availableAttributes.add(Attribute.TRANSPORT_AUTH_LITERAL);
        }

        if (TransportAuth.MAIL.equals(accountDescription.getTransportAuth()) || TransportAuth.NONE.equals(accountDescription.getTransportAuth())) {
            availableAttributes.remove(Attribute.TRANSPORT_LOGIN_LITERAL);
            availableAttributes.remove(Attribute.TRANSPORT_PASSWORD_LITERAL);
            accountDescription.setTransportLogin(null);
            accountDescription.setTransportPassword(null);
            accountDescription.setTransportOAuthId(-1);
        }

        checkNeededFields(accountDescription, true);

        if (isEmpty(accountDescription.getSpamHandler())) {
            accountDescription.setSpamHandler(SpamHandler.SPAM_HANDLER_FALLBACK);
        }

        // Check if account denotes a Unified Mail account
        if (isUnifiedINBOXAccount(accountDescription.getMailProtocol())) {
            // Deny creation of Unified Mail account
            throw MailAccountExceptionCodes.UNIFIED_INBOX_ACCOUNT_CREATION_FAILED.create(accountDescription.getId());
        }

        {
            String name = accountDescription.getName();
            if (isEmpty(name) || "null".equalsIgnoreCase(name)) {
                accountDescription.setName(accountDescription.getPrimaryAddress());
            }
        }

        MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

        // Don't check for POP3 account due to access restrictions (login only allowed every n minutes)
        boolean pop3 = Strings.toLowerCase(accountDescription.getMailProtocol()).startsWith("pop3");

        // Check credentials validity
        boolean valid = true;
        if (!pop3) {
            session.setParameter("mail-account.validate.type", "create");
            try {
                if (false == ValidateAction.actionValidateBoolean(accountDescription, session, true, warnings, true).booleanValue()) {
                    valid = false;
                    final OXException warning = MimeMailExceptionCode.CONNECT_ERROR.create(accountDescription.getMailServer(), accountDescription.getLogin());
                    warning.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(0, warning);
                } else if (false == ValidateAction.actionValidateMailTransportBoolean(accountDescription, session, warnings, true).booleanValue()) {
                    String login = accountDescription.getTransportLogin();
                    if (false == ValidateAction.seemsValid(login)) {
                        login = accountDescription.getLogin();
                    }
                    OXException warning = MimeMailExceptionCode.CONNECT_ERROR.create(accountDescription.getTransportServer(), login);
                    warning.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(0, warning);
                }
            } finally {
                session.setParameter("mail-account.validate.type", null);
            }
        }

        // Check standard folder names against full names
        if (!pop3 && valid) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = getMailAccess(accountDescription, session, warnings);
                mailAccess.connect(false);

                // Determine separator character
                char separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();

                // Close MailAccess and discard
                mailAccess.close(false);
                mailAccess = null;

                // Check names
                Tools.checkNames(accountDescription, availableAttributes, Character.valueOf(separator));
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(false);
                }
            }
        }

        int cid = session.getContextId();
        int id;
        MailAccount newAccount = null;
        {
            Connection wcon = Database.get(cid, true);
            int rollback = 0;
            try {
                Databases.startTransaction(wcon);
                rollback = 1;

                // Insert account
                id = storageService.insertMailAccount(accountDescription, session.getUserId(), session.getContext(), session, wcon);

                // Check full names after successful creation
                newAccount = storageService.getMailAccount(id, session.getUserId(), cid, wcon);
                if (null == newAccount) {
                    throw MailAccountExceptionCodes.NOT_FOUND.create(id, session.getUserId(), session.getContextId());
                }
                Map<String, String> defaultFolderFullNames = null;
                if (!pop3 && valid) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = getMailAccess(accountDescription, session, warnings);
                        mailAccess.connect(false);
                        IMailFolderStorage storage = mailAccess.getFolderStorage();
                        IMailFolderStorageDefaultFolderAware defaultFolderAware = storage.supports(IMailFolderStorageDefaultFolderAware.class);
                        if (null != defaultFolderAware) {
                            defaultFolderFullNames = defaultFolderAware.getSpecialUseFolder();
                        }
                    } finally {
                        if (null != mailAccess) {
                            mailAccess.close(false);
                        }
                    }
                }

                if (valid) {
                    newAccount = checkFullNames(newAccount, storageService, session, wcon, defaultFolderFullNames);
                }
                wcon.commit();
                rollback = 2;
            } catch (SQLException e) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(wcon);
                    }
                    Databases.autocommit(wcon);
                }
                Database.back(cid, true, wcon);
            }
        }

        // Live connect to orderly check default folders
        boolean reload = false;
        {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, id);
                mailAccess.connect(true);
                reload = true;
            } catch (Exception e) {
                LOGGER.warn("Failed to live-connect against mail server {} on port {}. Aborting to check default folders consistency.", accountDescription.getMailServer(), accountDescription.getMailPort(), e);
            } finally {
                if (null != mailAccess) {
                    mailAccess.close();
                }
            }
        }

        // Reload account
        if (reload) {
            Connection wcon = Database.get(cid, true);
            try {
                // Insert account
                newAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId(), wcon);
            } finally {
                Database.backAfterReading(cid, wcon);
            }
        }

        {
            JSONObject jBody = jData.toObject();
            if (jBody.hasAndNotNull(META)) {
                final JSONObject jMeta = jBody.optJSONObject(META);
                getStorage().store(new JSlobId(JSLOB_SERVICE_ID, Integer.toString(id), session.getUserId(), session.getContextId()), new DefaultJSlob(jMeta));
            }
        }

        JSONObject jsonAccount;
        if (null == newAccount) {
            MailAccount loadedMailAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId());
            if (null == loadedMailAccount) {
                throw MailAccountExceptionCodes.NOT_FOUND.create(id, session.getUserId(), session.getContextId());
            }
            MailAccount mailAccount = checkFullNames(loadedMailAccount, storageService, session);
            mailAccount = checkSpamInfo(mailAccount, session);
            jsonAccount = DefaultMailAccountWriter.write(mailAccount);
        } else {
            newAccount = checkSpamInfo(newAccount, session);
            jsonAccount = DefaultMailAccountWriter.write(newAccount);
        }

        return new AJAXRequestResult(jsonAccount).addWarnings(warnings);
    }

}
