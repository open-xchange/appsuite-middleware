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

package com.openexchange.dav.push;

import com.google.common.io.BaseEncoding;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link DAVPushUtility}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class DAVPushUtility {

    /** The push client identifier for CardDAV */
    public static final String CLIENT_CARDDAV = Interface.CARDDAV.toString().toLowerCase();

    /** The push client identifier for CalDAV */
    public static final String CLIENT_CALDAV = Interface.CALDAV.toString().toLowerCase();

    /**
     * Extracts the client identifier from the subscription resource's name.
     *
     * @param subscriptionResourceName The subscription resource name (i.e. the last part of the resource's WebDAV path)
     * @return The client identifier, or <code>null</code> if no valid client identifier can be extracted
     */
    public static String getClientId(String subscriptionResourceName) {
        if (Strings.isNotEmpty(subscriptionResourceName)) {
            int idx = subscriptionResourceName.indexOf('-');
            if (-1 != idx) {
                String clientId = subscriptionResourceName.substring(0, idx);
                if (CLIENT_CALDAV.equals(clientId) || CLIENT_CARDDAV.equals(clientId)) {
                    return clientId;
                }
            }
        }
        return null;
    }

    /**
     * Extracts the transport identifier from the subscription resource's name.
     *
     * @param subscriptionResourceName The subscription resource name (i.e. the last part of the resource's WebDAV path)
     * @return The transport identifier, or <code>null</code> if no valid transport identifier can be extracted
     */
    public static String getTransportId(String subscriptionResourceName) {
        if (Strings.isNotEmpty(subscriptionResourceName)) {
            int idx = subscriptionResourceName.indexOf('-');
            if (-1 != idx && idx < subscriptionResourceName.length() - 1) {
                String value = subscriptionResourceName.substring(idx + 1);
                if (null != KnownTransport.knownTransportFor(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Extracts the (internal) push topic from a client-supplied push key string, implicitly verifying the encoded target user.
     *
     * @param pushKey The push key to extract the topic from
     * @param expectedContextId The expected context identifier
     * @param expectedUserId The expected user identifier
     * @return The topic
     * @throws OXException {@link PushExceptionCodes#INVALID_TOPIC}
     */
    public static String extractTopic(String pushKey, int expectedContextId, int expectedUserId) throws OXException {
        try {
            String decoded = new String(BaseEncoding.base64Url().omitPadding().decode(pushKey), Charsets.UTF_8);
            int idx1 = decoded.indexOf(':');
            if (-1 != idx1 && idx1 < decoded.length() - 1 && expectedContextId == Integer.parseInt(decoded.substring(0, idx1))) {
                int idx2 = decoded.indexOf(':', idx1 + 1);
                if (-1 != idx2 && idx2 < decoded.length() - 1 && expectedUserId == Integer.parseInt(decoded.substring(idx1 + 1, idx2))) {
                    return decoded.substring(idx2 + 1);
                }
            }
        } catch (IllegalArgumentException e) {
            throw PushExceptionCodes.INVALID_TOPIC.create(pushKey, e);
        }
        throw PushExceptionCodes.INVALID_TOPIC.create(pushKey);
    }

    /**
     * Gets the push subscription URL to indicate to clients.
     *
     * @param clientId The targeted push client identifier
     * @param transportId The targeted push transport identifier
     * @return The subscription URL, or <code>null</code> if passed client identifier was <code>null</code>
     */
    public static String getSubscriptionURL(String clientId, String transportId) {
        return null != clientId ? "/subscribe/" + clientId + '-' + transportId : null;
    }

    /**
     * Derives the client identifier appropriate for a specific WebDAV resource.
     *
     * @param resource The WebDAV resource to get the client identifier for
     * @return The client identifier, or <code>null</code> if not available
     */
    public static String getClientId(WebdavResource resource) {
        if (null != resource) {
            if (DAVRootCollection.class.isInstance(resource)) {
                return getClientId((DAVRootCollection) resource);
            }
            if (CommonFolderCollection.class.isInstance(resource)) {
                return getClientId((CommonFolderCollection<?>) resource);
            }
        }
        return null;
    }

    /**
     * Gets the push key indicated to clients for a specific WebDAV resource, encapsulating the underlying push topic.
     *
     * @param resource The WebDAV resource to get the push key for
     * @return The push key, or <code>null</code> if not available for the resource
     */
    public static String getPushKey(WebdavResource resource) {
        if (null != resource && DAVCollection.class.isInstance(resource)) {
            DAVCollection collection = (DAVCollection) resource;
            String topic = collection.getPushTopic();
            if (null != topic) {
                return getPushKey(topic, collection.getFactory().getContext().getContextId(), collection.getFactory().getUser().getId());
            }
        }
        return null;
    }

    /**
     * Gets the push key indicated to clients for a specific WebDAV resource, encapsulating the underlying push topic.
     *
     * @param topic The (internal) push topic
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The push key, or <code>null</code> if passed topic was <code>null</code>
     */
    public static String getPushKey(String topic, int contextId, int userId) {
        if (null != topic) {
            String key = new StringBuilder().append(contextId).append(':').append(userId).append(':').append(topic).toString();
            return BaseEncoding.base64Url().omitPadding().encode(key.getBytes(Charsets.UTF_8));
        }
        return null;
    }

    /**
     * Gets the static <i>root</i> push topic for a specific client.
     *
     * @param clientId The client identifier to get the root topic for
     * @return The root topic, or <code>null</code> if passed client identifier was <code>null</code>
     */
    public static String getRootTopic(String clientId) {
        return null != clientId ? "ox:" + clientId : null;
    }

    /**
     * Gets the push topic for a specific folder and client.
     *
     * @param clientId The client identifier to get the topic for
     * @param folderId The folder identifier to get the topic for
     * @return The folder topic, or <code>null</code> if passed client identifier was <code>null</code>
     */
    public static String getFolderTopic(String clientId, String folderId) {
        String rootTopic = getRootTopic(clientId);
        return null != rootTopic ? rootTopic + ":" + folderId : null;
    }

    private static String getClientId(DAVRootCollection rootCollection) {
        WebdavPath url = rootCollection.getUrl();
        WebdavPath rootUrl = new WebdavPath(rootCollection.getFactory().getURLPrefix());
        if (CalendarHomeSet.CALENDAR_HOME.equals(url) || (0 == url.size() && CalendarHomeSet.CALENDAR_HOME.equals(rootUrl))) {
            return CLIENT_CALDAV;
        }
        if (AddressbookHomeSet.ADDRESSBOOK_HOME.equals(url) || (0 == url.size() && AddressbookHomeSet.ADDRESSBOOK_HOME.equals(rootUrl))) {
            return CLIENT_CARDDAV;
        }
        return null;
    }

    private static String getClientId(CommonFolderCollection<?> folderCollection) {
        UserizedFolder folder = folderCollection.getFolder();
        if (null != folder) {
            ContentType contentType = folder.getContentType();
            if (CalendarContentType.getInstance().equals(contentType) || TaskContentType.getInstance().equals(contentType)) {
                return CLIENT_CALDAV;
            }
            if (ContactContentType.getInstance().equals(contentType)) {
                return CLIENT_CARDDAV;
            }
        }
        return null;
    }

}
