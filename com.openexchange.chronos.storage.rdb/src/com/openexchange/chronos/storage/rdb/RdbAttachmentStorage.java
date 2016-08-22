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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.put;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttachmentStorage extends RdbStorage implements AttachmentStorage {

    /**
     * Initializes a new {@link RdbAttachmentStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param The transaction policy
     */
    public RdbAttachmentStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
    }

    @Override
    public List<Attachment> loadAttachments(int objectID) throws OXException {
        return loadAttachments(new int[] { objectID }).get(I(objectID));
    }

    @Override
    public Map<Integer, List<Attachment>> loadAttachments(int[] objectIDs) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAttachments(connection, context.getContextId(), objectIDs);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private static Map<Integer, List<Attachment>> selectAttachments(Connection connection, int contextID, int[] objectIDs) throws SQLException {
        Map<Integer, List<Attachment>> attachmentsById = new HashMap<Integer, List<Attachment>>();
        String sql = new StringBuilder()
            .append("SELECT attached,id,file_mimetype,file_size,filename,file_id,creation_date FROM prg_attachment ")
            .append("WHERE cid=? AND attached IN (").append(getParameters(objectIDs.length)).append(") AND module=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            stmt.setInt(parameterIndex++, com.openexchange.groupware.Types.APPOINTMENT);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attachment attachment = new Attachment();
                    attachment.setManagedId(String.valueOf(resultSet.getInt("id")));
                    attachment.setFormatType(resultSet.getString("file_mimetype"));
                    attachment.setSize(resultSet.getLong("file_size"));
                    attachment.setManagedId(resultSet.getString("filename"));
                    attachment.setContentId(resultSet.getString("file_id"));
                    attachment.setLastModified(new Date(resultSet.getLong("creation_date")));
                    put(attachmentsById, I(resultSet.getInt("attached")), attachment);
                }
            }
        }
        return attachmentsById;
    }

}
