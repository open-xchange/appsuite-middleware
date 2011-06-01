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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.json.fields.MailAccountFields;
import com.openexchange.secret.SecretService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailAccountAction} - An abstract folder action.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccountAction implements AJAXActionService {

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
     * @throws AjaxException If parameter is not present in given request
     */
    protected static int parseIntParameter(final String parameterName, final AJAXRequestData request) throws AjaxException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, parameterName);
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
     * @throws AjaxException If parameter is not present in given request
     */
    protected static int[] parseIntArrayParameter(final String parameterName, final AJAXRequestData request) throws AjaxException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, parameterName);
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

    protected static void checkNeededFields(final MailAccountDescription accountDescription) throws AjaxException {
        // Check needed fields
        if (null == accountDescription.getMailServer()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, MailAccountFields.MAIL_URL);
        }
        if (null == accountDescription.getLogin()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, MailAccountFields.LOGIN);
        }
        if (null == accountDescription.getPassword()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, MailAccountFields.PASSWORD);
        }
    }

    protected static String getSecret(final ServerSession session) {
        final SecretService secretService = ServerServiceRegistry.getInstance().getService(SecretService.class);
        return secretService.getSecret(session);
    }

    protected static boolean isDefaultMailAccount(final MailAccount mailAccount) {
        return mailAccount.isDefaultAccount() || MailAccount.DEFAULT_ID == mailAccount.getId();
    }

    protected static boolean isDefaultMailAccount(final MailAccountDescription mailAccount) {
        return MailAccount.DEFAULT_ID == mailAccount.getId();
    }

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

    protected static MailAccount checkFullNames(final MailAccount account, final MailAccountStorageService storageService, final ServerSession session) throws MailAccountException {
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
        /*
         * Check full names
         */
        String fullName = account.getConfirmedHamFullname();
        if (null == fullName) {
            prefix = getPrefix(accountId, session);
            mad.setConfirmedHamFullname((tmp = new StringBuilder(prefix)).append(account.getConfirmedHam()).toString());
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
            mad.setConfirmedSpamFullname(tmp.append(account.getConfirmedSpam()).toString());
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
            mad.setDraftsFullname(tmp.append(account.getDrafts()).toString());
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
            mad.setSentFullname(tmp.append(account.getSent()).toString());
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
            mad.setSpamFullname(tmp.append(account.getSpam()).toString());
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
            mad.setTrashFullname(tmp.append(account.getTrash()).toString());
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
    }

    private static String getPrefix(final int accountId, final ServerSession session) throws MailAccountException {
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = MailAccess.getInstance(session, accountId);
            access.connect(false);
            try {
                return ((MailFolderStorage) access.getFolderStorage()).getDefaultFolderPrefix();
            } finally {
                access.close(true);
            }
        } catch (final MailException e) {
            throw new MailAccountException(e);
        }
    }

}
