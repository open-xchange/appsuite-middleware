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

package com.openexchange.sql.grammar.example;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
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
import com.openexchange.sql.grammar.Table;

public class InsertExample {

    private static final Table dates = new Table("prg_dates", "pd");
    private static final Table delDates = new Table("del_dates", "dd");

	public static void main(String[] args) throws Exception {
		simpleUpdate();
		extendedInsert();
		insertWithPlaceholder();
	}

	public static void simpleUpdate() {
		INSERT insert = new INSERT().INTO("a_table").SET("a",
				Constant.PLACEHOLDER).SET(
				"b",
				new PLUS(new Constant(Integer.valueOf(10)), new TIMES(new SQRT(Integer.valueOf(45)),
						new Constant(Integer.valueOf(3)))));
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
            WHERE(new EQUALS(dates.getColumn("intfield01"), new Constant(Integer.valueOf(1000))))
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
        java.util.Properties defaults = new java.util.Properties();
        defaults.put("user", "openexchange");
        defaults.put("password", "secret");
        defaults.setProperty("useSSL", "false");
        return DriverManager.getConnection("jdbc:mysql://localhost/ox_sandbox", defaults);
	}
}
