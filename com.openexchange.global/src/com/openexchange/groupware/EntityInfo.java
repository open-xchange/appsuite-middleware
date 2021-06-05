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

package com.openexchange.groupware;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;

/**
 * {@link EntityInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class EntityInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = 2803304999527402064L;

    private final static String IDENTIFER = "identifier";
    private final static String TYPE = "type";
    private final static String DISPLAY_NAME = "display_name";
    private final static String TITLE = "title";
    private final static String FIRST_NAME = "first_name";
    private final static String LAST_NAME = "last_name";
    private final static String EMAIL1 = "email1";
    private final static String IMAGE_URL = "image1_url";
    private final static String ENTITY = "entity";
    private final static String CONTACT = "contact";

    private final String identifier;
    private final String displayName;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String email1;
    private final int entity;
    private final String imageUrl;
    private final Type type;

    /**
     * Initializes a new {@link EntityInfo}, initialized with the data from another one.
     * 
     * @param entityInfo The entity info to use for initialization
     */
    public EntityInfo(EntityInfo entityInfo) {
        this(entityInfo.getIdentifier(), entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), entityInfo.getEntity(), entityInfo.getImageUrl(), entityInfo.getType());
    }

    public EntityInfo(String identifier, String displayName, String title, String firstName, String lastName, String email1, int entity, String imageUrl, Type type) {
        super();
        this.identifier = identifier;
        this.displayName = displayName;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email1 = email1;
        this.entity = entity;
        this.imageUrl = imageUrl;
        this.type = type;
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getEntity() {
        return entity;
    }

    public String getEmail1() {
        return email1;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Type getType() {
        return type;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(IDENTIFER, identifier);
            json.put(TYPE, String.valueOf(type).toLowerCase());
            json.put(DISPLAY_NAME, displayName);
            if (0 < entity) {
                json.put(ENTITY, entity);
            }
            JSONObject contact = new JSONObject(6);
            if (Strings.isNotEmpty(title)) {
                contact.put(TITLE, title);
            }
            if (Strings.isNotEmpty(firstName)) {
                contact.put(FIRST_NAME, firstName);
            }
            if (Strings.isNotEmpty(lastName)) {
                contact.put(LAST_NAME, lastName);
            }
            if (Strings.isNotEmpty(email1)) {
                contact.put(EMAIL1, email1);
            }
            if (Strings.isNotEmpty(imageUrl)) {
                contact.put(IMAGE_URL, imageUrl);
            }
            json.put(CONTACT, contact);
        } catch (JSONException e) {
            // will not happen
        }
        return json;
    }

    public static EntityInfo parseJSON(JSONObject entityInfo) {
        if (JSONObject.NULL.equals(entityInfo)) {
            return null;
        }
        String identifier = entityInfo.optString(IDENTIFER, null);
        String displayName = entityInfo.optString(DISPLAY_NAME, null);
        int entity = entityInfo.optInt(ENTITY, -1);
        Type type = Enums.parse(Type.class, entityInfo.optString(TYPE, null), null);
        JSONObject contact = entityInfo.optJSONObject(CONTACT);
        if (false == JSONObject.NULL.equals(contact)) {
            String title = entityInfo.optString(TITLE, null);
            String firstName = entityInfo.optString(FIRST_NAME, null);
            String lastName = entityInfo.optString(LAST_NAME, null);
            String email1 = entityInfo.optString(EMAIL1, null);
            String imageUrl = entityInfo.optString(IMAGE_URL, null);
            return new EntityInfo(identifier, displayName, title, firstName, lastName, email1, entity, imageUrl, type);
        }
        return new EntityInfo(identifier, displayName, null, null, null, null, entity, null, type);
    }

    @Override
    public Object clone() {
        return new EntityInfo(this);
    }
    
    @Override
    public String toString() {
        return "EntityInfo [" + identifier + "," + title + "," + displayName + "," + email1 + "," + entity + "," + type + "]";
    }

    public enum Type {
        /**
         * An internal user
         */
        USER,
        /**
         * An internal group
         */
        GROUP,
        /**
         * A named external guest
         */
        GUEST,
        /**
         * An anonymous external guest
         */
        ANONYMOUS,

        ;
    }

}
