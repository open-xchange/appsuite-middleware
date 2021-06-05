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
package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;

/**
 * This class implements the global storage interface and creates a layer
 * between the abstract storage definition and a storage in a SQL accessible
 * database
 *
 * @author d7
 * @author cutmasta
 *
 */
public abstract class OXResourceSQLStorage extends OXResourceStorageInterface {

    @Override
    abstract public void changeLastModified(final int resource_id, final Context ctx, final Connection write_ox_con) throws StorageException;

    @Override
    abstract public void change(final Context ctx, final Resource res) throws StorageException;

    @Override
    abstract public int create(final Context ctx, final Resource res) throws StorageException;

    @Override
    abstract public void createRecoveryData(final int resource_id, final Context ctx, final Connection con) throws StorageException;

    @Override
    abstract public void deleteAllRecoveryData(final Context ctx, final Connection con) throws StorageException;

    abstract public void delete(final Context ctx, final int resource_id) throws StorageException;

    @Override
    abstract public void deleteRecoveryData(final int resource_id, final Context ctx, final Connection con) throws StorageException;

    @Override
    abstract public Resource[] list(final Context ctx, final String pattern) throws StorageException;

}
