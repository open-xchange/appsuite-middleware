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

package com.openexchange.imap;

import static com.openexchange.imap.IMAPCommandsCollection.performCommand;
import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link AllFetch} - Utility class to fetch all messages from a certain IMAP folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllFetch {

    /**
     * The logger constant.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AllFetch.class);

    /**
     * Initializes a new {@link AllFetch}.
     */
    private AllFetch() {
        super();
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
        public abstract void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) throws OXException;
    }

    /**
     * The low cost fetch item enumeration.
     */
    public static enum LowCostItem {
        /**
         * INTERNALDATE
         */
        INTERNALDATE("INTERNALDATE", INTERNALDATE.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) {
                m.setReceivedDate(((INTERNALDATE) item).getDate());
            }
        }),
        /**
         * UID
         */
        UID("UID", UID.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) {
                m.setMailId(Long.toString(((UID) item).uid));
            }
        }),
        /**
         * FLAGS
         */
        FLAGS("FLAGS", FLAGS.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) throws OXException {
                MimeMessageConverter.parseFlags((FLAGS) item, m);
            }
        }),
        /**
         * BODYSTRUCTURE
         */
        BODYSTRUCTURE("BODYSTRUCTURE", BODYSTRUCTURE.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) throws OXException {
                final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
                final StringBuilder sb = new StringBuilder();
                sb.append(bs.type).append('/').append(bs.subtype);
                if (bs.cParams != null) {
                    sb.append(bs.cParams);
                }
                try {
                    m.setContentType(new ContentType(sb.toString()));
                } catch (final OXException e) {
                    logger.warn("", e);
                    m.setContentType(new ContentType(MimeTypes.MIME_DEFAULT));
                }
                m.setHasAttachment(bs.isMulti() && MimeMessageUtility.hasAttachments(bs));
            }
        }),
        /**
         * SIZE
         */
        SIZE("RFC822.SIZE", RFC822SIZE.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) {
                m.setSize(((RFC822SIZE) item).size);
            }
        }),
        /**
         * ENVELOPE
         * <p>
         * The fields of the envelope: date, subject, from, sender, reply-to, to, cc, bcc, in-reply-to, and message-id.
         */
        ENVELOPE("ENVELOPE", ENVELOPE.class, new FetchItemHandler() {

            @Override
            public void handleItem(final Item item, final MailMessage m, final org.slf4j.Logger logger) {
                final com.sun.mail.imap.protocol.ENVELOPE envelope = (ENVELOPE) item;
                // Date
                m.setSentDate(envelope.date);
                // Bcc, Cc, To, and From
                m.addBcc(envelope.bcc);
                m.addCc(envelope.cc);
                m.addTo(envelope.to);
                m.addFrom(envelope.from);
                // Sender and Reply-To
                m.addHeader("Sender", addrs2String(envelope.sender));
                m.addReplyTo(envelope.replyTo);
                // In-Reply-To and Message-Id
                m.addHeader("In-Reply-To", envelope.inReplyTo);
                m.addHeader("Message-Id", envelope.messageId);
                m.setSubject(MimeMessageUtility.decodeEnvelopeSubject(envelope.subject));
            }

            private String addrs2String(final InternetAddress[] addrs) {
                if (null == addrs || addrs.length == 0) {
                    return null;
                }
                final StringBuilder sb = new StringBuilder(addrs.length * 16);
                sb.append(addrs[0].toString());
                for (int i = 1; i < addrs.length; i++) {
                    sb.append(", ").append(addrs[i].toString());
                }
                return sb.toString();
            }

        });

        private final String item;

        private final Class<? extends Item> itemClass;

        private final FetchItemHandler itemHandler;

        private LowCostItem(final String item, final Class<? extends Item> itemClass, final FetchItemHandler itemHandler) {
            this.item = item;
            this.itemClass = itemClass;
            this.itemHandler = itemHandler;
        }

        /**
         * Gets the Fetch item string.
         *
         * @return The Fetch item string
         */
        public String getItemString() {
            return item;
        }

        /**
         * Gets the item class.
         *
         * @return The item class
         */
        public Class<? extends Item> getItemClass() {
            return itemClass;
        }

        /**
         * Gets the item handler.
         *
         * @return The item handler
         */
        public FetchItemHandler getItemHandler() {
            return itemHandler;
        }

    }

    /*-
     * ######################## METHODS ########################
     */

    private static final LowCostItem[] ITEMS = new LowCostItem[] { LowCostItem.UID, LowCostItem.INTERNALDATE };

    /**
     * Fetches all messages from given IMAP folder and pre-fills instances with UID, folder fullname and received date.
     *
     * @param imapFolder The IMAP folder
     * @param ascending <code>true</code> to order messages by received date in ascending order; otherwise descending
     * @param config The IMAP configuration
     * @param session The session
     * @return All messages from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static MailMessage[] fetchAll(final IMAPFolder imapFolder, final boolean ascending, final IMAPConfig config, final Session session) throws MessagingException {
        return fetchLowCost(imapFolder, ITEMS, ascending, config, session);
    }

    /**
     * Fetches all messages from given IMAP folder and pre-fills instances with fullname and given low-cost fetch item list.
     * <p>
     * Since returned instances are sorted, the low-cost fetch item list must contain <code>"INTERNALDATE"</code>.
     *
     * @param imapFolder The IMAP folder
     * @param items The low-cost fetch items
     * @param ascending <code>true</code> to order messages by received date in ascending order; otherwise descending
     * @param config The IMAP configuration
     * @param session The session
     * @return All messages from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static MailMessage[] fetchLowCost(final IMAPFolder imapFolder, final LowCostItem[] items, final boolean ascending, final IMAPConfig config, final Session session) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return new MailMessage[0];
        }
        return (MailMessage[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                /*-
                 * Arguments:  sequence set
                 * message data item names or macro
                 *
                 * Responses:  untagged responses: FETCH
                 *
                 * Result:     OK - fetch completed
                 *             NO - fetch error: can't fetch that data
                 *             BAD - command unknown or arguments invalid
                 */
                final String command;
                {
                    final StringBuilder sb = new StringBuilder(64);
                    sb.append("FETCH ").append(1 == messageCount ? "1" : "1:*").append(" (");
                    appendFetchCommand(items, sb);
                    sb.append(')');
                    command = sb.toString();
                }
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, command);
                final int len = r.length - 1;
                final Response response = r[len];
                final List<MailMessage> l = new ArrayList<MailMessage>(len);
                if (response.isOK()) {
                    // final int recentCount = imapFolder.getNewMessageCount();
                    final String fullname = imapFolder.getFullName();
                    for (int j = 0; j < len; j++) {
                        final Response resp = r[j];
                        if (resp instanceof FetchResponse) {
                            final FetchResponse fr = (FetchResponse) resp;
                            try {
                                final MailMessage m = new IDMailMessage(null, fullname);
                                // m.setRecentCount(recentCount);
                                for (final LowCostItem lowCostItem : items) {
                                    final Item item = getItemOf(lowCostItem.getItemClass(), fr, lowCostItem.getItemString(), config, session);
                                    try {
                                        lowCostItem.getItemHandler().handleItem(item, m, LOG);
                                    } catch (final OXException e) {
                                        LOG.error("", e);
                                    }
                                }
                                l.add(m);
                            } catch (final ProtocolException e) {
                                // Ignore that FETCH response
                            }
                            r[j] = null;
                        }
                    }
                    protocol.notifyResponseHandlers(r);
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new MailMessage[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    /*
                     * Check number of messages
                     */
                    try {
                        if (IMAPCommandsCollection.getTotal((IMAPStore) imapFolder.getStore(), imapFolder.getFullName()) <= 0) {
                            return new MailMessage[0];
                        }
                    } catch (final MessagingException e) {
                        LOG.warn("STATUS command failed. Throwing original exception: {}", response, e);
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    protocol.handleResult(response);
                }
                Collections.sort(l, ascending ? ASC_COMP : DESC_COMP);
                return l.toArray(new MailMessage[l.size()]);
                // } catch (final MessagingException e) {
                // throw new ProtocolException(e.getMessage(), e);
            }
        }));
    }

    /**
     * Restores trace state for specified IMAP protocol.
     *
     * @param protocol The protocol whose trace state shall be restored
     * @param sbout The output stream written to in the meantime
     * @param tracerState The trace state
     */
    protected static void restoreTracerFor(final IMAPProtocol protocol, final SBOutputStream sbout, final IMAPTracer.TracerState tracerState) {
        if (null == tracerState) {
            return;
        }
        try {
            IMAPTracer.restoreTraceState(protocol, tracerState);
            if (tracerState.isTrace()) {
                /*
                 * Trace was enabled before, thus write trace to previous output stream to maintain debug logs properly.
                 */
                final StringBuilder sb = sbout.getTrace();
                try {
                    /*
                     * DON'T CLOSE THE WRITER BECAUSE IT CLOSES UNDERLYING STREAM, TOO!!!
                     */
                    final OutputStreamWriter writer = new OutputStreamWriter(tracerState.getOut(), "UTF-8");
                    writer.write(sb.toString());
                    writer.flush();
                } catch (final IOException e) {
                    // Writing trace to stream failed...
                }
            }
        } catch (final SecurityException e) {
            LOG.error("", e);
        } catch (final IllegalArgumentException e) {
            LOG.error("", e);
        } catch (final NoSuchFieldException e) {
            LOG.error("", e);
        } catch (final IllegalAccessException e) {
            LOG.error("", e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
        }
    }

    /**
     * Gets the trace state for specified IMAP protocol.
     *
     * @param protocol The protocol whose trace state shall be returned
     * @param sbout The output stream to write to
     * @return The trace state
     */
    protected static IMAPTracer.TracerState traceStateFor(final IMAPProtocol protocol, final SBOutputStream sbout) {
        try {
            return IMAPTracer.enableTrace(protocol, sbout);
        } catch (final SecurityException e) {
            LOG.error("", e);
        } catch (final IllegalArgumentException e) {
            LOG.error("", e);
        } catch (final NoSuchFieldException e) {
            LOG.error("", e);
        } catch (final IllegalAccessException e) {
            LOG.error("", e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
        }
        return null;
    }

    /**
     * A {@link Comparator} comparing instances of {@link MailMessage} by their received date in ascending order.
     */
    protected static final Comparator<MailMessage> ASC_COMP = new Comparator<MailMessage>() {

        @Override
        public int compare(final MailMessage m1, final MailMessage m2) {
            final Date d1 = m1.getReceivedDate();
            final Date d2 = m2.getReceivedDate();
            final Integer refComp = compareReferences(d1, d2);
            return (refComp == null ? d1.compareTo(d2) : refComp.intValue());
        }
    };

    /**
     * A {@link Comparator} comparing instances of {@link MailMessage} by their received date in descending order.
     */
    protected static final Comparator<MailMessage> DESC_COMP = new Comparator<MailMessage>() {

        @Override
        public int compare(final MailMessage m1, final MailMessage m2) {
            final Date d1 = m1.getReceivedDateDirect();
            final Date d2 = m2.getReceivedDateDirect();
            final Integer refComp = compareReferences(d1, d2);
            return (refComp == null ? d1.compareTo(d2) : refComp.intValue()) * (-1);
        }
    };

    /**
     * Compares given object references being <code>null</code>.
     *
     * @param o1 The first object reference
     * @param o2 The second object reference
     * @return An {@link Integer} of <code>-1</code> if first reference is <code>null</code> but the second is not, an {@link Integer} of
     *         <code>1</code> if first reference is not <code>null</code> but the second is, an {@link Integer} of <code>0</code> if both
     *         references are <code>null</code>, or returns <code>null</code> if both references are not <code>null</code>
     */
    protected static Integer compareReferences(final Object o1, final Object o2) {
        if ((o1 == null)) {
            return (o2 == null) ? /* both null */Integer.valueOf(0) : Integer.valueOf(-1);
        }
        return (o2 == null) ? Integer.valueOf(1) : /* both not null */null;
    }

    /**
     * An {@link OutputStream} writing to a {@link StringBuilder} instance.
     */
    private static final class SBOutputStream extends OutputStream {

        private final StringBuilder sb;

        /**
         * Initializes a new {@link SBOutputStream}.
         */
        public SBOutputStream() {
            super();
            sb = new StringBuilder(8192);
        }

        @Override
        public void write(final int b) throws IOException {
            sb.append((char) (b & 0xFF));
        }

        @Override
        public void write(final byte b[], final int off, final int len) throws IOException {
            if (b == null) {
                throw new NullPointerException("data is null");
            } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            final char[] chars = new char[len];
            for (int i = 0; i < chars.length; i++) {
                chars[i] = (char) (b[off + i] & 0xFF);
            }
            sb.append(chars);
        }

        /**
         * Gets the trace.
         *
         * @return The trace
         */
        public StringBuilder getTrace() {
            return sb;
        }

    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response; throws an appropriate protocol exception if not present
     * in given <i>FETCH</i> response.
     *
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @param itemName The item name to generate appropriate error message on absence
     * @param config The IMAP configuration
     * @param session The session
     * @return The item associated with given class in specified <i>FETCH</i> response.
     */
    static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse, final String itemName, final IMAPConfig config, final Session session) throws ProtocolException {
        final I retval = optItemOf(clazz, fetchResponse);
        if (null == retval) {
            throw missingFetchItem(itemName, config, session);
        }
        return retval;
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     *
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     * @see #getItemOf(Class, FetchResponse, String)
     */
    static <I extends Item> I optItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
        final int len = fetchResponse.getItemCount();
        for (int i = 0; i < len; i++) {
            final Item item = fetchResponse.getItem(i);
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }
        return null;
    }

    /**
     * Generates a new protocol exception according to following template:<br>
     * <code>&quot;Missing &lt;itemName&gt; item in FETCH response.&quot;</code>
     *
     * @param itemName The item name; e.g. <code>UID</code>, <code>FLAGS</code>, etc.
     * @param config The IMAP configuration
     * @param session The session
     * @return A new protocol exception with appropriate message.
     */
    static ProtocolException missingFetchItem(final String itemName, final IMAPConfig config, final Session session) {
        final StringBuilder sb = new StringBuilder(128).append("Missing ").append(itemName).append(" item in FETCH response.");
        sb.append(" Login=").append(config.getLogin()).append(", server=").append(config.getServer());
        sb.append(", user=").append(session.getUserId()).append(", context=").append(session.getContextId());
        return new ProtocolException(sb.toString());
    }

    /**
     * Gets the fetch items' string representation; e.g <code>"UID INTERNALDATE"</code>.
     *
     * @param items The items
     * @return The string representation
     */
    public static String getFetchCommand(final LowCostItem[] items) {
        final StringBuilder command = new StringBuilder(64);
        appendFetchCommand(items, command);
        return command.toString();
    }

    /**
     * Gets the fetch items' string representation; e.g <code>"UID INTERNALDATE"</code>.
     *
     * @param items The items
     * @return The string representation
     */
    public static void appendFetchCommand(final LowCostItem[] items, final StringBuilder command) {
        command.append(items[0].getItemString());
        for (int i = 1; i < items.length; i++) {
            command.append(' ').append(items[i].getItemString());
        }
    }

}
