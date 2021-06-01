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

package com.openexchange.chronos;

import java.util.UUID;

/**
 * {@link ResourceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ResourceId {

    /** Type identifier used for {@link CalendarUserType#INDIVIDUAL} */
    private static final int TYPE_INDIVIDUAL = 1;

    /** Type identifier used for {@link CalendarUserType#GROUP} */
    private static final int TYPE_GROUP = 2;

    /** Type identifier used for {@link CalendarUserType#RESOURCE} */
    private static final int TYPE_RESOURCE = 3;

    /** Type identifier used for {@link CalendarUserType#UNKNOWN} */
    private static final int TYPE_UNKNOWN = -1;

    /** Static number encoded into resource identifiers */
    private static final long MAGIC = 4240498974L << 24;

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
            if (36 != uuidString.length()) {
                return null;
            }
            try {
                UUID uuid = UUID.fromString(uuidString);
                if (0 == uuid.getLeastSignificantBits() >> 32) {
                    return new ResourceId(decodeV1ContextID(uuid), decodeV1Entity(uuid), decodeV1Type(uuid));
                }
                return new ResourceId(decodeContextID(uuid), decodeEntity(uuid), decodeType(uuid));
            } catch (IllegalArgumentException e) {
                // org.slf4j.LoggerFactory.getLogger(ResourceId.class).debug("Error parsing resource ID", e);
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
        long msb = (long) contextID << 32 | entity & 0xffffffffL;
        long lsb = MAGIC | getType(type) & 0xffffffffL;
        return new UUID(msb, lsb);
    }

    private static int decodeContextID(UUID encoded) {
        return (int) (encoded.getMostSignificantBits() >> 32);
    }

    private static CalendarUserType decodeType(UUID encoded) {
        int type = (int) (encoded.getLeastSignificantBits() - MAGIC);
        return getCalendarUserType(type);
    }

    private static int decodeEntity(UUID encoded) {
        return (int) encoded.getMostSignificantBits();
    }

    private static int decodeV1ContextID(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        return (int) msb >> 16;
    }

    private static CalendarUserType decodeV1Type(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        long cid = msb >> 16;
        return getCalendarUserType((int) (msb - (cid << 16)));
    }

    private static int decodeV1Entity(UUID encoded) {
        return (int) encoded.getLeastSignificantBits();
    }

    /**
     * Gets the corresponding type identifier for this calendar user type.
     *
     * @return The type, or {@link ResourceId#TYPE_UNKNOWN} if no matching type exists
     */
    private static int getType(CalendarUserType type) {
        if (CalendarUserType.INDIVIDUAL.equals(type)) {
            return TYPE_INDIVIDUAL;
        }
        if (CalendarUserType.GROUP.equals(type)) {
            return TYPE_GROUP;
        }
        if (CalendarUserType.RESOURCE.equals(type)) {
            return TYPE_RESOURCE;
        }
        return TYPE_UNKNOWN;
    }

    /**
     * Gets the calendar user type for a specific type identifier.
     *
     * @param type The type identifier
     * @return The corresponding calendar user type, or {@link CalendarUserType#UNKNOWN} if no matching type found
     */
    private static CalendarUserType getCalendarUserType(int type) {
        switch (type) {
            case TYPE_INDIVIDUAL:
                return CalendarUserType.INDIVIDUAL;
            case TYPE_GROUP:
                return CalendarUserType.GROUP;
            case TYPE_RESOURCE:
                return CalendarUserType.RESOURCE;
            default:
                return CalendarUserType.UNKNOWN;
        }
    }

}
