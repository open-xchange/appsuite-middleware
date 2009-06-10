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

package com.openexchange.imap.search;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.search.SearchTerm;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSearch}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSearch {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPSearch.class);

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
     * @throws MailException If a searching fails
     */
    public static int[] searchMessages(final IMAPFolder imapFolder, final com.openexchange.mail.search.SearchTerm<?> searchTerm, final IMAPConfig imapConfig) throws MessagingException, MailException {
        final int msgCount = imapFolder.getMessageCount();
        /*
         * Perform an IMAP-based search if IMAP search is enabled through config or number of messages to search in exceeds limit.
         */
        final boolean hasSearchCapability;
        {
            final IMAPCapabilities imapCapabilities = (IMAPCapabilities) imapConfig.getCapabilities();
            hasSearchCapability = imapCapabilities.hasIMAP4() || imapCapabilities.hasIMAP4rev1();
        }
        if (imapConfig.isImapSearch() || (hasSearchCapability && (msgCount >= MailProperties.getInstance().getMailFetchLimit()))) {
            try {
                final int[] matchSeqNums;
                if (searchTerm.containsWildcard()) {
                    /*
                     * Try to pre-select with non-wildcard part
                     */
                    final Message[] msgs = issueNonWildcardSearch(searchTerm.getNonWildcardJavaMailSearchTerm(), imapFolder);
                    final SmartIntArray sia = new SmartIntArray(msgs.length);
                    for (int i = 0; i < msgs.length; i++) {
                        if (searchTerm.matches(msgs[i])) {
                            sia.append(msgs[i].getMessageNumber());
                        }
                    }
                    matchSeqNums = sia.toArray();
                } else {
                    final Message[] msgs = issueNonWildcardSearch(searchTerm.getJavaMailSearchTerm(), imapFolder);
                    if ((msgs.length < 50) && !searchTerm.isAscii()) {
                        /*
                         * Search with respect to umlauts in pre-selected messages
                         */
                        return searchWithUmlautSupport(searchTerm, msgs);
                    }
                    matchSeqNums = new int[msgs.length];
                    for (int i = 0; i < msgs.length; i++) {
                        matchSeqNums[i] = msgs[i].getMessageNumber();
                    }
                }
                return matchSeqNums;
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
                if (LOG.isWarnEnabled()) {
                    final IMAPException imapException = IMAPException.create(IMAPException.Code.IMAP_SEARCH_FAILED, e, e.getMessage());
                    LOG.warn(imapException.getMessage(), imapException);
                }
            }
        }
        final MailField[] searchFields = new MailFields(MailField.getMailFieldsFromSearchTerm(searchTerm)).toArray();
        final Message[] allMsgs;
        {
            final long start = System.currentTimeMillis();
            allMsgs = new FetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), getFetchProfile(
                searchFields,
                imapConfig.getIMAPProperties().isFastFetch()), msgCount).doCommand();
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        }
        final SmartIntArray sia = new SmartIntArray(allMsgs.length >> 1);
        for (int i = 0; i < allMsgs.length; i++) {
            if (searchTerm.matches(allMsgs[i])) {
                sia.append(allMsgs[i].getMessageNumber());
            }
        }
        return sia.toArray();
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
    private static Message[] issueNonWildcardSearch(final SearchTerm term, final IMAPFolder imapFolder) throws MessagingException {
        final long start = System.currentTimeMillis();
        /*-
         * JavaMail already searches dependent on whether pattern contains non-ascii characters. If yes a charset is used:
         * SEARCH CHARSET UTF-8 <one or more search criteria>
         */
        final Message[] msgs = imapFolder.search(term);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder(128).append("IMAP search took ").append((System.currentTimeMillis() - start)).append("msec").toString());
        }
        return msgs;
    }

    /**
     * Searches with respect to umlauts
     * 
     * @param searchTerm
     *            The search term
     * @param msgs
     *            The messages
     * @return Matching messages
     * @throws MailException
     *             If searching fails
     */
    private static int[] searchWithUmlautSupport(final com.openexchange.mail.search.SearchTerm<?> searchTerm,
            final Message[] msgs) throws MailException {
        final SmartIntArray sia = new SmartIntArray(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            if (searchTerm.matches(msgs[i])) {
                sia.append(msgs[i].getMessageNumber());
            }
        }
        return sia.toArray();
    }
}
