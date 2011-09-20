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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.parser.MailAccountParser;
import com.openexchange.mailaccount.json.writer.MailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewAction extends AbstractMailAccountAction {

    public static final String ACTION = AJAXServlet.ACTION_NEW;

    /**
     * Initializes a new {@link NewAction}.
     */
    public NewAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        final JSONObject jData = (JSONObject) request.getData();

        try {
            if (!session.getUserConfiguration().isMultipleMailAccounts()) {
                throw
                    MailAccountExceptionCodes.NOT_ENABLED.create(
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }

            final MailAccountDescription accountDescription = new MailAccountDescription();
            new MailAccountParser().parse(accountDescription, jData);

            checkNeededFields(accountDescription);

            // Check if account denotes a Unified INBOX account
            if (isUnifiedINBOXAccount(accountDescription.getMailProtocol())) {
                // Deny creation of Unified INBOX account
                throw MailAccountExceptionCodes.CREATION_FAILED.create();
            }

            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

            checkFullNames(accountDescription, storageService, session);

            final int id =
                storageService.insertMailAccount(accountDescription, session.getUserId(), session.getContext(), getSecret(session));

            final JSONObject jsonAccount =
                MailAccountWriter.write(checkFullNames(
                    storageService.getMailAccount(id, session.getUserId(), session.getContextId()),
                    storageService,
                    session));

            return new AJAXRequestResult(jsonAccount);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    private static void checkFullNames(final MailAccountDescription account, final MailAccountStorageService storageService, final ServerSession session) {
        /*
         * Variables
         */
        String prefix = null;
        StringBuilder tmp = null;
        MailAccount primaryAccount = null;
        /*
         * Check full names
         */
        try {
            String fullName = account.getConfirmedHamFullname();
            if (null == fullName) {
                prefix = getPrefix(account, session);
                String name = account.getConfirmedHam();
                if (null == name) {
                    primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    name = getName(StorageUtility.INDEX_CONFIRMED_HAM, primaryAccount);
                }
                account.setConfirmedHam(name);
                tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                account.setConfirmedHamFullname(null == tmp ? name : tmp.append(name).toString());
            }
            // Confirmed-Ham
            fullName = account.getConfirmedSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(account, session);
                    tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                } else {
                    if (null != tmp) {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getConfirmedSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_CONFIRMED_SPAM, primaryAccount);
                }
                account.setConfirmedSpam(name);
                account.setConfirmedSpamFullname(null == tmp ? name : tmp.append(name).toString());
            }
            // Drafts
            fullName = account.getDraftsFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(account, session);
                    tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                } else {
                    if (null != tmp) {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getDrafts();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_DRAFTS, primaryAccount);
                }
                account.setDrafts(name);
                account.setDraftsFullname(null == tmp ? name : tmp.append(name).toString());
            }
            // Sent
            fullName = account.getSentFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(account, session);
                    tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                } else {
                    if (null != tmp) {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getSent();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SENT, primaryAccount);
                }
                account.setSent(name);
                account.setSentFullname(null == tmp ? name : tmp.append(name).toString());
            }
            // Spam
            fullName = account.getSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(account, session);
                    tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                } else {
                    if (null != tmp) {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SPAM, primaryAccount);
                }
                account.setSpam(name);
                account.setSpamFullname(null == tmp ? name : tmp.append(name).toString());
            }
            // Trash
            fullName = account.getTrashFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(account, session);
                    tmp = 0 == prefix.length() ? null : new StringBuilder(prefix);
                } else {
                    if (null != tmp) {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getTrash();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_TRASH, primaryAccount);
                }
                account.setTrash(name);
                account.setTrashFullname(null == tmp ? name : tmp.append(name).toString());
            }
        } catch (final OXException e) {
            /*
             * Checking full names failed
             */
            final StringBuilder sb = new StringBuilder("Checking default folder full names for account ");
            sb.append(account.getId()).append(" failed with user ").append(session.getUserId());
            sb.append(" in context ").append(session.getContextId());
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AbstractMailAccountAction.class)).warn(sb.toString(), e);
        }
    }

    private static String getName(final int index, final MailAccount primaryAccount) {
        String retval;
        switch (index) {
        case StorageUtility.INDEX_DRAFTS:
            retval = primaryAccount.getDrafts();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getDrafts();
            }
            break;
        case StorageUtility.INDEX_SENT:
            retval = primaryAccount.getSent();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSent();
            }
            break;
        case StorageUtility.INDEX_SPAM:
            retval = primaryAccount.getSpam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSpam();
            }
            break;
        case StorageUtility.INDEX_TRASH:
            retval = primaryAccount.getTrash();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getTrash();
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_SPAM:
            retval = primaryAccount.getConfirmedSpam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedSpam();
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_HAM:
            retval = primaryAccount.getConfirmedHam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedHam();
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown index value: " + index);
        }
        return retval;
    }

    private static String getPrefix(final MailAccountDescription description, final ServerSession session) throws OXException {
        if (description.getMailProtocol().startsWith("pop3")) {
            return "";
        }
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = getMailAccess(description, session);
            access.connect(false);
            try {
                return access.getFolderStorage().getDefaultFolderPrefix();
            } finally {
                access.close(true);
            }
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

}
