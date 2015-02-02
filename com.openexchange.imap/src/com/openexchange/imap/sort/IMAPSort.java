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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import javax.mail.FetchProfile;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.search.SearchException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPException.Code;
import com.openexchange.imap.command.MessageFetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.imap.util.WrappingProtocolException;
import com.openexchange.java.Strings;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.config.MailProperties;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.ProtocolCommand;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.SearchSequence;

/**
 * {@link IMAPSort} - Perform the IMAP sort.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSort {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPSort.class);

    private static final EnumMap<MailSortField, SortTerm> SORT_FIELDS = new EnumMap<MailSortField, SortTerm>(MailSortField.class);
    static {
        SORT_FIELDS.put(MailSortField.FROM, SortTerm.FROM);
        SORT_FIELDS.put(MailSortField.TO, SortTerm.TO);
        SORT_FIELDS.put(MailSortField.CC, SortTerm.CC);
        SORT_FIELDS.put(MailSortField.RECEIVED_DATE, SortTerm.ARRIVAL);
        SORT_FIELDS.put(MailSortField.SENT_DATE, SortTerm.DATE);
        SORT_FIELDS.put(MailSortField.SIZE, SortTerm.SIZE);
        SORT_FIELDS.put(MailSortField.SUBJECT, SortTerm.SUBJECT);
    }

    /**
     * No instantiation
     */
    private IMAPSort() {
        super();
    }

    /**
     * Attempts to perform a IMAP-based sort.
     *
     * @param imapFolder The IMAP folder
     * @param filter The optional filter
     * @param sortField The sort field
     * @param orderDir The sort order
     * @param imapConfig The IMAP configuration
     * @return The IMAP-sorted sequence number or <code>null</code> if unable to do IMAP sort
     * @throws MessagingException If sort attempt fails horribly
     */
    public static int[] sortMessages(final IMAPFolder imapFolder, final int[] filter, final MailSortField sortField, final OrderDirection orderDir, final IMAPConfig imapConfig) throws MessagingException {
        return sortMessages(imapFolder, filter, sortField, orderDir, imapConfig, imapConfig.isImapSort(), MailProperties.getInstance().getMailFetchLimit());
    }

    /**
     * Attempts to perform a IMAP-based sort.
     *
     * @param imapFolder The IMAP folder
     * @param filter The optional filter
     * @param sortField The sort field
     * @param orderDir The sort order
     * @param imapConfig The IMAP configuration
     * @return The IMAP-sorted sequence number or <code>null</code> if unable to do IMAP sort
     * @throws MessagingException If sort attempt fails horribly
     */
    public static int[] sortMessages(final IMAPFolder imapFolder, final int[] filter, final MailSortField sortField, final OrderDirection orderDir, final IMAPConfig imapConfig, final boolean doImapSort, final int threshold) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            return new int[0];
        }
        final int size = filter == null ? messageCount : filter.length;
        if (doImapSort || (imapConfig.getCapabilities().hasSort() && (size >= threshold))) {
            try {
                // Get IMAP sort criteria
                final MailSortField sortBy = sortField == null ? MailSortField.RECEIVED_DATE : sortField;
                final String sortCriteria = getSortCritForIMAPCommand(sortBy, orderDir == OrderDirection.DESC);
                if (null != sortCriteria) {
                    final int[] seqNums;
                    {
                        // Do IMAP sort
                        final long start = System.currentTimeMillis();
                        seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria, filter);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                        LOG.debug("IMAP sort took {}msec", (System.currentTimeMillis() - start));
                    }
                    if ((seqNums == null) || (seqNums.length == 0)) {
                        return new int[0];
                    }
                    return seqNums;
                }
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
                LOG.warn("", IMAPException.create(IMAPException.Code.IMAP_SORT_FAILED, e, e.getMessage()));
            }
        }
        return null;
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
        boolean applicationSort = true;
        if (imapConfig.isImapSort() || (imapConfig.getCapabilities().hasSort() && (size >= MailProperties.getInstance().getMailFetchLimit()))) {
            try {
                // Get IMAP sort criteria
                final String sortCriteria = getSortCritForIMAPCommand(sortBy, orderDir == OrderDirection.DESC);
                if (null != sortCriteria) {
                    final int[] seqNums;
                    {
                        // Do IMAP sort
                        final long start = System.currentTimeMillis();
                        seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria, filter);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                        LOG.debug("IMAP sort took {}msec", (System.currentTimeMillis() - start));
                    }
                    if ((seqNums == null) || (seqNums.length == 0)) {
                        return EMPTY_MSGS;
                    }
                    final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), imapConfig.getIMAPProperties().isFastFetch());
                    final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
                    final long start = System.currentTimeMillis();
                    msgs = new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), seqNums, fetchProfile, false, true, body).doCommand();
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    LOG.debug("IMAP fetch for {} messages took {}msec", seqNums.length, (System.currentTimeMillis() - start));
                    if ((msgs == null) || (msgs.length == 0)) {
                        return EMPTY_MSGS;
                    }
                    applicationSort = false;
                }
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
                LOG.warn("", IMAPException.create(IMAPException.Code.IMAP_SORT_FAILED, e, e.getMessage()));
                applicationSort = true;
            }
        }
        if (applicationSort) {
            return null;
        }
        return msgs;
    }

    /**
     * Attempts to perform a IMAP-based sort with a given search term.
     *
     * @param imapFolder The IMAP folder; not <code>null</code>
     * @param searchTerm The search term or <code>null</code> to sort all messages
     * @param sortField The sort field; not <code>null</code>
     * @param order The sort order; not <code>null</code>
     * @param allowESORT Whether to allow the ESORT command being issued (if supported) to limit number of sort results
     * @param imapConfig The IMAP configuration; not <code>null</code>
     * @return The IMAP-sorted sequence number
     * @throws MessagingException
     * @throws OXException
     */
    public static ImapSortResult sortMessages(IMAPFolder imapFolder, com.openexchange.mail.search.SearchTerm<?> searchTerm, MailSortField sortField, OrderDirection order, IndexRange indexRange, boolean allowESORT, IMAPConfig imapConfig) throws MessagingException, OXException {
        SortTerm[] sortTerms = IMAPSort.getSortTermsForIMAPCommand(sortField, order == OrderDirection.DESC);
        if (sortTerms == null) {
            throw IMAPException.create(Code.UNSUPPORTED_SORT_FIELD, sortField.toString());
        }

        javax.mail.search.SearchTerm jmsSearchTerm;
        if (searchTerm == null) {
            jmsSearchTerm = null;
        } else {
            if (searchTerm.containsWildcard()) {
                jmsSearchTerm = searchTerm.getNonWildcardJavaMailSearchTerm();
            } else {
                jmsSearchTerm = searchTerm.getJavaMailSearchTerm();
            }
        }

        boolean rangeApplied = false;
        int[] seqNums;
        if (allowESORT && null != indexRange && imapConfig.asMap().containsKey("ESORT") && (null == searchTerm || searchTerm.isAscii())) {
            seqNums = sortReturnPartial(sortTerms, jmsSearchTerm, indexRange, imapFolder);

            // Check result
            if (null == seqNums) {
                // Apparently, SORT RETURN PARTIAL command failed
                try {    imapFolder.close(false);    } catch (Exception x) { /*Ignore*/ }
                try {    imapFolder.open(IMAPFolder.READ_ONLY);    } catch (Exception x) { /*Ignore*/ }
                seqNums = sort(sortTerms, jmsSearchTerm, imapFolder);
            } else {
                // SORT RETURN PARTIAL command succeeded
                rangeApplied = true;
            }
        } else {
            seqNums = sort(sortTerms, jmsSearchTerm, imapFolder);
        }

        int umlautFilterThreshold = IMAPSearch.umlautFilterThreshold();
        if (searchTerm != null && umlautFilterThreshold > 0 && seqNums.length <= umlautFilterThreshold && !searchTerm.isAscii()) {
            /*
             * Search with respect to umlauts in pre-selected messages
             */
            seqNums = IMAPSearch.searchWithUmlautSupport(searchTerm, seqNums, imapFolder);
        }

        return new ImapSortResult(seqNums, rangeApplied);
    }

    private static int[] sortReturnPartial(final SortTerm[] sortTerms, final javax.mail.search.SearchTerm jmsSearchTerm, IndexRange indexRange, IMAPFolder imapFolder) throws MessagingException {
        try {
            final String atom = new StringBuilder(16).append(indexRange.start + 1).append(':').append(indexRange.end).toString();
            return (int[]) imapFolder.doCommand(new ProtocolCommand() {

                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    Argument args = new Argument();

                    args.writeAtom("RETURN");    // context extension according to https://tools.ietf.org/html/rfc5267

                    {
                        Argument sargs = new Argument();
                        sargs.writeAtom("PARTIAL");
                        sargs.writeAtom(atom);
                        args.writeArgument(sargs);  // PARTIAL argument
                    }

                    {
                        Argument sargs = new Argument();
                        for (int i = 0; i < sortTerms.length; i++) {
                            sargs.writeAtom(sortTerms[i].toString());
                        }
                        args.writeArgument(sargs);  // sort criteria
                    }

                    args.writeAtom("UTF-8");    // charset specification
                    if (jmsSearchTerm != null) {
                        try {
                            args.append(new SearchSequence().generateSequence(jmsSearchTerm, "UTF-8"));
                        } catch (final IOException ioex) {
                            // should never happen
                            throw new WrappingProtocolException("", new SearchException(ioex.toString()));
                        } catch (MessagingException e) {
                            throw new WrappingProtocolException("", e);
                        }
                    } else {
                        args.writeAtom("ALL");
                    }

                    Response[] r = protocol.command("SORT", args);
                    Response response = r[r.length - 1];
                    int[] matches = null;

                    // Grab all SORT responses
                    if (response.isOK()) { // command successful
                        List<Integer> v = new ArrayList<Integer>(r.length);

                        for (int i = 0, len = r.length; i < len; i++) {
                            if (!(r[i] instanceof IMAPResponse)) {
                                continue;
                            }

                            IMAPResponse ir = (IMAPResponse) r[i];
                            if (ir.keyEquals("ESEARCH")) {
                                // E.g. " * ESEARCH (TAG "s") PARTIAL (1:10 972,485,971,484,970,483,969,482,968,481)"
                                ir.readAtomStringList();
                                ir.readAtom();

                                String partialResults = ir.readAtomStringList()[1];
                                if ("NIL".equalsIgnoreCase(partialResults)) {
                                    return new int[0];
                                }
                                for (String snum : Strings.splitByComma(partialResults)) {
                                    int pos = snum.indexOf(':');
                                    if (pos > 0) {
                                        int start = Integer.parseInt(snum.substring(0, pos));
                                        int end = Integer.parseInt(snum.substring(pos + 1));
                                        for (int num = start; num <= end; num++) {
                                            v.add(Integer.valueOf(num));
                                        }
                                    } else {
                                        v.add(Integer.valueOf(snum));
                                    }
                                }

                                r[i] = null;
                            }
                        }

                        // Copy the vector into 'matches'
                        int vsize = v.size();
                        matches = new int[vsize];
                        for (int i = 0; i < vsize; i++) {
                            matches[i] = v.get(i).intValue();
                        }
                    }

                    // dispatch remaining untagged responses
                    protocol.notifyResponseHandlers(r);
                    protocol.handleResult(response);
                    return matches;
                }
            });
        } catch (FolderClosedException e) {
            Exception cause = e.getNextException();
            if (cause instanceof com.sun.mail.iap.ConnectionException) {
                if (cause.getCause() instanceof com.sun.mail.iap.ByeIOException) {
                    // SORT RETURN PARTIAL command failed...
                    LOG.warn("SORT RETURN PARTIAL command failed. Fall-back to normal SORT command.", cause);
                    return null;
                }
            }
            throw e;
        } catch (StoreClosedException e) {
            throw e;
        } catch (MessagingException e) {
            Exception cause = e.getNextException();
            if (cause instanceof WrappingProtocolException) {
                throw ((WrappingProtocolException) cause).getMessagingException();
            }
            throw e;
        }
    }

    private static int[] sort(final SortTerm[] sortTerms, final javax.mail.search.SearchTerm jmsSearchTerm, IMAPFolder imapFolder) throws MessagingException {
        return (int[]) imapFolder.doCommand(new ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                try {
                    return protocol.sort(sortTerms, jmsSearchTerm);
                } catch (SearchException e) {
                    throw new ProtocolException(e.getMessage(), e);
                }
            }
        });
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
     * Example:<br> {@link MailSortField#SENT_DATE} in descending order is turned to <code>"REVERSE DATE"</code>.
     *
     * @param sortField The sort field
     * @param descendingDirection The order direction
     * @return The sort criteria ready for being used inside IMAP's <i>SORT</i> command or <code>null</code> if sort field is not supported by IMAP
     */
    public static String getSortCritForIMAPCommand(final MailSortField sortField, final boolean descendingDirection) {
        SortTerm[] terms = getSortTermsForIMAPCommand(sortField, descendingDirection);
        if (terms == null || terms.length == 0) {
            return null;
        }

        if (terms.length == 1) {
            return terms[0].toString();
        }

        StringBuilder sb = new StringBuilder();
        for (SortTerm term : terms) {
            sb.append(term).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Checks if the given field may be used for sorting via IMAPs SORT command.
     *
     * @param sortField The field; never <code>null</code>
     * @return <code>true</code> if the field is valid
     */
    public static boolean isValidSortField(MailSortField sortField) {
        return SORT_FIELDS.containsKey(sortField);
    }

    private static SortTerm[] getSortTermsForIMAPCommand(final MailSortField sortField, final boolean descendingDirection) {
        SortTerm sortTerm = SORT_FIELDS.get(sortField);
        if (sortTerm == null) {
            return null;
        }

        if (descendingDirection) {
            return new SortTerm[] { SortTerm.REVERSE, sortTerm };
        }

        return new SortTerm[] { sortTerm };
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
            final String command = new StringBuilder("UID SORT (").append(descending ? "REVERSE " : "").append("ARRIVAL) UTF-8 ALL").toString();
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
                                LOG.error("", e);
                                throw new ProtocolException("Invalid UID: " + num, e);
                            }
                        }
                    }
                    r[i] = null;
                }
                p.notifyResponseHandlers(r);
            } else if (response.isBAD()) {
                throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else if (response.isNO()) {
                throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else {
                p.handleResult(response);
            }
            /*
             * Return UIDs
             */
            return list.toArray();
        }
    } // End of SORTProtocolCommand

    // ---------------------------------------------------------------------------------------------

    /**
     * The result object for IMAP SORT.
     */
    public static final class ImapSortResult {

        /** The sorted message sequence numbers */
        public final int[] msgIds;

        /** Whether index range has already been applied */
        public final boolean rangeApplied;

        ImapSortResult(int[] msgIds, boolean rangeApplied) {
            super();
            this.msgIds = msgIds;
            this.rangeApplied = rangeApplied;
        }
    }

}
