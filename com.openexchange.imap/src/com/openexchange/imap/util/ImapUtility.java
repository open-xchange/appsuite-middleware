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

package com.openexchange.imap.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.Store;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * {@link ImapUtility} - IMAP utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImapUtility {

    /**
     * Initializes a new {@link ImapUtility}.
     */
    private ImapUtility() {
        super();
    }

    /**
     * Appends command information to given information string.
     *
     * @param info The information
     * @param imapFolder The optional IMAP folder
     * @return The command with optional information appended
     */
    public static String appendCommandInfo(String info, IMAPFolder imapFolder) {
        if (null == imapFolder) {
            return info;
        }
        final StringBuilder sb = new StringBuilder(info);
        sb.append(" (folder=\"").append(imapFolder.getFullName()).append('"');
        final Store store = imapFolder.getStore();
        if (null != store) {
            sb.append(", store=\"").append(store.toString()).append('"');
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Appends command information to given information string.
     *
     * @param info The information
     * @param fullName The optional full name of associated folder
     * @param store The optional description of connected IMAP store
     * @return The command with optional information appended
     */
    public static String appendCommandInfo(String info, String fullName, String store) {
        final StringBuilder sb = new StringBuilder(info);
        boolean parenthesis = true;
        if (!com.openexchange.java.Strings.isEmpty(fullName)) {
            sb.append(" (folder=\"").append(fullName).append('"');
            parenthesis = false;
        }
        if (!com.openexchange.java.Strings.isEmpty(store)) {
            if (parenthesis) {
                sb.append(" (");
                parenthesis = false;
            } else {
                sb.append(", ");
            }
            sb.append("store=\"").append(store).append('"');
        }
        if (!parenthesis) {
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Checks if given <code>MessagingException</code> indicates "Invalid messageset" or "Invalid uidset" error.
     *
     * @param response The IMAP response to check
     * @return <code>true</code> if given <code>MessagingException</code> indicates "Invalid messageset" or "Invalid uidset" error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(MessagingException error) {
        if (null == error) {
            return false;
        }
        final Exception exception = error.getNextException();
        if (!(exception instanceof com.sun.mail.iap.BadCommandException)) {
            return false;
        }
        return isInvalidMessageset((com.sun.mail.iap.BadCommandException) exception);
    }

    /**
     * Checks if given <code>BadCommandException</code> indicates "Invalid messageset" or "Invalid uidset" error.
     *
     * @param response The IMAP response to check
     * @return <code>true</code> if given <code>BadCommandException</code> indicates "Invalid messageset" or "Invalid uidset" error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(com.sun.mail.iap.BadCommandException error) {
        // A90 BAD Error in IMAP command FETCH: Invalid messageset
        if (null == error) {
            return false;
        }
        String sResponse = error.getMessage();
        if (null == sResponse) {
            return false;
        }
        sResponse = sResponse.toLowerCase(Locale.US);
        return sResponse.indexOf("invalid messageset") >= 0 || sResponse.indexOf("invalid uidset") >= 0;
    }

    /**
     * Checks if given response indicates "Invalid messageset" or "Invalid uidset" IMAP error.
     *
     * @param response The IMAP response to check
     * @return <code>true</code> if given response indicates "Invalid messageset" or "Invalid uidset" IMAP error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(Response response) {
        // A90 BAD Error in IMAP command FETCH: Invalid messageset
        if (null == response || !response.isBAD()) {
            return false;
        }
        String sResponse = response.toString();
        if (null == sResponse) {
            return false;
        }
        sResponse = sResponse.toLowerCase(Locale.US);
        return sResponse.indexOf("invalid messageset") >= 0 || sResponse.indexOf("invalid uidset") >= 0;
    }

    /**
     * Prepares the IMAP command for logging purpose.
     *
     * @param imapCommand The IMAP command to prepare
     * @param args The command arguments
     * @return The prepared IMAP command
     */
    public static String prepareImapCommandForLogging(String imapCommand, Argument args) {
        if (null == args) {
            return prepareImapCommandForLogging(imapCommand);
        }

        return (null == imapCommand ? null : prepareImapCommandForLogging(new StringBuilder(imapCommand).append(' ').append(args).toString()));
    }

    /**
     * Prepares the IMAP command for logging purpose.
     *
     * @param imapCommand The IMAP command to prepare
     * @return The prepared IMAP command
     */
    public static String prepareImapCommandForLogging(String imapCommand) {
        if (null == imapCommand) {
            return "NIL";
        }
        if (imapCommand.startsWith("FETCH ")) {
            int openParenthesis = imapCommand.indexOf('(', 6);
            if (openParenthesis <= 32) {
                return surroundWithSingleQuotes(imapCommand);
            }
            return new StringBuilder(imapCommand.length()).append('\'').append("FETCH ... ").append(imapCommand.substring(openParenthesis)).append('\'').toString();
        } else if (imapCommand.startsWith("UID FETCH ")) {
            int openParenthesis = imapCommand.indexOf('(', 6);
            if (openParenthesis <= 36) {
                return surroundWithSingleQuotes(imapCommand);
            }
            return new StringBuilder(imapCommand.length()).append('\'').append("UID FETCH ... ").append(imapCommand.substring(openParenthesis)).append('\'').toString();
        } else if (imapCommand.startsWith("UID EXPUNGE ")) {
            return imapCommand.length() > 32 ? "'UID EXPUNGE ...'" : surroundWithSingleQuotes(imapCommand);
        } else {
            return surroundWithSingleQuotes(imapCommand);
        }
    }

    private static String surroundWithSingleQuotes(String imapCommand) {
        return new StringBuilder(imapCommand.length() + 2).append('\'').append(imapCommand).append('\'').toString();
    }
    
    /**
     * Encodes a full folder name. When encoding, capabilities of the protocol are taken into consideration.
     * <p>
     * If the protocol supports <code>UTF8</code> encoded names, folder name will be added as <code>UTF8</code>.
     * If not the folder name will be encoded as per RFC2060, with <code>Base64</code>
     *
     * @param fullFolderName The full name of the folder to encode
     * @param protocol The {@link IMAPProtocol} to get capabilities from
     * @return The encoded folder name written to an {@link Argument}
     * @see com.sun.mail.imap.protocol.IMAPProtocol#writeMailboxName(Argument, String)
     */
    public static Argument encodeFolderName(String fullFolderName, IMAPProtocol protocol) {
        if (null != protocol && protocol.supportsUtf8()) {
            return new Argument().writeString(fullFolderName, StandardCharsets.UTF_8);
        }
        return new Argument().writeString(BASE64MailboxEncoder.encode(fullFolderName));
    }

}
