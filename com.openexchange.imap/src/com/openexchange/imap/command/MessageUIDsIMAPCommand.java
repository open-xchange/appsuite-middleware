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

import javax.mail.Message;
import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.UID;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 * {@link MessageUIDsIMAPCommand} - gets the corresponding message UIDs to given array of <code>Message</code> as an array of
 * <code>long</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageUIDsIMAPCommand extends AbstractIMAPCommand<long[]> {

    private final String[] args;

    private final int length;

    private final TLongList sla;

    private int index;

    /**
     * Initializes a new {@link MessageUIDsIMAPCommand}
     *
     * @param imapFolder The IMAP folder
     * @param msgs The messages
     */
    public MessageUIDsIMAPCommand(IMAPFolder imapFolder, Message[] msgs) {
        super(imapFolder);
        if (msgs == null) {
            returnDefaultValue = true;
            args = ARGS_EMPTY;
            length = -1;
            sla = null;
        } else {
            args = IMAPNumArgSplitter.splitMessageArg(msgs, true, -1);
            length = msgs.length;
            sla = new TLongArrayList(length);
        }
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
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        sb.append("FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (UID)");
        return sb.toString();
    }

    private static final long[] EMPTY_ARR = new long[0];

    @Override
    protected long[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected long[] getReturnVal() {
        return sla.toArray();
    }

    @Override
    protected boolean handleResponse(Response response) throws MessagingException {
        /*
         * Response is null or not a FetchResponse
         */
        if (response == null) {
            return true;
        }
        if (!(response instanceof FetchResponse)) {
            return false;
        }
        sla.add(((UID) (((FetchResponse) response).getItem(0))).uid);
        index++;
        return true;
    }

}
