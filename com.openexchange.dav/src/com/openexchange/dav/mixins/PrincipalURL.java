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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.dav.mixins;

import com.openexchange.dav.CUType;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link PrincipalURL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class PrincipalURL extends SingleXMLPropertyMixin {

    /**
     * Gets the principal URL value.
     *
     * @param userID The identifier of the user to get the principal URL for
     * @return The principal URL
     */
    public static String forUser(int userID) {
        return "/principals/users/" + userID;
    }

    /**
     * Gets the principal URL value.
     *
     * @param groupID The identifier of the group to get the principal URL for
     * @return The principal URL
     */
    public static String forGroup(int groupID) {
        return "/principals/groups/" + groupID;
    }

    /**
     * Gets the principal URL value.
     *
     * @param resourceID The identifier of the resource to get the principal URL for
     * @return The principal URL
     */
    public static String forResource(int resourceID) {
        return "/principals/resources/" + resourceID;
    }

    private static final String PROPERTY_NAME = "principal-URL";
    private final int principalID;
    private final CUType type;

    /**
     * Initializes a new {@link PrincipalURL}.
     *
     * @param principalID The identifier of the principal
     * @param type The calendar user type of the principal
     */
    public PrincipalURL(int principalID, CUType type) {
        super(Protocol.DAV_NS.getURI(), PROPERTY_NAME);
        this.principalID = principalID;
        this.type = type;
    }

    @Override
    protected String getValue() {
        String url;
        switch (type) {
            case INDIVIDUAL:
                url = forUser(principalID);
                break;
            case GROUP:
                url = forGroup(principalID);
                break;
            case RESOURCE:
                url = forResource(principalID);
                break;
            default:
                throw new IllegalArgumentException(type.toString());
        }
        return "<D:href>" + url + "</D:href>";
    }

}
