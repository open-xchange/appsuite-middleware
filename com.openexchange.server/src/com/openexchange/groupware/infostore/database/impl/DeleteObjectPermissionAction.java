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
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link DeleteObjectPermissionAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DeleteObjectPermissionAction extends AbstractObjectPermissionAction {

    private final List<DocumentMetadata> documents;

    /**
     * Initializes a new {@link DeleteObjectPermissionAction}.
     *
     * @param provider The database provider
     * @param context The context
     * @param documents The documents being deleted
     */
    public DeleteObjectPermissionAction(DBProvider provider, Context context, List<DocumentMetadata> documents) {
        super(provider, context);
        this.documents = documents;
    }

    @Override
    public void perform() throws OXException {
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        for (final DocumentMetadata document : documents) {
            List<ObjectPermission> objectPermissions = document.getObjectPermissions();
            if (null != objectPermissions) {
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
                /*
                 * insert tombstone record for each removed object permission
                 */
                for (final ObjectPermission objectPermission : objectPermissions) {
                    updates.add(new Update(REPLACE_DEL_OBJECT_PERMISSION) {

                        @Override
                        public void fillStatement() throws SQLException {
                            stmt.setInt(1, getContext().getContextId());
                            stmt.setInt(2, objectPermission.getEntity());
                            stmt.setInt(3, FolderObject.INFOSTORE);
                            stmt.setInt(4, (int) document.getFolderId());
                            stmt.setInt(5, document.getId());
                            stmt.setInt(6, document.getModifiedBy());
                            stmt.setInt(7, document.getCreatedBy());
                            stmt.setInt(8, objectPermission.getPermissions());
                            stmt.setLong(9, document.getSequenceNumber());
                            stmt.setBoolean(10, objectPermission.isGroup());
                        }
                    });
                }
            }
        }
        doUpdates(updates);
    }

    @Override
    protected void undoAction() throws OXException {
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        for (final DocumentMetadata document : documents) {
            List<ObjectPermission> objectPermissions = document.getObjectPermissions();
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
                            stmt.setInt(4, (int) document.getFolderId());
                            stmt.setInt(5, document.getId());
                            stmt.setInt(6, document.getModifiedBy());
                            stmt.setInt(7, document.getCreatedBy());
                            stmt.setInt(8, objectPermission.getPermissions());
                            stmt.setLong(9, document.getSequenceNumber());
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
                            stmt.setInt(2, document.getId());
                            stmt.setInt(3, objectPermission.getEntity());
                        }
                    });
                }
            }
        }
        doUpdates(updates);
    }

}
