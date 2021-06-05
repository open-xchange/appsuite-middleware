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

package com.openexchange.mail.utils;

import java.util.Optional;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link MailFolderUtility} - Provides utility methods for mail folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderUtility {

    /**
     * The static prefix used to quickly identify fully-qualified mail folder identifiers; e.g. <code>"default0/INBOX"</code>.
     *
     * @value "default"
     */
    private static final String MAIL_PREFIX = MailFolder.MAIL_PREFIX;

    /**
     * Virtual full name of mailbox's root folder
     *
     * @value "" (empty string)
     */
    public static final String ROOT_FOLDER_ID = MailFolder.ROOT_FOLDER_ID;

    /**
     * Initializes a new {@link MailFolderUtility}.
     */
    private MailFolderUtility() {
        super();
    }

    /**
     * Checks for an empty string.
     *
     * @param string The string
     * @return <code>true</code> if input is null or empty; else <code>false</code>
     */
    public static boolean isEmpty(String string) {
        return Strings.isEmpty(string);
    }

    /**
     * Performs mailbox encoding.
     *
     * @param fullName The full name
     * @return The encoded full name
     */
    public static String encode(String fullName) {
        return BASE64MailboxEncoder.encode(fullName);
    }

    /**
     * Performs mailbox decoding.
     *
     * @param encoded The encoded full name
     * @return The decoded full name
     */
    public static String decode(String encoded) {
        return BASE64MailboxDecoder.decode(encoded);
    }

    /**
     * The length of the MAIL_PREFIX
     */
    private static final int LEN = MAIL_PREFIX.length();

    /**
     * Parses specified full name argument to an appropriate instance of {@link FullnameArgument}.
     * <p>
     * Cuts off starting {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator from specified folder full name argument only if
     * full name argument is not <code>null</code> and is not equal to {@link MailFolder#DEFAULT_FOLDER_ID}.<br>
     * Example:
     *
     * <pre>
     * &quot;default0/INBOX&quot; -&gt; &quot;INBOX&quot;
     * </pre>
     *
     * @param fullnameArgument The groupware's mail folder full name
     * @return The stripped mail folder full name argument
     * @throws IllegalArgumentException If mail folder identifier is invalid
     * @see #optPrepareMailFolderParam(String)
     */
    public static FullnameArgument prepareMailFolderParam(String fullnameArgument) {
        if (fullnameArgument == null) {
            return null;
        }
        Optional<FullnameArgument> optional = optPrepareMailFolderParam(fullnameArgument);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Invalid fully-qualifying mail folder identifier: " + fullnameArgument);
        }
        return optional.get();
    }
    /**
     * Parses specified full name argument to an appropriate instance of {@link FullnameArgument} if possible. If parsing is applicable,
     * the resulting full name of parsed {@link FullnameArgument} is returned; otherwise the given argument.
     * <p>
     * This is a convenience method for:
     *
     * <pre>
     * Optional<FullnameArgument> optional = MailFolderUtility.optPrepareMailFolderParam(fullnameParameter);
     * return optional.isPresent() ? optional.get().getFullName() : fullnameParameter;
     * </pre>
     *
     * @param fullnameArgument The groupware's mail folder full name
     * @return The account-specific full name argument
     * @throws IllegalArgumentException If mail account identifier cannot be parsed to an integer
     * @see #optPrepareMailFolderParam(String)
     */
    public static String prepareMailFolderParamOrElseReturn(String fullnameArgument) {
        Optional<FullnameArgument> optional = MailFolderUtility.optPrepareMailFolderParam(fullnameArgument);
        return optional.isPresent() ? optional.get().getFullName() : fullnameArgument;
    }

    /**
     * Parses specified full name argument to an appropriate instance of {@link FullnameArgument}.
     * <p>
     * Cuts off starting {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator from specified folder full name argument only if
     * full name argument is not <code>null</code> and is not equal to {@link MailFolder#DEFAULT_FOLDER_ID}.<br>
     * Example:
     *
     * <pre>
     * &quot;default0/INBOX&quot; -&gt; &quot;INBOX&quot;
     * </pre>
     *
     * @param fullnameArgument The groupware's mail folder full name
     * @return The stripped mail folder full name argument
     * @throws IllegalArgumentException If mail account identifier cannot be parsed to an integer
     */
    public static Optional<FullnameArgument> optPrepareMailFolderParam(String fullnameArgument) {
        if (fullnameArgument == null) {
            return Optional.empty();
        }
        if (!fullnameArgument.startsWith(MAIL_PREFIX)) {
            return Optional.empty();
        }

        int len = fullnameArgument.length();
        char separator = MailProperties.getInstance().getDefaultSeparator();
        int index = LEN;
        {
            char c = 0;
            while (index < len && Character.isDigit(c = fullnameArgument.charAt(index)) && c != separator) {
                index++;
            }
        }

        if (index == LEN) {
            // Only got "default"
            return Optional.empty();
        }

        // Parse account identifier
        final int accountId;
        try {
            accountId = Integer.parseInt(fullnameArgument.substring(LEN, index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mail account identifier is not a number: " + fullnameArgument, e);
        }

        if (index >= len) {
            // Only got "default0"
            return Optional.of(new FullnameArgument(accountId, ROOT_FOLDER_ID));
        }

        if (fullnameArgument.charAt(index) != separator) {
            // Missing expected separator character
            return Optional.empty();
        }
        return Optional.of(new FullnameArgument(accountId, fullnameArgument.substring(index + 1)));
    }

    /**
     * Checks if specified full name argument's real full name equals given full name.
     *
     * @param fullnameArgument The full name argument
     * @param fullname The full name to compare with
     * @return <code>true</code> if specified full name argument's real full name equals given full name; otherwise <code>false</code>
     */
    public static boolean equalsFullname(String fullnameArgument, String fullname) {
        if (fullnameArgument == null) {
            if (fullname == null) {
                return true;
            }
            return false;
        }
        return prepareMailFolderParam(fullnameArgument).getFullname().equals(fullname);
    }

    /**
     * Prepends {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator (obtained by {@link MailConfig#getDefaultSeparator()}) to
     * given folder full name. <br>
     * Example:
     *
     * <pre>
     * &quot;INBOX&quot; -&gt; &quot;default2/INBOX&quot;
     * </pre>
     *
     * @param accountId The account ID
     * @param fullname The folder full name
     * @return The groupware's mail folder full name
     */
    public static String prepareFullname(int accountId, String fullname) {
        if (fullname == null) {
            return null;
        }
        if (ROOT_FOLDER_ID.equals(fullname)) {
            return new StringBuilder(MAIL_PREFIX).append(accountId).toString();
        }

        int length = fullname.length();
        char separator = MailProperties.getInstance().getDefaultSeparator();
        if (false == fullname.startsWith(MAIL_PREFIX)) {
            return new StringBuilder(LEN + length + 4).append(MAIL_PREFIX).append(accountId).append(separator).append(fullname).toString();
        }

        // Tricky case...
        int sepPos = fullname.indexOf(separator);
        if (sepPos >= LEN) {
            int accId = optNumFor(fullname.substring(LEN, sepPos));
            if (accId >= 0) {
                FullnameArgument fa = prepareMailFolderParam(fullname);
                if (fa.getAccountId() == accountId) {
                    return fullname;
                }
            }
        }
        return new StringBuilder(LEN + length + 4).append(MAIL_PREFIX).append(accountId).append(separator).append(fullname).toString();
    }

    private static int optNumFor(String possibleNumber) {
        try {
            return Strings.parseInt(possibleNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
