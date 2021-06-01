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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.mail.MessagingException;
import com.openexchange.java.Streams;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;

/**
 * {@link BodyFetchIMAPCommand} - performs a prefetch of messages in given folder with only those fields set that need to be
 * present for display and sorting. A corresponding instance of <code>javax.mail.FetchProfile</code> is going to be generated from given
 * fields.
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitly requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code> objects.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BodyFetchIMAPCommand extends AbstractIMAPCommand<byte[]> {

    private final String[] args;

    private final boolean uid;

    private final String sequenceId;


    private int index;

    private byte[] bytes;

    /**
     * Initializes a new {@link BodyFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param separator The separator character
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param seqNums The sequence numbers to fetch
     * @throws MessagingException If initialization fails
     */
    public BodyFetchIMAPCommand(IMAPFolder imapFolder, int seqNum, String sequenceId, boolean isRev1) throws MessagingException {
        super(imapFolder);
        if (!isRev1) {
            throw new IllegalArgumentException("IMAP4rev1 is required!");
        }
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        this.sequenceId = sequenceId;
        uid = false;
        args = new String[] { String.valueOf(seqNum) };
    }

    /**
     * Initializes a new {@link BodyFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param separator The separator character
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param uids The UIDs to fetch
     * @param fp The fetch profile to use
     * @throws MessagingException If initialization fails
     */
    public BodyFetchIMAPCommand(IMAPFolder imapFolder, long uid, String sequenceId, boolean isRev1) throws MessagingException {
        super(imapFolder);
        if (!isRev1) {
            throw new IllegalArgumentException("IMAP4rev1 is required!");
        }
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        this.sequenceId = sequenceId;
        this.uid = true;
        args = new String[] { Long.toString(uid) };
    }

    @Override
    protected boolean addLoopCondition() {
        return (index < 1);
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
        sb.append("FETCH ").append(arg).append(" (BODY.PEEK[").append(sequenceId).append("])");
        return sb.toString();
    }

    @Override
    protected byte[] getDefaultValue() {
        return new byte[0];
    }

    @Override
    protected  byte[] getReturnVal() throws MessagingException {
        return bytes;
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
        final int count = fetchResponse.getItemCount();
        ByteArrayInputStream bais = null;
        for (int i = 0; i < count && null == bais; i++) {
            final Item item = fetchResponse.getItem(i);
            if (item instanceof BODY) {
                /*
                 * IMAP4rev1
                 */
                bais = ((BODY) item).getByteArrayInputStream();
            } else if (item instanceof RFC822DATA) {
                /*
                 * IMAP4
                 */
                bais = ((RFC822DATA) item).getByteArrayInputStream();
            }
        }
        if (null == bais) {
            throw new MessagingException("No BODY item in fetch response.");
        }
        final ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(8192);
        final byte[] bb = new byte[2048];
        for (int r; (r = bais.read(bb, 0, 2048)) > 0;) {
            outputStream.write(bb, 0, r);
        }
        bytes = outputStream.toByteArray();
        return true;
    }

}
