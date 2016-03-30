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

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link BodystructureFetchIMAPCommand} - performs a prefetch of messages in given folder with only those fields set that need to be
 * present for display and sorting. A corresponding instance of <code>javax.mail.FetchProfile</code> is going to be generated from given
 * fields.
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitly requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code> objects.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BodystructureFetchIMAPCommand extends AbstractIMAPCommand<BODYSTRUCTURE[]> {

    private static final int LENGTH = 9; // "FETCH <nums> (<command>)"

    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"

    private static final int LENGTH_BODYSTRUCTURE = 13;

    private final String[] args;

    private final boolean uid;

    private final int length;

    private int index;

    private final TLongIntMap posMap;

    private final BODYSTRUCTURE[] retval;

    /**
     * Initializes a new {@link BodystructureFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param separator The separator character
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param seqNums The sequence numbers to fetch
     * @throws MessagingException If initialization fails
     */
    public BodystructureFetchIMAPCommand(final IMAPFolder imapFolder, final int[] seqNums) throws MessagingException {
        super(imapFolder);
        posMap = new TLongIntHashMap(seqNums.length, 0.5f, -1L, -1);
        for (int i = 0; i < seqNums.length; i++) {
            posMap.put(seqNums[i], i);
        }
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        uid = false;
        length = seqNums.length;
        args = length == messageCount ? (1 == length ? new String[] { "1" } : ARGS_ALL) : IMAPNumArgSplitter.splitSeqNumArg(seqNums, false, LENGTH + LENGTH_BODYSTRUCTURE);
        if (0 == length) {
            returnDefaultValue = true;
        }
        retval = new BODYSTRUCTURE[length];
    }

    /**
     * Initializes a new {@link BodystructureFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param separator The separator character
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param uids The UIDs to fetch
     * @param fp The fetch profile to use
     * @throws MessagingException If initialization fails
     */
    public BodystructureFetchIMAPCommand(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        super(imapFolder);
        posMap = new TLongIntHashMap(uids.length, 0.5f, -1L, -1);
        for (int i = 0; i < uids.length; i++) {
            posMap.put(uids[i], i);
        }
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        length = uids.length;
        args = IMAPNumArgSplitter.splitUIDArg(uids, false, LENGTH_WITH_UID + LENGTH_BODYSTRUCTURE);
        uid = true;
        if (0 == length) {
            returnDefaultValue = true;
        }
        retval = new BODYSTRUCTURE[length];
    }

    @Override
    protected String getDebugInfo(final int argsIndex) {
        final StringBuilder sb = new StringBuilder(64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ");
        final String arg = args[argsIndex];
        if (arg.length() > 32) {
            final int pos = arg.indexOf(',');
            if (pos == -1) {
                sb.append("...");
            } else {
                sb.append(arg.substring(0, pos)).append(",...,").append(arg.substring(arg.lastIndexOf(',') + 1));
            }
        } else {
            sb.append(arg);
        }
        sb.append(" (BODYSTRUCTURE UID)");
        return sb.toString();
    }

    @Override
    protected boolean addLoopCondition() {
        return (index < length);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(final int argsIndex) {
        final String arg = args[argsIndex];
        final StringBuilder sb = new StringBuilder(arg.length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ").append(arg).append(" (BODYSTRUCTURE UID)");
        return sb.toString();
    }

    private static final BODYSTRUCTURE[] EMPTY_ARR = new BODYSTRUCTURE[0];

    @Override
    protected BODYSTRUCTURE[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected BODYSTRUCTURE[] getReturnVal() throws MessagingException {
        return retval;
    }

    @Override
    protected boolean handleResponse(final Response currentReponse) throws MessagingException {
        /*
         * Response is null or not a FetchResponse
         */
        if (!FetchResponse.class.isInstance(currentReponse)) {
            return false;
        }
        final FetchResponse fetchResponse = (FetchResponse) currentReponse;
        index++;
        final int pos;
        if (this.uid) {
            final long uid = getItemOf(UID.class, fetchResponse).uid;
            pos = posMap.get(uid);
        } else {
            pos = posMap.get(fetchResponse.getNumber());
        }
        if (pos >= 0) {
            retval[pos] = getItemOf(BODYSTRUCTURE.class, fetchResponse);
        }
        return true;
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     *
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     */
    private static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
        final int len = fetchResponse.getItemCount();
        for (int i = 0; i < len; i++) {
            final Item item = fetchResponse.getItem(i);
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }
        return null;
    }

}
