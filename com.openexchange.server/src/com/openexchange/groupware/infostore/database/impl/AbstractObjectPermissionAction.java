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

import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.AbstractDBAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link AbstractObjectPermissionAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractObjectPermissionAction extends AbstractDBAction {

    protected static final String INSERT_OBJECT_PERMISSION =
        "INSERT INTO object_permission (cid,permission_id,module,folder_id,object_id,created_by,shared_by,bits,last_modified,group_flag) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?);";

    protected static final String DELETE_OBJECT_PERMISSION =
        "DELETE FROM object_permission " +
        "WHERE cid=? AND object_id=?;";

    protected static final String UPDATE_OBJECT_PERMISSION =
        "UPDATE object_permission SET created_by=?,shared_by=?,bits=?,last_modified=? " +
        "WHERE cid=? AND object_id=? AND permission_id=?;";

    protected static final String DELETE_OBJECT_PERMISSION_ENTITY =
        "DELETE FROM object_permission " +
        "WHERE cid=? AND object_id=? AND permission_id=?;";

    protected static final String DELETE_DEL_OBJECT_PERMISSION_ENTITY =
        "DELETE FROM del_object_permission " +
        "WHERE cid=? AND object_id=? AND permission_id=?;";

    protected static final String REPLACE_DEL_OBJECT_PERMISSION =
        "REPLACE INTO del_object_permission (cid,permission_id,module,folder_id,object_id,created_by,shared_by,bits,last_modified,group_flag) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?);";

    /**
     * Initializes a new {@link AbstractObjectPermissionAction}.
     *
     * @param provider The database provider
     * @param context The context
     */
    protected AbstractObjectPermissionAction(DBProvider provider, Context context) {
        super();
        setProvider(provider);
        setContext(context);
    }

    @Override
    protected int doUpdates(List<UpdateBlock> updates) throws OXException {
        if (null != updates && 0 < updates.size()) {
            return super.doUpdates(updates);
        }
        return 0;
    }

}
