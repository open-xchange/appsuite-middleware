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

package com.openexchange.datatypes.genericonf.storage.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.IterationBreak;


/**
 * {@link UpdateIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class UpdateIterator implements MapIterator<String, Object> {

    private Map<String, Object> original;

    private static final String UPDATE_STRING = "UPDATE genconf_attributes_strings SET value = ? WHERE cid = ? AND id = ? AND name = ?";
    private static final String UPDATE_BOOL = "UPDATE genconf_attributes_bools SET value = ? WHERE cid = ? AND id = ? AND name = ?";

    private static final String DELETE_STRING = "DELETE FROM genconf_attributes_strings WHERE cid = ? AND id = ? AND name = ?";
    private static final String DELETE_BOOL = "DELETE FROM genconf_attributes_bools WHERE cid = ? AND id = ? AND name = ?";


    private final Map<Class<?>, PreparedStatement> updateStatements = new HashMap<>();
    private final List<PreparedStatement> deleteStatements = new LinkedList<>();

    private SQLException exception;

    private final InsertIterator insertIterator = new InsertIterator();

    @Override
    public void handle(String name, Object value) throws IterationBreak {
        try {
            if (original.containsKey(name)) {
                if (value != null) {
                    PreparedStatement update = updateStatements.get(value.getClass());
                    update.setObject(1, value);
                    update.setString(4, name);
                    update.execute();
                } else {
                    for(PreparedStatement delete : deleteStatements) {
                        delete.setString(3, name);
                        delete.execute();
                        if (delete.getUpdateCount() > 0) {
                            break;
                        }
                    }
                }
            } else if (value != null){
                insertIterator.handle(name, value);
            }
        } catch (SQLException x) {
            exception = x;
            throw new IterationBreak();
        }
    }

    public void setOriginal(Map<String, Object> original) {
        this.original = original;
    }

    public void prepareStatements(TX tx) throws SQLException {
        insertIterator.prepareStatements(tx);
        PreparedStatement updateString = tx.prepare(UPDATE_STRING);
        PreparedStatement updateBool = tx.prepare(UPDATE_BOOL);

        updateStatements.put(String.class, updateString);
        updateStatements.put(Boolean.class, updateBool);

        PreparedStatement deleteString = tx.prepare(DELETE_STRING);
        PreparedStatement deleteBool = tx.prepare(DELETE_BOOL);

        deleteStatements.add(deleteString);
        deleteStatements.add(deleteBool);
    }

    public void setIds(int contextId, int id) throws SQLException {
        insertIterator.setIds(contextId, id);
        for(PreparedStatement updateStatement : updateStatements.values()) {
            updateStatement.setInt(2, contextId);
            updateStatement.setInt(3, id);
        }

        for(PreparedStatement deleteStatement : deleteStatements) {
            deleteStatement.setInt(1, contextId);
            deleteStatement.setInt(2, id);
        }
    }

    public void throwException() throws SQLException {
        insertIterator.throwException();
        if (null != exception) {
            throw exception;
        }
    }

    public void close() {
        List<PreparedStatement> allStatements = new ArrayList<PreparedStatement>(updateStatements.size()+deleteStatements.size());
        Collection<PreparedStatement> updates = updateStatements.values();

        allStatements.addAll(updates);
        allStatements.addAll(deleteStatements);

        for (PreparedStatement preparedStatement : allStatements) {
            try {
                preparedStatement.close();
            } catch (SQLException x) {
                //IGNORE
            }
        }

    }

}
