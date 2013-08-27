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

package com.openexchange.messaging.facebook;

import static com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.fireFQLJsonQuery;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.w3c.dom.Element;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.parser.group.FacebookFQLGroupJsonParser;
import com.openexchange.messaging.facebook.parser.page.FacebookFQLPageJsonParser;
import com.openexchange.messaging.facebook.parser.stream.FacebookFQLStreamJsonParser;
import com.openexchange.messaging.facebook.parser.stream.FacebookFQLStreamParser;
import com.openexchange.messaging.facebook.parser.user.FacebookFQLUserJsonParser;
import com.openexchange.messaging.facebook.services.Services;
import com.openexchange.messaging.facebook.session.FacebookOAuthAccess;
import com.openexchange.messaging.facebook.utility.FacebookGroup;
import com.openexchange.messaging.facebook.utility.FacebookMessagingMessage;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.FQLQuery;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.FQLQueryType;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.StaticFiller;
import com.openexchange.messaging.facebook.utility.FacebookPage;
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

    private static final String FB_IN_FEED = "fb_in_feed";

    private static Boolean ignoreFbInFeed;
    private static boolean ignoreFbInFeed() {
        Boolean tmp = ignoreFbInFeed;
        if (null == tmp) {
            synchronized (FacebookMessagingMessageAccess.class) {
                tmp = ignoreFbInFeed;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return true;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.messaging.facebook.ignoreFbInFeed", true));
                    ignoreFbInFeed = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private boolean retrySafetyCheck;

    /**
     * Initializes a new {@link FacebookMessagingMessageAccess}.
     *
     * @param facebookOAuthAccess The Facebook OAuth access
     * @param messagingAccount The Facebook messaging account
     * @param session The associated session
     */
    public FacebookMessagingMessageAccess(final FacebookOAuthAccess facebookOAuthAccess, final MessagingAccount messagingAccount, final Session session) {
        super(facebookOAuthAccess, messagingAccount, session);
        retrySafetyCheck = false;
    }

    /**
     * Sets whether to retry requesting failed user data.
     *
     * @param retrySafetyCheck <code>true</code> to retry requesting failed user data; otherwise <code>false</code>
     * @return This Facebook message access with new behavior applied
     */
    public FacebookMessagingMessageAccess setRetrySafetyCheck(final boolean retrySafetyCheck) {
        this.retrySafetyCheck = retrySafetyCheck;
        return this;
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

    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
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

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
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

    private static final EnumSet<MessagingField> SET_FULL = EnumSet.of(MessagingField.FULL);

    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
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
            final FQLQuery query =
                FacebookMessagingUtility.composeFQLStreamQueryFor(FQLQueryType.queryTypeFor(folder), SET_FULL, facebookUserId);
            final List<JSONObject> results = fireFQLJsonQuery(query.getCharSequence(), facebookOAuthAccess);
            message = FacebookFQLStreamJsonParser.parseStreamJsonElement(results.iterator().next(), getUserLocale(), session);
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
        if (message.isGroup()) {
            final List<JSONObject> results =
                fireFQLJsonQuery(new StringBuilder("SELECT name FROM group WHERE gid = ").append(message.getFromId()), facebookOAuthAccess);
            final FacebookGroup facebookGroup = FacebookFQLGroupJsonParser.parseGroupJsonElement(results.iterator().next());
            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, facebookGroup.getName(), Long.toString(facebookGroup.getGid())));
        } else {
            final List<JSONObject> results =
                fireFQLJsonQuery(new StringBuilder("SELECT name FROM user WHERE uid = ").append(message.getFromId()), facebookOAuthAccess);
            final FacebookUser facebookUser = FacebookFQLUserJsonParser.parseUserJsonElement(results.iterator().next());
            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, facebookUser.getName(), Long.toString(facebookUser.getUid())));
        }
        return message;
    }

    private static final EnumSet<MessagingField> SET_ID = EnumSet.of(MessagingField.ID);

    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
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
        final EnumSet<MessagingField> entityFieldSet = EnumSet.copyOf(fieldSet);
        entityFieldSet.retainAll(FacebookMessagingUtility.getEntityQueryableFields());
        /*
         * Static fillers
         */
        final List<StaticFiller> staticFillers = FacebookMessagingUtility.getStreamStaticFillers(fieldSet, this);
        if (fieldSet.contains(MessagingField.FOLDER_ID) || fieldSet.contains(MessagingField.FULL)) {
            staticFillers.add(new FacebookMessagingUtility.FolderFiller(folder));
        }
        /*
         * Ensure post_id is contained to maintain order
         */
        fieldSet.add(MessagingField.ID);
        /*
         * Perform FB query
         */
        final FQLQuery query = FacebookMessagingUtility.composeFQLStreamQueryFor(fieldSet, messageIds);
        final List<MessagingMessage> messages;
        if (null == query) {
            messages = new ArrayList<MessagingMessage>(messageIds.length);
            for (int i = 0; i < messageIds.length; i++) {
                final FacebookMessagingMessage message = new FacebookMessagingMessage(getUserLocale());
                for (final StaticFiller filler : staticFillers) {
                    filler.fill(message);
                }
                messages.add(message);
            }
        } else {
            final TLongObjectMap<List<FacebookMessagingMessage>> mUser;
            final TLongObjectMap<List<FacebookMessagingMessage>> mGroup;
            final TLongSet safetyCheckUser;
            final TLongSet safetyCheckGroup;
            {
                final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(query.getCharSequence(), facebookOAuthAccess);
                final int size = results.size();
                if (size != messageIds.length) {
                    final OXException warning =
                        FacebookMessagingExceptionCodes.FQL_QUERY_RESULT_MISMATCH.create(
                            Integer.valueOf(size),
                            Integer.valueOf(messageIds.length));
                    com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookMessagingMessageAccess.class)).debug(warning.getMessage(), warning);
                }
                final Iterator<JSONObject> iterator = results.iterator();
                final Map<String, FacebookMessagingMessage> orderMap = new HashMap<String, FacebookMessagingMessage>(size);
                if (entityFieldSet.isEmpty()) {
                    mUser = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                    mGroup = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                    safetyCheckUser = new TLongHashSet(0);
                    safetyCheckGroup = new TLongHashSet(0);
                    for (int i = 0; i < size; i++) {
                        final FacebookMessagingMessage message = parseFromElement(staticFillers, iterator.next());
                        /*
                         * Add to list/map
                         */
                        if (null != message) {
                            orderMap.put(message.getId(), message);
                        }
                    }
                } else { // Contains any
                    mUser = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                    mGroup = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                    safetyCheckUser = new TLongHashSet(size);
                    safetyCheckGroup = new TLongHashSet(size);
                    for (int i = 0; i < size; i++) {
                        final FacebookMessagingMessage message = parseFromElement(staticFillers, iterator.next());
                        /*
                         * Add to list/map
                         */
                        if (null != message) {
                            orderMap.put(message.getId(), message);
                            final long facebookId = message.getFromId();
                            if (message.isGroup()) {
                                List<FacebookMessagingMessage> l = mGroup.get(facebookId);
                                if (null == l) {
                                    l = new ArrayList<FacebookMessagingMessage>(4);
                                    mGroup.put(facebookId, l);
                                    safetyCheckGroup.add(facebookId);
                                }
                                l.add(message);
                            } else {
                                List<FacebookMessagingMessage> l = mUser.get(facebookId);
                                if (null == l) {
                                    l = new ArrayList<FacebookMessagingMessage>(4);
                                    mUser.put(facebookId, l);
                                    safetyCheckUser.add(facebookId);
                                }
                                l.add(message);
                            }
                        }
                    }
                }
                /*
                 * Fill in proper order
                 */
                messages = new ArrayList<MessagingMessage>(size);
                for (final String messageId : messageIds) {
                    messages.add(orderMap.get(messageId));
                }
            }
            /*
             * Replace from with proper user name
             */
            if (!mUser.isEmpty()) {
                final FQLQuery userQuery = FacebookMessagingUtility.composeFQLUserQueryFor(entityFieldSet, mUser.keys());
                /*
                 * Fire FQL query
                 */
                final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(userQuery.getCharSequence(), facebookOAuthAccess);
                final Iterator<JSONObject> iterator = results.iterator();
                final int resSize = results.size();
                for (int i = 0; i < resSize; i++) {
                    final FacebookUser facebookUser = FacebookFQLUserJsonParser.parseUserJsonElement(iterator.next());
                    final String name = facebookUser.getName();
                    final long facebookUserId = facebookUser.getUid();
                    final String userIdStr = Long.toString(facebookUserId);
                    final String picSmall = facebookUser.getPicSmall();
                    for (final FacebookMessagingMessage message : mUser.get(facebookUserId)) {
                        message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, name, userIdStr));
                        message.setPicture(picSmall);
                    }
                    /*
                     * Remove from safety check
                     */
                    safetyCheckUser.remove(facebookUserId);
                }
                /*
                 * Check if any user is missing
                 */
                check(safetyCheckUser, entityFieldSet, mUser, messages, false);
            }
            /*
             * Replace from with proper group name
             */
            if (!mGroup.isEmpty()) {
                final FQLQuery userQuery = FacebookMessagingUtility.composeFQLGroupQueryFor(entityFieldSet, mGroup.keys());
                /*
                 * Fire FQL query
                 */
                final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(userQuery.getCharSequence(), facebookOAuthAccess);
                final Iterator<JSONObject> iterator = results.iterator();
                final int resSize = results.size();
                for (int i = 0; i < resSize; i++) {
                    final FacebookGroup facebookGroup = FacebookFQLGroupJsonParser.parseGroupJsonElement(iterator.next());
                    final String name = facebookGroup.getName();
                    final long facebookGroupId = facebookGroup.getGid();
                    final String groupIdStr = Long.toString(facebookGroupId);
                    final String picSmall = facebookGroup.getPicSmall();
                    for (final FacebookMessagingMessage message : mGroup.get(facebookGroupId)) {
                        message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, name, groupIdStr));
                        message.setPicture(picSmall);
                    }
                    /*
                     * Remove from safety check
                     */
                    safetyCheckGroup.remove(facebookGroupId);
                }
                /*
                 * Check if any group is missing
                 */
                check(safetyCheckGroup, entityFieldSet, mGroup, messages, true);
            }
        }
        /*
         * Return
         */
        return messages;
    }

    private FacebookMessagingMessage parseFromElement(final List<StaticFiller> staticFillers, final Element element) throws OXException {
        final FacebookMessagingMessage message = FacebookFQLStreamParser.parseStreamDOMElement(element, getUserLocale(), session);
        if (null != message) {
            for (final StaticFiller filler : staticFillers) {
                filler.fill(message);
            }
        }
        return message;
    }

    private FacebookMessagingMessage parseFromElement(final List<StaticFiller> staticFillers, final JSONObject element) throws OXException {
        final FacebookMessagingMessage message = FacebookFQLStreamJsonParser.parseStreamJsonElement(element, getUserLocale(), session);
        if (null != message) {
            for (final StaticFiller filler : staticFillers) {
                filler.fill(message);
            }
        }
        return message;
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
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

    @Override
    public MessagingMessage perform(final String folder, final String id, final String action) throws OXException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final String action) throws OXException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final MessagingMessage message, final String action) throws OXException {
        if (FacebookConstants.TYPE_UPDATE_STATUS.equalsIgnoreCase(action)) {
            try {
                final StringContent content = FacebookMessagingUtility.checkContent(StringContent.class, message);
                /*
                 * Post it
                 */
                final OAuthRequest request =
                    new OAuthRequest(
                        Verb.POST,
                        "https://graph.facebook.com/" + facebookUserId + "/feed?message=" + encode(content.getData()));
                facebookOAuthAccess.getFacebookOAuthService().signRequest(facebookOAuthAccess.getFacebookAccessToken(), request);
                final Response response = request.send();
                final JSONObject result = new JSONObject(response.getBody());
                if (result.has("error")) {
                    final JSONObject error = result.getJSONObject("error");
                    final String type = error.optString("type");
                    final String msg = error.optString("message");
                    if ("OAuthException".equals(type)) {
                        throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == msg ? "" : msg);
                    }
                    throw FacebookMessagingExceptionCodes.FQL_ERROR.create(null == type ? "<unknown>" : type, null == msg ? "" : msg);
                }
                return null;
            } catch (final JSONException e) {
                throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            } catch (final OXException e) {
                throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
            } catch (final Exception e) {
                throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else if (FacebookConstants.TYPE_POST.equalsIgnoreCase(action)) {
            FacebookMessagingAccountTransport.transport(
                message,
                Collections.<MessagingAddressHeader> emptyList(),
                facebookOAuthAccess,
                facebookUserId);
            return null;
        }
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
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
        // Always request user/group to ignore advertisement "FB_In_Feed"
        final boolean ignoreFbInFeed = ignoreFbInFeed();
        final EnumSet<MessagingField> entityFieldSet;
        if (ignoreFbInFeed) {
            entityFieldSet = FacebookMessagingUtility.getEntityQueryableFields();
        } else {
            entityFieldSet = EnumSet.copyOf(fieldSet);
            entityFieldSet.retainAll(FacebookMessagingUtility.getEntityQueryableFields());
        }
        if (null != searchTerm) {
            searchTerm.addMessagingField(fieldSet);
        }
        /*
         * Ensure "created_time" is set in Facebook messages
         */
        fieldSet.add(MessagingField.RECEIVED_DATE);
        /*
         * Static fillers
         */
        final List<StaticFiller> staticFillers = FacebookMessagingUtility.getStreamStaticFillers(fieldSet, this);
        if (fieldSet.contains(MessagingField.FOLDER_ID) || fieldSet.contains(MessagingField.FULL)) {
            staticFillers.add(new FacebookMessagingUtility.FolderFiller(folder));
        }
        /*
         * Query; must not be null to determine proper number of wall posts
         */
        final FQLQueryType queryType = FQLQueryType.queryTypeFor(folder);
        final FQLQuery query;
        if (EnumSet.copyOf(fieldSet).removeAll(FacebookMessagingUtility.getStreamQueryableFields())) { // Contains any
            query = FacebookMessagingUtility.composeFQLStreamQueryFor(queryType, fieldSet, sortField, order, facebookUserId);
        } else {
            query = FacebookMessagingUtility.composeFQLStreamQueryFor(queryType, SET_ID, sortField, order, facebookUserId);
        }
        final List<MessagingMessage> messages;
        final TLongObjectMap<List<FacebookMessagingMessage>> mUser;
        final TLongObjectMap<List<FacebookMessagingMessage>> mGroup;
        final TLongSet safetyCheckUser;
        final TLongSet safetyCheckGroup;
        long oldestCreatedTime = Long.MAX_VALUE;
        {
            final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(query.getCharSequence(), facebookOAuthAccess);
            final int size = results.size();
            if (size <= 0) {
                return Collections.<MessagingMessage> emptyList();
            }
            final Iterator<JSONObject> iterator = results.iterator();
            messages = new ArrayList<MessagingMessage>(size);
            if (entityFieldSet.isEmpty()) {
                mUser = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                mGroup = new TLongObjectHashMap<List<FacebookMessagingMessage>>(0);
                safetyCheckUser = new TLongHashSet(0);
                safetyCheckGroup = new TLongHashSet(0);
                for (int i = 0; i < size; i++) {
                    final FacebookMessagingMessage message = parseFromElement(staticFillers, iterator.next());
                    if (null != message) {
                        /*
                         * Add to list
                         */
                        messages.add(message);
                        /*
                         * Check date
                         */
                        final long receivedDate = message.getReceivedDate();
                        if (oldestCreatedTime > receivedDate) {
                            oldestCreatedTime = receivedDate;
                        }
                    }
                }
            } else { // Contains any
                mUser = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                mGroup = new TLongObjectHashMap<List<FacebookMessagingMessage>>(size);
                safetyCheckUser = new TLongHashSet(size);
                safetyCheckGroup = new TLongHashSet(size);
                for (int i = 0; i < size; i++) {
                    final FacebookMessagingMessage message = parseFromElement(staticFillers, iterator.next());
                    /*
                     * Add to list/map
                     */
                    if (null != message) {
                        messages.add(message);
                        final long facebookId = message.getFromId();
                        if (message.isGroup()) {
                            List<FacebookMessagingMessage> l = mGroup.get(facebookId);
                            if (null == l) {
                                l = new ArrayList<FacebookMessagingMessage>(4);
                                mGroup.put(facebookId, l);
                                safetyCheckGroup.add(facebookId);
                            }
                            l.add(message);
                        } else {
                            List<FacebookMessagingMessage> l = mUser.get(facebookId);
                            if (null == l) {
                                l = new ArrayList<FacebookMessagingMessage>(4);
                                mUser.put(facebookId, l);
                                safetyCheckUser.add(facebookId);
                            }
                            l.add(message);
                        }
                        /*
                         * Check date
                         */
                        final long receivedDate = message.getReceivedDate();
                        if (oldestCreatedTime > receivedDate) {
                            oldestCreatedTime = receivedDate;
                        }
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
         * Store oldest timestamp
         */
        updateQueryTimestamp(queryType, oldestCreatedTime);
        /*
         * Replace from with proper user name
         */
        if (!mUser.isEmpty()) {
            final FQLQuery userQuery = FacebookMessagingUtility.composeFQLUserQueryFor(entityFieldSet, mUser.keys());
            /*
             * Fire FQL query
             */
            final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(userQuery.getCharSequence(), facebookOAuthAccess);
            final Iterator<JSONObject> iterator = results.iterator();
            final int resSize = results.size();
            for (int i = 0; i < resSize; i++) {
                final FacebookUser facebookUser = FacebookFQLUserJsonParser.parseUserJsonElement(iterator.next());
                final String name = facebookUser.getName();
                final long facebookUserId = facebookUser.getUid();
                if (ignoreFbInFeed && FB_IN_FEED.equals(toLowerCase(name))) {
                    // Drop messages from results list
                    for (final FacebookMessagingMessage message : mUser.get(facebookUserId)) {
                        messages.remove(message);
                    }
                } else {
                    final String userIdStr = Long.toString(facebookUserId);
                    final String picSmall = facebookUser.getPicSmall();
                    for (final FacebookMessagingMessage message : mUser.get(facebookUserId)) {
                        message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, name, userIdStr));
                        message.setPicture(picSmall);
                    }
                }
                /*
                 * Remove from safety check
                 */
                safetyCheckUser.remove(facebookUserId);
            }
            /*
             * Check if any user is missing
             */
            check(safetyCheckUser, entityFieldSet, mUser, messages, false);
        }
        /*
         * Replace from with proper group name
         */
        if (!mGroup.isEmpty()) {
            final FQLQuery groupQuery = FacebookMessagingUtility.composeFQLGroupQueryFor(entityFieldSet, mGroup.keys());
            /*
             * Fire FQL query
             */
            final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(groupQuery.getCharSequence(), facebookOAuthAccess);
            final Iterator<JSONObject> iterator = results.iterator();
            final int resSize = results.size();
            for (int i = 0; i < resSize; i++) {
                final FacebookGroup facebookGroup = FacebookFQLGroupJsonParser.parseGroupJsonElement(iterator.next());
                final String name = facebookGroup.getName();
                final long facebookGroupId = facebookGroup.getGid();
                if (ignoreFbInFeed && FB_IN_FEED.equals(toLowerCase(name))) {
                    // Drop messages from results list
                    for (final FacebookMessagingMessage message : mGroup.get(facebookGroupId)) {
                        messages.remove(message);
                    }
                } else {
                    final String groupIdStr = Long.toString(facebookGroupId);
                    final String picSmall = facebookGroup.getPicSmall();
                    for (final FacebookMessagingMessage message : mGroup.get(facebookGroupId)) {
                        message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, name, groupIdStr));
                        message.setPicture(picSmall);
                    }
                }
                /*
                 * Remove from safety check
                 */
                safetyCheckGroup.remove(facebookGroupId);
            }
            /*
             * Check if any group is missing
             */
            check(safetyCheckGroup, entityFieldSet, mGroup, messages, true);
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

    private void check(final TLongSet safetyCheck, final EnumSet<MessagingField> entityFieldSet, final TLongObjectMap<List<FacebookMessagingMessage>> m, final List<MessagingMessage> messages, final boolean group) throws OXException {
        if (!safetyCheck.isEmpty()) {
            /*
             * Check page table
             */
            entityFieldSet.add(MessagingField.FROM); // Ensure FROM is contained
            {
                final List<JSONObject> results =
                    FacebookMessagingUtility.fireFQLJsonQuery(
                        FacebookMessagingUtility.composeFQLPageQueryFor(entityFieldSet, safetyCheck.toArray()).getCharSequence(),
                        facebookOAuthAccess);
                if (!results.isEmpty()) {
                    final Iterator<JSONObject> iterator = results.iterator();
                    final int resSize = results.size();
                    for (int i = 0; i < resSize; i++) {
                        final FacebookPage fbPage = FacebookFQLPageJsonParser.parsePageJsonElement(iterator.next());
                        final long pageId = fbPage.getPageId();
                        safetyCheck.remove(pageId);
                        final String pageIdStr = Long.toString(pageId);
                        for (final FacebookMessagingMessage message : m.get(pageId)) {
                            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, fbPage.getName(), pageIdStr));
                            message.setPicture(fbPage.getPicSmall());
                        }
                    }
                }
            }
            /*
             * Check group table if non-group
             */
            if (!safetyCheck.isEmpty() && !group) {
                final List<JSONObject> results =
                    FacebookMessagingUtility.fireFQLJsonQuery(
                        FacebookMessagingUtility.composeFQLGroupQueryFor(entityFieldSet, safetyCheck.toArray()).getCharSequence(),
                        facebookOAuthAccess);
                if (!results.isEmpty()) {
                    final Iterator<JSONObject> iterator = results.iterator();
                    final int resSize = results.size();
                    for (int i = 0; i < resSize; i++) {
                        final FacebookGroup fbGroup = FacebookFQLGroupJsonParser.parseGroupJsonElement(iterator.next());
                        final long groupId = fbGroup.getGid();
                        safetyCheck.remove(groupId);
                        final String groupIdStr = Long.toString(groupId);
                        for (final FacebookMessagingMessage message : m.get(groupId)) {
                            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, fbGroup.getName(), groupIdStr));
                            message.setPicture(fbGroup.getPicSmall());
                        }
                    }
                }
            }
        }
        /*
         * Check if any entity is missing
         */
        if (!safetyCheck.isEmpty()) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookMessagingMessageAccess.class));
            if (logger.isWarnEnabled()) {
                logger.warn("Information of following Facebook " + (group ? "groups" : "users") + " are missing: " + Arrays.toString(safetyCheck.toArray()));
            }
            if (retrySafetyCheck) {
                for (final TLongIterator iter = safetyCheck.iterator(); iter.hasNext();) {
                    final long missingId = iter.next();
                    /*
                     * Remove from safety check
                     */
                    iter.remove();
                    /*
                     * Retry requesting user data
                     */
                    final FQLQuery fqlQuery;
                    if (group) {
                        fqlQuery = FacebookMessagingUtility.composeFQLGroupQueryFor(entityFieldSet, missingId);
                    } else {
                        fqlQuery = FacebookMessagingUtility.composeFQLUserQueryFor(entityFieldSet, missingId);
                    }
                    final List<JSONObject> results = FacebookMessagingUtility.fireFQLJsonQuery(fqlQuery.getCharSequence(), facebookOAuthAccess);
                    if (results.isEmpty()) {
                        /*
                         * Entity not visible
                         */
                        if (logger.isWarnEnabled()) {
                            logger.warn("FQL query delivered no result(s):\n" + fqlQuery.getCharSequence());
                        }
                        /*
                         * Remove corresponding messages.
                         */
                        messages.removeAll(m.remove(missingId));
                    } else {
                        /*
                         * Add missing user data to appropriate messages
                         */
                        final Iterator<JSONObject> iterator = results.iterator();
                        final int resSize = results.size();
                        if (group) {
                            for (int i = 0; i < resSize; i++) {
                                final FacebookGroup fbGroup = FacebookFQLGroupJsonParser.parseGroupJsonElement(iterator.next());
                                final long facebookGroupId = fbGroup.getGid();
                                final String groupIdStr = Long.toString(facebookGroupId);
                                for (final FacebookMessagingMessage message : m.get(facebookGroupId)) {
                                    message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, fbGroup.getName(), groupIdStr));
                                    message.setPicture(fbGroup.getPicSmall());
                                }
                            }
                        } else {
                            for (int i = 0; i < resSize; i++) {
                                final FacebookUser fbUser = FacebookFQLUserJsonParser.parseUserJsonElement(iterator.next());
                                final long facebookUserId = fbUser.getUid();
                                final String userIdStr = Long.toString(facebookUserId);
                                for (final FacebookMessagingMessage message : m.get(facebookUserId)) {
                                    message.setHeader(MimeAddressMessagingHeader.valueOfPlain(FROM, fbUser.getName(), userIdStr));
                                    message.setPicture(fbUser.getPicSmall());
                                }
                            }
                        }
                    }
                }
            } else {
                /*
                 * No retry. Just remove corresponding messages.
                 */
                for (final TLongIterator iter = safetyCheck.iterator(); iter.hasNext();) {
                    final long missingUserId = iter.next();
                    /*
                     * Remove from safety check
                     */
                    iter.remove();
                    /*
                     * Remove corresponding messages.
                     */

                    // {
                    // System.out.println("-- Affected Messages:");
                    // final List<FacebookMessagingMessage> list = m.get(missingUserId);
                    // for (final FacebookMessagingMessage fbMessagingMessage : list) {
                    // final FQLQuery fqlQuery =
                    // FacebookMessagingUtility.composeFQLStreamQueryFor(SET_FULL, fbMessagingMessage.getId());
                    // final List<Element> results =
                    // FacebookMessagingUtility.fireFQLQuery(fqlQuery.getCharSequence(), facebookOAuthAccess);
                    // System.out.println(Utility.prettyPrintXML(results.get(0)));
                    // }
                    // }

                    messages.removeAll(m.remove(missingUserId));
                }
            }
        }
    } // End of method check()

    @Override
    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws OXException {
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
     * Gets the Facebook user identifier.
     *
     * @return The Facebook user identifier
     */
    public String getFacebookUserId() {
        return facebookUserId;
    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        throw new UnsupportedOperationException();
    }

    private Long getQueryTimestamp(final FQLQueryType queryType) {
        final String tmp = (String) messagingAccount.getConfiguration().get(queryType.toString());
        return null == tmp ? null : Long.valueOf(tmp);
    }

    private void updateQueryTimestamp(final FQLQueryType queryType, final long timestamp) throws OXException {
        messagingAccount.getConfiguration().put(queryType.toString(), Long.toString(timestamp));
        messagingAccount.getMessagingService().getAccountManager().updateAccount(messagingAccount, session);
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
