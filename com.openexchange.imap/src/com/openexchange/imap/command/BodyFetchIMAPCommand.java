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
    public BodyFetchIMAPCommand(final IMAPFolder imapFolder, final int seqNum, final String sequenceId, final boolean isRev1) throws MessagingException {
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
    public BodyFetchIMAPCommand(final IMAPFolder imapFolder, final long uid, final String sequenceId, final boolean isRev1) throws MessagingException {
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
    protected String getCommand(final int argsIndex) {
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
    protected boolean handleResponse(final Response currentReponse) throws MessagingException {
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
