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

package com.openexchange.file.storage;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import org.osgi.service.event.Event;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageEventHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileStorageEventHelper {

    public static final class EventProperty {

        private final String key;

        private final Object value;

        /**
         * Initializes a new {@link EventProperty}.
         * @param key
         * @param value
         */
        public EventProperty(final String key, final Object value) {
            super();
            if (key == null) {
                throw new IllegalArgumentException("Argument 'key' must not be null!");
            }
            if (value == null) {
                throw new IllegalArgumentException("Argument 'value' must not be null!");
            }
            this.key = key;
            this.value = value;
        }


        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EventProperty other = (EventProperty) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }

    public static Event buildUpdateEvent(final Session session, final String service, final String accountId, final String folderId, final String objectId, final String optFileName, final EventProperty... properties) {
        return new Event(FileStorageEventConstants.UPDATE_TOPIC, buildProperties(session, service, accountId, folderId, objectId, optFileName, properties));
    }

    public static Event buildCreateEvent(final Session session, final String service, final String accountId, final String folderId, final String objectId, final String optFileName, final EventProperty... properties) {
        return new Event(FileStorageEventConstants.CREATE_TOPIC, buildProperties(session, service, accountId, folderId, objectId, optFileName, properties));
    }

    public static Event buildDeleteEvent(final Session session, final String service, final String accountId, final String folderId, final String objectId, final String optFileName, final Set<String> versions, final EventProperty... properties) {
        final Dictionary<String, Object> props = buildProperties(session, service, accountId, folderId, objectId, optFileName, properties);
        /*
         * version may be null to indicate a complete deletion of a document.
         */
        if (versions != null) {
            props.put(FileStorageEventConstants.VERSIONS, versions);
        }
        return new Event(FileStorageEventConstants.DELETE_TOPIC, props);
    }

    public static Event buildAccessEvent(Session session, String service, String accountId, String folderId, final String objectId, final String optFileName, final EventProperty... properties) {
        return new Event(FileStorageEventConstants.ACCESS_TOPIC, buildProperties(session, service, accountId, folderId, objectId, optFileName, properties));
    }

    private static Dictionary<String, Object> buildProperties(final Session session, final String service, final String accountId, final String folderId, final String objectId, final String optFileName, final EventProperty... properties) {
        final Dictionary<String, Object> ht = new Hashtable<String, Object>(8);
        if (null != session) {
            ht.put(FileStorageEventConstants.SESSION, session);
        }
        if (null != service) {
            ht.put(FileStorageEventConstants.SERVICE, service);
        }
        if (null != accountId) {
            ht.put(FileStorageEventConstants.ACCOUNT_ID, accountId);
        }
        if (null != objectId) {
            ht.put(FileStorageEventConstants.OBJECT_ID, objectId);
            ht.put(FileStorageEventConstants.E_TAG, FileStorageUtility.getETagFor(objectId, FileStorageFileAccess.CURRENT_VERSION, null));
        }
        if (null != folderId) {
            ht.put(FileStorageEventConstants.FOLDER_ID, folderId);
        }
        if (null != optFileName) {
            ht.put(FileStorageEventConstants.FILE_NAME, optFileName);
        }
        if (null != properties && 0 != properties.length) {
            for (EventProperty property : properties) {
                if (property != null) {
                    ht.put(property.getKey(), property.getValue());
                }
            }
        }
        return ht;
    }

    public static boolean isCreateEvent(final Event event) {
        return FileStorageEventConstants.CREATE_TOPIC.equals(event.getTopic());
    }

    public static boolean isUpdateEvent(final Event event) {
        return FileStorageEventConstants.UPDATE_TOPIC.equals(event.getTopic());
    }

    public static boolean isDeleteEvent(final Event event) {
        return FileStorageEventConstants.DELETE_TOPIC.equals(event.getTopic());
    }

    public static boolean isInfostoreEvent(final Event event) {
        return "com.openexchange.infostore".equals(event.getProperty(FileStorageEventConstants.SERVICE));
    }

    public static Session extractSession(final Event event) throws OXException {
        final Object sessionObj = event.getProperty(FileStorageEventConstants.SESSION);
        if (sessionObj == null) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(FileStorageEventConstants.SESSION);
        }

        if (!(sessionObj instanceof Session)) {
            throw FileStorageExceptionCodes.INVALID_PARAMETER.create(FileStorageEventConstants.SESSION, sessionObj.getClass().getName());
        }
        return (Session) sessionObj;
    }

    public static String extractObjectId(final Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.OBJECT_ID);
    }

    public static String extractFolderId(final Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.FOLDER_ID);
    }

    public static String extractAccountId(final Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.ACCOUNT_ID);
    }

    public static String extractService(final Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.SERVICE);
    }

    public static Set<String> extractVersions(final Event event) {
        final Object versionsObj = event.getProperty(FileStorageEventConstants.VERSIONS);
        if (versionsObj == null || !(versionsObj instanceof Set<?>)) {
            return null;
        }

        return (Set<String>) versionsObj;
    }

    public static String createDebugMessage(final String eventName, final Event event) {
        final StringBuilder sb = new StringBuilder("Received ");
        sb.append(eventName);
        sb.append(": ");
        sb.append(event.toString());
        for (final String key : event.getPropertyNames()) {
            final Object value = event.getProperty(key);
            sb.append("\n    ");
            sb.append(key);
            sb.append(": ");
            if (FileStorageEventConstants.FOLDER_PATH.equals(key) && Object[].class.isInstance(value)) {
                sb.append(Arrays.toString((Object[]) value));
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    private static String extractValue(final Event event, final String key) throws OXException {
        final Object obj = event.getProperty(key);
        if (obj == null) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(key);
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        throw FileStorageExceptionCodes.INVALID_PARAMETER.create(key, obj.getClass().getName(), String.class.getName());
    }
}
