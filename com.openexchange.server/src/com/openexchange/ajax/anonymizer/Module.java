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

package com.openexchange.ajax.anonymizer;

import com.openexchange.java.Strings;

/**
 * {@link Module} - A module to anonymize.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Module {

    /**
     * The user contact module
     */
    CONTACT("contact"),
    /**
     * The user module
     */
    USER("user"),
    /**
     * The group module
     */
    GROUP("group"),
    /**
     * The resource module
     */
    RESOURCE("resource"),
    ;

    private static final Module[] VALUES = Module.values();

    // --------------------------------------------------------------------------------------------------------------------------- //

    private final String name;

    Module(String name) {
        this.name = name;
    }

    /**
     * Gets the module string.
     *
     * @return The module string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the module for given module name
     *
     * @param name The module name to look-up by
     * @return The associated module or <code>null</code>
     */
    public static Module moduleFor(String name) {
        if (null == name) {
            return null;
        }
        String id = Strings.asciiLowerCase(name);
        for (Module m : VALUES) {
            if (id.equals(m.name)) {
                return m;
            }
        }
        return null;
    }

}
