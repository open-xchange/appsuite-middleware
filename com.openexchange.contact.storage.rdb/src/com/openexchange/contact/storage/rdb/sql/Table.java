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

package com.openexchange.contact.storage.rdb.sql;

/**
 * {@link Table} - Encapsulates the relevant database table names.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum Table {
    /**
     * The 'prg_contacts' table
     */
    CONTACTS("prg_contacts"),
    /**
     * The 'del_contacts' table
     */
    DELETED_CONTACTS("del_contacts"),
    /**
     * The 'prg_contacts_image' table
     */
    IMAGES("prg_contacts_image"),
    /**
     * The 'del_contacts_image' table
     */
    DELETED_IMAGES("del_contacts_image"),
    /**
     * The 'prg_dlist' table
     */
    DISTLIST("prg_dlist"),
    /**
     * The 'del_dlist' table
     */
    DELETED_DISTLIST("del_dlist"),
    /**
     * The 'del_contacts_linkage' table
     */
    DELETED_LINKS("del_contacts_linkage"),
    /**
     * The 'object_use_count' table
     */
    OBJECT_USE_COUNT("object_use_count"),
    ;

    private final String name;

    private Table(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the table.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    public boolean isImageTable() {
        return Table.IMAGES.equals(this) || Table.DELETED_IMAGES.equals(this);
    }

    public boolean isDistListTable() {
        return Table.DISTLIST.equals(this) || Table.DELETED_DISTLIST.equals(this);
    }

    public boolean isContactTable() {
        return Table.CONTACTS.equals(this) || Table.DELETED_CONTACTS.equals(this);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
