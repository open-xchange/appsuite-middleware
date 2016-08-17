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
     * The 'prg_contacts_linkage' table
     */
    LINKS("prg_contacts_linkage"),
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
