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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
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
 * {@link SimpleFetchIMAPCommand} - performs a prefetch of messages in given folder with only those fields set that need to be present for
 * display and sorting. A corresponding instance of <code>javax.mail.FetchProfile</code> is going to be generated from given fields.
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitly requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code> objects.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SimpleFetchIMAPCommand extends AbstractIMAPCommand<TLongObjectMap<MailMessage>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SimpleFetchIMAPCommand.class);

    private static final int LENGTH = 9; // "FETCH <nums> (<command>)"

    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"


    private String[] args;
    private final String command;
    private boolean uid;
    private final int length;
    private int index;
    private boolean determineAttachmentByHeader;
    private final String fullname;
    private final Set<FetchItemHandler> lastHandlers;
    private final TLongObjectMap<MailMessage> map;

    /**
     * Initializes a new {@link SimpleFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param seqNums The sequence numbers to fetch
     * @param fp The fetch profile to use
     * @throws MessagingException If initialization fails
     */
    public SimpleFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, int[] seqNums, FetchProfile fp, IMAPServerInfo serverInfo) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        lastHandlers = new HashSet<FetchItemHandler>();
        command = getFetchCommand(isRev1, fp, false, serverInfo);
        uid = false;
        length = seqNums.length;
        map = new TLongObjectHashMap<MailMessage>(length);
        args = length == messageCount ? (1 == length ? new String[] { "1" } : ARGS_ALL) : IMAPNumArgSplitter.splitSeqNumArg(seqNums, false, LENGTH + command.length());
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
    }

    /**
     * Initializes a new {@link SimpleFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param uids The UIDs to fetch
     * @param fp The fetch profile to use
     * @throws MessagingException If initialization fails
     */
    public SimpleFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, long[] uids, FetchProfile fp, IMAPServerInfo serverInfo) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        lastHandlers = new HashSet<FetchItemHandler>();
        length = uids.length;
        map = new TLongObjectHashMap<MailMessage>(length);
        if (length == messageCount) {
            fp.add(UIDFolder.FetchProfileItem.UID);
            command = getFetchCommand(isRev1, fp, false, serverInfo);
            args = (1 == length ? new String[] { "1" } : ARGS_ALL);
            uid = false;
        } else {
            command = getFetchCommand(isRev1, fp, false, serverInfo);
            args = IMAPNumArgSplitter.splitUIDArg(uids, false, LENGTH_WITH_UID + command.length());
            uid = true;
        }
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
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
    public SimpleFetchIMAPCommand setDetermineAttachmentByHeader(final boolean determineAttachmentByHeader) {
        this.determineAttachmentByHeader = determineAttachmentByHeader;
        return this;
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

    @Override
    protected TLongObjectMap<MailMessage> getDefaultValue() {
        return new TLongObjectHashMap<MailMessage>(1);
    }

    @Override
    protected TLongObjectMap<MailMessage> getReturnVal() throws MessagingException {
        return map;
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
        final int seqNum = fetchResponse.getNumber();
        final long id;
        if (uid) {
            id = getItemOf(UID.class, fetchResponse).uid;
        } else {
            id = seqNum;
        }
        index++;
        final IDMailMessage mail = new IDMailMessage(null, fullname);
        // mail.setRecentCount(recentCount);
        mail.setSeqnum(seqNum);
        boolean error = false;
        try {
            final int itemCount = fetchResponse.getItemCount();
            for (int j = 0; j < itemCount; j++) {
                final Item item = fetchResponse.getItem(j);
                FetchItemHandler itemHandler = MAP.get(item.getClass());
                if (null == itemHandler) {
                    itemHandler = getItemHandlerByItem(item);
                    if (null == itemHandler) {
                        LOG.warn("Unknown FETCH item: {}", item.getClass().getName());
                    } else {
                        lastHandlers.add(itemHandler);
                        itemHandler.handleItem(item, mail, LOG);
                    }
                } else {
                    lastHandlers.add(itemHandler);
                    itemHandler.handleItem(item, mail, LOG);
                }
            }
            if (determineAttachmentByHeader) {
                final String cts = mail.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                if (null != cts) {
                    mail.setHasAttachment(new ContentType(cts).startsWith("multipart/mixed"));
                }
            }
        } catch (final MessagingException e) {
            /*
             * Discard corrupt message
             */
            {
                final OXException imapExc = MimeMailException.handleMessagingException(e);
                LOG.warn("Message #{} discarded", mail.getSeqnum(), imapExc);
            }
            error = true;
        } catch (final OXException e) {
            /*
             * Discard corrupt message
             */
            LOG.warn("Message #{} discarded", mail.getSeqnum(), e);
            error = true;
        }
        if (!error) {
            map.put(id, mail);
        }
        return true;
    }

    private static FetchItemHandler getItemHandlerByItem(final Item item) {
        if ((item instanceof RFC822DATA) || (item instanceof BODY)) {
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
        void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException;

        void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException;
    }

    private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    /*-
     * ++++++++++++++ Item handlers ++++++++++++++
     */

    private interface HeaderHandler {

        void handle(Header hdr, IDMailMessage mailMessage) throws OXException;

    }

    private static final FetchItemHandler HEADER_ITEM_HANDLER = new FetchItemHandler() {

        private final Map<String, HeaderHandler> hh = new HashMap<String, HeaderHandler>() {

            {
                put(MessageHeaders.HDR_FROM, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.addFrom(MimeMessageConverter.getAddressHeader(hdr.getValue()));
                    }
                });
                put(MessageHeaders.HDR_TO, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.addTo(MimeMessageConverter.getAddressHeader(hdr.getValue()));
                    }
                });
                put(MessageHeaders.HDR_CC, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.addCc(MimeMessageConverter.getAddressHeader(hdr.getValue()));
                    }
                });
                put(MessageHeaders.HDR_BCC, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.addBcc(MimeMessageConverter.getAddressHeader(hdr.getValue()));
                    }
                });
                put(MessageHeaders.HDR_REPLY_TO, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.addReplyTo(MimeMessageConverter.getAddressHeader(hdr.getValue()));
                    }
                });
                put(MessageHeaders.HDR_DISP_NOT_TO, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.setDispositionNotification(MimeMessageConverter.getAddressHeader(hdr.getValue())[0]);
                    }
                });
                put(MessageHeaders.HDR_SUBJECT, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        mailMessage.setSubject(MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.checkNonAscii(hdr.getValue())));
                    }
                });
                put(MessageHeaders.HDR_DATE, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        final MailDateFormat mdf = MimeMessageUtility.getDefaultMailDateFormat();
                        synchronized (mdf) {
                            try {
                                mailMessage.setSentDate(mdf.parse(hdr.getValue()));
                            } catch (final ParseException e) {
                                LOG.error("", e);
                            }
                        }
                    }
                });
                put(MessageHeaders.HDR_IMPORTANCE, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        final String value = hdr.getValue();
                        if (null != value) {
                            mailMessage.setPriority(MimeMessageConverter.parseImportance(value));
                        }
                    }
                });
                put(MessageHeaders.HDR_X_PRIORITY, new HeaderHandler() {

                    @Override
                    public void handle(final Header hdr, final IDMailMessage mailMessage) throws OXException {
                        if (!mailMessage.containsPriority()) {
                            mailMessage.setPriority(MimeMessageConverter.parsePriority(hdr.getValue()));
                        }
                    }
                });
            }
        };

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException {
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
                    logger.debug("Cannot retrieve headers from message #{} in folder {}", msg.getSeqnum(), msg.getFolder());
                } else {
                    h.load(headerStream);
                }
            }
            for (final Enumeration<?> e = h.getAllHeaders(); e.hasMoreElements();) {
                final Header hdr = (Header) e.nextElement();
                final String name = hdr.getName();
                {
                    final HeaderHandler headerHandler = hh.get(name);
                    if (null != headerHandler) {
                        headerHandler.handle(hdr, msg);
                    }
                }
                try {
                    msg.addHeader(name, hdr.getValue());
                } catch (final IllegalArgumentException illegalArgumentExc) {
                    logger.debug("Ignoring invalid header.", illegalArgumentExc);
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

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException {
            for (final Enumeration<?> e = message.getAllHeaders(); e.hasMoreElements();) {
                final Header hdr = (Header) e.nextElement();
                final String name = hdr.getName();
                {
                    final HeaderHandler headerHandler = hh.get(name);
                    if (null != headerHandler) {
                        headerHandler.handle(hdr, msg);
                    }
                }
                try {
                    msg.addHeader(name, hdr.getValue());
                } catch (final IllegalArgumentException illegalArgumentExc) {
                    logger.debug("Ignoring invalid header.", illegalArgumentExc);
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
    };

    private static final FetchItemHandler FLAGS_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            final FLAGS flags = (FLAGS) item;
            /*
             * Parse system flags
             */
            int retval = 0;
            int colorLabel = MailMessage.COLOR_LABEL_NONE;
            Collection<String> ufCol = null;
            if (flags.contains(Flags.Flag.ANSWERED)) {
                retval |= MailMessage.FLAG_ANSWERED;
            }
            if (flags.contains(Flags.Flag.DELETED)) {
                retval |= MailMessage.FLAG_DELETED;
            }
            if (flags.contains(Flags.Flag.DRAFT)) {
                retval |= MailMessage.FLAG_DRAFT;
            }
            if (flags.contains(Flags.Flag.FLAGGED)) {
                retval |= MailMessage.FLAG_FLAGGED;
            }
            if (flags.contains(Flags.Flag.RECENT)) {
                retval |= MailMessage.FLAG_RECENT;
            }
            if (flags.contains(Flags.Flag.SEEN)) {
                retval |= MailMessage.FLAG_SEEN;
            }
            if (flags.contains(Flags.Flag.USER)) {
                retval |= MailMessage.FLAG_USER;
            }
            final String[] userFlags = flags.getUserFlags();
            if (userFlags != null) {
                /*
                 * Mark message to contain user flags
                 */
                final Set<String> set = new HashSet<String>(userFlags.length);
                for (final String userFlag : userFlags) {
                    if (MailMessage.isColorLabel(userFlag)) {
                        try {
                            colorLabel = MailMessage.getColorLabelIntValue(userFlag);
                        } catch (final OXException e) {
                            // Cannot occur
                            colorLabel = MailMessage.COLOR_LABEL_NONE;
                        }
                    } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_FORWARDED;
                    } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_READ_ACK;
                    } else {
                        set.add(userFlag);
                    }
                }
                ufCol = set.isEmpty() ? null : set;
            }
            /*
             * Apply parsed flags
             */
            msg.setFlags(retval);
            msg.setColorLabel(colorLabel);
            if (null != ufCol) {
                msg.addUserFlags(ufCol.toArray(new String[ufCol.size()]));
            }
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            final Flags flags = message.getFlags();
            /*
             * Parse system flags
             */
            int retval = 0;
            int colorLabel = MailMessage.COLOR_LABEL_NONE;
            Collection<String> ufCol = null;
            if (flags.contains(Flags.Flag.ANSWERED)) {
                retval |= MailMessage.FLAG_ANSWERED;
            }
            if (flags.contains(Flags.Flag.DELETED)) {
                retval |= MailMessage.FLAG_DELETED;
            }
            if (flags.contains(Flags.Flag.DRAFT)) {
                retval |= MailMessage.FLAG_DRAFT;
            }
            if (flags.contains(Flags.Flag.FLAGGED)) {
                retval |= MailMessage.FLAG_FLAGGED;
            }
            if (flags.contains(Flags.Flag.RECENT)) {
                retval |= MailMessage.FLAG_RECENT;
            }
            if (flags.contains(Flags.Flag.SEEN)) {
                retval |= MailMessage.FLAG_SEEN;
            }
            if (flags.contains(Flags.Flag.USER)) {
                retval |= MailMessage.FLAG_USER;
            }
            final String[] userFlags = flags.getUserFlags();
            if (userFlags != null) {
                /*
                 * Mark message to contain user flags
                 */
                final Set<String> set = new HashSet<String>(userFlags.length);
                for (final String userFlag : userFlags) {
                    if (MailMessage.isColorLabel(userFlag)) {
                        try {
                            colorLabel = MailMessage.getColorLabelIntValue(userFlag);
                        } catch (final OXException e) {
                            // Cannot occur
                            colorLabel = MailMessage.COLOR_LABEL_NONE;
                        }
                    } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_FORWARDED;
                    } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_READ_ACK;
                    } else {
                        set.add(userFlag);
                    }
                }
                ufCol = set.isEmpty() ? null : set;
            }
            /*
             * Apply parsed flags
             */
            msg.setFlags(retval);
            msg.setColorLabel(colorLabel);
            if (null != ufCol) {
                msg.addUserFlags(ufCol.toArray(new String[ufCol.size()]));
            }
        }
    };

    private static final FetchItemHandler ENVELOPE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            final ENVELOPE env = (ENVELOPE) item;
            msg.addFrom(env.from);
            msg.addTo(env.to);
            msg.addCc(env.cc);
            msg.addBcc(env.bcc);
            msg.addReplyTo(env.replyTo);
            msg.addHeader("In-Reply-To", env.inReplyTo);
            msg.addHeader("Message-Id", env.messageId);
            msg.setSubject(MimeMessageUtility.decodeEnvelopeSubject(env.subject));
            msg.setSentDate(env.date);
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            msg.addFrom((InternetAddress[]) message.getFrom());
            msg.addTo((InternetAddress[]) message.getRecipients(RecipientType.TO));
            msg.addCc((InternetAddress[]) message.getRecipients(RecipientType.CC));
            msg.addBcc((InternetAddress[]) message.getRecipients(RecipientType.BCC));
            msg.addReplyTo((InternetAddress[]) message.getReplyTo());
            String[] header = message.getHeader("In-Reply-To");
            if (null != header && header.length > 0) {
                msg.addHeader("In-Reply-To", header[0]);
            }
            header = message.getHeader("Message-Id");
            if (null != header && header.length > 0) {
                msg.addHeader("Message-Id", header[0]);
            }
            header = message.getHeader("Subject");
            if (null != header && header.length > 0) {
                msg.setSubject(MimeMessageUtility.decodeMultiEncodedHeader(header[0]));
            }
            msg.setSentDate(message.getSentDate());
        }
    };

    private static final FetchItemHandler INTERNALDATE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) {
            msg.setReceivedDate(((INTERNALDATE) item).getDate());
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            msg.setReceivedDate(message.getReceivedDate());
        }
    };

    private static final FetchItemHandler SIZE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) {
            msg.setSize(((RFC822SIZE) item).size);
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            msg.setSize(message.getSize());
        }
    };

    private static final FetchItemHandler BODYSTRUCTURE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) throws OXException {
            final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
            final StringBuilder sb = new StringBuilder();
            sb.append(bs.type).append('/').append(bs.subtype);
            if (bs.cParams != null) {
                sb.append(bs.cParams);
            }
            try {
                final String contentType = sb.toString();
                msg.setContentType(new ContentType(contentType));
                msg.addHeader("Content-Type", contentType);
            } catch (final OXException e) {
                logger.warn("", e);
                msg.setContentType(new ContentType(MimeTypes.MIME_DEFAULT));
                msg.addHeader("Content-Type", MimeTypes.MIME_DEFAULT);
            }
            msg.setHasAttachment(MimeMessageUtility.hasAttachments(bs));
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException, OXException {
            String contentType;
            try {
                contentType = message.getContentType();
            } catch (final MessagingException e) {
                final String[] header = message.getHeader("Content-Type");
                if (null != header && header.length > 0) {
                    contentType = header[0];
                } else {
                    contentType = null;
                }
            }
            if (null == contentType) {
                msg.setHasAttachment(false);
            } else {
                try {
                    final ContentType ct = new ContentType(contentType);
                    msg.setHasAttachment(ct.startsWith("multipart/") && MimeMessageUtility.hasAttachments((Part) message.getContent()));
                } catch (final IOException e) {
                    throw new MessagingException(e.getMessage(), e);
                }
            }
        }
    };

    private static final FetchItemHandler UID_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(final Item item, final IDMailMessage msg, final org.slf4j.Logger logger) {
            final long id = ((UID) item).uid;
            msg.setMailId(Long.toString(id));
            msg.setUid(id);
        }

        @Override
        public void handleMessage(final Message message, final IDMailMessage msg, final org.slf4j.Logger logger) throws MessagingException {
            final long id = ((IMAPFolder) message.getFolder()).getUID(message);
            msg.setMailId(Long.toString(id));
            msg.setUid(id);
        }
    };

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
            final javax.mail.FetchProfile.Item[] items = fetchProfile.getItems();
            for (final javax.mail.FetchProfile.Item item : items) {
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
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     *
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     */
    protected static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
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
