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

import java.util.UUID;
import com.openexchange.dav.CUType;
import com.openexchange.groupware.container.Participant;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link ResourceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ResourceId extends SingleXMLPropertyMixin {

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param userID The identifier of the user to get the resource ID for
     * @return The resource ID
     */
    public static String forUser(int contextID, int userID) {
        return "urn:uuid:" + encode(contextID, Participant.USER, userID);
    }

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param groupID The identifier of the group to get the resource ID for
     * @return The resource ID
     */
    public static String forGroup(int contextID, int groupID) {
        return "urn:uuid:" + encode(contextID, Participant.GROUP, groupID);
    }

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param resourceID The identifier of the resource to get the resource ID for
     * @return The resource ID
     */
    public static String forResource(int contextID, int resourceID) {
        return "urn:uuid:" + encode(contextID, Participant.RESOURCE, resourceID);
    }

    private final int principalID;
    private final int contextID;
    private final CUType type;

    /**
     * Initializes a new {@link ResourceId}.
     *
     * @param contextID The context identifier
     * @param principalID The identifier of the principal
     * @param type The calendar user type of the principal
     */
    public ResourceId(int contextID, int principalID, CUType type) {
        super(Protocol.DAV_NS.getURI(), "resource-id");
        this.contextID = contextID;
        this.principalID = principalID;
        this.type = type;
    }

    @Override
    protected String getValue() {
        switch (type) {
            case INDIVIDUAL:
                return forUser(contextID, principalID);
            case GROUP:
                return forGroup(contextID, principalID);
            case RESOURCE:
                return forResource(contextID, principalID);
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }

    private static UUID encode(int contextID, int type, int entity) {
        long lsb = entity;
        long cid = contextID & 0xffffffffL;
        long msb = cid << 16;
        msb += type;
        return new UUID(msb, lsb);
    }

}
