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

package com.openexchange.sql.grammar.example;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.dates;
import static com.openexchange.sql.schema.Tables.delDates;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.PLUS;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.SQRT;
import com.openexchange.sql.grammar.TIMES;

public class InsertExample {

	public static void main(String[] args) throws Exception {
		simpleUpdate();
		extendedInsert();
		insertWithPlaceholder();
	}

	public static void simpleUpdate() {
		INSERT insert = new INSERT().INTO("a_table").SET("a",
				Constant.PLACEHOLDER).SET(
				"b",
				new PLUS(new Constant(10), new TIMES(new SQRT(45),
						new Constant(3))));
		System.out.println(new StatementBuilder().buildCommand(insert));
	}

	/**
     * This INSERT-Statement contains several OX-Modifications with sub-selects and other stuff.
     *
     * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
     */
	public static void extendedInsert() {
	    INSERT insert = new INSERT().
        INTO(delDates).
        subSelect(new SELECT("*").
            FROM(dates).
            WHERE(new EQUALS(dates.getColumn("intfield01"), new Constant(1000)))
        );

	    System.out.println(new StatementBuilder().buildCommand(insert));
	}

	public static void insertWithPlaceholder() throws Exception {
        List<Object> values = new ArrayList<Object>();

	    INSERT insert = new INSERT().INTO(dates);

	    insert.SET("field01", new Constant("field01_a"), new Constant("field01_b"));

	    insert.SET("timestampfield01", PLACEHOLDER, PLACEHOLDER);
        values.add(new Date(0));
        values.add(new Date(3600000));

	    insert.SET("timestampfield02", PLACEHOLDER, PLACEHOLDER);
        values.add(new Date(3600000));
        values.add(new Date(7200000));

        Connection con = getConnection();
        PreparedStatement stmt = new StatementBuilder().prepareStatement(con, insert, values);
        System.out.println(stmt.toString());
        stmt.close();
        con.close();
	}

	private static Connection getConnection() throws Exception {
	    Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost/ox_sandbox", "openexchange", "secret");
	}
}
