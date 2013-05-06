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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap.sort;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MimeStorageUtility.getFetchProfile;
import static com.openexchange.mail.utils.StorageUtility.EMPTY_MSGS;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import java.util.Locale;
import javax.mail.FetchProfile;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.MessageFetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.config.MailProperties;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link IMAPSort} - Perform the IMAP sort.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSort {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPSort.class));

    /**
     * No instantiation
     */
    private IMAPSort() {
        super();
    }

    /**
     * Sorts messages located in given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @param usedFields The desired fields
     * @param filter Pre-Selected messages' sequence numbers to sort or <code>null</code> to sort all
     * @param sortField The sort field
     * @param orderDir The order direction
     * @param locale The locale
     * @return Sorted messages
     * @throws MessagingException If a messaging error occurs
     */
    public static Message[] sortMessages(final IMAPFolder imapFolder, final MailFields usedFields, final int[] filter, final MailSortField sortField, final OrderDirection orderDir, final Locale locale, final IMAPConfig imapConfig) throws MessagingException {
        boolean applicationSort = true;
        Message[] msgs = null;
        final MailSortField sortBy = sortField == null ? MailSortField.RECEIVED_DATE : sortField;
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            return new Message[0];
        }
        final int size = filter == null ? messageCount : filter.length;
        /*
         * Perform an IMAP-based sort provided that SORT capability is supported and IMAP sort is enabled through config or number of
         * messages to sort exceeds limit.
         */
        if (imapConfig.isImapSort() || (imapConfig.getCapabilities().hasSort() && (size >= MailProperties.getInstance().getMailFetchLimit()))) {
            try {
                final int[] seqNums;
                {
                    /*
                     * Get IMAP sort criteria
                     */
                    final String sortCriteria = getSortCritForIMAPCommand(sortBy, orderDir == OrderDirection.DESC);
                    final long start = System.currentTimeMillis();
                    seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria, filter);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new com.openexchange.java.StringAllocator(128).append("IMAP sort took ").append((System.currentTimeMillis() - start)).append(
                            "msec").toString());
                    }
                }
                if ((seqNums == null) || (seqNums.length == 0)) {
                    return EMPTY_MSGS;
                }
                final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), imapConfig.getIMAPProperties().isFastFetch());
                final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
                final long start = System.currentTimeMillis();
                msgs = new MessageFetchIMAPCommand(
                    imapFolder,
                    imapConfig.getImapCapabilities().hasIMAP4rev1(),
                    seqNums,
                    fetchProfile,
                    false,
                    true,
                    body).doCommand();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new com.openexchange.java.StringAllocator(128).append("IMAP fetch for ").append(seqNums.length).append(" messages took ").append(
                        (System.currentTimeMillis() - start)).append("msec").toString());
                }
                if ((msgs == null) || (msgs.length == 0)) {
                    return EMPTY_MSGS;
                }

                // CHECK SORTING FOR DEBUG PURPOSE
                // System.out.println("\n\tCHECKING SORTING!!!\n\n");
                // boolean failed = false;
                // for (int i = 0; i < seqNums.length && !failed; i++) {
                // if (seqNums[i] != msgs[i].getMessageNumber()) {
                // failed = true;
                // }
                // }
                // if (failed) {
                // System.out.println("\n\tSORTING LOST DURING FETCH!!!\n\n");
                // } else {
                // System.out.println("\n\tSORTING OK!!!\n\n");
                // }

                applicationSort = false;
            } catch (final FolderClosedException e) {
                /*
                 * Caused by a protocol error such as a socket error. No retry in this case.
                 */
                throw e;
            } catch (final StoreClosedException e) {
                /*
                 * Caused by a protocol error such as a socket error. No retry in this case.
                 */
                throw e;
            } catch (final OXException e) {
                if (IMAPException.Code.UNSUPPORTED_SORT_FIELD.equals(e) || e.getCode() == 2049) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getMessage(), e);
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
                applicationSort = true;
            } catch (final MessagingException e) {
                if (e.getNextException() instanceof ProtocolException) {
                    final ProtocolException protocolException = (ProtocolException) e.getNextException();
                    final Response response = protocolException.getResponse();
                    if (response != null && response.isBYE()) {
                        /*
                         * The BYE response is always untagged, and indicates that the server is about to close the connection.
                         */
                        throw new StoreClosedException(imapFolder.getStore(), protocolException.getMessage());
                    }
                    final Throwable cause = protocolException.getCause();
                    if (cause instanceof StoreClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw ((StoreClosedException) cause);
                    } else if (cause instanceof FolderClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw ((FolderClosedException) cause);
                    }
                }
                if (LOG.isWarnEnabled()) {
                    final OXException imapException = IMAPException.create(IMAPException.Code.IMAP_SORT_FAILED, e, e.getMessage());
                    LOG.warn(imapException.getMessage(), imapException);
                }
                applicationSort = true;
            }
        }
        if (applicationSort) {
            return null;
        }
        return msgs;
    }

    /**
     * Generates an appropriate <i>SORT</i> command as defined through the IMAP SORT EXTENSION corresponding to specified sort field and
     * order direction.
     * <p>
     * The supported sort criteria are:
     * <ul>
     * <li><b>ARRIVAL</b><br>
     * Internal date and time of the message. This differs from the ON criteria in SEARCH, which uses just the internal date.</li>
     * <li><b>CC</b><br>
     * RFC-822 local-part of the first "Cc" address.</li>
     * <li><b>DATE</b><br>
     * Sent date and time from the Date: header, adjusted by time zone. This differs from the SENTON criteria in SEARCH, which uses just the
     * date and not the time, nor adjusts by time zone.</li>
     * <li><b>FROM</b><br>
     * RFC-822 local-part of the "From" address.</li>
     * <li><b>REVERSE</b><br>
     * Followed by another sort criterion, has the effect of that criterion but in reverse order.</li>
     * <li><b>SIZE</b><br>
     * Size of the message in octets.</li>
     * <li><b>SUBJECT</b><br>
     * Extracted subject text.</li>
     * <li><b>TO</b><br>
     * RFC-822 local-part of the first "To" address.</li>
     * </ul>
     * <p>
     * Example:<br>
     * {@link MailSortField#SENT_DATE} in descending order is turned to <code>"REVERSE DATE"</code>.
     *
     * @param sortField The sort field
     * @param descendingDirection The order direction
     * @return The sort criteria ready for being used inside IMAP's <i>SORT</i> command
     * @throws OXException If an unsupported sort field is specified
     */
    public static String getSortCritForIMAPCommand(final MailSortField sortField, final boolean descendingDirection) throws OXException {
        final StringBuilder imapSortCritBuilder = new StringBuilder(16).append(descendingDirection ? "REVERSE " : "");
        switch (sortField) {
        case SENT_DATE:
            imapSortCritBuilder.append("DATE");
            break;
        case RECEIVED_DATE:
            imapSortCritBuilder.append("ARRIVAL");
            break;
        case FROM:
            imapSortCritBuilder.append("FROM");
            break;
        case TO:
            imapSortCritBuilder.append("TO");
            break;
        case CC:
            imapSortCritBuilder.append("CC");
            break;
        case SUBJECT:
            imapSortCritBuilder.append("SUBJECT");
            break;
        case SIZE:
            imapSortCritBuilder.append("SIZE");
            break;
        default:
            throw IMAPException.create(IMAPException.Code.UNSUPPORTED_SORT_FIELD, sortField.getKey());
        }
        return imapSortCritBuilder.toString();
    }

    /**
     * Gets all UIDs of specified (selected) IMAP folder in either ascending or descending received date order.
     *
     * @param imapFolder The (selected) IMAP folder
     * @param descending Whether in ascending or descending received date order
     * @param imapConfig The IMAP configuration
     * @return The sorted UIDs or <code>null</code> if needed capabilities aren't supported
     * @throws MessagingException If a messaging error occurs
     */
    public static long[] allUIDs(final IMAPFolder imapFolder, final boolean descending, final IMAPConfig imapConfig) throws MessagingException {
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        final IMAPCapabilities capabilities = imapConfig.getImapCapabilities();
        if (!capabilities.hasSort() || !capabilities.hasUIDPlus()) {
            /*
             * Missing necessary extensions
             */
            return null;
        }
        /*
         * Cast & return
         */
        return ((long[]) imapFolder.doCommand(new SORTProtocolCommand(descending, imapFolder)));
    }

    private static final class SORTProtocolCommand implements IMAPFolder.ProtocolCommand {

        private final boolean descending;
        private final IMAPFolder imapFolder;

        public SORTProtocolCommand(final boolean descending, IMAPFolder imapFolder) {
            this.descending = descending;
            this.imapFolder = imapFolder;
        }

        @Override
        public Object doCommand(final IMAPProtocol p) throws ProtocolException {
            final String command = new com.openexchange.java.StringAllocator("UID SORT (").append(descending ? "REVERSE " : "").append("ARRIVAL) UTF-8 ALL").toString();
            final Response[] r = IMAPCommandsCollection.performCommand(p, command);
            final Response response = r[r.length - 1];
            final TLongList list = new TLongArrayList(256);
            if (response.isOK()) {
                final String key = "SORT";
                for (int i = 0, len = r.length; i < len; i++) {
                    if (!(r[i] instanceof IMAPResponse)) {
                        continue;
                    }
                    final IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals(key)) {
                        String num;
                        while ((num = ir.readAtomString()) != null) {
                            try {
                                list.add(Long.parseLong(num));
                            } catch (final NumberFormatException e) {
                                LOG.error(e.getMessage(), e);
                                throw new ProtocolException("Invalid UID: " + num, e);
                            }
                        }
                    }
                    r[i] = null;
                }
                p.notifyResponseHandlers(r);
            } else if (response.isBAD()) {
                throw new BadCommandException(IMAPException.getFormattedMessage(
                    IMAPException.Code.PROTOCOL_ERROR,
                    command,
                    ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else if (response.isNO()) {
                throw new CommandFailedException(IMAPException.getFormattedMessage(
                    IMAPException.Code.PROTOCOL_ERROR,
                    command,
                    ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else {
                p.handleResult(response);
            }
            /*
             * Return UIDs
             */
            return list.toArray();
        }
    } // End of SORTProtocolCommand

}
