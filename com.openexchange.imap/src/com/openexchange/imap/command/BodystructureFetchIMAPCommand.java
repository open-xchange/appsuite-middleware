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

import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.UID;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

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
    public BodystructureFetchIMAPCommand(IMAPFolder imapFolder, int[] seqNums) throws MessagingException {
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
        args = length == messageCount ? (1 == length ? ARGS_FIRST : ARGS_ALL) : IMAPNumArgSplitter.splitSeqNumArg(seqNums, false, LENGTH + LENGTH_BODYSTRUCTURE);
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
    public BodystructureFetchIMAPCommand(IMAPFolder imapFolder, long[] uids) throws MessagingException {
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
    protected String getDebugInfo(int argsIndex) {
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
    protected String getCommand(int argsIndex) {
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
    protected boolean handleResponse(Response currentReponse) throws MessagingException {
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
            UID item = getItemOf(UID.class, fetchResponse);
            pos = item == null ? posMap.getNoEntryValue() : posMap.get(item.uid);
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
    private static <I extends Item> I getItemOf(Class<? extends I> clazz, FetchResponse fetchResponse) {
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
