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
 * {@link StyleManipulation}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class StyleManipulation extends AbstractXSSVectors {
    @Test
    public void testStyleManipulation() {
        /**
         * Style imports
         */
        xss.add(new XSSHolder("<STYLE>@im\\port'\\ja\\vasc\\ript:alert(\"XSS\")';</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import'javas&#13;cript:alert('XSS');';</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import 'javas&#13;cript:alert('XSS');';</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import \"jav\tascript:alert('XSS');\"</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import \"jav&#x09;ascript:alert('XSS');\"</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import \"jav&#x0A;ascript:alert('XSS');\"</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        xss.add(new XSSHolder("<STYLE>@import \"jav&#x0D;ascript:alert('XSS');\"</STYLE>", AssertExpression.NOT_CONTAINED, "alert('xss')"));
        /**
         * Remote style sheet
         */
        xss.add(new XSSHolder("<LINK REL=\"stylesheet\" HREF=\"http://ha.ckers.org/xss.css\">", AssertExpression.NOT_CONTAINED, "http://ha.ckers.org/xss.css"));
        xss.add(new XSSHolder("<STYLE>@import'http://ha.ckers.org/xss.css';</STYLE>", AssertExpression.NOT_CONTAINED, "http://ha.ckers.org/xss.css"));
        xss.add(new XSSHolder("<LINK REL=\"stylesheet\" HREF=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));

        assertVectors();
    }
}
