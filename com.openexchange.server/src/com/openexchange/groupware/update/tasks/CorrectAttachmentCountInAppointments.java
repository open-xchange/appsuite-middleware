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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.groupware.update.SimpleStatementsUpdateTask;


/**
 * {@link CorrectAttachmentCountInAppointments}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CorrectAttachmentCountInAppointments extends SimpleStatementsUpdateTask {

    @Override
    protected boolean shouldRun(Connection con) throws SQLException {
        return Databases.tableExists(con, "prg_dates");
    }

    @Override
    protected void statements() {
        add("UPDATE prg_dates AS d SET d.intfield08 = (SELECT count(*) FROM prg_attachment AS a WHERE a.cid=d.cid AND a.module = 1 AND a.attached = d.intfield01)");
    }


}
