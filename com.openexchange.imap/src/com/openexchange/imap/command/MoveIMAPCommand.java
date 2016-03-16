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
 *    on the web site http://www.open-xchange.com/EN/legal/fetchRespIndex.html.
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

import static com.openexchange.imap.IMAPCommandsCollection.prepareStringArgument;
import java.util.Arrays;
import javax.mail.MessagingException;
import com.openexchange.java.Strings;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link MoveIMAPCommand} - Moves messages from given folder to given destination folder just using their sequence numbers/UIDs
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MoveIMAPCommand extends AbstractIMAPCommand<long[]> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MoveIMAPCommand.class);

    private static final long[] DEFAULT_RETVAL = new long[0];

    private final boolean uid;

    private final boolean fast;

    private final int length;

    private final String[] args;

    private final long[] uids;

    private final long[] retval;

    private final String destFolderName;

    private boolean proceed = true;

    /**
     * Constructor using sequence numbers and performs a fast <code>MOVE</code> command; meaning optional <i>COPYUID</i> response is
     * discarded.
     *
     * @param imapFolder - the IMAP folder
     * @param startSeqNum - the starting sequence number of the messages that shall be copied
     * @param endSeqNum - the ending sequence number of the messages that shall be copied
     * @param destFolderName - the destination folder fullname
     * @throws MessagingException If a messaging error occurs
     */
    public MoveIMAPCommand(final IMAPFolder imapFolder, final int startSeqNum, final int endSeqNum, final String destFolderName) throws MessagingException {
        this(imapFolder, startend2long(startSeqNum, endSeqNum), destFolderName, true, true, false);
    }

    /**
     * Constructor using sequence numbers and performs a fast <code>MOVE</code> command; meaning optional <i>COPYUID</i> response is
     * discarded.
     *
     * @param imapFolder - the IMAP folder
     * @param seqNums - the sequence numbers of the messages that shall be copied
     * @param destFolderName - the destination folder fullname
     * @param isSequential - whether sequence numbers are sequential or not
     * @throws MessagingException If a messaging error occurs
     */
    public MoveIMAPCommand(final IMAPFolder imapFolder, final int[] seqNums, final String destFolderName, final boolean isSequential) throws MessagingException {
        this(imapFolder, int2long(seqNums), destFolderName, isSequential, true, false);
    }

    /**
     * Constructor using UIDs and consequently performs a <code>UID MOVE</code> command
     *
     * @param imapFolder - the IMAP folder
     * @param uids - the UIDs of the messages that shall be copied
     * @param destFolderName - the destination folder fullname
     * @param isSequential - whether UIDs are sequential or not
     * @param fast - <code>true</code> to ignore corresponding UIDs of copied messages and return value is empty (array of length zero)
     * @throws MessagingException If a messaging error occurs
     */
    public MoveIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final String destFolderName, final boolean isSequential, final boolean fast) throws MessagingException {
        this(imapFolder, uids, destFolderName, isSequential, fast, true);
    }

    private static final int LENGTH = 6; // "COPY <nums> <destination-folder>"

    private static final int LENGTH_WITH_UID = 10; // "UID COPY <nums> <destination-folder>"

    private MoveIMAPCommand(final IMAPFolder imapFolder, final long[] nums, final String destFolderName, final boolean isSequential, final boolean fast, final boolean uid) throws MessagingException {
        super(imapFolder);
        uids = nums == null ? DEFAULT_RETVAL : nums;
        this.uid = uid;
        if (imapFolder.getMessageCount() <= 0) {
            returnDefaultValue = true;
        } else {
            returnDefaultValue = (uids.length == 0);
        }
        this.fast = fast;
        this.destFolderName = prepareStringArgument(destFolderName);
        length = uids.length;
        args = length == 0 ? ARGS_EMPTY : (isSequential ? new String[] { new StringBuilder(64).append(uids[0]).append(':').append(
            uids[length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(
            uids,
            false,
            (uid ? LENGTH_WITH_UID : LENGTH) + destFolderName.length()));
        if (fast) {
            retval = DEFAULT_RETVAL;
        } else {
            retval = new long[length];
            Arrays.fill(retval, -1);
        }
    }

    /**
     * Constructor to move all messages of given folder to given destination folder by performing a <code>MOVE 1:*</code> command.
     * <p>
     * <b>Note</b>: Ensure that denoted folder is not empty.
     *
     * @param imapFolder - the IMAP folder
     * @param destFolderName - the destination folder
     * @throws MessagingException If a messaging error occurs
     */
    public MoveIMAPCommand(final IMAPFolder imapFolder, final String destFolderName) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        fast = true;
        uid = false;
        uids = DEFAULT_RETVAL;
        this.destFolderName = prepareStringArgument(destFolderName);
        retval = DEFAULT_RETVAL;
        args = 1 == messageCount ? new String[] { "1" } : ARGS_ALL;
        length = -1;
    }

    @Override
    protected boolean addLoopCondition() {
        return (fast ? false : proceed);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(final int argsIndex) {
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("MOVE ");
        sb.append(args[argsIndex]);
        sb.append(' ').append(destFolderName);
        return sb.toString();
    }

    @Override
    protected long[] getDefaultValue() {
        return DEFAULT_RETVAL;
    }

    @Override
    protected long[] getReturnVal() {
        return retval;
    }

    private static final String COPYUID = "copyuid";

    @Override
    protected boolean handleResponse(final Response response) throws MessagingException {
        if (fast || !response.isOK()) {
            return false;
        }
        /*-
         * Parse response:
         *
         * OK [COPYUID 1184051486 10031:10523,10525:11020,11022:11027,11030:11047,11050:11051,11053:11558 1024:2544] Completed
         *
         * * 45 EXISTS
         * * 2 RECENT
         * A4 OK [COPYUID 1185853191 7,32 44:45] Completed
         */
        String resp = Strings.asciiLowerCase(response.toString());
        int pos = resp.indexOf(COPYUID);
        if (pos < 0) {
            return false;
        }
        /*
         * Found COPYUID...
         */
        final COPYUIDResponse copyuidResp = new COPYUIDResponse(LOG);
        /*
         * Find next starting ATOM in IMAP response
         */
        pos += COPYUID.length();
        while (Strings.isWhitespace(resp.charAt(pos))) {
            pos++;
        }
        /*
         * Split by ATOMs
         */
        final String[] sa = Strings.splitByWhitespaces(resp.substring(pos));
        if (sa.length >= 3) {
            /*-
             * Array contains atoms like:
             *
             * "1167880112", "11937", "11939]", "Completed"
             */
            copyuidResp.src = sa[1];
            {
                final String destStr = sa[2];
                final int mlen = destStr.length() - 1;
                if (']' == destStr.charAt(mlen)) {
                    copyuidResp.dest = destStr.substring(0, mlen);
                } else {
                    copyuidResp.dest = destStr;
                }
            }
            copyuidResp.fillResponse(uids, retval);
        } else {
            LOG.error("Invalid COPYUID response: {}", resp);
        }
        proceed = false;
        return true;
    }

    private static long[] startend2long(final int start, final int end) {
        final long[] longArr = new long[2];
        longArr[0] = start;
        longArr[1] = end;
        return longArr;
    }

    private static long[] int2long(final int[] intArr) {
        final long[] longArr = new long[intArr.length];
        System.arraycopy(intArr, 0, longArr, 0, intArr.length);
        return longArr;
    }
}
