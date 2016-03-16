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
    public FlagsIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final Flags flags, final boolean enable, final boolean silent, final boolean isSequential) throws MessagingException {
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

    private void appendSystemFlags(final Flag[] systemFlags, final StringBuilder flagsStrBuilder) throws MessagingException {
        if (systemFlags.length > 0) {
            flagsStrBuilder.append(getFlagString(systemFlags[0]));
            for (int i = 1; i < systemFlags.length; i++) {
                flagsStrBuilder.append(' ').append(getFlagString(systemFlags[i]));
            }
        }
    }

    private void appendUserFlags(final String[] userFlags, final StringBuilder flagsStrBuilder) {
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
    public FlagsIMAPCommand(final IMAPFolder imapFolder, final Flags flags, final boolean enable, final boolean silent) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        args = 1 == messageCount ? new String[] { "1" } : ARGS_ALL;
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
    public FlagsIMAPCommand(final IMAPFolder imapFolder, final int startSeqNum, final int endSeqNum, final Flags flags, final boolean enable, final boolean silent) throws MessagingException {
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

    private static String getFlagString(final Flag systemFlag) throws MessagingException {
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
    protected String getCommand(final int argsIndex) {
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
    protected boolean handleResponse(final Response response) throws MessagingException {
        // No intermediate response expected
        return false;
    }

}
