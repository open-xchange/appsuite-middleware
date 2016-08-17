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

package com.openexchange.messaging.rss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.Serializer;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xml.sax.InputSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Streams;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.generic.AttachmentFinderHandler;
import com.openexchange.messaging.generic.MessageParser;
import com.openexchange.messaging.generic.MessagingComparator;
import com.openexchange.session.Session;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;


/**
 * {@link RSSMessageAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSMessageAccess extends RSSCommon implements MessagingMessageAccess {

    private final FeedFetcher feedFetcher;
    private final MessagingAccountManager accounts;

    private FeedAdapter feed = null;

    public RSSMessageAccess(final int accountId, final Session session, final FeedFetcher fetcher, final MessagingAccountManager accounts) {
        super(accountId, session);
        this.accountId = accountId;
        this.session = session;
        feedFetcher = fetcher;
        this.accounts = accounts;
    }

    @Override
    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws OXException {
        final AttachmentFinderHandler handler = new AttachmentFinderHandler(sectionId);
        new MessageParser().parseMessage(getMessage(folder, messageId, true), handler);
        final MessagingPart part = handler.getMessagingPart();
        if (null == part) {
            throw MessagingExceptionCodes.ATTACHMENT_NOT_FOUND.create(sectionId, messageId, folder);
        }
        return part;
    }

    @Override
    public void appendMessages(final String folder, final MessagingMessage[] messages) throws OXException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }


    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }


    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
        checkFolder(folder);
        return loadFeed().get(id);
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        checkFolder(folder);
        final List<MessagingMessage> messages = new ArrayList<MessagingMessage>(messageIds.length);
        for (final String id : messageIds) {
            messages.add(getMessage(folder, id, true));
        }
        return messages;
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public MessagingMessage perform(final String folder, final String id, final String action) throws OXException {
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final String action) throws OXException {
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final MessagingMessage message, final String action) throws OXException {
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
        checkFolder(folder);
        List<SyndMessage> messages = loadFeed().getMessages();

        messages = filter(messages, searchTerm);
        sort(messages, sortField, order);
        messages = sublist(messages, indexRange);

        return new ArrayList<MessagingMessage>(messages);
    }

    private List<SyndMessage> sublist(final List<SyndMessage> messages, final IndexRange indexRange) {
        if(indexRange == null) {
            return messages;
        }
        int start = Math.min(indexRange.getStart(), messages.size()-1);
        int end = Math.min(indexRange.getEnd(), messages.size()-1);

        if (start < 0) {
            start = 0;
        }

        if(end < 0) {
            end = 0;
        }

        return messages.subList(start, end);
    }

    private void sort(final List<SyndMessage> messages, final MessagingField sortField, final OrderDirection order) throws OXException {
        if(sortField == null) {
            return;
        }
        final MessagingComparator comparator = new MessagingComparator(sortField, null);
        try {
            Collections.sort(messages, comparator);
            if(order == OrderDirection.DESC) {
                Collections.reverse(messages);
            }
        } catch (final RuntimeException x) {
            final Throwable cause = x.getCause();
            if(OXException.class.isInstance(cause)) {
                throw (OXException) cause;
            }
            throw new OXException(x);
        }
    }

    private List<SyndMessage> filter(final List<SyndMessage> messages, final SearchTerm<?> searchTerm) throws OXException {
        if (searchTerm == null) {
            return messages;
        }

        List<SyndMessage> list = new ArrayList<SyndMessage>(messages.size());
        for (SyndMessage syndMessage : messages) {
            if (searchTerm.matches(syndMessage)) {
                list.add(syndMessage);
            }
        }
        return list;
    }

    @Override
    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    private FeedAdapter loadFeed() throws OXException {
        if(feed != null) {
            return feed;
        }
        final String url = (String) accounts.getAccount(accountId, session).getConfiguration().get("url");

        try {
            return feed = new FeedAdapter(retrieveFeed(new URL(url)), "", session);
        } catch (final Exception e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * The {@link HtmlCleaner} constant which is safe being used by multiple threads as of <a
     * href="http://htmlcleaner.sourceforge.net/javause.php#example2">this example</a>.
     */
    private static final HtmlCleaner HTML_CLEANER;

    /**
     * The {@link Serializer} constant which is safe being used by multiple threads as of <a
     * href="http://htmlcleaner.sourceforge.net/javause.php#example2">this example</a>.
     */
    private static final Serializer SERIALIZER;

    static {
        final CleanerProperties props = new CleanerProperties();
        /*-
         *
        props.setOmitDoctypeDeclaration(true);
        props.setOmitXmlDeclaration(true);
        props.setTransSpecialEntitiesToNCR(true);
        props.setTransResCharsToNCR(true);
        props.setRecognizeUnicodeChars(false);
        props.setUseEmptyElementTags(false);
        props.setIgnoreQuestAndExclam(false);
        props.setUseCdataForScriptAndStyle(false);
        props.setIgnoreQuestAndExclam(true);
         *
         */
        HTML_CLEANER = new HtmlCleaner(props);
        SERIALIZER = new SimpleXmlSerializer(props);
    }

    private SyndFeed retrieveFeed(final URL url) throws IOException, FeedException, FetcherException {
        try {
            return feedFetcher.retrieveFeed(url);
        } catch (final com.sun.syndication.io.ParsingFeedException e) {
            // Retry using InputSource class
            InputStream in = null;
            try {
                final SyndFeedInput input = new SyndFeedInput();
                final URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(2500);
                urlConnection.setReadTimeout(2500);
                urlConnection.connect();
                in = urlConnection.getInputStream();
                /*
                 * Read from stream
                 */
                final int initLen = 8192;
                ByteArrayOutputStream tmp = new ByteArrayOutputStream(initLen);
                final int blen = 2048;
                final byte[] buf = new byte[blen];
                for (int read; (read = in.read(buf, 0, blen)) > 0;) {
                    tmp.write(buf, 0, read);
                }
                /*
                 * Ensure well-formed
                 */
                final TagNode htmlNode = HTML_CLEANER.clean(new String(tmp.toByteArray())); // No charset needed???
                tmp = null;
                final AllocatingStringWriter writer = new AllocatingStringWriter(initLen);
                SERIALIZER.write(htmlNode, writer, "UTF-8");
                final StringBuilder builder = writer.getAllocator();
                /*
                 * Return feed
                 */
                return input.build(new InputSource(new UnsynchronizedStringReader(builder.toString())));
            } finally {
                Streams.close(in);
            }
        }
    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        throw new UnsupportedOperationException();
    }


}
