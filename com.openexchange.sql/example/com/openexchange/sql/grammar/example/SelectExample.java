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

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.dateRights;
import static com.openexchange.sql.schema.Tables.dates;
import java.util.Date;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.COUNT;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.sql.grammar.NOTNULL;
import com.openexchange.sql.grammar.SELECT;

public class SelectExample {

    public static void main(String[] args) {
        new SelectExample().simpleSelect();
        new SelectExample().extendedSelect();
        new SelectExample().selectWithIn();
    }

    public void simpleSelect() {
        SELECT select = new SELECT("a", "b", "c").FROM("bebe").WHERE(
            new EQUALS("a", 10).AND(new EQUALS("b", "lala").OR(new EQUALS("c", new Date()))));
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
