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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.html.vulntests.xss;

import org.junit.Test;
import com.openexchange.html.AssertExpression;
import com.openexchange.html.XSSHolder;

/**
 * {@link TagManipulation}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class TagManipulation extends AbstractXSSVectors {
    @Test
    public void testTagManipulation() {
        /**
         * Double open angle brackets
         */
        xss.add(new XSSHolder("<iframe src=http://ha.ckers.org/scriptlet.html <"));
        /**
         * Input image
         */
        xss.add(new XSSHolder("<INPUT TYPE=\"IMAGE\" SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * BODY image
         */
        xss.add(new XSSHolder("<BODY BACKGROUND=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * BODY tag
         */
        xss.add(new XSSHolder("<BODY ONLOAD=alert('XSS')>", AssertExpression.NOT_CONTAINED, "alert('XSS')"));
        /**
         * BGSOUND
         */
        xss.add(new XSSHolder("<BGSOUND SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "alert('XSS')"));
        /**
         * & JavaScript includes
         */
        xss.add(new XSSHolder("<BR SIZE=\"&{alert('XSS')}\">", AssertExpression.NOT_CONTAINED, "alert('XSS')"));
        /**
         * STYLE sheet
         */
        /**
         * iframe
         */
        xss.add(new XSSHolder("<IFRAME SRC=\"javascript:alert('XSS');\"></IFRAME>"));
        xss.add(new XSSHolder("<IFRAME SRC=# onmouseover=\"alert(document.cookie)\"></IFRAME>"));
        xss.add(new XSSHolder("<FRAMESET><FRAME SRC=\"javascript:alert('XSS');\"></FRAMESET>"));
        /**
         * Base href
         */
        xss.add(new XSSHolder("<BASE HREF=\"javascript:alert('XSS');//\">"));
        /**
         * Object tag
         */
        xss.add(new XSSHolder("<OBJECT TYPE=\"text/x-scriptlet\" DATA=\"http://ha.ckers.org/scriptlet.html\"></OBJECT>"));
        xss.add(new XSSHolder("<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>"));

        assertVectors();
    }
}
