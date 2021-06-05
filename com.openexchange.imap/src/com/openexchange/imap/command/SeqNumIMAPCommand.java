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
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.UID;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link SeqNumIMAPCommand}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SeqNumIMAPCommand extends AbstractIMAPCommand<int[]> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SeqNumIMAPCommand.class);

    private static final long[] L1 = new long[0];

    private final long[] uids;

    private final int length;

    private final String[] args;

    private final TIntList sia;

    private int fetchRespIndex;

    /**
     * @param imapFolder
     */
    public SeqNumIMAPCommand(IMAPFolder imapFolder, long[] uids, boolean isSequential) {
        super(imapFolder);
        this.uids = uids == null ? L1 : uids;
        returnDefaultValue = (this.uids.length == 0);
        length = this.uids.length;
        args = length == 0 ? ARGS_EMPTY : (isSequential ? new String[] { new StringBuilder(64).append(this.uids[0]).append(':').append(
            this.uids[this.uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(this.uids, true, -1));
        sia = new TIntArrayList(length);
    }

    @Override
    protected boolean addLoopCondition() {
        return (fetchRespIndex < length);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(int argsIndex) {
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        sb.append("UID FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (UID)");
        return sb.toString();
    }

    private static final int[] EMPTY_ARR = new int[0];

    @Override
    protected int[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected int[] getReturnVal() {
        return sia.toArray();
    }

    @Override
    protected boolean handleResponse(Response response) throws MessagingException {
        if (!(response instanceof FetchResponse)) {
            return false;
        }
        final FetchResponse f = (FetchResponse) response;
        /*
         * Check if response's uid matches corresponding uid
         */
        final long currentUID = ((UID) f.getItem(0)).uid;
        final long correspondingUID = uids[fetchRespIndex++];
        if (correspondingUID != currentUID) {
            LOG.warn("IMAPUtils.getSequenceNumbers(): UID mismatch");
            return false;
        }
        sia.add(f.getNumber());
        return true;
    }

}
