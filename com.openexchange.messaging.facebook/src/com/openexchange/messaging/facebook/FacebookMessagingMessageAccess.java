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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import static com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.fireFQLQuery;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongObjectHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.IFacebookRestClient;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.parser.stream.FacebookFQLStreamParser;
import com.openexchange.messaging.facebook.parser.user.FacebookFQLUserParser;
import com.openexchange.messaging.facebook.utility.FacebookMessagingMessage;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.Query;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.QueryType;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.StaticFiller;
import com.openexchange.messaging.facebook.utility.FacebookUser;
import com.openexchange.messaging.generic.AttachmentFinderHandler;
import com.openexchange.messaging.generic.MessageParser;
import com.openexchange.messaging.generic.MessagingComparator;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.session.Session;

/**
 * {@link FacebookMessagingMessageAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookMessagingMessageAccess extends AbstractFacebookAccess implements MessagingMessageAccess {

    private static final String FROM = MessagingHeader.KnownHeader.FROM.toString();

    /**
     * Initializes a new {@link FacebookMessagingMessageAccess}.
     * 
     * @param facebookRestClient The facebook REST client
     * @param messagingAccount The facebook messaging account
     * @param session The session
     * @param facebookUserId The facebook user identifier
     * @param facebookSession The facebook session identifier
     */
    public FacebookMessagingMessageAccess(final IFacebookRestClient<Object> facebookRestClient, final MessagingAccount messagingAccount, final Session session, final long facebookUserId, final String facebookSession) {
        super(facebookRestClient, messagingAccount, session, facebookUserId, facebookSession);
    }

    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws MessagingException {
        final AttachmentFinderHandler handler = new AttachmentFinderHandler(sectionId);
        new MessageParser().parseMessage(getMessage(folder, messageId, true), handler);
        final MessagingPart part = handler.getMessagingPart();
        if (null == part) {
            throw MessagingExceptionCodes.ATTACHMENT_NOT_FOUND.create(sectionId, messageId, folder);
        }
        return part;
    }

    public void appendMessages(final String folder, final MessagingMessage[] messages) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(sourceFolder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                sourceFolder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        if (!KNOWN_FOLDER_IDS.contains(destFolder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                destFolder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    private static final MessagingField[] FIELDS_FULL = { MessagingField.FULL };

    private static final EnumSet<MessagingField> SET_FULL = EnumSet.of(MessagingField.FULL);

    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws MessagingException {
        final FacebookMessagingMessage message;
        {
            /*
             * Static fillers
             */
            final List<StaticFiller> staticFillers = FacebookMessagingUtility.getStreamStaticFillers(SET_FULL, this);
            staticFillers.add(new FacebookMessagingUtility.FolderFiller(folder));
            /*
             * Query
             */
            final Query query =
                FacebookMessagingUtility.composeFQLStreamQueryFor(QueryType.queryTypeFor(folder), FIELDS_FULL, facebookUserId);
            final List<Object> results = fireFQLQuery(query.getCharSequence(), facebookRestClient);
            message = FacebookFQLStreamParser.parseStreamDOMElement((Element) results.iterator().next());
            if (null == message) {
                throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, folder);
            }
            /*
             * Add static fields
             */
            for (final StaticFiller filler : staticFillers) {
                filler.fill(message);
            }
        }
        /*
         * Replace from with proper user name
         */
        {
            final List<Object> results =
                fireFQLQuery(new StringBuilder("SELECT name FROM user WHERE uid = ").append(message.getFromUserId()), facebookRestClient);
            final FacebookUser facebookUser = FacebookFQLUserParser.parseUserDOMElement((Element) results.iterator().next());
            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, facebookUser.getName(), String.valueOf(facebookUser.getUid())));
        }
        return message;
    }

    private static final MessagingField[] FIELDS_ID = { MessagingField.ID };

    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws MessagingException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        if ((null == messageIds || 0 == messageIds.length) || (null == fields || 0 == fields.length)) {
            return Collections.emptyList();
        }
        final EnumSet<MessagingField> fieldSet = EnumSet.copyOf(Arrays.asList(fields));
        final EnumSet<MessagingField> userFieldSet = EnumSet.copyOf(fieldSet);
        userFieldSet.retainAll(FacebookMessagingUtility.getUserQueryableFields());
        /*
         * Static fillers
         */
        final List<StaticFiller> staticFillers = FacebookMessagingUtility.getStreamStaticFillers(fieldSet, this);
        if (fieldSet.contains(MessagingField.FOLDER_ID) || fieldSet.contains(MessagingField.FULL)) {
            staticFillers.add(new FacebookMessagingUtility.FolderFiller(folder));
        }
        /*
         * Query; Ensure post_id is contained to maintain order
         */
        final Query query;
        if (fieldSet.contains(MessagingField.ID)) { // Contains post_id
            query = FacebookMessagingUtility.composeFQLStreamQueryFor(QueryType.queryTypeFor(folder), fields, messageIds, facebookUserId);
        } else {
            final MessagingField[] arg = new MessagingField[fields.length + 1];
            arg[0] = MessagingField.ID;
            System.arraycopy(fields, 0, arg, 1, fields.length);
            query = FacebookMessagingUtility.composeFQLStreamQueryFor(QueryType.queryTypeFor(folder), arg, messageIds, facebookUserId);
        }
        final List<MessagingMessage> messages;
        if (null != query) {
            final TLongObjectHashMap<List<FacebookMessagingMessage>> m;
            final TLongHashSet safetyCheck;
            {
                final List<Object> results = FacebookMessagingUtility.fireFQLQuery(query.getCharSequence(), facebookRestClient);
                final int size = results.size();
                if (size != messageIds.length) {
                    final FacebookMessagingException warning =
                        FacebookMessagingExceptionCodes.FQL_QUERY_RESULT_MISMATCH.create(
                            Integer.valueOf(size),
                            Integer.valueOf(messageIds.length));
                    org.apache.commons.logging.LogFactory.getLog(FacebookMessagingMessageAccess.class).warn(warning.getMessage(), warning);
                }
                final Iterator<Object> iterator = results.iterator();
                final Map<String, FacebookMessagingMessage> orderMap = new HashMap<String, FacebookMessagingMessage>(size);
                if (userFieldSet.isEmpty()) {
                    m = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                    safetyCheck = new TLongHashSet(0);
                    for (int i = 0; i < size; i++) {
                        final FacebookMessagingMessage message = parseFromElement(staticFillers, (Element) iterator.next());
                        /*
                         * Add to list/map
                         */
                        if (null != message) {
                            orderMap.put(message.getId(), message);
                        }
                    }
                } else { // Contains any
                    m = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                    safetyCheck = new TLongHashSet(size);
                    for (int i = 0; i < size; i++) {
                        final FacebookMessagingMessage message = parseFromElement(staticFillers, (Element) iterator.next());
                        /*
                         * Add to list/map
                         */
                        if (null != message) {
                            orderMap.put(message.getId(), message);
                            final long facebookUserId = message.getFromUserId();
                            List<FacebookMessagingMessage> l = m.get(facebookUserId);
                            if (null == l) {
                                l = new ArrayList<FacebookMessagingMessage>(4);
                                m.put(facebookUserId, l);
                                safetyCheck.add(facebookUserId);
                            }
                            l.add(message);
                        }
                    }
                }
                /*
                 * Fill in proper order
                 */
                messages = new ArrayList<MessagingMessage>(size);
                for (int i = 0; i < messageIds.length; i++) {
                    messages.add(orderMap.get(messageIds[i]));
                }
            }
            /*
             * Replace from with proper user name
             */
            if (!m.isEmpty()) {
                final Query userQuery =
                    FacebookMessagingUtility.composeFQLUserQueryFor(userFieldSet.toArray(new MessagingField[userFieldSet.size()]), m.keys());
                /*
                 * Fire FQL query
                 */
                final List<Object> results = FacebookMessagingUtility.fireFQLQuery(userQuery.getCharSequence(), facebookRestClient);
                final Iterator<Object> iterator = results.iterator();
                final int resSize = results.size();
                for (int i = 0; i < resSize; i++) {
                    final FacebookUser facebookUser = FacebookFQLUserParser.parseUserDOMElement((Element) iterator.next());
                    final long facebookUserId = facebookUser.getUid();
                    final String userIdStr = String.valueOf(facebookUserId);
                    for (final FacebookMessagingMessage message : m.get(facebookUserId)) {
                        message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, facebookUser.getName(), userIdStr));
                        message.setPicture(facebookUser.getPicSmall());
                    }
                    /*
                     * Remove from safety check
                     */
                    safetyCheck.remove(facebookUserId);
                }
                /*
                 * Check if any user is missing
                 */
                if (!safetyCheck.isEmpty()) {
                    final Log logger = LogFactory.getLog(FacebookMessagingMessageAccess.class);
                    if (logger.isWarnEnabled()) {
                        logger.warn("Information of following Facebook users are missing: " + Arrays.toString(safetyCheck.toArray()));
                    }
                }
            }
        } else {
            messages = new ArrayList<MessagingMessage>(messageIds.length);
            for (int i = 0; i < messageIds.length; i++) {
                final FacebookMessagingMessage message = new FacebookMessagingMessage();
                for (final StaticFiller filler : staticFillers) {
                    filler.fill(message);
                }
                messages.add(message);
            }
        }
        /*
         * Return
         */
        return messages;
    }

    private static FacebookMessagingMessage parseFromElement(final List<StaticFiller> staticFillers, final Element element) throws MessagingException {
        final FacebookMessagingMessage message = FacebookFQLStreamParser.parseStreamDOMElement(element);
        if (null != message) {
            for (final StaticFiller filler : staticFillers) {
                filler.fill(message);
            }
        }
        return message;
    }

    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(sourceFolder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                sourceFolder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        if (!KNOWN_FOLDER_IDS.contains(destFolder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                destFolder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    public MessagingMessage perform(final String folder, final String id, final String action) throws MessagingException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    public MessagingMessage perform(final String action) throws MessagingException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    public MessagingMessage perform(final MessagingMessage message, final String action) throws MessagingException {
        if (FacebookConstants.TYPE_UPDATE_STATUS.equalsIgnoreCase(action)) {
            try {
                final StringContent content = FacebookMessagingUtility.checkContent(StringContent.class, message);
                facebookRestClient.users_setStatus(content.getData());
                return null;
            } catch (final FacebookException e) {
                throw FacebookMessagingException.create(e);
            }
        } else if (FacebookConstants.TYPE_POST.equalsIgnoreCase(action)) {
            FacebookMessagingAccountTransport.transport(
                message,
                Collections.<MessagingAddressHeader> emptyList(),
                facebookRestClient,
                facebookUserId);
            return null;
        }
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        if (null == fields || 0 == fields.length) {
            return Collections.emptyList();
        }
        final EnumSet<MessagingField> fieldSet = EnumSet.copyOf(Arrays.asList(fields));
        final EnumSet<MessagingField> userFieldSet = EnumSet.copyOf(fieldSet);
        userFieldSet.retainAll(FacebookMessagingUtility.getUserQueryableFields());
        if (null != searchTerm) {
            searchTerm.addMessagingField(fieldSet);
        }
        /*
         * Static fillers
         */
        final MessagingField[] daFields = fieldSet.toArray(new MessagingField[fieldSet.size()]);
        final List<StaticFiller> staticFillers = FacebookMessagingUtility.getStreamStaticFillers(daFields, this);
        if (fieldSet.contains(MessagingField.FOLDER_ID) || fieldSet.contains(MessagingField.FULL)) {
            staticFillers.add(new FacebookMessagingUtility.FolderFiller(folder));
        }
        /*
         * Query; must not be null to determine proper number of wall posts
         */
        final Query query;
        if (EnumSet.copyOf(fieldSet).removeAll(FacebookMessagingUtility.getStreamQueryableFields())) { // Contains any
            query =
                FacebookMessagingUtility.composeFQLStreamQueryFor(
                    QueryType.queryTypeFor(folder),
                    daFields,
                    sortField,
                    order,
                    facebookUserId);
        } else {
            query =
                FacebookMessagingUtility.composeFQLStreamQueryFor(
                    QueryType.queryTypeFor(folder),
                    FIELDS_ID,
                    sortField,
                    order,
                    facebookUserId);
        }
        final List<MessagingMessage> messages;
        final TLongObjectHashMap<List<FacebookMessagingMessage>> m;
        final TLongHashSet safetyCheck;
        {
            final List<Object> results = FacebookMessagingUtility.fireFQLQuery(query.getCharSequence(), facebookRestClient);
            final int size = results.size();
            final Iterator<Object> iterator = results.iterator();
            messages = new ArrayList<MessagingMessage>(size);
            if (userFieldSet.isEmpty()) {
                m = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                safetyCheck = new TLongHashSet(0);
                for (int i = 0; i < size; i++) {
                    final FacebookMessagingMessage message = parseFromElement(staticFillers, (Element) iterator.next());
                    if (null != message) {
                        /*
                         * Add to list
                         */
                        messages.add(message);
                    }
                }
            } else { // Contains any
                m = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                safetyCheck = new TLongHashSet(size);
                for (int i = 0; i < size; i++) {
                    final FacebookMessagingMessage message = parseFromElement(staticFillers, (Element) iterator.next());
                    /*
                     * Add to list/map
                     */
                    if (null != message) {
                        messages.add(message);
                        final long facebookUserId = message.getFromUserId();
                        List<FacebookMessagingMessage> l = m.get(facebookUserId);
                        if (null == l) {
                            l = new ArrayList<FacebookMessagingMessage>(4);
                            m.put(facebookUserId, l);
                            safetyCheck.add(facebookUserId);
                        }
                        l.add(message);
                    }
                }
            }
        }
        /*
         * Empty?
         */
        if (messages.isEmpty()) {
            return messages;
        }
        /*
         * Replace from with proper user name
         */
        if (!m.isEmpty()) {
            final Query userQuery =
                FacebookMessagingUtility.composeFQLUserQueryFor(userFieldSet.toArray(new MessagingField[userFieldSet.size()]), m.keys());
            /*
             * Fire FQL query
             */
            final List<Object> results = FacebookMessagingUtility.fireFQLQuery(userQuery.getCharSequence(), facebookRestClient);
            final Iterator<Object> iterator = results.iterator();
            final int resSize = results.size();
            for (int i = 0; i < resSize; i++) {
                final FacebookUser facebookUser = FacebookFQLUserParser.parseUserDOMElement((Element) iterator.next());
                final long facebookUserId = facebookUser.getUid();
                final String userIdStr = String.valueOf(facebookUserId);
                for (final FacebookMessagingMessage message : m.get(facebookUserId)) {
                    message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, facebookUser.getName(), userIdStr));
                    message.setPicture(facebookUser.getPicSmall());
                }
                /*
                 * Remove from safety check
                 */
                safetyCheck.remove(facebookUserId);
            }
            /*
             * Check if any user is missing
             */
            if (!safetyCheck.isEmpty()) {
                final Log logger = LogFactory.getLog(FacebookMessagingMessageAccess.class);
                if (logger.isWarnEnabled()) {
                    logger.warn("Information of following Facebook users are missing: " + Arrays.toString(safetyCheck.toArray()));
                }
            }
        }
        /*
         * Filter?
         */
        if (null != searchTerm) {
            for (final Iterator<MessagingMessage> iter = messages.iterator(); iter.hasNext();) {
                final MessagingMessage message = iter.next();
                if (!searchTerm.matches(message)) {
                    iter.remove();
                }
            }
        }
        /*
         * Already sorted by query itself?
         */
        if (!query.containsOrderBy() && null != sortField) {
            /*
             * Sort manually
             */
            Collections.sort(messages, new MessagingComparator(sortField, OrderDirection.DESC.equals(order), getUserLocale()));
        }
        /*
         * Range specified?
         */
        if (null == indexRange) {
            /*
             * Return
             */
            return messages;
        }
        final int fromIndex = indexRange.start;
        int toIndex = indexRange.end;
        final int size = messages.size();
        if ((fromIndex) > size) {
            /*
             * Return empty list if start is out of range
             */
            return Collections.emptyList();
        }
        /*
         * Reset end index if out of range
         */
        if (toIndex >= size) {
            toIndex = size;
        }
        return messages.subList(fromIndex, toIndex);
    }

    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws MessagingException {
        if (!KNOWN_FOLDER_IDS.contains(message.getFolder())) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                message.getFolder(),
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    /**
     * Gets the messaging account.
     * 
     * @return The messaging account
     */
    public MessagingAccount getMessagingAccount() {
        return messagingAccount;
    }

    /**
     * Gets the facebook user identifier.
     * 
     * @return The facebook user identifier
     */
    public long getFacebookUserId() {
        return facebookUserId;
    }

    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws MessagingException {
        throw new UnsupportedOperationException();
    }

}
