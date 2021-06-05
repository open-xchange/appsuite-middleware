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

package com.openexchange.admin.rmi.dataobjects;

/**
 * {@link RecalculationScope} defines scopes for filestore usage recalculation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public enum RecalculationScope {

    /**
     * Recalculates all filestores.
     */
    ALL("all"),
    /**
     * Recalculates only context filestores.
     */
    CONTEXT("context"),
    /**
     * Recalculates only user filestores.
     */
    USER("user");

    private final String id;

    private RecalculationScope(String id) {
        this.id = id;
    }

    /**
     * Gets the scope associated with given name.
     *
     * @param name The name to look-up
     * @return The associated scope or <code>null</code>
     * @throws IllegalArgumentException
     */
    public static RecalculationScope getScopeByName(String name) throws IllegalArgumentException {
        if (isEmpty(name)) {
            return null;
        }

        String n = name.trim();
        for (RecalculationScope scope : RecalculationScope.values()) {
            if (scope.id.equalsIgnoreCase(n)) {
                return scope;
            }
        }
        return null;
    }

    private static boolean isEmpty(String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static boolean isWhitespace(final char c) {
        switch (c) {
            case 9: // 'unicode: 0009
            case 10: // 'unicode: 000A'
            case 11: // 'unicode: 000B'
            case 12: // 'unicode: 000C'
            case 13: // 'unicode: 000D'
            case 28: // 'unicode: 001C'
            case 29: // 'unicode: 001D'
            case 30: // 'unicode: 001E'
            case 31: // 'unicode: 001F'
            case ' ': // Space
                // case Character.SPACE_SEPARATOR:
                // case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }

}
