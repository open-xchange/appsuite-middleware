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

package com.openexchange.groupware.infostore.facade.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;

/**
 * {@link ObjectPermissionLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ObjectPermissionLoader extends DbMetadataLoader<List<ObjectPermission>> {

    private static final ResultProcessor<Map<Integer, List<ObjectPermission>>> RESULT_PROCESSOR = new ResultProcessor<Map<Integer, List<ObjectPermission>>>() {

        @Override
        public Map<Integer, List<ObjectPermission>> process(ResultSet results) throws SQLException {
            Map<Integer, List<ObjectPermission>> objectPermissions = new HashMap<Integer, List<ObjectPermission>>();
            while (results.next()) {
                Integer id = Integer.valueOf(results.getInt(1));
                List<ObjectPermission> permissions = objectPermissions.get(id);
                if (null == permissions) {
                    permissions = new ArrayList<ObjectPermission>();
                    objectPermissions.put(id, permissions);
                }
                permissions.add(new ObjectPermission(results.getInt(2), results.getBoolean(3), results.getInt(4)));
            }
            return objectPermissions;
        }
    };

    /**
     * Initializes a new {@link ObjectPermissionLoader}.
     *
     * @param provider The underlying database provider
     */
    public ObjectPermissionLoader(DBProvider provider) {
        super(provider);
    }

    @Override
    protected DocumentMetadata set(DocumentMetadata document, List<ObjectPermission> metadata) {
        document.setObjectPermissions(metadata);
        return document;
    }

    @Override
    public Map<Integer, List<ObjectPermission>> load(Collection<Integer> ids, Context context) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyMap();
        }
        List<Object> parameters = new ArrayList<Object>(ids.size() + 1);
        parameters.add(Integer.valueOf(context.getContextId()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT object_id,permission_id,group_flag,bits FROM object_permission WHERE cid=? AND object_id");
        if (1 == ids.size()) {
            stringBuilder.append("=?;");
            Integer id = ids.iterator().next();
            parameters.add(id);
        } else {
            Iterator<Integer> iterator = ids.iterator();
            stringBuilder.append(" IN (?");
            Integer id = iterator.next();
            parameters.add(id);
            do {
                stringBuilder.append(",?");
                id = iterator.next();
                parameters.add(id);
            } while (iterator.hasNext());
            stringBuilder.append(");");
        }
        try {
            return performQuery(context, stringBuilder.toString(), RESULT_PROCESSOR, parameters.toArray(new Object[parameters.size()]));
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

    /**
     * Loads additional metadata for all documents in a folder and puts them into a map, ready to be used for further result processing.
     *
     * @param folderID The folder identifiers of the documents to load the metadata for
     * @param context The context
     * @return A map holding the metadata (or <code>null</code>) to a document's id
     */
    public Map<Integer, List<ObjectPermission>> load(long folderID, Context context) throws OXException {
        String query = "SELECT object_id,permission_id,group_flag,bits FROM object_permission WHERE cid=? AND folder_id=?;";
        try {
            return performQuery(context, query, RESULT_PROCESSOR, Integer.valueOf(context.getContextId()), Integer.valueOf((int)folderID));
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

}
