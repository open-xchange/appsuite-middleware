package com.openxchange.documentation.tools.internal;
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

import java.util.List;

/**
 * {@link Property}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class Property {

    private String key;
    private String description;
    private String version;
    private String defaultValue;
    private boolean reloadable;
    private boolean configcascadeAware;
    private Object related;
    private String file;
    private String packageName;
    private List<String> tags;

    /**
     * Initializes a new {@link Property}.
     */
    public Property() {
        super();
    }

    /**
     * Gets the key
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key
     *
     * @param key The key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the version
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version
     *
     * @param version The version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the default_value
     *
     * @return The default_value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default_value
     *
     * @param default_value The default_value to set
     */
    public void setDefaultValue(String default_value) {
        this.defaultValue = default_value;
    }

    /**
     * Gets the reloadable
     *
     * @return The reloadable
     */
    public boolean isReloadable() {
        return reloadable;
    }

    /**
     * Sets the reloadable
     *
     * @param reloadable The reloadable to set
     */
    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    /**
     * Gets the configcascadeAware
     *
     * @return The configcascadeAware
     */
    public boolean isConfigcascadeAware() {
        return configcascadeAware;
    }

    /**
     * Sets the configcascadeAware
     *
     * @param configcascadeAware The configcascadeAware to set
     */
    public void setConfigcascadeAware(boolean configcascadeAware) {
        this.configcascadeAware = configcascadeAware;
    }

    /**
     * Gets the related
     *
     * @return The related
     */
    public Object getRelated() {
        return related;
    }

    /**
     * Sets the related
     *
     * @param related The related to set
     */
    public void setRelated(Object related) {
        this.related = related;
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the file
     *
     * @param file The file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the packageName
     *
     * @return The packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the packageName
     *
     * @param packageName The packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the tags
     *
     * @return The tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags
     *
     * @param tags The tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * @param term
     * @return
     */
    public boolean contains(String term) {
        if (key.contains(term)) {
            return true;
        }
        if (description.contains(term)) {
            return true;
        }
        if (defaultValue != null && defaultValue.contains(term)) {
            return true;
        }
        if (file != null && file.contains(term)) {
            return true;
        }
        if (packageName != null && packageName.contains(term)) {
            return true;
        }
        if (tags != null && tags.contains(term)) {
            return true;
        }
        if (version != null && version.contains(term)) {
            return true;
        }

        return false;
    }
}
