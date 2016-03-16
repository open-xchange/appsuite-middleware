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

package com.openexchange.unifiedinbox.utility;

import static com.openexchange.mail.MailPath.SEPERATOR;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.unifiedinbox.UnifiedInboxAccess;
import com.openexchange.unifiedinbox.UnifiedInboxException;
import com.openexchange.unifiedinbox.services.Services;

/**
 * {@link UnifiedInboxUtility} - Utility methods for Unified Mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxUtility {

    /**
     * Initializes a new {@link UnifiedInboxUtility}.
     */
    private UnifiedInboxUtility() {
        super();
    }

    /**
     * Gets the default max. running millis.
     *
     * @return The default max. running millis
     */
    public static long getMaxRunningMillis() {
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null == service) {
            return 60000L;
        }
        return service.getIntProperty("com.openexchange.requestwatcher.maxRequestAge", 60000);
    }

    /**
     * Parses specified Unified Mail mail IDs.
     *
     * @param mailIDs The Unified Mail mail IDs to parse
     * @return A map grouping referenced accounts and referenced full names and IDs.
     * @throws OXException If parsing mail IDs fails
     */
    public static TIntObjectMap<Map<String, List<String>>> parseMailIDs(String[] mailIDs) throws OXException {
        final TIntObjectMap<Map<String, List<String>>> map = new TIntObjectHashMap<Map<String,List<String>>>(mailIDs.length);
        // Start parsing
        final UnifiedInboxUID uidl = new UnifiedInboxUID();
        for (final String mailID : mailIDs) {
            uidl.setUIDString(mailID);
            Map<String, List<String>> folderUIDMap = map.get(uidl.getAccountId());
            if (null == folderUIDMap) {
                folderUIDMap = new HashMap<String, List<String>>(mailIDs.length / 2);
                map.put(uidl.getAccountId(), folderUIDMap);
            }
            final String folder = uidl.getFullName();
            List<String> uids = folderUIDMap.get(folder);
            if (null == uids) {
                uids = new ArrayList<String>();
                folderUIDMap.put(folder, uids);
            }
            uids.add(uidl.getId());
        }
        return map;
    }

    /**
     * Generates a nested folder's full name.
     *
     * @param uiAccountId The Unified Mail's account ID
     * @param uiFullName The Unified Mail's full name
     * @param nestedAccountId The nested account's ID
     * @param nestedFullName The nested folder's full name or <code>null</code>
     * @return The generated nested folder's full name.
     */
    public static String generateNestedFullName(int uiAccountId, String uiFullName, int nestedAccountId, String nestedFullName) {
        if (null == nestedFullName) {
            return new StringBuilder(16).append(/*prepareFullname(uiAccountId, uiFullName)*/ uiFullName).toString();
        }
        return new StringBuilder(32).append(/*prepareFullname(uiAccountId, uiFullName)*/ uiFullName).append(SEPERATOR).append(prepareFullname(nestedAccountId, nestedFullName)).toString();
    }

    /**
     * Parses nested full name.
     * <p>
     * <code>"INBOX/default3/INBOX"</code> =&gt; <code>"default3/INBOX"</code>
     *
     * @param nestedFullName The nested full name to parse
     * @return The parsed nested full name argument
     * @throws OXException If specified nested full name is invalid
     */
    public static FullnameArgument parseNestedFullName(String nestedFullName) throws OXException {
        return UnifiedInboxUID.parseNestedFullName(nestedFullName);
    }

    /**
     * Gets the account's full name.
     *
     * @param mailAccess The mail access to desired account
     * @param fullName The full name to look-up
     * @return The account's full name
     * @throws OXException If full name look-up fails
     */
    public static String determineAccountFullName(MailAccess<?, ?> mailAccess, String fullName) throws OXException {
        if (UnifiedInboxAccess.INBOX.equals(fullName)) {
            return UnifiedInboxAccess.INBOX;
        }
        if (UnifiedInboxAccess.DRAFTS.equals(fullName)) {
            return mailAccess.getFolderStorage().getDraftsFolder();
        }
        if (UnifiedInboxAccess.SENT.equals(fullName)) {
            return mailAccess.getFolderStorage().getSentFolder();
        }
        if (UnifiedInboxAccess.SPAM.equals(fullName)) {
            return mailAccess.getFolderStorage().getSpamFolder();
        }
        if (UnifiedInboxAccess.TRASH.equals(fullName)) {
            return mailAccess.getFolderStorage().getTrashFolder();
        }
        throw UnifiedInboxException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX.create(fullName);
    }

    /**
     * Prints specified {@link Throwable}'s stack trace to given string builder.
     *
     * @param t The {@link Throwable} instance
     * @param builder The string builder to append to
     */
    public static void appendStackTrace2StringBuilder(Throwable t, StringBuilder builder) {
        t.printStackTrace(new java.io.PrintWriter(new UnsynchronizedStringWriter(builder)));
    }

}
