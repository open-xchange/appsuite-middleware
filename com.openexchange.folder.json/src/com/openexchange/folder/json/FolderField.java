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

package com.openexchange.folder.json;

/**
 * {@link FolderField} - Enumeration for folder fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum FolderField {

    /**
     * The folder identifier
     */
    ID(1, "id"),
    /**
     * The entity which created this folder
     */
    CREATED_BY(2, "created_by"),
    /**
     * The entity which modified this folder last time
     */
    MODIFIED_BY(3, "modified_by"),
    /**
     * The creation time stamp in requesting session's user time zone
     */
    CREATION_DATE(4, "creation_date"),
    /**
     * The last-modified time stamp in requesting session's user time zone
     */
    LAST_MODIFIED(5, "last_modified"),
    /**
     * The last-modified time stamp in UTC
     */
    LAST_MODIFIED_UTC(6, "last_modified_utc"),
    /**
     * The folder's parent folder identifier
     */
    FOLDER_ID(20, "folder_id"),
    /**
     * The folder name
     */
    FOLDER_NAME(300, "title"),
    /**
     * The folder module
     */
    MODULE(301, "module"),
    /**
     * The folder type
     */
    TYPE(302, "type"),
    /**
     * A boolean to indicate if folder contains subfolders
     */
    SUBFOLDERS(304, "subfolders"),
    /**
     * The rights for requesting session's user
     */
    OWN_RIGHTS(305, "own_rights"),
    /**
     * The permissions added to folder
     */
    PERMISSIONS_BITS(306, "permissions"),
    /**
     * The summary string
     */
    SUMMARY(307, "summary"),
    /**
     * A boolean indicating if this folder is a default folder
     */
    STANDARD_FOLDER(308, "standard_folder"),
    /**
     * The total number of objects held by this folder
     */
    TOTAL(309, "total"),
    /**
     * The number of new objects held by this folder
     */
    NEW(310, "new"),
    /**
     * The number of unread objects held by this folder
     */
    UNREAD(311, "unread"),
    /**
     * The number of deleted objects held by this folder
     */
    DELETED(312, "deleted"),
    /**
     * The folder's capabilities
     */
    CAPABILITIES(313, "capabilities"),
    /**
     * Folder's subscription
     */
    SUBSCRIBED(314, "subscribed"),
    /**
     * Subscribed subfolders
     */
    SUBSCR_SUBFLDS(315, "subscr_subflds"),
    /**
     * An integer denoting the default folder type.
     */
    STANDARD_FOLDER_TYPE(316, "standard_folder_type"),
    /**
     * The folder's supported capabilities
     */
    SUPPORTED_CAPABILITIES(317, "supported_capabilities"),
    /**
     * The folders account ID
     */
    ACCOUNT_ID(318, "account_id"),
    /**
     * The permissions bits
     */
    BITS(-1, "bits"),
    /**
     * The permission's entity
     */
    ENTITY(-1, "entity"),
    /**
     * The permission's group flag
     */
    GROUP(-1, "group"),
    /**
     * Mail address for an external permission
     */
    EMAIL_ADDRESS(-1, "email_address"),
    /**
     * Contact id for an external permission
     */
    CONTACT_ID(-1, "contact_id"),
    /**
     * Contact folder id for an external permission
     */
    CONTACT_FOLDER_ID(-1, "contact_folder"),
    /**
     * The date when an external permission should expire
     */
    EXPIRY_DATE(-1, "expiry_date"),
    /**
     * The date when an external permission should become active
     */
    ACTIVATION_DATE(-1, "activation_date"),
    /**
     * Display name for an external permission
     */
    DISPLAY_NAME(-1, "display_name"),
    /**
     * Password for an external permission
     */
    PASSWORD(-1, "password"),
    /**
     * The meta field
     */
    META(23, "meta");

    private final int column;

    private final String name;

    private FolderField(final int column, final String name) {
        this.column = column;
        this.name = name;
    }

    /**
     * Gets the column or <code>-1</code> if none available
     *
     * @return The column or <code>-1</code> if none available
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

}
