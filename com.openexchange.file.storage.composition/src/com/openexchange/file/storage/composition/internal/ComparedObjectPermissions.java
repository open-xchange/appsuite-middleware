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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;

/**
 * {@link ComparedObjectPermissions}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ComparedObjectPermissions {

    private List<FileStorageGuestObjectPermission> addedGuestPermissions;
    private List<FileStorageObjectPermission> removedGuestPermissions;

    /**
     * Empty compared permissions with no added or removed guest permissions.
     */
    public static ComparedObjectPermissions EMPTY = new ComparedObjectPermissions((File) null, (File) null, null);

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param oldMetadata The old metadata, or <code>null</code> for new documents
     * @param newMetadata The new metadata
     * @param modifiedColumns The modified columns as supplied by the client, or <code>null</code> if not available
     */
    public ComparedObjectPermissions(File oldMetadata, File newMetadata, List<Field> modifiedColumns) {
        this(null == oldMetadata ? null: oldMetadata.getObjectPermissions(), null == newMetadata ? null : newMetadata.getObjectPermissions(), modifiedColumns);
    }

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param oldPermissions The old object permissions, or <code>null</code> for new documents
     * @param newPermissions The new object permissions
     * @param modifiedColumns The modified columns as supplied by the client, or <code>null</code> if not available
     */
    public ComparedObjectPermissions(List<FileStorageObjectPermission> oldPermissions, List<FileStorageObjectPermission> newPermissions, List<Field> modifiedColumns) {
        super();
        if (null != modifiedColumns && false == modifiedColumns.contains(Field.OBJECT_PERMISSIONS)) {
            /*
             * no object permissions modified
             */
            addedGuestPermissions = Collections.emptyList();
            removedGuestPermissions = Collections.emptyList();
        } else {
            /*
             * diff old / new permissions
             */
            addedGuestPermissions = getAddedGuestPermissions(newPermissions);
            removedGuestPermissions = getRemovedUserPermissions(oldPermissions, newPermissions);
        }
    }

    /**
     * Gets a value indicating whether the comparison yielded added guest permissions or not.
     *
     * @return <code>true</code> if there is at least one added guest permission, <code>false</code>, otherwise
     */
    public boolean hasAddedGuestPermissions() {
        return 0 < addedGuestPermissions.size();
    }
    /**
     * Gets a value indicating whether the comparison yielded removed guest permissions or not.
     *
     * @return <code>true</code> if there is at least one removed guest permission, <code>false</code>, otherwise
     */
    public boolean hasRemovedGuestPermissions() {
        return 0 < removedGuestPermissions.size();
    }

    /**
     * Gets all guest object permissions that are considered as "new", i.e. guest object permissions from the new document metadata that
     * are not yet resolved to a guest user entity, i.e. those permissions that were newly added by the client.
     *
     * @return The new guest object permissions, or an empty list of none were found
     */
    public List<FileStorageGuestObjectPermission> getAddedGuestPermissions() {
        return addedGuestPermissions;
    }

    /**
     * Gets all object permissions that were present in the object permissions of the old document metadata, but no longer appear in the
     * object permissions of the new document metadata.
     * <p/>
     * <b>Note:</b>
     * For now, this may both include regular internal users as well as guest users, while group permissions are ignored implicitly.
     *
     * @return The removed user object permissions
     */
    public List<FileStorageObjectPermission> getRemovedGuestPermissions() {
        return removedGuestPermissions;
    }

    /**
     * Extracts all guest object permissions from the supplied list that are not yet resolved to a guest user entity, i.e. those
     * permissions that were newly added by the client.
     *
     * @param permissions The object permissions to extract the guest permissions from, or <code>null</code> if there are none
     * @return The new guest object permissions, or an empty list of none were found
     */
    private static List<FileStorageGuestObjectPermission> getAddedGuestPermissions(List<FileStorageObjectPermission> permissions) {
        if (null == permissions || 0 == permissions.size()) {
            return Collections.emptyList();
        }
        List<FileStorageGuestObjectPermission> addedPermissions = new ArrayList<FileStorageGuestObjectPermission>();
        for (FileStorageObjectPermission newPermission : permissions) {
            if (FileStorageGuestObjectPermission.class.isInstance(newPermission) && 0 == newPermission.getEntity()) {
                addedPermissions.add((FileStorageGuestObjectPermission) newPermission);
            }
        }
        return addedPermissions;
    }

    /**
     * Extracts all object permissions that are present in the supplied old permission list, but no longer appear in the new object
     * permissions. Group permissions are ignored implicitly.
     *
     * @param oldPermissions The old object permissions, or <code>null</code> if there are none
     * @param newPermissions The new object permissions, or <code>null</code> if there are none
     * @return The removed user object permissions
     */
    private static List<FileStorageObjectPermission> getRemovedUserPermissions(List<FileStorageObjectPermission> oldPermissions, List<FileStorageObjectPermission> newPermissions) {
        if (null == oldPermissions || 0 == oldPermissions.size()) {
            return Collections.emptyList();
        }
        List<FileStorageObjectPermission> removedPermissions = new ArrayList<FileStorageObjectPermission>();
        for (FileStorageObjectPermission oldPermission : oldPermissions) {
            FileStorageObjectPermission matchingPermission = findByEntity(newPermissions, oldPermission.getEntity());
            if (null == matchingPermission) {
                removedPermissions.add(oldPermission);
            }
        }
        return removedPermissions;
    }

    private static FileStorageObjectPermission findByEntity(List<FileStorageObjectPermission> permissions, int entity) {
        if (null != permissions && 0 < permissions.size()) {
            for (FileStorageObjectPermission permission : permissions) {
                if (permission.getEntity() == entity) {
                    return permission;
                }
            }
        }
        return null;
    }

}
