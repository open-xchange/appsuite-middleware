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

package com.openexchange.groupware.infostore.facade.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;

/**
 * {@link NumberOfVersionsLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NumberOfVersionsLoader extends DbMetadataLoader<Integer> {

    /**
     * Initializes a new {@link NumberOfVersionsLoader}.
     *
     * @param provider The underlying database provider
     */
    public NumberOfVersionsLoader(DBProvider provider) {
        super(provider);
    }

    @Override
    protected DocumentMetadata set(DocumentMetadata document, Integer metadata) {
        if(metadata != null) {
            document.setNumberOfVersions(metadata.intValue());
        }
        return document;
    }

    @Override
    public Map<Integer, Integer> load(Collection<Integer> ids, Context context) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyMap();
        }
        final Map<Integer, Integer> numberOfVersions = new HashMap<Integer, Integer>(ids.size());
        List<Object> parameters = new ArrayList<Object>(ids.size() + 1);
        parameters.add(I(context.getContextId()));
        parameters.addAll(ids);
        String query = InfostoreQueryCatalog.getInstance().getNumberOfVersionsQueryForDocuments(I2i(ids));
        try {
            performQuery(context, query, new ResultProcessor<Void>() {

                @Override
                public Void process(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        numberOfVersions.put(I(rs.getInt(1)), I(rs.getInt(2) - 1));
                    }
                    return null;
                }
            }, parameters.toArray(new Object[parameters.size()]));
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return numberOfVersions;
    }

}
