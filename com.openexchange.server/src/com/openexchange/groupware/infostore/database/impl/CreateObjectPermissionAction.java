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
 * {@link CreateObjectPermissionAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateObjectPermissionAction extends AbstractObjectPermissionAction {

    private final DocumentMetadata document;

    /**
     * Initializes a new {@link CreateObjectPermissionAction}.
     *
     * @param provider The database provider
     * @param context The context
     * @param document The document to create the object permissions for
     */
    public CreateObjectPermissionAction(DBProvider provider, Context context, DocumentMetadata document) {
        super(provider, context);
        this.document = document;
    }

    @Override
    public void perform() throws OXException {
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        List<ObjectPermission> objectPermissions = document.getObjectPermissions();
        if (null != objectPermissions) {
            for (final ObjectPermission objectPermission : objectPermissions) {
                updates.add(new Update(INSERT_OBJECT_PERMISSION) {

                    @Override
                    public void fillStatement() throws SQLException {

                        // shared by == session user
                        // created by == object created by

                        // (cid, permission_id, module, folder_id, object_id, created_by, shared_by, bits, group_flag)
                        stmt.setInt(1, getContext().getContextId());
                        stmt.setInt(2, objectPermission.getEntity());
                        stmt.setInt(3, FolderObject.INFOSTORE);
                        stmt.setInt(4, (int) document.getFolderId());
                        stmt.setInt(5, document.getId());
                        stmt.setInt(6, document.getModifiedBy());
                        stmt.setInt(7, document.getCreatedBy());
                        stmt.setInt(8, objectPermission.getPermissions());
                        stmt.setLong(9, document.getLastModified().getTime());
                        stmt.setBoolean(10, objectPermission.isGroup());
                    }
                });
            }
        }
        doUpdates(updates);
    }

    @Override
    protected void undoAction() throws OXException {
        doUpdates(new Update(DELETE_OBJECT_PERMISSION) {

            @Override
            public void fillStatement() throws SQLException {
                stmt.setInt(1, getContext().getContextId());
                stmt.setInt(2, document.getId());
            }
        });
    }

}
