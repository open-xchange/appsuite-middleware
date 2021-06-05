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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.sql.builder.IStatementBuilder;

public class Table extends Element {
	private String name;
	private String alias = null;

	public Table() {
	}

	public Table(String name) {
		this.name = name;
	}

	public Table(String name, String alias) {
	    this(name);
	    this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public String getAlias() {
	    return alias;
	}

	public Column getColumn(String column) {
	    return new Column((alias == null ? getName() : getAlias()) + "." + column);
	}

	public List<Column> getColumns(String... columns) {
	    List<Column> retVal = new ArrayList<Column>();
	    for (String column : columns) {
            retVal.add(getColumn(column));
        }
	    return retVal;
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildTable(this);
	}
}
