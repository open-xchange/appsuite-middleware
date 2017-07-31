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
