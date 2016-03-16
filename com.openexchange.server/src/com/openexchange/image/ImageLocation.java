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

package com.openexchange.image;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link ImageLocation} - An image location description.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageLocation {

    public static final String PROPERTY_REGISTRATION_NAME = "registrationName";

    public static final String PROPERTY_NO_ROUTE = "__noRoute";

    /**
     * The builder for a {@link ImageLocation}.
     */
    public static final class Builder {
        protected String imageId;
        protected String accountId;
        protected String folder;
        protected String id;
        protected String timestamp;
        protected String optImageHost;
        protected String registrationName;

        public Builder() {
            super();
            this.imageId = null;
        }
        public Builder(final String imageId) {
            super();
            this.imageId = imageId;
        }
        public Builder imageId(final String imageId) {
            this.imageId = imageId; return this;
        }
        public Builder accountId(final String accountId) {
            this.accountId = accountId; return this;
        }
        public Builder folder(final String folder) {
            this.folder = folder; return this;
        }
        public Builder folder(final Object folder) {
            this.folder = folder.toString(); return this;
        }
        public Builder id(final String id) {
            this.id = id; return this;
        }
        public Builder id(final Object id) {
            this.id = id.toString(); return this;
        }
        public Builder timestamp(final String timestamp) {
            this.timestamp = timestamp; return this;
        }
        public Builder optImageHost(final String optImageHost) {
            this.optImageHost = optImageHost; return this;
        }
        public Builder registrationName(final String registrationName) {
            this.registrationName = registrationName; return this;
        }
        public String getRegistrationName() {
            return registrationName;
        }
        public ImageLocation build() {
            return new ImageLocation(this);
        }
    }

    private final String accountId;
    private final String folder;
    private final String id;
    private final String imageId;
    private final String timestamp;
    private final String optImageHost;
    private final ConcurrentMap<String, Object> properties;

    /**
     * Initializes a new {@link ImageLocation}.
     *
     * @param builder The builder
     */
    protected ImageLocation(final Builder builder) {
        super();
        properties = new ConcurrentHashMap<String, Object>(2, 0.9f, 1);
        this.accountId = builder.accountId;
        this.folder = builder.folder;
        this.id = builder.id;
        this.imageId = builder.imageId;
        this.timestamp = builder.timestamp;
        this.optImageHost = builder.optImageHost;
    }

    /**
     * Puts specified property if none associated with given name before.
     *
     * @param name The name
     * @param value The value
     * @return <code>true</code> if property has been put; otherwise <code>false</code> if another already exists
     */
    public boolean putPropertyIfAbsent(final String name, final Object value) {
        return null == properties.putIfAbsent(name, value);
    }

    /**
     * Checks presence of denoted property.
     *
     * @param name The name
     * @return <code>true</code> if present; otherwise <code>false</code> if absent
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets denoted property
     *
     * @param name The name
     * @return The associated value
     */
    public Object getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * Puts specified property.
     *
     * @param name The name
     * @param value The value associated with the name
     */
    public void putProperty(final String name, final Object value) {
        properties.put(name, value);
    }

    /**
     * Removes denoted property.
     *
     * @param name The property name
     */
    public void removeProperty(final String name) {
        properties.remove(name);
    }

    /**
     * Clears all properties.
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Gets a set of all property names
     *
     * @return The property names
     */
    public Set<String> propertyNames() {
        return properties.keySet();
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder identifier
     *
     * @return The folder identifier
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the object identifier
     *
     * @return The object identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the time stamp
     *
     * @return The time stamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the optional image host.
     *
     * @return The image host or an empty string
     */
    public String getOptImageHost() {
        return optImageHost;
    }

    /**
     * Gets the image identifier
     *
     * @return The image identifier
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Gets the registration name
     *
     * @return The registration name
     */
    public String getRegistrationName() {
        return (String) properties.get(PROPERTY_REGISTRATION_NAME);
    }

    /**
     * Sets the registration name
     *
     * @param registrationName The registration name to set
     */
    public void setRegistrationName(final String registrationName) {
        properties.putIfAbsent(PROPERTY_REGISTRATION_NAME, registrationName);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("ImageLocation [");
        if (accountId != null) {
            builder.append("accountId=").append(accountId).append(", ");
        }
        if (folder != null) {
            builder.append("folder=").append(folder).append(", ");
        }
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (imageId != null) {
            builder.append("imageId=").append(imageId).append(", ");
        }
        if (timestamp != null) {
            builder.append("timestamp=").append(timestamp).append(", ");
        }
        if (properties != null && !properties.isEmpty()) {
            builder.append("properties=").append(properties);
        }
        builder.append(']');
        return builder.toString();
    }

}
