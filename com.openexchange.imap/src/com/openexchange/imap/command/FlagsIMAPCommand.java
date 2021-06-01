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

package com.openexchange.imap.command;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link FlagsIMAPCommand} - Enables/disables message's system e.g. \SEEN or \DELETED and user flags as well.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FlagsIMAPCommand extends AbstractIMAPCommand<Boolean> {

    private static final int MAX_LENGTH = 27; // "UID STORE <nums> +/-FLAGS.SILENT (<flags>)"

    private final String[] args;

    private final String flagsStr;

    private final boolean enable;

    private final boolean uid;

    private final boolean silent;

    /**
     * Constructor to set flags in messages identified through given UIDs.
     *
     * @param imapFolder - the IMAP folder
     * @param uids - the UIDs
     * @param flags - the flags
     * @param silent <code>true</code> to suppress returning the new value; otherwise <code>false</code>
     * @param enable - whether to enable or disable affected flags
     * @param isSequential - whether supplied UIDs are in sequential order or not
     * @throws MessagingException - if an unknown system flag is used
     */
    public FlagsIMAPCommand(IMAPFolder imapFolder, long[] uids, Flags flags, boolean enable, boolean silent, boolean isSequential) throws MessagingException {
        super(imapFolder);
        if (imapFolder.getMessageCount() <= 0) {
            returnDefaultValue = true;
        }
        if ((uids == null) || (uids.length == 0)) {
            returnDefaultValue = true;
            args = ARGS_EMPTY;
            flagsStr = null;
        } else {
            if (flags == null) {
                returnDefaultValue = true;
                flagsStr = null;
            } else {
                final StringBuilder flagsStrBuilder = new StringBuilder(16);
                appendSystemFlags(flags.getSystemFlags(), flagsStrBuilder);
                appendUserFlags(flags.getUserFlags(), flagsStrBuilder);
                if (flagsStrBuilder.length() == 0) {
                    returnDefaultValue = true;
                    flagsStr = null;
                } else {
                    flagsStr = flagsStrBuilder.toString();
                }
            }
            args =
                isSequential ? new String[] { new StringBuilder(64).append(uids[0]).append(':').append(uids[uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(
                    uids,
                    !silent,
                    MAX_LENGTH + (null == flagsStr ? 0 : flagsStr.length()));

        }
        this.enable = enable;
        this.silent = silent;
        uid = true;
    }

    private void appendSystemFlags(Flag[] systemFlags, StringBuilder flagsStrBuilder) throws MessagingException {
        if (systemFlags.length > 0) {
            flagsStrBuilder.append(getFlagString(systemFlags[0]));
            for (int i = 1; i < systemFlags.length; i++) {
                flagsStrBuilder.append(' ').append(getFlagString(systemFlags[i]));
            }
        }
    }

    private void appendUserFlags(String[] userFlags, StringBuilder flagsStrBuilder) {
        if (userFlags.length > 0) {
            if (flagsStrBuilder.length() > 0) {
                flagsStrBuilder.append(' ');
            }
            flagsStrBuilder.append(userFlags[0]);
            for (int i = 1; i < userFlags.length; i++) {
                flagsStrBuilder.append(' ').append(userFlags[i]);
            }
        }
    }

    /**
     * Constructor to set flags in all messages
     * <p>
     * <b>Note</b>: Ensure that denoted folder is not empty.
     *
     * @param imapFolder - the imap folder
     * @param flags - the flags
     * @param enable - whether to enable or disable affected flags
     * @param silent <code>true</code> to suppress returning the new value; otherwise <code>false</code>
     * @throws MessagingException - if an unknown system flag is used
     */
    public FlagsIMAPCommand(IMAPFolder imapFolder, Flags flags, boolean enable, boolean silent) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        args = 1 == messageCount ? ARGS_FIRST : ARGS_ALL;
        if (flags == null) {
            returnDefaultValue = true;
            flagsStr = null;
        } else {
            final StringBuilder flagsStrBuilder = new StringBuilder(16);
            appendSystemFlags(flags.getSystemFlags(), flagsStrBuilder);
            appendUserFlags(flags.getUserFlags(), flagsStrBuilder);
            if (flagsStrBuilder.length() == 0) {
                returnDefaultValue = true;
                flagsStr = null;
            } else {
                flagsStr = flagsStrBuilder.toString();
            }
        }
        this.enable = enable;
        this.silent = silent;
        uid = false;
    }

    /**
     * Constructor to set flags starting at message whose sequence number matches specified <code>startSeqNum</code> and ending at message
     * whose sequence number matches specified <code>endSeqNum</code>
     *
     * @param imapFolder - the imap folder
     * @param startSeqNum The start sequence number
     * @param endSeqNum The end sequence number
     * @param flags - the flags
     * @param enable - whether to enable or disable affected flags
     * @param silent <code>true</code> to suppress returning the new value; otherwise <code>false</code>
     * @throws MessagingException - if an unknown system flag is used
     */
    public FlagsIMAPCommand(IMAPFolder imapFolder, int startSeqNum, int endSeqNum, Flags flags, boolean enable, boolean silent) throws MessagingException {
        super(imapFolder);
        if (imapFolder.getMessageCount() <= 0) {
            returnDefaultValue = true;
        }
        args = new String[] { new StringBuilder(16).append(startSeqNum).append(':').append(endSeqNum).toString() };
        if (flags == null) {
            returnDefaultValue = true;
            flagsStr = null;
        } else {
            final StringBuilder flagsStrBuilder = new StringBuilder(16);
            appendSystemFlags(flags.getSystemFlags(), flagsStrBuilder);
            appendUserFlags(flags.getUserFlags(), flagsStrBuilder);
            if (flagsStrBuilder.length() == 0) {
                returnDefaultValue = true;
                flagsStr = null;
            } else {
                flagsStr = flagsStrBuilder.toString();
            }
        }
        this.enable = enable;
        uid = false;
        this.silent = silent;
    }

    public static final String FLAG_ANSWERED = "\\Answered";

    public static final String FLAG_DELETED = "\\Deleted";

    public static final String FLAG_DRAFT = "\\Draft";

    public static final String FLAG_FLAGGED = "\\Flagged";

    public static final String FLAG_RECENT = "\\Recent";

    public static final String FLAG_SEEN = "\\Seen";

    public static final String FLAG_USER = "\\User";

    private static String getFlagString(Flag systemFlag) throws MessagingException {
        if (Flags.Flag.ANSWERED.equals(systemFlag)) {
            return FLAG_ANSWERED;
        } else if (Flags.Flag.DELETED.equals(systemFlag)) {
            return FLAG_DELETED;
        } else if (Flags.Flag.DRAFT.equals(systemFlag)) {
            return FLAG_DRAFT;
        } else if (Flags.Flag.FLAGGED.equals(systemFlag)) {
            return FLAG_FLAGGED;
        } else if (Flags.Flag.RECENT.equals(systemFlag)) {
            return FLAG_RECENT;
        } else if (Flags.Flag.SEEN.equals(systemFlag)) {
            return FLAG_SEEN;
        } else if (Flags.Flag.USER.equals(systemFlag)) {
            return FLAG_USER;
        }
        throw new MessagingException("Unknown System Flag");
    }

    @Override
    protected boolean addLoopCondition() {
        return true;
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(int argsIndex) {
        // UID STORE %s %sFLAGS (%s)
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("STORE ");
        sb.append(args[argsIndex]);
        sb.append(' ').append(enable ? '+' : '-');
        sb.append("FLAGS");
        if (silent) {
            sb.append(".SILENT");
        }
        sb.append(" (").append(flagsStr).append(')');
        return sb.toString();
    }

    @Override
    protected Boolean getDefaultValue() {
        return Boolean.TRUE;
    }

    @Override
    protected Boolean getReturnVal() {
        return Boolean.TRUE;
    }

    @Override
    protected boolean handleResponse(Response response) throws MessagingException {
        // No intermediate response expected
        return false;
    }

}
