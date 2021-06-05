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
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.BitAND;
import com.openexchange.sql.grammar.BitOR;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.INVERT;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.UPDATE;

/**
 * {@link BitExample}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class BitExample {

    public static void main(String[] args) {
        new BitExample().simpleBit();
    }

    public void simpleBit() {
        //UPDATE update = new UPDATE("a_table").SET("a_column", ASTERISK);
        UPDATE update = new UPDATE(new Table("user_configuration")).SET(
            "permissions", new BitAND(
                new BitOR(new Column("permissions"), PLACEHOLDER),
                new INVERT(PLACEHOLDER)
            )
        );
        //UPDATE update = new UPDATE("a_table").SET("a_column", new BitOR(new BitAND(new Column("a_column"), new BitLSHIFT(new Constant(1), new Constant(5))), new INVERT(new BitLSHIFT(new Constant(1), new Constant(3)))));
        System.out.println(new StatementBuilder().buildCommand(update));
    }
}
