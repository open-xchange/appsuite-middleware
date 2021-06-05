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

package com.openexchange.modules.storage.sql;

import java.util.List;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;


/**
 * {@link Builder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Builder<T extends Model<T>> {

    private final Metadata<T> metadata;
    private String tableName = null;

    public Builder(Metadata<T> metadata) {
        this.metadata = metadata;
    }

    public INSERT insert(List<Attribute<T>> attributes, List<String> extraFields) {
        INSERT insert = new INSERT().INTO(getTableName());
        for (Attribute<T> attribute : attributes) {
            insert = insert.SET(attribute.getName(), Constant.PLACEHOLDER);
        }
        for (String field : extraFields) {
            insert = insert.SET(field, Constant.PLACEHOLDER);
        }
        return insert;
    }

    public UPDATE update(List<? extends Attribute<T>> attributes) {
        UPDATE update = new UPDATE(getTableName());
        for (Attribute<T> attribute : attributes) {
            update = update.SET(attribute.getName(), Constant.PLACEHOLDER);
        }
        update.WHERE(matchOne());
        return update;
    }


    public DELETE delete() {
        DELETE delete = new DELETE().FROM(getTableName());
        delete.WHERE(matchOne());
        return delete;
    }

    public SELECT select(List<Attribute<T>> attributes) {
        SELECT select = selectWithoutWhere(attributes);
        select.WHERE(matchOne());

        return select;
    }

    public SELECT selectWithoutWhere(List<Attribute<T>> attributes) {
        SELECT select = new SELECT().FROM(getTableName());
        for (Attribute<T> attribute : attributes) {
            select.addColumn(attribute.getName());
        }
        return select;
    }

    public Predicate matchOne() {
        return new EQUALS(metadata.getIdField().getName(), Constant.PLACEHOLDER).AND(new EQUALS("cid", Constant.PLACEHOLDER));
    }

    public String getTableName() {
        if (tableName == null) {
            return metadata.getName();
        }
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
