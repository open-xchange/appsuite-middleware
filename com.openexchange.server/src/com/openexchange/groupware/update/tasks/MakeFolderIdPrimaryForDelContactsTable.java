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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.lang.Validate;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * Adds the column fid to the primary key of table del_contacts.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class MakeFolderIdPrimaryForDelContactsTable extends UpdateTaskAdapter {

    /**
     * Constant of the del_ table name to add the primary key
     */
    protected static final String DEL_CONTACTS = "del_contacts";

    /**
     * Constant of the prg_ table name to add the primary key
     */
    protected static final String PRG_CONTACTS = "prg_contacts";

    /**
     * Initializes a new {@link MakeFolderIdPrimaryForDelContactsTable}.
     */
    public MakeFolderIdPrimaryForDelContactsTable() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        Validate.notNull(params);

        int cid = params.getContextId();
        Connection con = Database.getNoTimeout(cid, true);

        try {
            con.setAutoCommit(false);

            if (Tools.hasPrimaryKey(con, DEL_CONTACTS)) {
                Tools.dropPrimaryKey(con, DEL_CONTACTS);
            }
            Tools.createPrimaryKey(con, DEL_CONTACTS, new String[] { "cid", "intfield01", "fid" });

            if (Tools.hasPrimaryKey(con, PRG_CONTACTS)) {
                Tools.dropPrimaryKey(con, PRG_CONTACTS);
            }
            Tools.createPrimaryKey(con, PRG_CONTACTS, new String[] { "cid", "intfield01", "fid" });

            con.commit();

        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencies() {
        return new String[0];
    }
}
