/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
