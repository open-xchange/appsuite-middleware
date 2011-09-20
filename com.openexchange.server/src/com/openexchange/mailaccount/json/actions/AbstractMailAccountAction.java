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

import static com.openexchange.mailaccount.json.Tools.getUnsignedInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.json.fields.MailAccountFields;
import com.openexchange.secret.SecretService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailAccountAction} - An abstract folder action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccountAction implements AJAXActionService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AbstractMailAccountAction.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link AbstractMailAccountAction}.
     */
    protected AbstractMailAccountAction() {
        super();
    }

    /**
     * Gets the default tree identifier to use if request does not provide any.
     *
     * @return The default tree identifier
     */
    protected static String getDefaultTreeIdentifier() {
        return FolderStorage.REAL_TREE_ID;
    }

    /**
     * Gets the default allowed modules.
     *
     * @return The default allowed modules
     */
    protected static List<ContentType> getDefaultAllowedModules() {
        return Collections.emptyList();
    }

    /**
     * Parses specified parameter into <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int parseIntParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        return getUnsignedInteger(tmp);
    }

    private static final Pattern PAT = Pattern.compile(" *, *");

    /**
     * Parses specified parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int[] parseIntArrayParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    /**
     * Parses specified optional parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>; a zero length array is returned if parameter is missing
     */
    protected static int[] parseOptionalIntArrayParameter(final String parameterName, final AJAXRequestData request) {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return new int[0];
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    protected static boolean isUnifiedINBOXAccount(final MailAccount mailAccount) {
        return isUnifiedINBOXAccount(mailAccount.getMailProtocol());
    }

    protected static boolean isUnifiedINBOXAccount(final String mailProtocol) {
        return UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailProtocol);
    }

    protected static void checkNeededFields(final MailAccountDescription accountDescription) throws OXException {
        // Check needed fields
        if (null == accountDescription.getMailServer()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( MailAccountFields.MAIL_URL);
        }
        if (null == accountDescription.getLogin()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( MailAccountFields.LOGIN);
        }
        if (null == accountDescription.getPassword()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( MailAccountFields.PASSWORD);
        }
    }

    /**
     * Gets the secret string for specified session.
     *
     * @param session The session
     * @return The secret string
     * @throws OXException If secret string cannot be returned
     */
    protected static String getSecret(final ServerSession session) throws OXException {
        try {
            return ServerServiceRegistry.getInstance().getService(SecretService.class, true).getSecret(session);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * Checks if specified {@link MailAccount} is considered as default aka primary account.
     *
     * @param mailAccount The mail account to examine
     * @return <code>true</code> if specified {@link MailAccount} is considered as defaul account; otherwise <code>false</code>
     */
    protected static boolean isDefaultMailAccount(final MailAccount mailAccount) {
        return mailAccount.isDefaultAccount() || MailAccount.DEFAULT_ID == mailAccount.getId();
    }

    /**
     * Checks if specified {@link MailAccountDescription} is considered as default aka primary account.
     *
     * @param mailAccount The mail account description to examine
     * @return <code>true</code> if specified {@link MailAccountDescription} is considered as defaul account; otherwise <code>false</code>
     */
    protected static boolean isDefaultMailAccount(final MailAccountDescription mailAccount) {
        return MailAccount.DEFAULT_ID == mailAccount.getId();
    }

    /**
     * Parses the attributes from passed comma-separated list.
     *
     * @param colString The comma-separated list
     * @return The parsed attributes
     */
    protected static List<Attribute> getColumns(final String colString) {
        List<Attribute> attributes = null;
        if (colString != null && !"".equals(colString.trim())) {
            attributes = new LinkedList<Attribute>();
            for (final String col : colString.split("\\s*,\\s*")) {
                if ("".equals(col)) {
                    continue;
                }
                attributes.add(Attribute.getById(Integer.parseInt(col)));
            }
            return attributes;
        }
        // All columns
        return Arrays.asList(Attribute.values());
    }

    /**
     * Gets the appropriate {@link MailAccess} instance for specified mail account description and session.
     *
     * @param accountDescription The mail account description
     * @param session The session providing needed user information
     * @return The appropriate {@link MailAccess} instance
     * @throws OXException If appropriate {@link MailAccess} instance cannot be determined
     */
    protected static MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(final MailAccountDescription accountDescription, final ServerSession session) throws OXException {
        try {
            final String mailServerURL = accountDescription.generateMailServerURL();
            // Get the appropriate mail provider by mail server URL
            final MailProvider mailProvider = MailProviderRegistry.getMailProviderByURL(mailServerURL);
            if (null == mailProvider) {
                if (DEBUG) {
                    LOG.debug("Validating mail account failed. No mail provider found for URL: " + mailServerURL);
                }
                return null;
            }
            // Set marker
            session.setParameter("mail-account.request", "validate");
            try {
                // Create a mail access instance
                final MailAccess<?, ?> mailAccess = mailProvider.createNewMailAccess(session);
                final MailConfig mailConfig = mailAccess.getMailConfig();
                // Set login and password
                mailConfig.setLogin(accountDescription.getLogin());
                mailConfig.setPassword(accountDescription.getPassword());
                // Set server and port
                final URI uri;
                try {
                    uri = URIParser.parse(mailServerURL, URIDefaults.IMAP);
                } catch (final URISyntaxException e) {
                    throw MailExceptionCode.URI_PARSE_FAILED.create(e, mailServerURL);
                }
                mailConfig.setServer(uri.getHost());
                mailConfig.setPort(uri.getPort());
                mailConfig.setSecure(accountDescription.isMailSecure());
                mailAccess.setCacheable(false);
                return mailAccess;
            } finally {
                // Unset marker
                session.setParameter("mail-account.request", null);
            }
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @return The mail account with full names present
     */
    protected static MailAccount checkFullNames(final MailAccount account, final MailAccountStorageService storageService, final ServerSession session) {
        final int accountId = account.getId();
        if (MailAccount.DEFAULT_ID == accountId) {
            /*
             * No check for primary account
             */
            return account;
        }
        final MailAccountDescription mad = new MailAccountDescription();
        mad.setId(accountId);
        final Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
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
                prefix = getPrefix(accountId, session);
                String name = account.getConfirmedHam();
                if (null == name) {
                    primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    name = getName(StorageUtility.INDEX_CONFIRMED_HAM, primaryAccount);
                }
                mad.setConfirmedHamFullname((tmp = new StringBuilder(prefix)).append(name).toString());
                attributes.add(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL);
            }
            // Confirmed-Ham
            fullName = account.getConfirmedSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, session);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getConfirmedSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_CONFIRMED_SPAM, primaryAccount);
                }
                mad.setConfirmedSpamFullname(tmp.append(name).toString());
                attributes.add(Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL);
            }
            // Drafts
            fullName = account.getDraftsFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, session);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getDrafts();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_DRAFTS, primaryAccount);
                }
                mad.setDraftsFullname(tmp.append(name).toString());
                attributes.add(Attribute.DRAFTS_FULLNAME_LITERAL);
            }
            // Sent
            fullName = account.getSentFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, session);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getSent();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SENT, primaryAccount);
                }
                mad.setSentFullname(tmp.append(name).toString());
                attributes.add(Attribute.SENT_FULLNAME_LITERAL);
            }
            // Spam
            fullName = account.getSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, session);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SPAM, primaryAccount);
                }
                mad.setSpamFullname(tmp.append(name).toString());
                attributes.add(Attribute.SPAM_FULLNAME_LITERAL);
            }
            // Trash
            fullName = account.getTrashFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, session);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getTrash();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(session.getUserId(), session.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_TRASH, primaryAccount);
                }
                mad.setTrashFullname(tmp.append(name).toString());
                attributes.add(Attribute.TRASH_FULLNAME_LITERAL);
            }
            /*
             * Something to update?
             */
            if (attributes.isEmpty()) {
                return account;
            }
            /*
             * Update and return refetched account instance
             */
            storageService.updateMailAccount(mad, attributes, session.getUserId(), session.getContextId(), session.getPassword());
            return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
        } catch (final OXException e) {
            /*
             * Checking full names failed
             */
            final StringBuilder sb = new StringBuilder("Checking default folder full names for account ");
            sb.append(account.getId()).append(" failed with user ").append(session.getUserId());
            sb.append(" in context ").append(session.getContextId());
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AbstractMailAccountAction.class)).warn(sb.toString(), e);
            return account;
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

    private static String getPrefix(final int accountId, final ServerSession session) throws OXException {
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access =
                MailAccess.getInstance(session, accountId);
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
