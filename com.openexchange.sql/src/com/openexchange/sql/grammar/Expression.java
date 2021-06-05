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

public abstract class Expression extends Element {

	public ABS ABS() {
		return new ABS(this);
	}

	public LENGTH LENGTH() {
		return new LENGTH(this);
	}

	public SQRT SQRT() {
		return new SQRT(this);
	}

	public CONCAT CONCAT(Expression expression) {
		return new CONCAT(this, expression);
	}

	public LOCATE LOCATE(Expression inString, Expression start) {
		return new LOCATE(this, inString, start);
	}

	public SUBSTRING SUBSTRING(Expression start, Expression length) {
		return new SUBSTRING(this, start, length);
	}

	public PLUS PLUS(Expression expression) {
		return new PLUS(this, expression);
	}

	public MINUS MINUS(Expression expression) {
		return new MINUS(this, expression);
	}

	public TIMES TIMES(Expression expression) {
		return new TIMES(this, expression);
	}

	public DIVIDE DIVIDE(Expression expression) {
		return new DIVIDE(this, expression);
	}

	public UnaryPLUS PLUS() {
		return new UnaryPLUS(this);
	}

	public UnaryMINUS MINUS() {
		return new UnaryMINUS(this);
	}

	public BETWEEN BETWEEN(Expression middleExpression,
			Expression rightExpression) {
		return new BETWEEN(this, middleExpression, rightExpression);
	}

	@Override
    public String toString() {
		return getSqlKeyword();
	}

	public abstract String getSqlKeyword();
}
