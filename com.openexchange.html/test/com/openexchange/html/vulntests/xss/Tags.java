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

package com.openexchange.html.vulntests.xss;

import org.junit.Test;
import com.openexchange.html.AssertExpression;
import com.openexchange.html.XSSHolder;

/**
 * {@link Tags}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Tags extends AbstractXSSVectors {

    @Test
    public void testObjectTag() {
        xss.add(new XSSHolder(" <OBJECT TYPE=\"text/x-scriptlet\" DATA=\"http://ha.ckers.org/scriptlet.html\"></OBJECT>"));
        assertVectors();
    }

    @Test
    public void testEmbedSVG() {
        xss.add(new XSSHolder("<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>"));
        assertVectors();
    }

    @Test
    public void testEmbedFlash() {
        xss.add(new XSSHolder("<EMBED SRC=\"http://ha.ckers.org/xss.swf\" AllowScriptAccess=\"always\" ></EMBED>"));
        assertVectors();
    }

    @Test
    public void testBaseTag() {
        xss.add(new XSSHolder("<BASE HREF=\"javascript:alert('XSS');//\">"));
        assertVectors();
    }

    @Test
    public void testDivTags() {
        xss.add(new XSSHolder("<DIV STYLE=\"background-image: url(javascript:alert('XSS'))\">", AssertExpression.NOT_CONTAINED, "javascript:alert('xss')"));
        xss.add(new XSSHolder("<DIV STYLE=\"background-image:\0075\0072\006C\0028'\006a\0061\0076\0061\0073\0063\0072\0069\0070\0074\003a\0061\006c\0065\0072\0074\0028.1027\0058.1053\0053\0027\0029'\0029\">", AssertExpression.NOT_CONTAINED, "STYLE=\"background-image:\0075\0072\006C\0028'\006a\0061\0076\0061\0073\0063\0072\0069\0070\0074\003a\0061\006c\0065\0072\0074\0028.1027\0058.1053\0053\0027\0029'\0029\""));
        xss.add(new XSSHolder("<DIV STYLE=\"background-image: url(&#1;javascript:alert('XSS'))\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        xss.add(new XSSHolder("<DIV STYLE=\"width: expression(alert('XSS'));\">", AssertExpression.NOT_CONTAINED, "expression(alert('XSS'))"));
        assertVectors();
    }

    @Test
    public void testTableTags() {
        xss.add(new XSSHolder("<TABLE BACKGROUND=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        xss.add(new XSSHolder("<TABLE><TD BACKGROUND=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));

        assertVectors();
    }

    @Test
    public void testBreakTag() {
        xss.add(new XSSHolder("<BR SIZE=\"&{alert('XSS')}\">", AssertExpression.NOT_CONTAINED, "alert('XSS')"));

        assertVectors();
    }

    @Test
    public void testInput() {
        xss.add(new XSSHolder("<INPUT TYPE=\"IMAGE\" SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));

        assertVectors();
    }

    @Test
    public void testFrameTags() {
        xss.add(new XSSHolder("<IFRAME SRC=\"javascript:alert('XSS');\"></IFRAME>", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        xss.add(new XSSHolder("<IFRAME SRC=# onmouseover=\"alert(document.cookie)\"></IFRAME>", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        xss.add(new XSSHolder("<FRAMESET><FRAME SRC=\"javascript:alert('XSS');\"></FRAMESET>", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        xss.add(new XSSHolder("<iframe src=http://ha.ckers.org/scriptlet.html <"));

        assertVectors();
    }

    @Test
    public void testBodyTag() {
        xss.add(new XSSHolder("<BODY BACKGROUND=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        xss.add(new XSSHolder("<BODY ONLOAD=alert('XSS')>", AssertExpression.NOT_CONTAINED, "alert('XSS')"));

        assertVectors();
    }

    @Test
    public void testBGSoundTag() {
        xss.add(new XSSHolder("<BGSOUND SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "alert('XSS')"));

        assertVectors();
    }

    @Test
    public void testLinkManipulation() {
        xss.add(new XSSHolder("<a onmouseover=\"alert(document.cookie)\">xxs link</a>", AssertExpression.NOT_CONTAINED, "onmouseover=\"alert(document.cookie)\""));
        xss.add(new XSSHolder("<a onmouseover=alert(document.cookie)>xxs link</a>", AssertExpression.NOT_CONTAINED, "onmouseover=alert(document.cookie)"));
        xss.add(new XSSHolder("<a title=\"harmless  SCRIPT&#61;javascript:alert(1) ignored&#61;ignored\"></a>", AssertExpression.NOT_CONTAINED, "javascript:alert(1)"));

        assertVectors();
    }
}
