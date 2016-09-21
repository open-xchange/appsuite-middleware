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

package com.openexchange.oauth.scope;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link Module} - Defines the AppSuite's available scopes/features
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum Module {
    mail("Mail", false),
    calendar_ro("Calendars (Read Only)", true),
    contacts_ro("Contacts (Read Only)", true),
    calendar_rw("Calendars (Read/Write)", false),
    contacts_rw("Contacts (Read/Write)", false),
    drive("Drive", true),
    generic("", true),
    offline("Offline Access", true);

    private static final String modules = Strings.concat(", ", (Object[]) Module.values());
    private final boolean isLegacy;
    private final String displayName;

    /**
     * Initialises a new {@link Module}.
     */
    private Module(String displayName, boolean isLegacy) {
        this.displayName = displayName;
        this.isLegacy = isLegacy;
    }

    /**
     * Resolves the specified space separated string of {@link Module}s to an array of {@link Module} values
     * 
     * @param string A space separated String containing the {@link Module} strings
     * @return An array with the resolved {@link Module} values
     * @throws OXException if the specified string cannot be resolved to a valid {@link Module}
     */
    public static final Module[] valuesOf(String string) throws OXException {
        List<Module> list = new ArrayList<>();
        String[] split = Strings.splitByWhitespaces(string);
        for (String s : split) {
            try {
                list.add(valueOf(s));
            } catch (IllegalArgumentException e) {
                throw OAuthScopeExceptionCodes.CANNOT_RESOLVE_MODULE.create(s, modules);
            }
        }

        return list.toArray(new Module[list.size()]);
    }

    /**
     * Gets the isLegacy
     *
     * @return The isLegacy
     */
    public boolean isLegacy() {
        return isLegacy;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }
}
