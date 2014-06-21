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

package com.openexchange.imap.search;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MimeStorageUtility.getFetchProfile;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.MessageFetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.MessageSet;

/**
 * {@link IMAPSearch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSearch {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(IMAPSearch.class);

    /**
     * No instantiation
     */
    private IMAPSearch() {
        super();
    }

    /**
     * Searches messages in given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @param searchTerm The search term
     * @return Filtered messages' sequence numbers according to search term
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a searching fails
     */
    public static int[] searchMessages(final IMAPFolder imapFolder, final com.openexchange.mail.search.SearchTerm<?> searchTerm, final IMAPConfig imapConfig) throws MessagingException, OXException {
        final int msgCount = imapFolder.getMessageCount();
        if (msgCount <= 0) {
            return new int[0];
        }
        final MailFields mailFields = new MailFields(MailField.getMailFieldsFromSearchTerm(searchTerm));
        final boolean hasSearchCapability;
        {
            final IMAPCapabilities imapCapabilities = (IMAPCapabilities) imapConfig.getCapabilities();
            hasSearchCapability = imapCapabilities.hasIMAP4() || imapCapabilities.hasIMAP4rev1();
        }
        if (mailFields.contains(MailField.BODY) || mailFields.contains(MailField.FULL)) {
            if (hasSearchCapability && (imapConfig.forceImapSearch() || (msgCount >= MailProperties.getInstance().getMailFetchLimit()))) {
                /*
                 * Too many messages in IMAP folder or IMAP-based search should be forced.
                 * Fall-back to IMAP-based search and accept a non-type-sensitive search.
                 */
                final int[] seqNums = issueIMAPSearch(imapFolder, searchTerm);
                if (null != seqNums) {
                    return seqNums;
                }
            }
            /*
             * Manual search needed in case of body search since IMAP's SEARCH command does not perform type-sensitive search; e.g. extract
             * plain-text out of HTML content.
             */
            final Message[] allMsgs = imapFolder.getMessages();
            final TIntList list = new TIntArrayList(msgCount);
            for (int i = 0; i < allMsgs.length; i++) {
                if (searchTerm.matches(allMsgs[i])) {
                    list.add(allMsgs[i].getMessageNumber());
                }
            }
            return list.toArray();
        }
        /*
         * Perform an IMAP-based search if IMAP search is forces through configuration or is enabled and number of messages exceeds limit.
         */
        if (imapConfig.forceImapSearch() || (imapConfig.isImapSearch() || (hasSearchCapability && (msgCount >= MailProperties.getInstance().getMailFetchLimit())))) {
            final int[] seqNums = issueIMAPSearch(imapFolder, searchTerm);
            if (null != seqNums) {
                return seqNums;
            }
        }
        final Message[] allMsgs;
        if (mailFields.contains(MailField.BODY) || mailFields.contains(MailField.FULL)) {
            allMsgs = imapFolder.getMessages();
        } else {
            mailFields.add(MailField.CONTENT_TYPE); // Possibly checked by search term
            final long start = System.currentTimeMillis();
            allMsgs =
                new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), getFetchProfile(
                    mailFields.toArray(),
                    imapConfig.getIMAPProperties().isFastFetch()), msgCount).doCommand();
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        }
        final TIntList list = new TIntArrayList(msgCount);
        for (int i = 0; i < allMsgs.length; i++) {
            if (searchTerm.matches(allMsgs[i])) {
                list.add(allMsgs[i].getMessageNumber());
            }
        }
        return list.toArray();
    }

    private static volatile Integer umlautFilterThreshold;
    private static int umlautFilterThreshold() {
        Integer i = umlautFilterThreshold;
        if (null == i) {
            synchronized (IMAPSearch.class) {
                i = umlautFilterThreshold;
                if (null == i) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    final int defaultVal = 50;
                    if (null == service) {
                        return defaultVal;
                    }
                    i = Integer.valueOf(service.getIntProperty("com.openexchange.imap.umlautFilterThreshold", defaultVal));
                    umlautFilterThreshold = i;
                }
            }
        }
        return i.intValue();
    }

    private static volatile Boolean chunkWiseImapSearch;
    private static boolean chunkWiseImapSearch() {
        Boolean tmp = chunkWiseImapSearch;
        if (null == tmp) {
            synchronized (IMAPSearch.class) {
                tmp = chunkWiseImapSearch;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    final boolean defaultVal = false;
                    if (null == service) {
                        return defaultVal;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.imap.chunkWiseImapSearch", defaultVal));
                    chunkWiseImapSearch = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                umlautFilterThreshold = null;
                chunkWiseImapSearch = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

    private static int[] issueIMAPSearch(final IMAPFolder imapFolder, final com.openexchange.mail.search.SearchTerm<?> searchTerm) throws OXException, FolderClosedException, StoreClosedException {
        try {
            if (searchTerm.containsWildcard()) {
                /*
                 * Try to pre-select with non-wildcard part
                 */
                return issueNonWildcardSearch(searchTerm.getNonWildcardJavaMailSearchTerm(), imapFolder);
            }
            final int[] seqNums = issueNonWildcardSearch(searchTerm.getJavaMailSearchTerm(), imapFolder);
            final int umlautFilterThreshold = umlautFilterThreshold();
            if ((umlautFilterThreshold > 0) && (seqNums.length <= umlautFilterThreshold) && !searchTerm.isAscii()) {
                /*
                 * Search with respect to umlauts in pre-selected messages
                 */
                return searchWithUmlautSupport(searchTerm, seqNums, imapFolder);
            }
            return seqNums;
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
            final Exception nextException = e.getNextException();
            if (nextException instanceof ProtocolException) {
                final ProtocolException protocolException = (ProtocolException) nextException;
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
            LOG.warn("", IMAPException.Code.IMAP_SEARCH_FAILED.create(e, e.getMessage()));
            return null;
        }
    }

    /**
     * Executes {@link IMAPFolder#search(SearchTerm)} with specified search term passed to invocation.
     * <p>
     * The search term is considered to not contain any wildcard characters, but may contain non-ascii characters since IMAP search is
     * capable to deal with non-ascii characters through specifying a proper charset like UTF-8.
     *
     * @param term The search term to pass
     * @param imapFolder The IMAP folder to search in
     * @return The matching messages as an array
     * @throws MessagingException If a messaging error occurs
     */
    public static int[] issueNonWildcardSearch(final SearchTerm term, final IMAPFolder imapFolder) throws MessagingException {
        /*-
         * JavaMail already searches dependent on whether pattern contains non-ascii characters. If yes a charset is used:
         * SEARCH CHARSET UTF-8 <one or more search criteria>
         */
        return search(term, imapFolder);
    }

    /**
     * Searches with respect to umlauts
     */
    private static int[] searchWithUmlautSupport(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final int[] seqNums, final IMAPFolder imapFolder) throws OXException {
        try {
            final TIntList results = new TIntArrayList(seqNums.length);
            for (int i = 0; i < seqNums.length; i++) {
                final int msgnum = seqNums[i];
                if (searchTerm.matches(imapFolder.getMessage(msgnum))) {
                    results.add(msgnum);
                }
            }
            return results.toArray();
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    // --------------------------- IMAP commands ------------------------------

    private static int[] search(final SearchTerm term, final IMAPFolder imapFolder) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (0 >= messageCount) {
            return new int[0];
        }

        final boolean chunkWise = chunkWiseImapSearch();
        final Object oSeqNums = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                try {
                    if (!chunkWise) {
                        return protocol.search(term);
                    }

                    // Perform chunk-wise search
                    final List<MessageSet> sets = new LinkedList<MessageSet>();
                    {
                        int chunkSize = MailProperties.getInstance().getMailFetchLimit();
                        int start = 1;
                        while (start <= messageCount) {
                            if ((start + chunkSize) > messageCount) {
                                chunkSize = (messageCount - start + 1);
                            }
                            sets.add(new MessageSet(start, start + chunkSize - 1));
                            start += chunkSize;
                        }
                        if (start <= messageCount) {
                            sets.add(new MessageSet(start, messageCount));
                        }
                    }
                    final TIntList ret = new TIntLinkedList();
                    final MessageSet[] arr = new MessageSet[1];
                    for (final MessageSet messageSet : sets) {
                        arr[0] = messageSet;
                        int[] results = protocol.search(arr, term);
                        for (int seqNum : results) {
                            if (seqNum >= 1 && seqNum <= messageCount) {
                                ret.add(seqNum);
                            }
                        }
                    }
                    return ret;
                } catch (final SearchException e) {
                    throw new ProtocolException(e.getMessage(), e);
                } catch (final MessagingException e) {
                    throw new ProtocolException(e.getMessage(), e);
                }
            }
        });
        return oSeqNums instanceof TIntList ? ((TIntList) oSeqNums).toArray() : (int[]) oSeqNums;
    }

}
