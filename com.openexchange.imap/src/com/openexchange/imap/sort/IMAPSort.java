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

package com.openexchange.imap.sort;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.search.SearchException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPException.Code;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.imap.util.WrappingProtocolException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
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

    /**
     * Sort by first DISPLAY Sort Value for <i>From</i> email address.
     */
    public static final SortTerm DISPLAYFROM = new SortTerm("DISPLAYFROM");

    /**
     * Sort by first DISPLAY Sort Value for <i>To</i> email address.
     */
    public static final SortTerm DISPLAYTO = new SortTerm("DISPLAYTO");

    private static final EnumMap<MailSortField, SortTerm> SORT_FIELDS;
    private static final EnumMap<MailSortField, SortTerm> SORT_FIELDS_DISPLAY;

    static {
        EnumMap<MailSortField, SortTerm> map = new EnumMap<MailSortField, SortTerm>(MailSortField.class);
        map.put(MailSortField.FROM, SortTerm.FROM);
        map.put(MailSortField.TO, SortTerm.TO);
        map.put(MailSortField.CC, SortTerm.CC);
        map.put(MailSortField.RECEIVED_DATE, SortTerm.ARRIVAL);
        map.put(MailSortField.SENT_DATE, SortTerm.DATE);
        map.put(MailSortField.SIZE, SortTerm.SIZE);
        map.put(MailSortField.SUBJECT, SortTerm.SUBJECT);
        SORT_FIELDS = map;

        map = new EnumMap<MailSortField, SortTerm>(MailSortField.class);
        map.put(MailSortField.FROM, DISPLAYFROM);
        map.put(MailSortField.TO, DISPLAYTO);
        map.put(MailSortField.CC, SortTerm.CC);
        map.put(MailSortField.RECEIVED_DATE, SortTerm.ARRIVAL);
        map.put(MailSortField.SENT_DATE, SortTerm.DATE);
        map.put(MailSortField.SIZE, SortTerm.SIZE);
        map.put(MailSortField.SUBJECT, SortTerm.SUBJECT);
        SORT_FIELDS_DISPLAY = map;
    }

    /**
     * No instantiation
     */
    private IMAPSort() {
        super();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------

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
    public static int[] sortMessages(IMAPFolder imapFolder, int[] filter, MailSortField sortField, OrderDirection orderDir, IMAPConfig imapConfig, boolean doImapSort, int threshold) throws MessagingException {
        int messageCount = imapFolder.getMessageCount();
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
                        long start = System.currentTimeMillis();
                        seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria, filter);
                        long duration = System.currentTimeMillis() - start;
                        mailInterfaceMonitor.addUseTime(duration);
                        LOG.debug("IMAP sort took {}msec", duration);
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
     * @return The sort criteria ready for being used inside IMAP's <i>SORT</i> command or <code>null</code> if sort field is not supported by IMAP
     */
    public static String getSortCritForIMAPCommand(final MailSortField sortField, final boolean descendingDirection) {
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
            return null;
        }
        return imapSortCritBuilder.toString();
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Attempts to perform an IMAP-based sort with a given search term.
     *
     * @param imapFolder The IMAP folder; not <code>null</code>
     * @param jmsSearchTerm The search term or <code>null</code> to sort all messages
     * @param sortField The sort field; not <code>null</code>
     * @param order The sort order; not <code>null</code>
     * @param allowESORT Whether to allow the ESORT command being issued (if supported) to limit number of sort results
     * @param allowSORTDISPLAY Whether to allow the SORT=DISPLAY extension being used (if supported) to sort by DISPLAY value for From/To address
     * @param imapConfig The IMAP configuration; not <code>null</code>
     * @return The IMAP-sorted sequence number
     * @throws MessagingException
     * @throws OXException
     */
    public static ImapSortResult sortMessages(IMAPFolder imapFolder, javax.mail.search.SearchTerm jmsSearchTerm, MailSortField sortField, OrderDirection order, IndexRange indexRange, boolean allowESORT, boolean allowSORTDISPLAY, IMAPConfig imapConfig) throws MessagingException, OXException {
        return sortMessages(imapFolder, jmsSearchTerm, sortField, order, indexRange, allowESORT, allowSORTDISPLAY, false, imapConfig);
    }

    /**
     * Attempts to perform an IMAP-based sort with a given search term.
     *
     * @param imapFolder The IMAP folder; not <code>null</code>
     * @param jmsSearchTerm The search term or <code>null</code> to sort all messages
     * @param sortField The sort field; not <code>null</code>
     * @param order The sort order; not <code>null</code>
     * @param allowESORT Whether to allow the ESORT command being issued (if supported) to limit number of sort results
     * @param allowSORTDISPLAY Whether to allow the SORT=DISPLAY extension being used (if supported) to sort by DISPLAY value for From/To address
     * @param fallbackOnCommandFailed Whether to handle a possible "NO" response from IMAP server as a <code>UNSUPPORTED_SORT_FIELD</code> error (implicitly leading to an in-app search as fall-back)
     * @param imapConfig The IMAP configuration; not <code>null</code>
     * @return The IMAP-sorted sequence number
     * @throws MessagingException
     * @throws OXException
     */
    public static ImapSortResult sortMessages(IMAPFolder imapFolder, javax.mail.search.SearchTerm jmsSearchTerm, MailSortField sortField, OrderDirection order, IndexRange indexRange, boolean allowESORT, boolean allowSORTDISPLAY, boolean fallbackOnCommandFailed, IMAPConfig imapConfig) throws MessagingException, OXException {
        SortTerm[] sortTerms = IMAPSort.getSortTermsForIMAPCommand(sortField, order == OrderDirection.DESC, allowSORTDISPLAY && imapConfig.asMap().containsKey("SORT=DISPLAY"));
        if (sortTerms == null) {
            throw IMAPException.create(Code.UNSUPPORTED_SORT_FIELD, sortField.toString());
        }

        boolean sortedByLocalPart = false;
        for (SortTerm sortTerm : sortTerms) {
            if (SortTerm.FROM == sortTerm) {
                sortedByLocalPart = true;
            } else if (SortTerm.TO == sortTerm) {
                sortedByLocalPart = true;
            } else if (SortTerm.CC == sortTerm) {
                sortedByLocalPart = true;
            }
        }

        boolean rangeApplied = false;
        int[] seqNums = null;
        if (allowESORT && null != indexRange) {
            Map<String, String> caps = imapConfig.asMap();
            if (caps.containsKey("ESORT") && (caps.containsKey("CONTEXT=SEARCH") || caps.containsKey("CONTEXT=SORT")) && (null == jmsSearchTerm)) {
                SortPartialResult result = sortReturnPartial(sortTerms, jmsSearchTerm, indexRange, imapFolder);
                switch (result.reason) {
                    case SUCCESS:
                        {
                            seqNums = result.seqnums;
                            if (null != seqNums) {
                                // SORT RETURN PARTIAL command succeeded
                                rangeApplied = true;
                            }
                        }
                        break;
                    case COMMAND_FAILED:
                        break;
                    case FOLDER_CLOSED:
                        {
                            // Apparently, SORT RETURN PARTIAL command failed
                            try {    imapFolder.close(false);    } catch (Exception x) { /*Ignore*/ }
                            try {    imapFolder.open(IMAPFolder.READ_ONLY);    } catch (Exception x) { /*Ignore*/ }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (null == seqNums) {
            // Either insufficient capabilities/conditions not met or SORT RETURN PARTIAL failed
            seqNums = sort(sortTerms, jmsSearchTerm, imapFolder, fallbackOnCommandFailed);
        }

        return new ImapSortResult(seqNums, rangeApplied, sortedByLocalPart);
    }

    /**
     * Attempts to perform an IMAP-based sort with a given search term.
     *
     * @param imapFolder The IMAP folder; not <code>null</code>
     * @param searchTerm The search term or <code>null</code> to sort all messages
     * @param sortField The sort field; not <code>null</code>
     * @param order The sort order; not <code>null</code>
     * @param allowESORT Whether to allow the ESORT command being issued (if supported) to limit number of sort results
     * @param allowSORTDISPLAY Whether to allow the SORT=DISPLAY extension being used (if supported) to sort by DISPLAY value for From/To address
     * @param fallbackOnCommandFailed Whether to handle a possible "NO" response from IMAP server as a <code>UNSUPPORTED_SORT_FIELD</code> error (implicitly leading to an in-app search as fall-back)
     * @param imapConfig The IMAP configuration; not <code>null</code>
     * @return The IMAP-sorted sequence number
     * @throws MessagingException
     * @throws OXException
     */
    public static ImapSortResult sortMessages(IMAPFolder imapFolder, com.openexchange.mail.search.SearchTerm<?> searchTerm, MailSortField sortField, OrderDirection order, IndexRange indexRange, boolean allowESORT, boolean allowSORTDISPLAY, boolean fallbackOnCommandFailed, IMAPConfig imapConfig) throws MessagingException, OXException {
        SortTerm[] sortTerms = IMAPSort.getSortTermsForIMAPCommand(sortField, order == OrderDirection.DESC, allowSORTDISPLAY && imapConfig.asMap().containsKey("SORT=DISPLAY"));
        if (sortTerms == null) {
            throw IMAPException.create(Code.UNSUPPORTED_SORT_FIELD, sortField.toString());
        }

        boolean sortedByLocalPart = false;
        for (SortTerm sortTerm : sortTerms) {
            if (SortTerm.FROM == sortTerm) {
                sortedByLocalPart = true;
            } else if (SortTerm.TO == sortTerm) {
                sortedByLocalPart = true;
            } else if (SortTerm.CC == sortTerm) {
                sortedByLocalPart = true;
            }
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
        int[] seqNums = null;
        if (allowESORT && null != indexRange) {
            Map<String, String> caps = imapConfig.asMap();
            if (caps.containsKey("ESORT") && (caps.containsKey("CONTEXT=SEARCH") || caps.containsKey("CONTEXT=SORT")) && (null == searchTerm || searchTerm.isAscii())) {
                SortPartialResult result = sortReturnPartial(sortTerms, jmsSearchTerm, indexRange, imapFolder);
                switch (result.reason) {
                    case SUCCESS:
                        {
                            seqNums = result.seqnums;
                            if (null != seqNums) {
                                // SORT RETURN PARTIAL command succeeded
                                rangeApplied = true;
                            }
                        }
                        break;
                    case COMMAND_FAILED:
                        break;
                    case FOLDER_CLOSED:
                        {
                            // Apparently, SORT RETURN PARTIAL command failed
                            try {    imapFolder.close(false);    } catch (Exception x) { /*Ignore*/ }
                            try {    imapFolder.open(IMAPFolder.READ_ONLY);    } catch (Exception x) { /*Ignore*/ }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (null == seqNums) {
            // Either insufficient capabilities/conditions not met or SORT RETURN PARTIAL failed
            seqNums = sort(sortTerms, jmsSearchTerm, imapFolder, fallbackOnCommandFailed);
        }

        int umlautFilterThreshold = IMAPSearch.umlautFilterThreshold();
        if (searchTerm != null && umlautFilterThreshold > 0 && seqNums.length <= umlautFilterThreshold && !searchTerm.isAscii()) {
            /*
             * Search with respect to umlauts in pre-selected messages
             */
            seqNums = IMAPSearch.searchWithUmlautSupport(searchTerm, seqNums, imapFolder);
        }

        return new ImapSortResult(seqNums, rangeApplied, sortedByLocalPart);
    }

    /**
     * Performs an extended sort according to <a href="https://tools.ietf.org/html/rfc5267#section-3">ESORT extension</a>; e.g.
     * <pre>
     * UID SORT RETURN (PARTIAL 23500:24000) (REVERSE DATE) UTF-8 UNDELETED UNKEYWORD $Junk
     * </pre>
     *
     * @param sortTerms The sort terms to sort by; e.g. <code>"REVERSE ARRIVAL"</code>
     * @param jmsSearchTerm The JavaMail search term
     * @param indexRange The index range (end exclusive); e.g. get first 500 messages <code>IndexRange(0, 500)</code>
     * @param imapFolder The IMAP folder to sort/search in
     * @return The partial result
     * @throws MessagingException If ESORT fails
     */
    public static SortPartialResult sortReturnPartial(final SortTerm[] sortTerms, final javax.mail.search.SearchTerm jmsSearchTerm, IndexRange indexRange, IMAPFolder imapFolder) throws MessagingException {
        try {
            final String atom = new StringBuilder(16).append(indexRange.start + 1).append(':').append(indexRange.end).toString();
            return (SortPartialResult) imapFolder.doCommand(new ProtocolCommand() {

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
                        TIntList v = new TIntArrayList(r.length);

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
                                    return new SortPartialResult(new int[0], SortPartialReason.SUCCESS);
                                }
                                for (String snum : Strings.splitByComma(partialResults)) {
                                    int pos = snum.indexOf(':');
                                    if (pos > 0) {
                                        int start = Integer.parseInt(snum.substring(0, pos));
                                        int end = Integer.parseInt(snum.substring(pos + 1));
                                        for (int num = start; num <= end; num++) {
                                            v.add(num);
                                        }
                                    } else {
                                        v.add(Integer.parseInt(snum));
                                    }
                                }

                                r[i] = null;
                            }
                        }

                        // Copy the vector into 'matches'
                        matches = v.toArray();
                    } else if (response.isBAD()) {
                        // Obviously the SORT RETURN (PARTIAL ...) command failed
                        return new SortPartialResult(null, SortPartialReason.COMMAND_FAILED);
                    }

                    // dispatch remaining untagged responses
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("SORT", args));
                    protocol.notifyResponseHandlers(r);
                    protocol.handleResult(response);
                    return new SortPartialResult(matches, SortPartialReason.SUCCESS);
                }
            });
        } catch (FolderClosedException e) {
            Exception cause = e.getNextException();
            if (cause instanceof com.sun.mail.iap.ConnectionException) {
                if (cause.getCause() instanceof com.sun.mail.iap.ByeIOException) {
                    // SORT RETURN PARTIAL command failed...
                    LOG.warn("SORT RETURN PARTIAL command failed. Fall-back to normal SORT command.", cause);
                    return new SortPartialResult(null, SortPartialReason.FOLDER_CLOSED);
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

    private static int[] sort(final SortTerm[] sortTerms, final javax.mail.search.SearchTerm jmsSearchTerm, IMAPFolder imapFolder, boolean fallbackOnCommandFailed) throws MessagingException, OXException {
        try {
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
        } catch (FolderClosedException e) {
            throw e;
        } catch (StoreClosedException e) {
            throw e;
        } catch (MessagingException e) {
            if (fallbackOnCommandFailed) {
                Exception cause = e.getNextException();
                if (cause instanceof CommandFailedException) {
                    // The SORT command failed with a "NO" response; handle it as an unsupported sort field
                    throw IMAPException.create(Code.UNSUPPORTED_SORT_FIELD, e, sortTerms[sortTerms.length - 1].toString());
                }
            }
            throw e;
        }
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
     * <li><b>FROM</b> / <b>DISPLAYFROM</b><br>
     * RFC-822 local-part or DISPLAY sort value of the "From" address.</li>
     * <li><b>REVERSE</b><br>
     * Followed by another sort criterion, has the effect of that criterion but in reverse order.</li>
     * <li><b>SIZE</b><br>
     * Size of the message in octets.</li>
     * <li><b>SUBJECT</b><br>
     * Extracted subject text.</li>
     * <li><b>TO</b> / <b>DISPLAYTO</b><br>
     * RFC-822 local-part or DISPLAY sort value of the first "To" address.</li>
     * </ul>
     * <p>
     * Example:<br> {@link MailSortField#SENT_DATE} in descending order is turned to <code>"REVERSE DATE"</code>.
     *
     * @param sortField The sort field
     * @param desc The order direction
     * @param useSortDisplay Whether to use the <a href="https://tools.ietf.org/html/rfc5957">SORT=DISPLAY</a> capability
     * @return The sort criteria ready for being used inside IMAP's <i>SORT</i> command or <code>null</code> if sort field is not supported by IMAP
     */
    public static String getSortCritForIMAPCommand(MailSortField sortField, boolean desc, boolean useSortDisplay) {
        SortTerm[] terms = getSortTermsForIMAPCommand(sortField, desc, useSortDisplay);
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

    private static SortTerm[] getSortTermsForIMAPCommand(MailSortField sortField, boolean desc, boolean useSortDisplay) {
        SortTerm sortTerm = useSortDisplay ? SORT_FIELDS_DISPLAY.get(sortField) : SORT_FIELDS.get(sortField);
        if (sortTerm == null) {
            return null;
        }

        return desc ? new SortTerm[] { SortTerm.REVERSE, sortTerm } : new SortTerm[] { sortTerm };
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
                LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else if (response.isNO()) {
                LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
            } else {
                LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
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

        /** Whether sort by local-part was performed, rather than using DISPLAY sort value */
        public final boolean sortedByLocalPart;

        ImapSortResult(int[] msgIds, boolean rangeApplied, boolean sortedByLocalPart) {
            super();
            this.msgIds = msgIds;
            this.rangeApplied = rangeApplied;
            this.sortedByLocalPart = sortedByLocalPart;
        }
    }

    /** Specifies the result reason */
    public static enum SortPartialReason {
        SUCCESS, COMMAND_FAILED, FOLDER_CLOSED;
    }

    /** The result for an issued ESORT */
    public static final class SortPartialResult {

        /** The sequence numbers of sorted (and matching) messages */
        public final int[] seqnums;

        /** The result reason */
        public final SortPartialReason reason;

        SortPartialResult(int[] seqnums, SortPartialReason reason) {
            super();
            this.seqnums = seqnums;
            this.reason = reason;
        }
    }

}
