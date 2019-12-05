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

package com.openexchange.authentication.oauth.impl;

import com.openexchange.authentication.NamePart;

/**
 * {@link LookupSource} denotes the input value used to resolve a user or context
 * within App Suite. The taken value might be evaluated further to determine the
 * actual identifier to lookup the user or context, see {@link NamePart}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @see NamePart
 * @since v7.10.3
 */
public enum LookupSource {

    /**
     * The name given as user input during login.
     */
    LOGIN_NAME("login-name"),
    /**
     * A response parameter from the authorization server.
     */
    RESPONSE_PARAMETER("response-parameter");

    private final String configName;

    private LookupSource(String configName) {
        this.configName = configName;
    }

    /**
     * Gets the name of this part as it would be defined in a configuration property.
     * 
     * @return The configuration name
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Gets the {@link NamePart} for a given config name or <code>null</code> if none matches.
     * 
     * @param configName
     * @return The config name or <code>null</code>
     */
    public static LookupSource of(String configName) {
        for (LookupSource value : LookupSource.values()) {
            if (value.configName.equals(configName)) {
                return value;
            }
        }

        return null;
    }

}
