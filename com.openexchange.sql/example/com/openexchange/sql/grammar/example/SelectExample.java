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

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.util.Date;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.COUNT;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.sql.grammar.NOTNULL;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.Table;

public class SelectExample {

    private static final Table dates = new Table("prg_dates", "pd");
    private static final Table dateRights = new Table("prg_date_rights", "pdr");

    public static void main(String[] args) {
        new SelectExample().simpleSelect();
        new SelectExample().extendedSelect();
        new SelectExample().selectWithIn();
    }

    public void simpleSelect() {
        SELECT select = new SELECT("a", "b", "c").FROM("bebe").WHERE(
            new EQUALS("a", Integer.valueOf(10)).AND(new EQUALS("b", "lala").OR(new EQUALS("c", new Date()))));
        System.out.println(new StatementBuilder().buildCommand(select));
        // WHERE(new EQUALS("a", 10));
        // SELECT select2 = new SELECT(new String[] { "a", "b", "c" });
    }

    /**
     * This SELECT-Statement contains several OX-Modifications for selecting with joins, aliases and other stuff.
     *
     * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
     */
    public void extendedSelect() {
        SELECT select =
            new SELECT(new COUNT(ASTERISK)).
            FROM(dates).
            JOIN(dateRights, new EQUALS(dates.getColumn("intfield01"), dateRights.getColumn("object_id"))).
            WHERE(new EQUALS(dates.getColumn("intfield01"), dates.getColumn("intfield02")).AND(new NOTNULL(dates.getColumn("field06"))));

        System.out.println(new StatementBuilder().buildCommand(select));
    }

    public void selectWithIn() {
        SELECT select =
            new SELECT(new COUNT(ASTERISK)).FROM(dates).WHERE(new IN("intfield01", new LIST(PLACEHOLDER, PLACEHOLDER)));

        System.out.println(new StatementBuilder().buildCommand(select));
    }
}
