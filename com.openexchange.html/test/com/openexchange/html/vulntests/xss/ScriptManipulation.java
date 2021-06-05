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

package com.openexchange.html.vulntests.xss;

import org.junit.Test;
import com.openexchange.html.AssertExpression;
import com.openexchange.html.XSSHolder;


/**
 * {@link ScriptManipulation}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ScriptManipulation extends AbstractXSSVectors {
    @Test
    public void testScriptManipulation() {
        xss.add(new XSSHolder("<SCRIPT/XSS SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        xss.add(new XSSHolder("<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        xss.add(new XSSHolder("<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        xss.add(new XSSHolder("<<SCRIPT>alert(\"XSS\");//<</SCRIPT>script>"));
        xss.add(new XSSHolder("<SCRIPT SRC=//ha.ckers.org/.j>"));
        xss.add(new XSSHolder("';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//\";" +
            "alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//--" +
            "></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>", AssertExpression.NOT_CONTAINED, "<SCRIPT>"));
        xss.add(new XSSHolder("'';!--\"<XSS>=&{()}", AssertExpression.NOT_CONTAINED, "<XSS>"));
        xss.add(new XSSHolder("<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT>"));
        xss.add(new XSSHolder("<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\"", AssertExpression.NOT_CONTAINED, "alert(\"XSS\")"));
        xss.add(new XSSHolder("</TITLE><SCRIPT>alert(\"XSS\");</SCRIPT>", AssertExpression.NOT_CONTAINED, "<SCRIPT>alert(\"XSS\");</SCRIPT>"));

        assertVectors();
    }
}
