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

package com.openexchange.groupware;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Strings;

/**
 * {@link EntityInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class EntityInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = 2803304999527402064L;

    private final static String IDENTIFER = "id";
    private final static String TYPE = "type";
    private final static String DISPLAY_NAME = "displayName";
    private final static String TITLE = "title";
    private final static String FIRST_NAME = "firstName";
    private final static String LAST_NAME = "lastName";
    private final static String EMAIL1 = "email1";
    private final static String IMAGE_URL = "imageUrl";
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
            json.put(TYPE, type.getType());
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
        String identifier = entityInfo.optString(IDENTIFER);
        String displayName = entityInfo.optString(DISPLAY_NAME);
        int entity = entityInfo.has(ENTITY) ? entityInfo.optInt(ENTITY) : -1;
        Type type = Type.forName(entityInfo.optString(TYPE));
        JSONObject contact = entityInfo.optJSONObject(CONTACT);
        if (false == JSONObject.NULL.equals(contact)) {
            String title = entityInfo.optString(TITLE);
            String firstName = entityInfo.optString(FIRST_NAME);
            String lastName = entityInfo.optString(LAST_NAME);
            String email1 = entityInfo.optString(EMAIL1);
            String imageUrl = entityInfo.optString(IMAGE_URL);
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
        return "EntityInfo [" + identifier + "," + title + "," + displayName + "," + email1 + "," + entity + "," + type.getType() + "]";
    }

    public enum Type {
        USER("user"),
        GUEST("guest"),
        GROUP("group"),
        ;

        private static final Map<String, Type> name2Type = new HashMap<String, Type>();
        static {
            for (Type t : values()) {
                name2Type.put(t.type, t);
            }
        }

        private final String type;

        private Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static Type forName(String name) {
            return name2Type.get(name);
        }
    }

}
