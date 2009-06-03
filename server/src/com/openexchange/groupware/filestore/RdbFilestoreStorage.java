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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.sql.DBUtils;

@OXExceptionSource(
    classId = Classes.RDB_FILESTORE_STORAGE,
    component = EnumComponent.FILESTORE
)
public class RdbFilestoreStorage extends FilestoreStorage {

    /**
     * For creating exceptions.
     */
    private static final FilestoreExceptionFactory EXCEPTION = new FilestoreExceptionFactory(RdbFilestoreStorage.class);

    private static final String SELECT = "SELECT uri, size, max_context FROM filestore WHERE id = ?";

    @Override
    @OXThrowsMultiple(
        category = { Category.SUBSYSTEM_OR_SERVICE_DOWN },
        desc = { "" },
        exceptionId = { 5 },
        msg = { "Can't access DBPool" }
    )
    public Filestore getFilestore(final int id) throws FilestoreException {
        final Connection con;
        try {
            con = DBPool.pickup();
        } catch (final DBPoolingException e) {
            throw EXCEPTION.create(5, e);
        }
        try {
            return getFilestore(con, id);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    @Override
    @OXThrowsMultiple(
        category = { Category.SETUP_ERROR, Category.SETUP_ERROR, Category.CODE_ERROR },
        desc = { "", "", "" },
        exceptionId = { 3, 4, 6 },
        msg = { "Cannot find filestore with id %1$d.", "Cannot create URI from \"%1$s\".", "Got SQL Exception" }
    )
    public Filestore getFilestore(Connection con, int id) throws FilestoreException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1,id);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw EXCEPTION.create(3, I(id));
            }
            final FilestoreImpl filestore = new FilestoreImpl();
            filestore.setId(id);
            String tmp = null;
            try {
                tmp = result.getString("uri");
                filestore.setUri(new URI(tmp));
            } catch (final URISyntaxException e) {
                throw EXCEPTION.create(4, e, tmp);
            }
            filestore.setSize(result.getLong("size"));
            filestore.setMaxContext(result.getLong("max_context"));
            return filestore;
        } catch (SQLException e) {
            throw EXCEPTION.create(6, e);
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }
    }
}
