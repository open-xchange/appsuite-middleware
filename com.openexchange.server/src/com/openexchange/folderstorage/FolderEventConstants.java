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

package com.openexchange.folderstorage;

/**
 * {@link FolderEventConstants} - Provides constants for push events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderEventConstants {

    /**
     * Initializes a new {@link FolderEventConstants}.
     */
    private FolderEventConstants() {
        super();
    }

    /**
     * The topic of folder storage events.
     */
    public static final String TOPIC = "com/openexchange/folderstorage";

    /**
     * The topic of folder storage events for changed attributes of a folder's entry.
     */
    public static final String TOPIC_ATTR = "com/openexchange/folderstorage/attributes";

    /**
     * The topic of folder storage events for changed identifiers.
     */
    public static final String TOPIC_IDENTIFIERS = "com/openexchange/folderstorage/identifiers";

    /**
     * An array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     */
    private static final String[] TOPICS = { TOPIC, TOPIC_ATTR, TOPIC_IDENTIFIERS };

    /**
     * Gets an array of {@link String string} including all known topics.
     * <p>
     * Needed on event handler registration to a bundle context.
     *
     * @return An array of {@link String string} including all known topics.
     */
    public static String[] getAllTopics() {
        final String[] retval = new String[TOPICS.length];
        System.arraycopy(TOPICS, 0, retval, 0, TOPICS.length);
        return retval;
    }

    /**
     * Whether the folder's content/attributes or the folder itself has changed. Default is <code>true</code>; meaning if not present the folder content has
     * changed, but not the folder itself. Property value is of type <code>java.lang.Boolean</code>.
     */
    public static final String PROPERTY_CONTENT_RELATED = "com.openexchange.folderstorage.content-related";

    /**
     * The context ID property of a push event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_CONTEXT = "com.openexchange.folderstorage.context";

    /**
     * The user ID property of a push event. Property value is of type <code>java.lang.Integer</code>.
     */
    public static final String PROPERTY_USER = "com.openexchange.folderstorage.user";

    /**
     * The folder fullname property of a push event. Property value is of type <code>java.lang.String</code>.
     */
    public static final String PROPERTY_FOLDER = "com.openexchange.folderstorage.folder";

    /**
     * The <b>optional</b> session property of a push event. Property value is of type <code>com.openexchange.session.Session</code>.
     */
    public static final String PROPERTY_SESSION = "com.openexchange.folderstorage.session";

    /**
     * Force an immediate delivery of the associated event.
     */
    public static final String PROPERTY_IMMEDIATELY = "com.openexchange.folderstorage.immediately";

    /**
     * The old identifier.
     */
    public static final String PROPERTY_OLD_IDENTIFIER = "com.openexchange.folderstorage.oldIdentifier";

    /**
     * The new identifier.
     */
    public static final String PROPERTY_NEW_IDENTIFIER = "com.openexchange.folderstorage.newIdentifier";

    /**
     * The delimiter string.
     */
    public static final String PROPERTY_DELIMITER = "com.openexchange.folderstorage.delimiter";

    /**
     * The path to the default folder in an array of folder IDs in their absolute/unique form, i.e. all containing the service/account
     * information.
     */
    public static final String PROPERTY_FOLDER_PATH = "com.openexchange.folderstorage.folderPath";

}
