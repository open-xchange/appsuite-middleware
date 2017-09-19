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
