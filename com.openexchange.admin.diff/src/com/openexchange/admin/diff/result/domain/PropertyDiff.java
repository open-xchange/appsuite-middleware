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

package com.openexchange.admin.diff.result.domain;


/**
 * {@link PropertyDiff}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class PropertyDiff {

    private final String fileName;

    private final String propertyName;

    private final String propertyValue;

    /**
     * Initializes a new {@link PropertyDiff}.
     * 
     * @param fileName
     * @param propertyName
     * @param propertyValue
     */
    public PropertyDiff(String fileName, String propertyName, String propertyValue) {
        this.fileName = fileName;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    /**
     * Gets the fileName
     * 
     * @return The fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the propertyName
     * 
     * @return The propertyName
     */
    protected String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the propertyValue
     * 
     * @return The propertyValue
     */
    protected String getPropertyValue() {
        return propertyValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.propertyValue == null) {
            return this.fileName + ": " + this.propertyName + "\n";
        }
        return this.fileName + ": " + this.propertyName + "=" + this.propertyValue + "\n";
    }
}
