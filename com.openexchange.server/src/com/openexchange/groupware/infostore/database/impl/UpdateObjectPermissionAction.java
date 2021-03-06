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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link UpdateObjectPermissionAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateObjectPermissionAction extends AbstractObjectPermissionAction {

    private final DocumentMetadata document;
    private final DocumentMetadata oldDocument;

    /**
     * Initializes a new {@link UpdateObjectPermissionAction}.
     *
     * @param provider The database provider
     * @param context The context
     * @param document The document holding the updated object permissions
     * @param oldDocument The document being updated
     */
    public UpdateObjectPermissionAction(DBProvider provider, Context context, DocumentMetadata document, DocumentMetadata oldDocument) {
        super(provider, context);
        this.document = document;
        this.oldDocument = oldDocument;
    }

    @Override
    public void perform() throws OXException {
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        ObjectPermissionDelta delta = new ObjectPermissionDelta(oldDocument.getObjectPermissions(), document.getObjectPermissions());
        for (final ObjectPermission removedPermission : delta.removedPermissions) {
            /*
             * delete removed object permissions
             */
            updates.add(new Update(DELETE_OBJECT_PERMISSION_ENTITY) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                    stmt.setInt(2, oldDocument.getId());
                    stmt.setInt(3, removedPermission.getEntity());
                }
            });
            /*
             * insert tombstone record for each removed object permission
             */
            updates.add(new Update(REPLACE_DEL_OBJECT_PERMISSION) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                    stmt.setInt(2, removedPermission.getEntity());
                    stmt.setInt(3, FolderObject.INFOSTORE);
                    stmt.setInt(4, (int) oldDocument.getFolderId());
                    stmt.setInt(5, oldDocument.getId());
                    stmt.setInt(6, oldDocument.getModifiedBy());
                    stmt.setInt(7, oldDocument.getCreatedBy());
                    stmt.setInt(8, removedPermission.getPermissions());
                    stmt.setLong(9, document.getSequenceNumber());
                    stmt.setBoolean(10, removedPermission.isGroup());
                }
            });
        }
        for (final ObjectPermission updatedPermission : delta.updatedPermissions) {
            /*
             * update modified object permissions
             */
            updates.add(new Update(UPDATE_OBJECT_PERMISSION) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, document.getModifiedBy());
                    stmt.setInt(2, document.getCreatedBy());
                    stmt.setInt(3, updatedPermission.getPermissions());
                    stmt.setLong(4, document.getSequenceNumber());
                    stmt.setInt(5, getContext().getContextId());
                    stmt.setInt(6, document.getId());
                    stmt.setInt(7, updatedPermission.getEntity());
                }
            });
        }
        for (final ObjectPermission addedPermission : delta.addedPermissions) {
            /*
             * insert added object permissions
             */
            updates.add(new Update(INSERT_OBJECT_PERMISSION) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                    stmt.setInt(2, addedPermission.getEntity());
                    stmt.setInt(3, FolderObject.INFOSTORE);
                    stmt.setInt(4, (int) document.getFolderId());
                    stmt.setInt(5, document.getId());
                    stmt.setInt(6, document.getModifiedBy());
                    stmt.setInt(7, document.getCreatedBy());
                    stmt.setInt(8, addedPermission.getPermissions());
                    stmt.setLong(9, document.getSequenceNumber());
                    stmt.setBoolean(10, addedPermission.isGroup());
                }
            });
        }
        doUpdates(updates);
    }

    @Override
    protected void undoAction() throws OXException {
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        /*
         * delete any previously assigned permissions
         */
        updates.add(new Update(DELETE_OBJECT_PERMISSION) {

            @Override
            public void fillStatement() throws SQLException {
                stmt.setInt(1, getContext().getContextId());
                stmt.setInt(2, document.getId());
            }
        });
        List<ObjectPermission> objectPermissions = oldDocument.getObjectPermissions();
        if (null != objectPermissions) {
            for (final ObjectPermission objectPermission : objectPermissions) {
                /*
                 * restore old permissions
                 */
                updates.add(new Update(INSERT_OBJECT_PERMISSION) {

                    @Override
                    public void fillStatement() throws SQLException {
                        stmt.setInt(1, getContext().getContextId());
                        stmt.setInt(2, objectPermission.getEntity());
                        stmt.setInt(3, FolderObject.INFOSTORE);
                        stmt.setInt(4, (int) oldDocument.getFolderId());
                        stmt.setInt(5, oldDocument.getId());
                        stmt.setInt(6, oldDocument.getModifiedBy());
                        stmt.setInt(7, oldDocument.getCreatedBy());
                        stmt.setInt(8, objectPermission.getPermissions());
                        stmt.setLong(9, oldDocument.getSequenceNumber());
                        stmt.setBoolean(10, objectPermission.isGroup());
                    }
                });
                /*
                 * undo possibly generated tombstones
                 */
                updates.add(new Update(DELETE_DEL_OBJECT_PERMISSION_ENTITY) {

                    @Override
                    public void fillStatement() throws SQLException {
                        stmt.setInt(1, getContext().getContextId());
                        stmt.setInt(2, oldDocument.getId());
                        stmt.setInt(3, objectPermission.getEntity());
                    }
                });
            }
        }
        doUpdates(updates);
    }

    private static class ObjectPermissionDelta {

        final List<ObjectPermission> addedPermissions;
        final List<ObjectPermission> removedPermissions;
        final List<ObjectPermission> updatedPermissions;

        /**
         * Initializes a new {@link ObjectPermissionDelta}.
         *
         * @param oldPermissions The previously assigned object permissions
         * @param newPermissions The updated object permissions
         */
        ObjectPermissionDelta(List<ObjectPermission> oldPermissions, List<ObjectPermission> newPermissions) {
            super();
            if (null == oldPermissions || 0 == oldPermissions.size()) {
                addedPermissions = null == newPermissions ? Collections.<ObjectPermission>emptyList() : newPermissions;
                removedPermissions = Collections.emptyList();
                updatedPermissions = Collections.emptyList();
            } else if (null == newPermissions || 0 == newPermissions.size()) {
                addedPermissions = Collections.emptyList();
                removedPermissions = oldPermissions;
                updatedPermissions = Collections.emptyList();
            } else {
                addedPermissions = new ArrayList<ObjectPermission>();
                updatedPermissions = new ArrayList<ObjectPermission>();
                removedPermissions = new ArrayList<ObjectPermission>();
                /*
                 * collect added/updated permissions
                 */
                for (ObjectPermission newPermission : newPermissions) {
                    ObjectPermission matchingPermission = findPermission(newPermission.getEntity(), oldPermissions);
                    if (null == matchingPermission) {
                        addedPermissions.add(newPermission);
                    } else if (false == matchingPermission.equals(newPermission)) {
                        updatedPermissions.add(newPermission);
                    }
                }
                /*
                 * check for removed permissions
                 */
                for (ObjectPermission oldPermission : oldPermissions) {
                    ObjectPermission matchingPermission = findPermission(oldPermission.getEntity(), newPermissions);
                    if (null == matchingPermission) {
                        removedPermissions.add(oldPermission);
                    }
                }
            }
        }

        private static ObjectPermission findPermission(int entity, Collection<ObjectPermission> permissions) {
            if (null != permissions) {
                for (ObjectPermission permission : permissions) {
                    if (permission.getEntity() == entity) {
                        return permission;
                    }
                }
            }
            return null;
        }

    }

}
