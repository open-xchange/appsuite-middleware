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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetHeaders;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link MessageFetchIMAPCommand} - performs a prefetch of messages in given folder with only those fields set that need to be present for display
 * and sorting. A corresponding instance of <code>javax.mail.FetchProfile</code> is going to be generated from given fields.
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitly requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code> objects.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageFetchIMAPCommand extends AbstractIMAPCommand<Message[]> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageFetchIMAPCommand.class);

    private static interface SeqNumFetcher {

        public int getNextSeqNum(int messageIndex);

        public int getIndexOf(int value);
    }

    private static class MsgSeqNumFetcher implements SeqNumFetcher {

        private final SeqNumFetcher delegate;

        public MsgSeqNumFetcher(final Message[] msgs) {
            /*
             * Create array from messages' sequence numbers
             */
            final int[] arr = new int[msgs.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = msgs[i].getMessageNumber();
            }
            /*
             * Create delegate
             */
            delegate = new IntSeqNumFetcher(arr);
        }

        @Override
        public int getNextSeqNum(final int index) {
            return delegate.getNextSeqNum(index);
        }

        @Override
        public int getIndexOf(final int value) {
            return delegate.getIndexOf(value);
        }
    }

    private static class IntSeqNumFetcher implements SeqNumFetcher {

        private final int[] arr;

        public IntSeqNumFetcher(final int[] arr) {
            this.arr = arr;
        }

        @Override
        public int getNextSeqNum(final int index) {
            return arr[index];
        }

        @Override
        public int getIndexOf(final int value) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == value) {
                    return i;
                }
            }
            return -1;
        }
    }

    private String[] args;
    private final String command;
    private SeqNumFetcher seqNumFetcher;
    private boolean uid;
    private int length;
    private int index;
    private ExtendedMimeMessage[] retval;
    private final boolean loadBody;
    private boolean determineAttachmentByHeader;

    /**
     * Initializes a new {@link MessageFetchIMAPCommand}.
     *
     * @param imapFolder - the IMAP folder
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param arr The source array (either <code>long</code> UIDs, <code>int</code> SeqNums or instances of <code>Message</code>)
     * @param fp The fetch profile
     * @param serverInfo The IMAP server information
     * @param isSequential Whether the source array values are sequential
     * @param keepOrder Whether to keep or to ignore given order through parameter <code>arr</code>; only has effect if parameter
     *            <code>arr</code> is of type <code>Message[]</code> or <code>int[]</code>
     * @throws MessagingException
     */
    public MessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, Object arr, FetchProfile fp, IMAPServerInfo serverInfo, boolean isSequential, boolean keepOrder) throws MessagingException {
        this(imapFolder, isRev1, arr, fp, serverInfo, isSequential, keepOrder, false);
    }

    /**
     * Initializes a new {@link MessageFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param arr The source array (either <code>long</code> UIDs, <code>int</code> SeqNums or instances of <code>Message</code>)
     * @param fp The fetch profile
     * @param serverInfo The IMAP server information
     * @param isSequential Whether the source array values are sequential
     * @param keepOrder Whether to keep or to ignore given order through parameter <code>arr</code>; only has effect if parameter
     *            <code>arr</code> is of type <code>Message[]</code> or <code>int[]</code>
     * @param loadBody <code>true</code> to load complete messages' bodies; otherwise <code>false</code>
     * @throws MessagingException
     */
    public MessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, Object arr, FetchProfile fp, IMAPServerInfo serverInfo, boolean isSequential, boolean keepOrder, boolean loadBody) throws MessagingException {
        super(imapFolder);
        if (imapFolder.getMessageCount() <= 0) {
            returnDefaultValue = true;
        }
        this.loadBody = loadBody;
        command = getFetchCommand(isRev1, fp, loadBody, serverInfo);
        set(arr, isSequential, keepOrder);
    }

    /**
     * Apply a new numeric argument to this IMAP <i>FETCH</i> command
     *
     * @param arr - the source array (either <code>long</code> UIDs, <code>int</code> SeqNums or instances of <code>Message</code>)
     * @param isSequential whether the source array values are sequential
     * @param keepOrder whether to keep or to ignore given order through parameter <code>arr</code>; only has effect if parameter
     *            <code>arr</code> is of type <code>Message[]</code> or <code>int[]</code>
     * @throws MessagingException
     */
    public void set(final Object arr, final boolean isSequential, final boolean keepOrder) throws MessagingException {
        if (null == arr) {
            returnDefaultValue = true;
        } else {
            createArgs(arr, isSequential, keepOrder);
        }
        retval = new ExtendedMimeMessage[length];
        index = 0;
    }

    /**
     * Sets whether detection if message contains attachment is performed by "Content-Type" header only.
     * <p>
     * If <code>true</code> a message is considered to contain attachments if its "Content-Type" header equals "multipart/mixed".
     *
     * @param determineAttachmentByHeader <code>true</code> to detect if message contains attachment is performed by "Content-Type" header
     *            only; otherwise <code>false</code>
     * @return This FETCH IMAP command with value applied
     */
    public MessageFetchIMAPCommand setDetermineAttachmentyHeader(final boolean determineAttachmentByHeader) {
        this.determineAttachmentByHeader = determineAttachmentByHeader;
        return this;
    }

    private static final int LENGTH = 9; // "FETCH <nums> (<command>)"

    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"

    private void createArgs(final Object arr, final boolean isSequential, final boolean keepOrder) throws MessagingException {
        if (arr instanceof int[]) {
            final int[] seqNums = (int[]) arr;
            uid = false;
            length = seqNums.length;
            if (0 == length) {
                returnDefaultValue = true;
            } else {
                args = isSequential ? new String[] { new StringBuilder(32).append(seqNums[0]).append(':').append(
                    seqNums[seqNums.length - 1]).toString() } : IMAPNumArgSplitter.splitSeqNumArg(
                    seqNums,
                    keepOrder,
                    LENGTH + command.length());
                seqNumFetcher = keepOrder ? new IntSeqNumFetcher(seqNums) : null;
            }
        } else if (arr instanceof long[]) {
            if (keepOrder) {
                /*
                 * Turn UIDs to corresponding sequence number to initialize seqNumFetcher which keeps track or proper order
                 */
                final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, (long[]) arr);
                uid = false;
                length = seqNums.length;
                if (0 == length) {
                    returnDefaultValue = true;
                } else {
                    args = isSequential ? new String[] { new StringBuilder(32).append(seqNums[0]).append(':').append(
                        seqNums[seqNums.length - 1]).toString() } : IMAPNumArgSplitter.splitSeqNumArg(
                        seqNums,
                        true,
                        LENGTH + command.length());
                    seqNumFetcher = new IntSeqNumFetcher(seqNums);
                }
            } else {
                final long[] uids = (long[]) arr;
                uid = true;
                length = uids.length;
                if (0 == length) {
                    returnDefaultValue = true;
                } else {
                    args = isSequential ? new String[] { new StringBuilder(32).append(uids[0]).append(':').append(uids[uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(
                        uids,
                        false,
                        LENGTH_WITH_UID + command.length());
                    seqNumFetcher = null;
                }
            }
        } else if (arr instanceof Message[]) {
            final Message[] msgs = (Message[]) arr;
            uid = false;
            length = msgs.length;
            if (0 == length) {
                returnDefaultValue = true;
            } else {
                args = isSequential ? new String[] { new StringBuilder(64).append(msgs[0].getMessageNumber()).append(':').append(
                    msgs[msgs.length - 1].getMessageNumber()).toString() } : IMAPNumArgSplitter.splitMessageArg(
                    msgs,
                    keepOrder,
                    LENGTH + command.length());
                seqNumFetcher = keepOrder ? new MsgSeqNumFetcher(msgs) : null;
            }
        } else {
            throw new MessagingException(new StringBuilder("Invalid array type! ").append(arr.getClass().getName()).toString());
        }
    }

    /**
     * Constructor to fetch all messages of given folder
     * <p>
     * <b>Note</b>: Ensure that denoted folder is not empty through {@link IMAPFolder#getMessageCount()}.
     *
     * @param imapFolder The IMAP folder
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param fp The fetch profile
     * @param serverInfo The IMAP server information
     * @param fetchLen The total message count
     * @throws MessagingException If a messaging error occurs
     */
    public MessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, FetchProfile fp, IMAPServerInfo serverInfo, int fetchLen) throws MessagingException {
        this(imapFolder, isRev1, fp, serverInfo, fetchLen, false);
    }

    /**
     * Constructor to fetch all messages of given folder
     * <p>
     * <b>Note</b>: Ensure that denoted folder is not empty through {@link IMAPFolder#getMessageCount()}.
     *
     * @param imapFolder The IMAP folder
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param fp The fetch profile
     * @param serverInfo The IMAP server information
     * @param fetchLen The total message count
     * @param loadBody <code>true</code> to load complete messages' bodies; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    public MessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, FetchProfile fp, IMAPServerInfo serverInfo, int fetchLen, boolean loadBody) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        this.loadBody = loadBody;
        if (0 == fetchLen) {
            returnDefaultValue = true;
        }
        args = 1 == messageCount ? new String[] { "1" } : ARGS_ALL;
        uid = false;
        length = fetchLen;
        command = getFetchCommand(isRev1, fp, loadBody, serverInfo);
        retval = new ExtendedMimeMessage[length];
        index = 0;
    }

    @Override
    protected String getDebugInfo(final int argsIndex) {
        final StringBuilder sb = new StringBuilder(command.length() + 64);
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
        sb.append(" (").append(command).append(')');
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
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (").append(command).append(')');
        return sb.toString();
    }

    private static final ExtendedMimeMessage[] EMPTY_ARR = new ExtendedMimeMessage[0];

    @Override
    protected Message[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected Message[] getReturnVal() throws MessagingException {
        if (index < length) {
            String server = imapFolder.getStore().toString();
            int pos = server.indexOf('@');
            if (pos >= 0 && ++pos < server.length()) {
                server = server.substring(pos);
            }
            final MessagingException e = new MessagingException(new StringBuilder(32).append("Expected ").append(length)
                    .append(" FETCH responses but got ").append(index).append(" from IMAP folder \"").append(imapFolder.getFullName())
                    .append("\" on server \"").append(server).append("\".").toString());
            LOG.warn("", e);
        }
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
        int seqnum;
        final int pos;
        if (null == seqNumFetcher) {
            seqnum = fetchResponse.getNumber();
            pos = index;
        } else {
            seqnum = seqNumFetcher.getNextSeqNum(index);
            if (seqnum == fetchResponse.getNumber()) {
                pos = index;
            } else {
                /*
                 * Assign to current response's sequence number
                 */
                seqnum = fetchResponse.getNumber();
                /*
                 * Look-up position
                 */
                pos = seqNumFetcher.getIndexOf(seqnum);
                if (pos == -1) {
                    throw new MessagingException("Unexpected sequence number in untagged FETCH response: " + seqnum);
                }
            }
        }
        index++;
        final ExtendedMimeMessage msg = new ExtendedMimeMessage(imapFolder.getFullName(), seqnum);
        boolean error = false;
        try {
            final int itemCount = fetchResponse.getItemCount();
            for (int j = 0; j < itemCount; j++) {
                final Item item = fetchResponse.getItem(j);
                FetchItemHandler itemHandler = MAP.get(item.getClass());
                if (null == itemHandler) {
                    itemHandler = getItemHandlerByItem(item, loadBody);
                    if (null == itemHandler) {
                        LOG.warn("Unknown FETCH item: {}", item.getClass().getName());
                    } else {
                        itemHandler.handleItem(item, msg, LOG);
                    }
                } else {
                    itemHandler.handleItem(item, msg, LOG);
                }
            }
            if (determineAttachmentByHeader) {
                final String cts = msg.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                if (null != cts) {
                    msg.setHasAttachment(new ContentType(cts).startsWith("multipart/mixed"));
                }
            }
        } catch (final MessagingException e) {
            /*
             * Discard corrupt message
             */
            final OXException imapExc = MimeMailException.handleMessagingException(e);
            LOG.error("Message #{} discarded", msg.getMessageNumber(), imapExc);
            error = true;
        } catch (final OXException e) {
            /*
             * Discard corrupt message
             */
            LOG.error("Message #{} discarded", msg.getMessageNumber(), e);
            error = true;
        }
        if (!error) {
            retval[pos] = msg;
        }
        return true;
    }

    /*-
     * private static void addFetchItem(final FetchProfile fp, final int field) {
        if (field == MailListField.ID.getField()) {
            fp.add(UIDFolder.FetchProfileItem.UID);
        } else if (field == MailListField.ATTACHMENT.getField()) {
            fp.add(FetchProfile.Item.CONTENT_INFO);
        } else if (field == MailListField.FROM.getField()) {
            fp.add(MessageHeaders.HDR_FROM);
        } else if (field == MailListField.TO.getField()) {
            fp.add(MessageHeaders.HDR_TO);
        } else if (field == MailListField.CC.getField()) {
            fp.add(MessageHeaders.HDR_CC);
        } else if (field == MailListField.BCC.getField()) {
            fp.add(MessageHeaders.HDR_BCC);
        } else if (field == MailListField.SUBJECT.getField()) {
            fp.add(MessageHeaders.HDR_SUBJECT);
        } else if (field == MailListField.SIZE.getField()) {
            fp.add(IMAPFolder.FetchProfileItem.SIZE);
        } else if (field == MailListField.SENT_DATE.getField()) {
            fp.add(MessageHeaders.HDR_DATE);
        } else if (field == MailListField.FLAGS.getField()) {
            if (!fp.contains(FetchProfile.Item.FLAGS)) {
                fp.add(FetchProfile.Item.FLAGS);
            }
        } else if (field == MailListField.DISPOSITION_NOTIFICATION_TO.getField()) {
            fp.add(MessageHeaders.HDR_DISP_NOT_TO);
        } else if (field == MailListField.PRIORITY.getField()) {
            fp.add(MessageHeaders.HDR_X_PRIORITY);
        } else if (field == MailListField.COLOR_LABEL.getField()) {
            if (!fp.contains(FetchProfile.Item.FLAGS)) {
                fp.add(FetchProfile.Item.FLAGS);
            }
        } else if ((field == MailListField.FLAG_SEEN.getField()) && !fp.contains(FetchProfile.Item.FLAGS)) {
            fp.add(FetchProfile.Item.FLAGS);
        }
    }
     */

    /*-
     * private static FetchItemHandler[] createItemHandlers(final int itemCount, final FetchResponse f,
            final boolean loadBody) {
        final FetchItemHandler[] itemHandlers = new FetchItemHandler[itemCount];
        for (int j = 0; j < itemCount; j++) {
            final Item item = f.getItem(j);
            FetchItemHandler h = MAP.get(item.getClass());
            if (null == h) {
                // Try through instanceof checks
                if ((item instanceof RFC822DATA) || (item instanceof BODY)) {
                    if (loadBody) {
                        h = BODY_ITEM_HANDLER;
                    } else {
                        h = HEADER_ITEM_HANDLER;
                    }
                } else if (item instanceof UID) {
                    h = UID_ITEM_HANDLER;
                } else if (item instanceof INTERNALDATE) {
                    h = INTERNALDATE_ITEM_HANDLER;
                } else if (item instanceof Flags) {
                    h = FLAGS_ITEM_HANDLER;
                } else if (item instanceof ENVELOPE) {
                    h = ENVELOPE_ITEM_HANDLER;
                } else if (item instanceof RFC822SIZE) {
                    h = SIZE_ITEM_HANDLER;
                } else if (item instanceof BODYSTRUCTURE) {
                    h = BODYSTRUCTURE_ITEM_HANDLER;
                }
            }
            itemHandlers[j] = h;
        }
        return itemHandlers;
    }
     */

    private static FetchItemHandler getItemHandlerByItem(final Item item, final boolean loadBody) {
        if ((item instanceof RFC822DATA) || (item instanceof BODY)) {
            if (loadBody) {
                return BODY_ITEM_HANDLER;
            }
            return HEADER_ITEM_HANDLER;
        } else if (item instanceof UID) {
            return UID_ITEM_HANDLER;
        } else if (item instanceof INTERNALDATE) {
            return INTERNALDATE_ITEM_HANDLER;
        } else if (item instanceof Flags) {
            return FLAGS_ITEM_HANDLER;
        } else if (item instanceof ENVELOPE) {
            return ENVELOPE_ITEM_HANDLER;
        } else if (item instanceof RFC822SIZE) {
            return SIZE_ITEM_HANDLER;
        } else if (item instanceof BODYSTRUCTURE) {
            return BODYSTRUCTURE_ITEM_HANDLER;
        } else {
            return null;
        }
    }

    private static interface FetchItemHandler {

        /**
         * Handles given <code>com.sun.mail.imap.protocol.Item</code> instance and applies it to given message.
         *
         * @param item The item to handle
         * @param msg The message to apply to
         * @param logger The logger
         * @throws MessagingException If a messaging error occurs
         * @throws OXException If a mail error occurs
         */
        public abstract void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException;
    }

    private static final class HeaderFetchItemHandler implements FetchItemHandler {

        public HeaderFetchItemHandler() {
            super();
        }

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException {
            final InternetHeaders h;
            {
                final InputStream headerStream;
                if (item instanceof BODY) {
                    /*
                     * IMAP4rev1
                     */
                    headerStream = ((BODY) item).getByteArrayInputStream();
                } else {
                    /*
                     * IMAP4
                     */
                    headerStream = ((RFC822DATA) item).getByteArrayInputStream();
                }
                h = new InternetHeaders();
                if (null == headerStream) {
                    logger.debug("Cannot retrieve headers from message #{} in folder {}", msg.getMessageNumber(), msg.getFullname());
                } else {
                    h.load(headerStream);
                }
            }
            for (final Enumeration<?> e = h.getAllHeaders(); e.hasMoreElements();) {
                final Header hdr = (Header) e.nextElement();
                final String name = hdr.getName();
                if (MessageHeaders.HDR_SUBJECT.equals(name)) {
                    msg.addHeader(name, MimeMessageUtility.checkNonAscii(hdr.getValue()));
                } else {
                    msg.addHeader(name, hdr.getValue());
                }
                /*-
                 *
                final HeaderHandler hdrHandler = hdrHandlers.get(hdr.getName());
                if (hdrHandler == null) {
                    msg.setHeader(hdr.getName(), hdr.getValue());
                } else {
                    hdrHandler.handleHeader(hdr.getValue(), msg);
                }
                 */
            }
        }

    } // End of HeaderFetchItemHandler

    private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    /*-
     * ++++++++++++++ Item handlers ++++++++++++++
     */

    private static final FetchItemHandler FLAGS_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            msg.setFlags((FLAGS) item, true);
        }
    };

    private static final FetchItemHandler ENVELOPE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            final ENVELOPE env = (ENVELOPE) item;
            msg.addFrom(env.from);
            msg.setRecipients(RecipientType.TO, env.to);
            msg.setRecipients(RecipientType.CC, env.cc);
            msg.setRecipients(RecipientType.BCC, env.bcc);
            msg.setReplyTo(env.replyTo);
            msg.setHeader(MessageHeaders.HDR_IN_REPLY_TO, env.inReplyTo);
            msg.setHeader(MessageHeaders.HDR_MESSAGE_ID, env.messageId);
            msg.setSubject(MimeMessageUtility.decodeEnvelopeSubject(env.subject));
            msg.setSentDate(env.date);
        }
    };

    private static final FetchItemHandler INTERNALDATE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) {
            msg.setReceivedDate(((INTERNALDATE) item).getDate());
        }
    };

    private static final FetchItemHandler SIZE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) {
            msg.setSize(((RFC822SIZE) item).size);
        }
    };

    private static final FetchItemHandler BODYSTRUCTURE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws OXException {
            final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
            msg.setBodystructure(bs);
            final StringBuilder sb = new StringBuilder();
            sb.append(bs.type).append('/').append(bs.subtype);
            if (bs.cParams != null) {
                sb.append(bs.cParams);
            }
            try {
                msg.setContentType(new ContentType(sb.toString()));
            } catch (final OXException e) {
                logger.warn("", e);
                msg.setContentType(new ContentType(MimeTypes.MIME_DEFAULT));
            }
            msg.setHasAttachment(bs.isMulti() && MimeMessageUtility.hasAttachments(bs));
        }
    };

    private static final FetchItemHandler UID_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) {
            msg.setUid(((UID) item).uid);
        }
    };

    private static final FetchItemHandler BODY_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final ExtendedMimeMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException {
            final InputStream msgStream;
            if (item instanceof RFC822DATA) {
                /*
                 * IMAP4
                 */
                msgStream = ((RFC822DATA) item).getByteArrayInputStream();
            } else {
                /*
                 * IMAP4rev1
                 */
                msgStream = ((BODY) item).getByteArrayInputStream();
            }
            if (null == msgStream) {
                logger.warn("Cannot retrieve body from message #{} in folder {}", msg.getMessageNumber(), msg.getFullname());
            } else {
                msg.parseStream(msgStream);
            }
        }
    };

    private static final FetchItemHandler HEADER_ITEM_HANDLER = new HeaderFetchItemHandler();

    private static final Map<Class<? extends Item>, FetchItemHandler> MAP;

    static {
        MAP = new HashMap<Class<? extends Item>, FetchItemHandler>(8);
        MAP.put(UID.class, UID_ITEM_HANDLER);
        MAP.put(INTERNALDATE.class, INTERNALDATE_ITEM_HANDLER);
        MAP.put(FLAGS.class, FLAGS_ITEM_HANDLER);
        MAP.put(ENVELOPE.class, ENVELOPE_ITEM_HANDLER);
        MAP.put(RFC822SIZE.class, SIZE_ITEM_HANDLER);
        MAP.put(BODYSTRUCTURE.class, BODYSTRUCTURE_ITEM_HANDLER);
        MAP.put(INTERNALDATE.class, INTERNALDATE_ITEM_HANDLER);
    }

    /*-
     * ++++++++++++++ End of item handlers ++++++++++++++
     */

    /**
     * Turns given fetch profile into FETCH items to craft a FETCH command.
     *
     * @param isRev1 Whether IMAP protocol is revision 1 or not
     * @param fp The fetch profile to convert
     * @param loadBody <code>true</code> if message body should be loaded; otherwise <code>false</code>
     * @param serverInfo The IMAP server information
     * @return The FETCH items to craft a FETCH command
     */
    private static String getFetchCommand(boolean isRev1, FetchProfile fp, boolean loadBody, IMAPServerInfo serverInfo) {
        return MailMessageFetchIMAPCommand.getFetchCommand(isRev1, fp, loadBody, serverInfo);
    }

    /**
     * Possibly modifies a {@link FetchProfile}.
     */
    public static interface FetchProfileModifier {

        /**
         * Modifies specified {@link FetchProfile} instance.
         *
         * @param fetchProfile The fetch profile
         * @return The modified fetch profile
         */
        FetchProfile modify(FetchProfile fetchProfile);

        boolean byContentTypeHeader();
    }

    /**
     * Default modifier which returns the fetch profile unchanged.
     */
    public static final FetchProfileModifier DEFAULT_PROFILE_MODIFIER = new FetchProfileModifier() {

        @Override
        public FetchProfile modify(final FetchProfile fetchProfile) {
            /*
             * Return unchanged
             */
            return fetchProfile;
        }

        @Override
        public boolean byContentTypeHeader() {
            return false;
        }
    };

    /**
     * Strips individual header names from fetch profile, but inserts {@link FetchProfileItem#HEADERS}.
     */
    public static final FetchProfileModifier HEADERLESS_PROFILE_MODIFIER = new FetchProfileModifier() {

        @Override
        public FetchProfile modify(final FetchProfile fetchProfile) {
            return getHeaderlessFetchProfile(fetchProfile);
        }

        @Override
        public boolean byContentTypeHeader() {
            return false;
        }
    };

    /**
     * Strips BODYSTRUCTURE fetch item and inserts "Content-Type" header name.
     */
    public static final FetchProfileModifier NO_BODYSTRUCTURE_PROFILE_MODIFIER = new FetchProfileModifier() {

        @Override
        public FetchProfile modify(final FetchProfile fetchProfile) {
            return getSafeFetchProfile(fetchProfile);
        }

        @Override
        public boolean byContentTypeHeader() {
            return true;
        }
    };

    /**
     * Strips BODYSTRUCTURE item from given fetch profile.
     *
     * @param fetchProfile The fetch profile
     * @return The fetch profile with BODYSTRUCTURE item stripped
     */
    public static final FetchProfile getSafeFetchProfile(final FetchProfile fetchProfile) {
        if (fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) {
            final FetchProfile newFetchProfile = new FetchProfile();
            newFetchProfile.add("Content-Type");
            if (!fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
                newFetchProfile.add(UIDFolder.FetchProfileItem.UID);
            }
            for (final javax.mail.FetchProfile.Item item : fetchProfile.getItems()) {
                if (!FetchProfile.Item.CONTENT_INFO.equals(item)) {
                    newFetchProfile.add(item);
                }
            }
            final String[] names = fetchProfile.getHeaderNames();
            for (final String name : names) {
                newFetchProfile.add(name);
            }
            return newFetchProfile;
        }
        return fetchProfile;
    }

    /**
     * Strips header names from given fetch profile.
     *
     * @param fetchProfile The fetch profile
     * @return The fetch profile with header names stripped
     */
    public static final FetchProfile getHeaderlessFetchProfile(final FetchProfile fetchProfile) {
        final String[] headerNames = fetchProfile.getHeaderNames();
        if (null == headerNames || headerNames.length <= 0) {
            return fetchProfile;
        }
        /*
         * Strip header names
         */
        final FetchProfile newFetchProfile = new FetchProfile();
        for (final javax.mail.FetchProfile.Item item : fetchProfile.getItems()) {
            newFetchProfile.add(item);
        }
        newFetchProfile.add(IMAPFolder.FetchProfileItem.HEADERS);
        return newFetchProfile;
    }

}
