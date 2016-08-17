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

package com.openexchange.snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * {@link DefaultSnippet} - The default snippet implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultSnippet implements Snippet {

    /**
     * The identifier.
     */
    private String id;

    /**
     * The account identifier.
     */
    private int accountId;

    /**
     * The module identifier.
     */
    private String module;

    /**
     * The type.
     */
    private String type;

    /**
     * The display name.
     */
    private String displayName;

    /**
     * The content.
     */
    private String content;

    /**
     * The attachments.
     */
    private List<Attachment> attachments;

    /**
     * The miscellaneous JSON object.
     */
    private Object misc;

    /**
     * The shared flag.
     */
    private Boolean shared;

    /**
     * The creator identifier.
     */
    private int createdBy;

    /**
     * The backing properties.
     */
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link DefaultSnippet}.
     */
    public DefaultSnippet() {
        super();
        properties = new HashMap<String, Object>(16);
        accountId = -1;
        createdBy = -1;
    }

    @Override
    public String getId() {
        String id = this.id;
        if (null == id) {
            final String s = (String) properties.get(PROP_ID);
            if (null == s) {
                return null;
            }
            id = s;
            this.id = id;
        }
        return id;
    }

    @Override
    public int getAccountId() {
        int accountId = this.accountId;
        if (accountId < 0) {
            final Integer itg = (Integer) properties.get(PROP_ACCOUNT_ID);
            if (null == itg) {
                return -1;
            }
            accountId = itg.intValue();
            this.accountId = accountId;
        }
        return accountId;
    }

    @Override
    public String getModule() {
        String module = this.module;
        if (null == module) {
            final String s = (String) properties.get(PROP_MODULE);
            if (null == s) {
                return null;
            }
            module = s;
            this.module = module;
        }
        return module;
    }

    @Override
    public String getType() {
        String type = this.type;
        if (null == type) {
            final String s = (String) properties.get(PROP_TYPE);
            if (null == s) {
                return null;
            }
            type = s;
            this.type = type;
        }
        return type;
    }

    @Override
    public String getDisplayName() {
        String displayName = this.displayName;
        if (null == displayName) {
            final String s = (String) properties.get(PROP_DISPLAY_NAME);
            if (null == s) {
                return null;
            }
            displayName = s;
            this.displayName = displayName;
        }
        return displayName;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public List<Attachment> getAttachments() {
        return attachments == null ? Collections.<Attachment> emptyList() : new ArrayList<Attachment>(attachments);
    }

    @Override
    public Object getMisc() {
        Object misc = this.misc;
        if (null == misc) {
            final Object obj = properties.get(PROP_MISC);
            if (null == obj) {
                return null;
            }
            try {
                misc = new JSONTokener(obj.toString()).nextValue();
            } catch (final JSONException e) {
                // Not a valid JSON value
                misc = JSONObject.NULL;
            }
            this.misc = misc;
        }
        return misc;
    }

    @Override
    public boolean isShared() {
        Boolean shared = this.shared;
        if (null == shared) {
            final Boolean b = (Boolean) properties.get(PROP_SHARED);
            shared = null == b ? Boolean.FALSE : b;
            this.shared = shared;
        }
        return shared.booleanValue();
    }

    @Override
    public int getCreatedBy() {
        int createdBy = this.createdBy;
        if (createdBy < 0) {
            final Integer itg = (Integer) properties.get(PROP_CREATED_BY);
            if (null == itg) {
                return -1;
            }
            createdBy = itg.intValue();
            this.createdBy = createdBy;
        }
        return createdBy;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Map<String, Object> getUnnamedProperties() {
        final Map<String, Object> m = new HashMap<String, Object>(properties);
        m.remove(PROP_ACCOUNT_ID);
        m.remove(PROP_CREATED_BY);
        m.remove(PROP_DISPLAY_NAME);
        m.remove(PROP_ID);
        m.remove(PROP_MISC);
        m.remove(PROP_MODULE);
        m.remove(PROP_SHARED);
        m.remove(PROP_TYPE);
        return m;
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setId(final String id) {
        this.id = id;
        if (id != null) {
            properties.put(PROP_ID, id);
        } else {
            properties.remove(PROP_ID);
        }
        return this;
    }

    /**
     * Sets the accountId
     *
     * @param accountId The accountId to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setAccountId(final int accountId) {
        this.accountId = accountId;
        if (accountId >= 0) {
            properties.put(PROP_ACCOUNT_ID, Integer.valueOf(accountId));
        } else {
            properties.remove(PROP_ACCOUNT_ID);
        }
        return this;
    }

    /**
     * Sets the module
     *
     * @param module The module to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setModule(final String module) {
        this.module = module;
        if (null == module) {
            properties.remove(PROP_MODULE);
        } else {
            properties.put(PROP_MODULE, module);
        }
        return this;
    }

    /**
     * Sets the type
     *
     * @param type The type to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setType(final String type) {
        this.type = type;
        if (null == type) {
            properties.remove(PROP_TYPE);
        } else {
            properties.put(PROP_TYPE, type);
        }
        return this;
    }

    /**
     * Sets the displayName
     *
     * @param displayName The displayName to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setDisplayName(final String displayName) {
        this.displayName = displayName;
        if (null == displayName) {
            properties.remove(PROP_DISPLAY_NAME);
        } else {
            properties.put(PROP_DISPLAY_NAME, displayName);
        }
        return this;
    }

    /**
     * Sets the content
     *
     * @param content The content to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setContent(final String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the attachments
     *
     * @param attachments The attachments to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setAttachments(final List<Attachment> attachments) {
        this.attachments = attachments == null ? Collections.<Attachment> emptyList() : new ArrayList<Attachment>(attachments);
        return this;
    }

    /**
     * Adds specified attachment.
     *
     * @param attachment The attachment
     * @return This snippet with attachment added
     */
    public DefaultSnippet addAttachment(final Attachment attachment) {
        if (null != attachment) {
            List<Attachment> attachments = this.attachments;
            if (null == attachments) {
                attachments = new ArrayList<Attachment>(4);
                this.attachments = attachments;
            }
            attachments.add(attachment);
        }
        return this;
    }

    /**
     * Sets the miscellaneous JSON object.
     *
     * @param misc The miscellaneous JSON object to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setMisc(final Object misc) {
        if (null == misc) {
            this.misc = null;
            properties.remove(PROP_MISC);
        } else {
            Object obj;
            try {
                obj = new JSONTokener(misc.toString()).nextValue();
            } catch (final JSONException e) {
                // Not a valid JSON value
                return this;
            }
            this.misc = misc;
            properties.put(PROP_MISC, obj);
        }
        return this;
    }

    /**
     * Sets the shared flag
     *
     * @param shared The shared flag to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setShared(final boolean shared) {
        final Boolean b = Boolean.valueOf(shared);
        this.shared = b;
        properties.put(PROP_SHARED, b);
        return this;
    }

    /**
     * Sets the creator
     *
     * @param createdBy The creator to set
     * @return This snippet with argument applied
     */
    public DefaultSnippet setCreatedBy(final int createdBy) {
        this.createdBy = createdBy;
        if (createdBy >= 0) {
            properties.put(PROP_CREATED_BY, Integer.valueOf(createdBy));
        } else {
            properties.remove(PROP_CREATED_BY);
        }
        return this;
    }

    /**
     * Tests for existence of specified property.
     *
     * @param propName The property name
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsProperty(final String propName) {
        return properties.containsKey(propName);
    }

    /**
     * Gets the named property's value.
     *
     * @param propName The property name
     * @return The associated value or <code>null</code> if absent
     */
    public Object get(final String propName) {
        return properties.get(propName);
    }

    /**
     * Puts specified property.
     *
     * @param propName The property name
     * @param value The property value
     */
    public void put(final String propName, final Object value) {
        if (PROP_ACCOUNT_ID.equals(propName)) {
            setAccountId(null == value ? -1 : ((value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString())));
            return;
        }
        if (PROP_CREATED_BY.equals(propName)) {
            setCreatedBy(null == value ? -1 : ((value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString())));
            return;
        }
        if (PROP_DISPLAY_NAME.equals(propName)) {
            setDisplayName(null == value ? null : value.toString());
            return;
        }
        if (PROP_ID.equals(propName)) {
            setId(null == value ? null : value.toString());
            return;
        }
        if (PROP_MISC.equals(propName)) {
            setMisc(value);
            return;
        }
        if (PROP_MODULE.equals(propName)) {
            setModule(null == value ? null : value.toString());
            return;
        }
        if (PROP_SHARED.equals(propName)) {
            if (null != value) {
                setShared((value instanceof Boolean) ? ((Boolean) value).booleanValue() : Boolean.parseBoolean(value.toString()));
            } else {
                shared = null;
                properties.remove(PROP_SHARED);
            }
            return;
        }
        if (PROP_TYPE.equals(propName)) {
            setType(null == value ? null : value.toString());
            return;
        }
        properties.put(propName, value);
    }

    /**
     * Puts unnamed properties from given map.
     *
     * @param properties The map providing unnamed properties
     * @return This snippet with unnamed properties applied
     */
    public DefaultSnippet putUnnamedProperties(final Map<String, Object> properties) {
        if (null == properties || properties.isEmpty()) {
            return this;
        }
        final Map<String, Object> thisProps = this.properties;
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            final String propName = entry.getKey();
            if (!NAMED_PROPERTIES.contains(propName)) {
                thisProps.put(propName, entry.getValue());
            }
        }
        return this;
    }

    /**
     * Removes named property.
     *
     * @param propName The property name
     */
    public void remove(final String propName) {
        if (PROP_ACCOUNT_ID.equals(propName)) {
            accountId = -1;
            return;
        }
        if (PROP_CREATED_BY.equals(propName)) {
            createdBy = -1;
            return;
        }
        if (PROP_DISPLAY_NAME.equals(propName)) {
            displayName = null;
            return;
        }
        if (PROP_ID.equals(propName)) {
            id = null;
            return;
        }
        if (PROP_MISC.equals(propName)) {
            misc = null;
            return;
        }
        if (PROP_MODULE.equals(propName)) {
            module = null;
            return;
        }
        if (PROP_SHARED.equals(propName)) {
            shared = null;
            return;
        }
        if (PROP_TYPE.equals(propName)) {
            type = null;
            return;
        }
        properties.remove(propName);
    }

    /**
     * Clears this snippet's properties.
     */
    public void clear() {
        accountId = -1;
        createdBy = -1;
        displayName = null;
        id = null;
        misc = null;
        module = null;
        shared = null;
        type = null;
        properties.clear();
    }

    /**
     * Gets the properties' entry set.
     *
     * @return The entry set
     */
    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("DefaultSnippet [");
        final String delim = ", ";
        if (id != null) {
            builder.append("id=").append(id).append(delim);
        }
        if (accountId >= 0) {
            builder.append("accountId=").append(accountId).append(delim);
        }
        if (module != null) {
            builder.append("module=").append(module).append(delim);
        }
        if (type != null) {
            builder.append("type=").append(type).append(delim);
        }
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(delim);
        }
        if (content != null) {
            builder.append("content=").append(content).append(delim);
        }
        if (attachments != null) {
            builder.append("attachments=").append(attachments).append(delim);
        }
        if (misc != null) {
            builder.append("misc=").append(misc).append(delim);
        }
        if (shared != null) {
            builder.append("shared=").append(shared).append(delim);
        }
        if (createdBy >= 0) {
            builder.append("createdBy=").append(createdBy).append(delim);
        }
        if (properties != null) {
            builder.append("properties=").append(getUnnamedProperties());
        }
        builder.append(']');
        return builder.toString();
    }

}
