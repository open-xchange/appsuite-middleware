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

package com.openexchange.mailaccount;

import static com.openexchange.mail.MailPath.SEPERATOR;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;
import org.apache.commons.codec.DecoderException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link UnifiedInboxUID} - The Unified Mail UID.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxUID {

    /**
     * Parses nested full name.
     * <p>
     * <code>"INBOX/default3/INBOX"</code> =&gt; <code>"default3/INBOX"</code>
     *
     * @param nestedFullName The nested full name to parse
     * @return The parsed nested full name argument or <code>null</code>
     */
    public static FullnameArgument parsePossibleNestedFullName(String nestedFullName) {
        // INBOX/default0/INBOX
        if (!startsWithKnownFullname(nestedFullName)) {
            return null;
        }
        // Cut off starting known full name and its separator character
        final String fn = nestedFullName.substring(nestedFullName.indexOf(SEPERATOR) + 1);
        return prepareMailFolderParam(fn);
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
        FullnameArgument ret = parsePossibleNestedFullName(nestedFullName);
        if (null == ret) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(prepareMailFolderParam(nestedFullName).getFullname());
        }
        return ret;
    }

    /**
     * Extracts the possible nested mail path form given Unified Mail UID (if that UID appears to be a Unified Mail UID string).
     *
     * @param uidl The Unified Mail UID string
     * @return The extracted mail path or <code>null</code>
     */
    public static MailPath extractPossibleNestedMailPath(String uidl) {
        UnifiedInboxUID uid;
        try {
            uid = new UnifiedInboxUID(uidl);
        } catch (OXException e) {
            return null;
        }

        // INBOX/default0/INBOX
        String nestedFullName = uid.getFullName();
        if (!startsWithKnownFullname(nestedFullName)) {
            return null;
        }

        int beginIndex = nestedFullName.indexOf(SEPERATOR) + 1;
        if (beginIndex <= 0) {
            return null;
        }

        String fn = nestedFullName.substring(beginIndex);
        if (!fn.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return null;
        }

        try {
            return new MailPath(fn + SEPERATOR + uid.getId());
        } catch (OXException e) {
            return null;
        }
    }

    private static boolean startsWithKnownFullname(String fullName) {
        for (final String knownFullname : UnifiedInboxManagement.KNOWN_FOLDERS) {
            if (fullName.startsWith(knownFullname)) {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------

    private int accountId;
    private String fullName;
    private String id;
    private String uidl;

    /**
     * Initializes an empty {@link UnifiedInboxUID}.
     */
    public UnifiedInboxUID() {
        super();
    }

    /**
     * Initializes a new {@link UnifiedInboxUID}.
     *
     * @param accountId The account ID
     * @param fullName The folder full name
     * @param id The mail ID
     */
    public UnifiedInboxUID(final int accountId, final String fullName, final String id) {
        super();
        setUID(accountId, fullName, id);
    }

    /**
     * Initializes a new {@link UnifiedInboxUID}.
     *
     * @param unifiedINBOXUID The Unified Mail UID as a string
     * @throws OXException If parsing Unified Mail UID fails
     */
    public UnifiedInboxUID(final String unifiedINBOXUID) throws OXException {
        super();
        setUIDString(unifiedINBOXUID);
    }

    /**
     * Sets the UID of this {@link UnifiedInboxUID}.
     *
     * @param accountId The account ID
     * @param fullName The folder full name
     * @param id The mail ID
     * @return This {@link UnifiedInboxUID} with new UID applied.
     */
    public UnifiedInboxUID setUID(final int accountId, final String fullName, final String id) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.id = id;
        final String mailPath = MailPath.getMailPath(accountId, fullName, id);
        uidl = encodeQP(mailPath);
        return this;
    }

    /**
     * Sets the UID of this {@link UnifiedInboxUID}.
     *
     * @param unifiedINBOXUID The Unified Mail UID as a string
     * @throws MailException If parsing Unified Mail UID fails
     * @return This {@link UnifiedInboxUID} with new UID applied.
     */
    public UnifiedInboxUID setUIDString(final String unifiedINBOXUID) throws OXException {
        final String decoded = decodeQP(unifiedINBOXUID);
        final MailPath mailPath = new MailPath(decoded);
        accountId = mailPath.getAccountId();
        fullName = mailPath.getFolder();
        id = mailPath.getMailID();
        uidl = decoded;
        return this;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder full name.
     *
     * @return The folder full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the mail ID.
     *
     * @return The mail ID
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return uidl;
    }

    private static final BitSet PRINTABLE_CHARS = new BitSet(256);
    // Static initializer for printable chars collection
    static {
        for (int i = '0'; i <= '9'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
    }

    private static String encodeQP(final String string) {
        try {
            return Charsets.toAsciiString(com.openexchange.mailaccount.utils.QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, string.getBytes(com.openexchange.java.Charsets.UTF_8)));
        } catch (final UnsupportedCharsetException e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    private static String decodeQP(final String string) {
        try {
            return new String(com.openexchange.mailaccount.utils.QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(string)), com.openexchange.java.Charsets.UTF_8);
        } catch (final DecoderException e) {
            throw new IllegalStateException(e);
        }
    }

}
