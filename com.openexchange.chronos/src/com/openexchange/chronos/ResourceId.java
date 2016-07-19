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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos;

import java.util.UUID;

/**
 * {@link ResourceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ResourceId {

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param userID The identifier of the user to get the resource ID for
     * @return The resource ID
     */
    public static String forUser(int contextID, int userID) {
        return new ResourceId(contextID, userID, CalendarUserType.INDIVIDUAL).getURI();
    }

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param groupID The identifier of the group to get the resource ID for
     * @return The resource ID
     */
    public static String forGroup(int contextID, int groupID) {
        return new ResourceId(contextID, groupID, CalendarUserType.GROUP).getURI();
    }

    /**
     * Gets the resource ID value.
     *
     * @param contextID The context identifier
     * @param resourceID The identifier of the resource to get the resource ID for
     * @return The resource ID
     */
    public static String forResource(int contextID, int resourceID) {
        return new ResourceId(contextID, resourceID, CalendarUserType.RESOURCE).getURI();
    }

    /**
     * Decodes the resource identifier from its uniform resource name string representation.
     *
     * @param uri The uniform resource name URI to parse
     * @return The resource ID, or <code>null</code> if the URI couldn't be parsed
     */
    public static ResourceId parse(String uri) {
        if (null != uri) {
            String uuidString = uri;
            if (uuidString.startsWith("urn:uuid:")) {
                uuidString = uuidString.substring(9);
            } else if (uuidString.startsWith("uuid:")) {
                uuidString = uuidString.substring(5);
            }
            try {
                UUID uuid = UUID.fromString(uuidString);
                return new ResourceId(decodeContextID(uuid), decodeEntity(uuid), decodeType(uuid));
            } catch (IllegalArgumentException e) {
                org.slf4j.LoggerFactory.getLogger(ResourceId.class).debug("Error parsing resource ID", e);
            }
        }
        return null;
    }

    private final int entity;
    private final int contextID;
    private final CalendarUserType type;

    /**
     * Initializes a new {@link ResourceId}.
     *
     * @param contextID The context identifier
     * @param entity The entity identifier
     * @param type The calendar user type
     */
    public ResourceId(int contextID, int entity, CalendarUserType type) {
        super();
        this.contextID = contextID;
        this.entity = entity;
        this.type = type;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Gets the entity identifier
     *
     * @return The entity identifier
     */
    public int getEntity() {
        return entity;
    }

    /**
     * Gets the participant type
     *
     * @return The participant type
     */
    public CalendarUserType getCalendarUserType() {
        return type;
    }

    /**
     * Gets the uniform resource name URI.
     *
     * @return The uniform resource name
     */
    public String getURI() {
        return "urn:uuid:" + encode(contextID, entity, type);
    }

    @Override
    public String toString() {
        return getURI();
    }

    private static UUID encode(int contextID, int entity, CalendarUserType type) {
        long lsb = entity;
        long cid = contextID & 0xffffffffL;
        long msb = cid << 16;
        msb += type.getType();
        return new UUID(msb, lsb);
    }

    private static int decodeContextID(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        return (int) msb >> 16;
    }

    private static CalendarUserType decodeType(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        long cid = msb >> 16;
        int type = (int) (msb - (cid << 16));
        return CalendarUserType.fromType(type);
    }

    private static int decodeEntity(UUID encoded) {
        return (int) encoded.getLeastSignificantBits();
    }

}
