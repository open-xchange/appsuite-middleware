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

package com.openexchange.userfeedback.fields;

/**
 * {@link UserFeedbackField}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class UserFeedbackField {

    private final String displayName;

    private final String name;

    private final boolean providedByClient;

    private final int storageSize;

    /**
     * Initializes a new {@link UserFeedbackField}.
     * 
     * @param lDisplayName String The display name of a field which is used for exports
     * @param lName String The internally used name of the field
     * @param lProvidedByClient boolean Defines if the fields should be provided by the client
     */
    public UserFeedbackField(String lDisplayName, String lName, boolean lProvidedByClient) {
        this(lDisplayName, lName, lProvidedByClient, 0);
    }

    /**
     * 
     * Initializes a new {@link UserFeedbackField}.
     * 
     * @param lDisplayName String The display name of a field which is used for exports
     * @param lName String The internally used name of the field
     * @param lProvidedByClient boolean Defines if the fields should be provided by the client
     * @param lStorageSize int Defines the length of the field
     */
    public UserFeedbackField(String lDisplayName, String lName, boolean lProvidedByClient, int lStorageSize) {
        this.displayName = lDisplayName;
        this.name = lName;
        this.providedByClient = lProvidedByClient;
        this.storageSize = lStorageSize;
    }

    /**
     * Returns the display name
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns if the field is expected to be provided by the client
     * 
     * @return if the field should be provided by the client
     */
    public boolean isProvidedByClient() {
        return providedByClient;
    }

    /**
     * Returns the storage size
     * 
     * @return the storage size
     */
    public int getStorageSize() {
        return storageSize;
    }
}
