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

public class Join extends Table {
	protected Table leftTable;
	protected Table rightTable;
	protected ON on;

	public Join(String leftTable, String rightTable, Predicate onCondition) {
		this(new Table(leftTable), new Table(rightTable), onCondition);
	}

	public Join(String leftTable, Table rightTable, Predicate onCondition) {
		this(new Table(leftTable), rightTable, onCondition);
	}

	public Join(Table leftTable, String rightTable, Predicate onCondition) {
		this(leftTable, new Table(rightTable), onCondition);
	}

	public Join(Table leftTable, Table rightTable, Predicate onCondition) {
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.on = new ON(onCondition);
	}

	public Table getLeftTable() {
		return leftTable;
	}

	public Table getRightTable() {
		return rightTable;
	}

	public ON getON() {
		return on;
	}

	@Override
    public void build(IStatementBuilder builder) {
		builder.buildJoin(this);
	}
}
