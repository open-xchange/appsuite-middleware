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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.sql.builder.IStatementBuilder;

public class FROM extends Element {
	protected List<Table> tables;

	public FROM() {
		super();
		tables = new LinkedList<Table>();
	}

	protected void addTable(String table) {
		tables.add(new Table(table));
	}

	protected void addTable(Table table) {
	    tables.add(table);
	}

	protected void addTables(String... tables) {
		for (String table : tables) {
			addTable(table);
		}
	}

	protected void addTables(Table... tables) {
	    for (Table table : tables) {
	        addTable(table);
	    }
	}

	public void JOIN(String rightTable, Predicate predicate) {
		Table leftTable = tables.remove(0);
		tables.add(new Join(leftTable, rightTable, predicate));
	}

	public void JOIN(Table rightTable, Predicate predicate) {
		Table leftTable = tables.remove(0);
		tables.add(new Join(leftTable, rightTable, predicate));
	}

	public List<Table> getTables() {
		return tables;
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildFROM(this);
	}
}
