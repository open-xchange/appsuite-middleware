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

package com.openexchange.sql.grammar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.sql.builder.IStatementBuilder;

public class INSERT extends ModifyCommand {
	private INTO into;
	private final List<String> columns;
	private final List<List<Expression>> values;
	private SELECT subSelect;

	public INSERT() {
		super();
		columns = new LinkedList<String>();
		values = new LinkedList<List<Expression>>();
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildINSERT(this);
	}

	public INSERT INTO(String tableName) {
		into = new INTO(tableName);
		return this;
	}

	public INSERT INTO(Table table) {
	    into = new INTO(table);
	    return this;
	}

    public INSERT subSelect(SELECT select) {
        subSelect = select;
        return this;
    }

	public List<String> getColumns() {
		return columns;
	}

	public INTO getInto() {
		return into;
	}

	public List<List<Expression>> getValues() {
		return values;
	}

	public SELECT getSubSelect() {
	    return subSelect;
	}

    public INSERT SET(String columnName, List<Expression> expressions) {
        columns.add(columnName);
        if (values.isEmpty()) {
            expressions.stream().forEach(e -> values.add(new LinkedList<>()));
        }
        for (int i = 0; i < expressions.size(); i++) {
            values.get(i).add(expressions.get(i));
        }
        return this;
    }

    public INSERT SET(Column column, List<Expression> expressions) {
        return SET(column.getName(), expressions);
    }

	public INSERT SET(String columnName, Expression... expressions) {
	    return SET(columnName, Arrays.asList(expressions));
	}

	public INSERT SET(Column column, Expression... expressions) {
	    return SET(column.getName(), expressions);
	}
}
