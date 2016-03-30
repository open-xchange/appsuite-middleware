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

package com.openexchange.groupware.tasks.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.tasks.AttributeNames;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;

/**
 * Methods for dealing with the uid attribute of tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class UID implements Mapper<String> {

    public static final Mapper<String> SINGLETON = new UID();

    private UID() {
        super();
    }

    @Override
    public int getId() {
        return CommonObject.UID;
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsUid();
    }

    @Override
    public String getDBColumnName() {
        return "uid";
    }

    @Override
    public String getDisplayName() {
        return AttributeNames.UID;
    }

    @Override
    public void toDB(final PreparedStatement stmt, final int pos, final Task task) throws SQLException {
        stmt.setString(pos, task.getUid());
    }

    @Override
    public void fromDB(final ResultSet result, final int pos, final Task task) throws SQLException {
        final String uid = result.getString(pos);
        if (!result.wasNull()) {
            task.setUid(uid);
        }
    }

    @Override
    public boolean equals(final Task task1, final Task task2) {
        return Mapping.equals(task1.getUid(), task2.getUid());
    }

    @Override
    public String get(final Task task) {
        return task.getUid();
    }

    @Override
    public void set(final Task task, final String value) {
        task.setUid(value);
    }
}
