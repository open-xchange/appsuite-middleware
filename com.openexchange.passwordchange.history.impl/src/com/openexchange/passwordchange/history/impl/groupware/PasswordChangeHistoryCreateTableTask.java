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

package com.openexchange.passwordchange.history.impl.groupware;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link PasswordChangeHistoryCreateTableTask} - Creates the table "user_password_history"
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHistoryCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    private static final String TABLE_NAME = "user_password_history";

    private static String getHistoryTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(TABLE_NAME).append(" (");
        sb.append("id INT UNSIGNED NOT NULL AUTO_INCREMENT,");
        sb.append("cid INT UNSIGNED NOT NULL,");
        sb.append("uid INT UNSIGNED NOT NULL,");
        sb.append("created LONG NOT NULL,");
        sb.append("source VARCHAR(256) NULL DEFAULT NULL,");
        sb.append("ip VARCHAR(45) NULL DEFAULT NULL,");
        sb.append("PRIMARY KEY (id),");
        sb.append("INDEX context_and_user (cid, uid)");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        return sb.toString();
    }

    /**
     * Initializes a new {@link PasswordChangeHistoryCreateTableTask}.
     */
    public PasswordChangeHistoryCreateTableTask() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] {};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { TABLE_NAME };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        createTable(TABLE_NAME, getHistoryTable(), params.getConnection());
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { getHistoryTable() };
    }
}
