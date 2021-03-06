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

package com.openexchange.folderstorage.contact.field;

import com.openexchange.folderstorage.FolderField;

/**
 * {@link ContactsConfigField}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsConfigField extends FolderField {

    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3305;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.contacts.config";

    private static final long serialVersionUID = -9173502864695474175L;

    private static final ContactsConfigField INSTANCE = new ContactsConfigField();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ContactsConfigField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ContactsConfigField}.
     */
    private ContactsConfigField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

}
