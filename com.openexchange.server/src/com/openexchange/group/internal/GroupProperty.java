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

package com.openexchange.group.internal;

import com.openexchange.config.lean.Property;

/**
 * {@link GroupProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public enum GroupProperty implements Property {

    /**
     * Configures whether the group "All users" should be hidden to clients when listing all groups or searching for groups in a context.
     * The virtual group "All users" always contains all existing users of a context but no guests.
     */
    HIDE_ALL_USERS("hideAllUsers", Boolean.FALSE),

    /**
     * Configures whether the group "Guests" should be hidden to clients when listing all groups or searching for groups in a context. The
     * virtual group "Guests" always contains all existing guest users of a context but no users, and is mainly used as entity in
     * permissions of system folders.
     */
    HIDE_ALL_GUESTS("hideAllGuests", Boolean.FALSE),

    /**
     * Configures whether the group "Standard group" should be hidden to clients when listing all groups or searching for groups in a
     * context. Every created user will be added to this non-virtual group automatically, but can be removed again later on.
     */
    HIDE_STANDARD_GROUP("hideStandardGroup", Boolean.FALSE),

    ;

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link GroupProperty}.
     *
     * @param suffix The property name suffix
     * @param defaultValue The property's default value
     */
    private GroupProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.group." + suffix;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
