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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.sql.builder.IStatementBuilder;

public class SELECT extends Command {
    protected Expression expression;
	protected List<String> columns;
	protected FROM from;
	protected WHERE where;

	public SELECT() {
		columns = new LinkedList<String>();
		from = new FROM();
	}

	public SELECT(String... columns) {
		this();
		this.columns.addAll(Arrays.asList(columns));
	}

	public SELECT(Column... columns) {
	    this();
	    for (Column column : columns) {
            this.columns.add(column.getName());
        }
	}

	public SELECT(List<Column> columns) {
	    this();
	    for (Column column : columns) {
            this.columns.add(column.getName());
        }
	}

	public SELECT(COUNT count) {
	    this();
	    this.expression = count;
	}

	public SELECT(Constant constant) {
	    this(constant.toString());
	}

	public void addColumn(String column) {
		this.columns.add(column);
	}

	public void addColumns(String... columns) {
		this.columns.addAll(Arrays.asList(columns));
	}

	public void addColumns(Collection<String> columns) {
		this.columns.addAll(columns);
	}

	public List<String> getColumns() {
		return columns;
	}

	public SELECT FROM(String table) {
		from.addTable(table);
		return this;
	}

	public SELECT FROM(Table table) {
	    from.addTable(table);
	    return this;
	}

	public SELECT FROM(String... tables) {
		from.addTables(tables);
		return this;
	}

	public SELECT FROM(Table... tables) {
	    from.addTables(tables);
	    return this;
	}

	public SELECT JOIN(String rightTable, Predicate predicate) {
		from.JOIN(rightTable, predicate);
		return this;
	}

	public SELECT JOIN(Table rightTable, Predicate predicate) {
		from.JOIN(rightTable, predicate);
		return this;
	}

	public SELECT WHERE(Predicate predicate) {
		where = new WHERE(predicate);
		return this;
	}

	public Expression getExpression() {
	    return this.expression;
	}

	public FROM getFrom() {
		return from;
	}

	public WHERE getWhere() {
		return where;
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildSELECT(this);
	}
}
