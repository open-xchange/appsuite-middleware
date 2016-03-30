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
