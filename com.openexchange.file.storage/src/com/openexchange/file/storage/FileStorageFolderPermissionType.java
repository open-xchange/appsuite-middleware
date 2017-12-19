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

package com.openexchange.file.storage;

/**
 * {@link FileStorageFolderPermissionType} defines available permission types for filestorage folders.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public enum FileStorageFolderPermissionType {
    /**
     * The normal permission type
     */
    NORMAL(0),
    /**
     * Permissions of this type are going to be handed down to sub-folders
     */
    LEGATOR(1),
    /**
     * Permissions of this type are inherited permissions of a {@link FileStorageFolderPermissionType#LEGATOR} permission
     */
    INHERITED(2);

    private final int type;

    /**
     * Initializes a new {@link FileStorageFolderPermissionType}.
     */
    private FileStorageFolderPermissionType(int type) {
        this.type = type;
    }

    /**
     * Return the corresponding {@link FolderPermissionType} with the given type number or the {@link FileStorageFolderPermissionType#NORMAL} type in case the given type number is unknown.
     *
     * @param type The type number
     * @return The {@link FileStorageFolderPermissionType}
     */
    public static FileStorageFolderPermissionType getType(int type) {
        for (FileStorageFolderPermissionType tmp : FileStorageFolderPermissionType.values()) {
            if (tmp.type == type) {
                return tmp;
            }
        }
        return NORMAL;
    }

    /**
     * Returns the identifying number of this type
     *
     * @return the type number
     */
    public int getTypeNumber() {
        return type;
    }
}
