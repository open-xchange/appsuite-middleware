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

import com.openexchange.sql.builder.IStatementBuilder;

public class DELETE extends ModifyCommand {
	private final FROM from;
	private WHERE where;

	public DELETE() {
		from = new FROM();
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildDELETE(this);
	}

	public DELETE FROM(String table) {
		from.addTable(table);
		return this;
	}

    public DELETE FROM(Table table) {
        from.addTable(table);
        return this;
    }

    public DELETE FROM(String... tables) {
        from.addTables(tables);
        return this;
    }

    public DELETE FROM(Table... tables) {
        from.addTables(tables);
        return this;
    }

	public DELETE WHERE(Predicate predicate) {
		where = new WHERE(predicate);
		return this;
	}

	public FROM getFrom() {
		return from;
	}

	public WHERE getWhere() {
		return where;
	}
}
