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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.apache.commons.logging.Log;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AllFetch.class);

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
         * @throws MailException If a mail error occurs
         */
        public abstract void handleItem(final Item item, final MailMessage m, final org.apache.commons.logging.Log logger) throws MailException;
    }

    private static final FetchItemHandler UID_ITEM_HANDLER = new FetchItemHandler() {

        public void handleItem(final Item item, final MailMessage m, final Log logger) {
            m.setMailId(String.valueOf(((UID) item).uid));
        }
    };

    private static final FetchItemHandler INTERNALDATE_ITEM_HANDLER = new FetchItemHandler() {

        public void handleItem(final Item item, final MailMessage m, final Log logger) {
            m.setReceivedDate(((INTERNALDATE) item).getDate());
        }
    };

    private static final FetchItemHandler FLAGS_ITEM_HANDLER = new FetchItemHandler() {

        public void handleItem(final Item item, final MailMessage m, final Log logger) throws MailException {
            MIMEMessageConverter.parseFlags((FLAGS) item, m);
        }
    };

    private static final FetchItemHandler SIZE_ITEM_HANDLER = new FetchItemHandler() {

        public void handleItem(final Item item, final MailMessage m, final Log logger) {
            m.setSize(((RFC822SIZE) item).size);
        }
    };

    private static final FetchItemHandler BODYSTRUCTURE_ITEM_HANDLER = new FetchItemHandler() {

        public void handleItem(final Item item, final MailMessage m, final Log logger) throws MailException {
            final BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
            final StringBuilder sb = new StringBuilder();
            sb.append(bs.type).append('/').append(bs.subtype);
            if (bs.cParams != null) {
                sb.append(bs.cParams);
            }
            try {
                m.setContentType(new ContentType(sb.toString()));
            } catch (final MailException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e.getMessage(), e);
                }
                m.setContentType(new ContentType(MIMETypes.MIME_DEFAULT));
            }
            m.setHasAttachment(bs.isMulti() && ("MIXED".equalsIgnoreCase(bs.subtype) || MIMEMessageUtility.hasAttachments(bs)));
        }
    };

    private static final Map<Class<? extends Item>, FetchItemHandler> MAP;

    static {
        MAP = new HashMap<Class<? extends Item>, FetchItemHandler>(8);
        MAP.put(UID.class, UID_ITEM_HANDLER);
        MAP.put(INTERNALDATE.class, INTERNALDATE_ITEM_HANDLER);
        MAP.put(FLAGS.class, FLAGS_ITEM_HANDLER);
        MAP.put(RFC822SIZE.class, SIZE_ITEM_HANDLER);
        MAP.put(BODYSTRUCTURE.class, BODYSTRUCTURE_ITEM_HANDLER);
        MAP.put(INTERNALDATE.class, INTERNALDATE_ITEM_HANDLER);
    }

    /*-
     * ######################## METHODS ########################
     */

    private static final String COMMAND_FETCH = "FETCH 1:* (UID INTERNALDATE)";

    /**
     * Fetches all messages from given IMAP folder and pre-fills instances with UID, folder fullname and received date.
     * 
     * @param imapFolder The IMAP folder
     * @param ascending <code>true</code> to order messages by received date in ascending order; otherwise descending
     * @return All messages from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static MailMessage[] fetchAll(final IMAPFolder imapFolder, final boolean ascending) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new MailMessage[0];
        }
        return (MailMessage[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
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
                final Response[] r = p.command(COMMAND_FETCH, null);
                final int len = r.length - 1;
                final Response response = r[len];
                final List<MailMessage> l = new ArrayList<MailMessage>(len);
                if (response.isOK()) {
                    final String fullname = imapFolder.getFullName();
                    final String internaldate = "INTERNALDATE";
                    for (int j = 0; j < len; j++) {
                        if ("FETCH".equals(((IMAPResponse) r[j]).getKey())) {
                            final FetchResponse fr = (FetchResponse) r[j];
                            final MailMessage m = new IDMailMessage(String.valueOf(getItemOf(UID.class, fr, "UID").uid), fullname);
                            m.setReceivedDate(getItemOf(INTERNALDATE.class, fr, internaldate).getDate());
                            l.add(m);
                            r[j] = null;
                        }
                    }
                    p.notifyResponseHandlers(r);
                } else if (response.isBAD()) {
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        COMMAND_FETCH,
                        response.toString()));
                } else if (response.isNO()) {
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        COMMAND_FETCH,
                        response.toString()));
                } else {
                    p.handleResult(response);
                }
                Collections.sort(l, ascending ? ASC_COMP : DESC_COMP);
                return l.toArray(new MailMessage[l.size()]);
            }

        }));
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response; throws an appropriate protocol exception if not present
     * in given <i>FETCH</i> response.
     * 
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @param itemName The item name to generate appropriate error message on absence
     * @return The item associated with given class in specified <i>FETCH</i> response.
     */
    static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse, final String itemName) throws ProtocolException {
        final I retval = getItemOf(clazz, fetchResponse);
        if (null == retval) {
            throw missingFetchItem(itemName);
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
    static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
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
     * @return A new protocol exception with appropriate message.
     */
    static ProtocolException missingFetchItem(final String itemName) {
        return new ProtocolException(
            new StringBuilder(48).append("Missing ").append(itemName).append(" item in FETCH response.").toString());
    }

    /**
     * Fetches all messages from given IMAP folder and pre-fills instances with given low-cost fetch item list.
     * <p>
     * Since returned instances are sorted, the low-cost fetch item list must contain <code>"INTERNALDATE"</code>.
     * 
     * @param imapFolder The IMAP folder
     * @param lowCostItems The low-cost fetch item list; e.g <code>"UID INTERNALDATE"</code>
     * @param ascending <code>true</code> to order messages by received date in ascending order; otherwise descending
     * @return All messages from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static MailMessage[] fetchLowCost(final IMAPFolder imapFolder, final String lowCostItems, final boolean ascending) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new MailMessage[0];
        }
        return (MailMessage[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
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
                final String command = new StringBuilder(12 + lowCostItems.length()).append("FETCH 1:* (").append(lowCostItems).append(')').toString();
                final Response[] r = p.command(command, null);
                final int len = r.length - 1;
                final Response response = r[len];
                final List<MailMessage> l = new ArrayList<MailMessage>(len);
                if (response.isOK()) {
                    final String fullname = imapFolder.getFullName();
                    final String internaldate = "INTERNALDATE";
                    for (int j = 0; j < len; j++) {
                        if ("FETCH".equals(((IMAPResponse) r[j]).getKey())) {
                            final FetchResponse fr = (FetchResponse) r[j];
                            final MailMessage m = new IDMailMessage(null, fullname);
                            final int itemCount = fr.getItemCount();
                            for (int k = 0; k < itemCount; k++) {
                                final Item item = fr.getItem(k);
                                final FetchItemHandler itemHandler = MAP.get(item.getClass());
                                if (null == itemHandler) {
                                    throw new ProtocolException("Unsupported FETCH item: " + item.getClass().getName());
                                }
                                try {
                                    itemHandler.handleItem(item, m, LOG);
                                } catch (final MailException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                            l.add(m);
                            r[j] = null;
                        }
                    }
                    p.notifyResponseHandlers(r);
                } else if (response.isBAD()) {
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString()));
                } else if (response.isNO()) {
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString()));
                } else {
                    p.handleResult(response);
                }
                Collections.sort(l, ascending ? ASC_COMP : DESC_COMP);
                return l.toArray(new MailMessage[l.size()]);
            }

        }));
    }

    /**
     * A {@link Comparator} comparing instances of {@link MailMessage} by their received date in ascending order.
     */
    static final Comparator<MailMessage> ASC_COMP = new Comparator<MailMessage>() {

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
    static final Comparator<MailMessage> DESC_COMP = new Comparator<MailMessage>() {

        public int compare(final MailMessage m1, final MailMessage m2) {
            final Date d1 = m1.getReceivedDate();
            final Date d2 = m2.getReceivedDate();
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
    static Integer compareReferences(final Object o1, final Object o2) {
        if ((o1 == null) && (o2 != null)) {
            return Integer.valueOf(-1);
        } else if ((o1 != null) && (o2 == null)) {
            return Integer.valueOf(1);
        } else if ((o1 == null) && (o2 == null)) {
            return Integer.valueOf(0);
        }
        /*
         * Both references are not null
         */
        return null;
    }

}
