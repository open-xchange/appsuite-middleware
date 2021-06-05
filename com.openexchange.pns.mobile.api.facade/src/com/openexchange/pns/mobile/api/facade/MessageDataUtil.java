/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.pns.mobile.api.facade;

import java.util.Map;

/**
 * {@link MessageDataUtil} - A utility class for message data.
 */
public class MessageDataUtil {

    /**
     * Initializes a new {@link MessageDataUtil}.
     */
    private MessageDataUtil() {
        super();
    }

    private static final String KEY_CID = "cid";
    private static final String KEY_DISPLAYNAME = "displayname";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_ID = "id";
    private static final String KEY_SENDER = "email";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_UNREAD = "unread";

    public static String getPath(Map<String, Object> messageData) {
        return getString(messageData, KEY_CID);
    }

    public static String getFolder(Map<String, Object> messageData) {
        return getString(messageData, KEY_FOLDER);
    }

    public static String getId(Map<String, Object> messageData) {
        return getString(messageData, KEY_ID);
    }

    public static String getDisplayName(Map<String, Object> messageData) {
        return getString(messageData, KEY_DISPLAYNAME);
    }

    public static String getSender(Map<String, Object> messageData) {
        return getString(messageData, KEY_SENDER);
    }

    public static String getSubject(Map<String, Object> messageData) {
        return getString(messageData, KEY_SUBJECT);
    }

    public static int getUnread(Map<String, Object> messageData) {
        return getInteger(messageData, KEY_UNREAD);
    }

    private static int getInteger(Map<String, Object> messageData, String key) {
        Object value = messageData.get(key);
        return value instanceof Integer ? ((Integer) value).intValue() : -1;
    }

    private static String getString(Map<String, Object> messageData, String key) {
        Object value = messageData.get(key);
        return value instanceof String ? (String) value : "";
    }

}
