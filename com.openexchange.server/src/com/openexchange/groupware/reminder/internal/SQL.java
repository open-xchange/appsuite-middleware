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

package com.openexchange.groupware.reminder.internal;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SQL {

    public static final String sqlInsert =
        "INSERT INTO reminder (object_id,cid,target_id,module,userid,alarm,"
        + "recurrence,last_modified,folder) VALUES (?,?,?,?,?,?,?,?,?)";

    public static final String sqlUpdatebyId =
        "UPDATE reminder SET alarm=?,recurrence=?,description=?,last_modified=?,"
        + "folder=? WHERE cid=? AND object_id=?";

    public static final String sqlUpdate =
        "UPDATE reminder SET alarm=?,recurrence=?,description=?,last_modified=?,"
        + "folder=? WHERE cid=? AND target_id=? AND module=? AND userid=?";

    public static final String DELETE_WITH_ID = "DELETE FROM reminder WHERE cid=? AND object_id=?";

    public static final String sqlDelete =
        "DELETE FROM reminder WHERE cid=? AND target_id=? AND module=? AND userid=?";

    public static final String sqlDeleteReminderOfObject =
        "DELETE FROM reminder WHERE cid=? AND target_id=? AND module=?";

    public static final String sqlLoadById =
        "SELECT object_id,target_id,module,userid,alarm,recurrence,description,"
        + "folder,last_modified FROM reminder WHERE cid=? AND object_id=?";

    public static final String sqlLoad =
        "SELECT object_id,target_id,module,userid,alarm,recurrence,description,"
        + "folder,last_modified FROM reminder WHERE cid=? AND target_id=? "
        + "AND module=? AND userid=?";

    public static final String sqlLoadMultiple =
        "SELECT object_id,target_id,module,userid,alarm,recurrence,description,"
        + "folder,last_modified FROM reminder WHERE cid=? AND module=? "
        + "AND userid=? AND target_id IN (";

    public static final String sqlListByTargetId =
        "SELECT object_id,target_id,module,userid,alarm,recurrence,description,"
        + "folder,last_modified FROM reminder WHERE cid=? AND module=? AND target_id=?";

    public static final String SELECT_RANGE = "SELECT object_id,target_id,module,userid,alarm,recurrence,description,folder,last_modified FROM reminder WHERE cid=? AND userid=? AND module!=1 AND alarm<=?";

    public static final String sqlModified =
        "SELECT object_id,target_id,module,userid,alarm,recurrence,description,"
        + "folder,last_modified FROM reminder WHERE cid=? AND userid=? AND module!=1 AND last_modified>?";

    /**
     * Prevent instantiation.
     */
    private SQL() {
        super();
    }
}
