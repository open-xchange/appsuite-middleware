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

package com.openexchange.i18n.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link TemplateToken} - An enumeration of possible occurring tokens in a
 * template which are supposed to be replaced with user/object information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum TemplateToken {

    /**
     * Display name of the owner
     */
    CREATED_BY("created_by"),
    /**
     * Folder owner
     */
    BEHALF_OF("behalf_of"),
    /**
     * Display name of the changing user
     */
    CHANGED_BY("changed_by"),
    /**
     * Creation date and time in standard date and time format
     */
    CREATION_DATETIME("creation_datetime"),
    /**
     * The tile
     */
    TITLE("title"),
    /**
     * The location
     */
    LOCATION("location"),
    /**
     * The ID of the folder where object is created
     */
    FOLDER_ID("folder"),
    /**
     * The name of the folder where object is created
     */
    FOLDER_NAME("folder_name"),
    /**
     * The object ID
     */
    OBJECT_ID("object"),
    /**
     * The module name; e.g. <i>Calendar</i>
     */
    MODULE("module"),
    /**
     * path to the JavaScript UI on the web server
     */
    UI_WEB_PATH("uiwebpath"),
    /**
     * The host name for generating links to an object
     */
    HOSTNAME("hostname"),
    /**
     * The link to an object
     */
    LINK("link"),
    /**
     * The start date
     */
    START("start"),
    /**
     * The end date
     */
    END("end"),
    /**
     * The recurring information; e.g. <i>Daily, starting 15.02.2007, ending
     * 17.02.2007</i>.
     */
    SERIES("series"),
    /**
     * The comment/description
     */
    DESCRIPTION("description"),
    /**
     * The participant list
     */
    PARTICIPANTS("participants"),
    /**
     * The resource list
     */
    RESOURCES("resources"),
    /**
     * The confirmation status
     */
    STATUS("status"),
    /**
     * The action
     */
    ACTION("action"),
    /**
     * The confirmation action
     */
    CONFIRMATION_ACTIN("confirmation_action"),
    /**
     * Task's priority
     */
    TASK_PRIORITY("priority"),
    /**
     * Task's status
     */
    TASK_STATUS("task_status"),
    /**
     * Delete exceptions
     */
    DELETE_EXCEPTIONS("delete_exceptions"),
    /**
     * Change exceptions
     */
    CHANGE_EXCEPTIONS("change_exceptions");

    private final String token;

    private TemplateToken(final String token) {
        this.token = token;
    }

    /**
     * Gets the tokens
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

    private static final transient Map<String, TemplateToken> MAP;

    static {
        final TemplateToken[] tokens = TemplateToken.values();
        MAP = new HashMap<String, TemplateToken>(tokens.length);
        for (final TemplateToken token : tokens) {
            MAP.put(token.token, token);
        }
    }

    /**
     * Gets the template token for specified string or <code>null</code> if
     * there's no corresponding template token.
     *
     * @param tokenString The token string
     * @return The template token for specified string or <code>null</code> if
     *         there's no corresponding template token.
     */
    public static TemplateToken getByString(final String tokenString) {
        return MAP.get(tokenString);
    }
}
