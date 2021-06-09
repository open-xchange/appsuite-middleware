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

import java.sql.Connection;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * Creates the table infostoreReservedPaths to exclusively create path and file names through the WebDAV interface.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFilenameReservationsCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    /**
     *
     */
    private static final String INFOSTORE_RESERVED_PATHS = "infostoreReservedPaths";

    public InfostoreFilenameReservationsCreateTableTask() {
        super();
    }

    private String getTableSQL() {
        return "CREATE TABLE infostoreReservedPaths (" +
                " cid INT4 unsigned NOT NULL," +
                " folder INT4 unsigned NOT NULL, " +
                " name VARCHAR(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, " +
                " uuid BINARY(16) NOT NULL, " +
                " PRIMARY KEY (cid, uuid) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { getTableSQL() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        createTable(INFOSTORE_RESERVED_PATHS, getTableSQL(), con);
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InfostoreFilenameReservationsCreateTableTask.class);
        logger.info("UpdateTask ''{}'' successfully performed!", InfostoreFilenameReservationsCreateTableTask.class.getSimpleName());
    }

    @Override
    public String[] requiredTables() {
        return new String[] {};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { INFOSTORE_RESERVED_PATHS };
    }

}
