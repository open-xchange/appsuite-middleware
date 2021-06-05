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

public abstract class Value extends Expression {

	public AVG AVG() {
		return new AVG(this);
	}

	public COUNT COUNT() {
		return new COUNT(this);
	}

	public EQUALS EQUAL(Expression expression) {
		return new EQUALS(this, expression);
	}

	@Override
    public String getSqlKeyword() {
		return "";
	}

	public GREATER GREATER(Expression expression) {
		return new GREATER(this, expression);
	}

	public GREATEROREQUAL GREATEROREQUAL(Expression expression) {
		return new GREATEROREQUAL(this, expression);
	}

	public IN IN(Expression expression) {
		return new IN(this, expression);
	}

	public ISNULL ISNULL() {
		return new ISNULL(this);
	}

	public LIKE LIKE(Expression expression, Expression escape) {
		return new LIKE(this, expression, escape);
	}

	public MAX MAX() {
		return new MAX(this);
	}

	public MIN MIN() {
		return new MIN(this);
	}

	public NOTEQUALS NOTEQUAL(Expression expression) {
		return new NOTEQUALS(this, expression);
	}

	public NOTIN NOTIN(Expression expression) {
		return new NOTIN(this, expression);
	}

	public NOTLIKE NOTLIKE(Expression expression, Expression escape) {
		return new NOTLIKE(this, expression, escape);
	}

	public NOTNULL NOTNULL() {
		return new NOTNULL(this);
	}

	public SMALLER SMALLER(Expression expression) {
		return new SMALLER(this, expression);
	}

	public SMALLEROREQUAL SMALLEROREQUAL(Expression expression) {
		return new SMALLEROREQUAL(this, expression);
	}

	public SUM SUM() {
		return new SUM(this);
	}
}
