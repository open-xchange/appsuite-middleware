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

package com.openexchange.jsieve.commands;

/**
 * {@link MatchType}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public enum MatchType {
    is,
    contains,
    matches,

    // regex match type
    regex("regex"),

    // relational match types
    value("relational"),
    ge("relational"),
    le("relational"),

    // Size match types
    over,
    under,

    // simplified matcher
    startswith,
    endswith,
    exists;

    private String argumentName;
    private String require;
    private String notName;

    /**
     * Initializes a new {@link MatchType}.
     */
    private MatchType() {
        this.argumentName = ":" + this.name();
        this.require = "";
        this.notName = "not " + this.name();
    }

    /**
     * Initializes a new {@link MatchType}.
     */
    private MatchType(String require) {
        this.argumentName = ":" + this.name();
        this.require = require;
        this.notName = "not " + this.name();
    }

    public String getArgumentName() {
        return argumentName;
    }

    public String getRequire() {
        return require;
    }

    public String getNotName() {
        return notName;
    }

    /**
     * Retrieves the name of the matcher if the given string is a "not name".
     *
     * @param notName The name of the matcher
     * @return The normal name or null
     */
    public static String getNormalName(String notName) {
        for (MatchType type : MatchType.values()) {
            if (notName.equals(type.getNotName())) {
                return type.name();
            }
        }
        return null;
    }

    /**
     * Retrieves the not name of the {@link MatchType} with the given argument name
     *
     * @param argumentName The name of the matcher
     * @return The normal name or null
     */
    public static String getNotNameForArgumentName(String argumentName) {
        return MatchType.valueOf(argumentName.substring(1)).getNotName();
    }

    public static boolean containsMatchType(String matchTypeName) {
        for (MatchType cmd : values()) {
            if (cmd.name().equals(matchTypeName) || cmd.getNotName().equals(matchTypeName)) {
                return true;
            }
        }
        return false;
    }

}
