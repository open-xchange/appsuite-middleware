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

package com.openexchange.file.storage.rdb.groupware;

import com.openexchange.groupware.update.ExtendedColumnCreationTask;
import com.openexchange.tools.update.Column;

/**
 * {@link FileStorageMetaDataColumnsTask}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FileStorageMetaDataColumnsTask extends ExtendedColumnCreationTask {

    private static final String TABLE_NAME = "filestorageAccount";
    private static final String[] DEPENDENCIES = new String[] {};

    //@formatter:off
    private final static Column[] NEW_COLUMNS = {
        new Column("metaData", "BLOB")
    };
    //@formatter:on

    @Override
    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected Column[] getColumns() {
        return NEW_COLUMNS;
    }
}
