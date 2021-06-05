package com.openxchange.documentation.tools.internal;
import java.util.List;

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

/**
 * {@link YamlFile}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class YamlFile {

    private String feature_name;
    private String feature_description;
    private List<Property> properties;

    /**
     * Initializes a new {@link YamlFile}.
     */
    public YamlFile() {
        super();
    }

    /**
     * Gets the feature_description
     *
     * @return The feature_description
     */
    public String getFeature_description() {
        return feature_description;
    }

    /**
     * Sets the feature_description
     *
     * @param feature_description The feature_description to set
     */
    public void setFeature_description(String feature_description) {
        this.feature_description = feature_description;
    }

    /**
     * Gets the properties
     *
     * @return The properties
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Sets the properties
     *
     * @param properties The properties to set
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Gets the feature_name
     *
     * @return The feature_name
     */
    public String getFeature_name() {
        return feature_name;
    }

    /**
     * Sets the feature_name
     *
     * @param feature_name The feature_name to set
     */
    public void setFeature_name(String feature_name) {
        this.feature_name = feature_name;
    }
}
