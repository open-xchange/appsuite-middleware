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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.html;

import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * {@link OwaspTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class OwaspTest extends AbstractSanitizing {

    @Test
    public void testOwaspEvasions() {
        List<XSSHolder> xss = new LinkedList<XSSHolder>();
        /**
         * XSS Locators
         */
        xss.add(new XSSHolder("';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//\";" +
            "alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//--" +
            "></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>", AssertExpression.NOT_CONTAINED, "<SCRIPT>"));
        xss.add(new XSSHolder("'';!--\"<XSS>=&{()}", AssertExpression.NOT_CONTAINED, "<XSS>"));

        /**
         * No filter evasion
         */
        xss.add(new XSSHolder("<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT>"));
        /**
         * Image XSS using the JavaScript directive
         */
        xss.add(new XSSHolder("<IMG SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "SRC=\"javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG SRC=javascript:alert('XSS')>", AssertExpression.NOT_CONTAINED, "SRC=\"javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG SRC=JaVaScRiPt:alert('XSS')>", AssertExpression.NOT_CONTAINED, "SRC=\"javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG SRC=javascript:alert(\"XSS\")>", AssertExpression.NOT_CONTAINED, "SRC=\"javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>", AssertExpression.NOT_CONTAINED, "SRC=`javascript:alert(\"RSnake says, 'XSS'\")`"));
        xss.add(new XSSHolder("<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>", AssertExpression.NOT_CONTAINED, "javascript:alert(String.fromCharCode(88,83,83))"));
        xss.add(new XSSHolder("<IMG SRC=# onmouseover=\"alert('xxs')\">", AssertExpression.NOT_CONTAINED, "alert('xxs')"));
        xss.add(new XSSHolder("<IMG onmouseover=\"alert('xxs')\">", AssertExpression.NOT_CONTAINED, "alert('xxs')"));
        xss.add(new XSSHolder("<IMG SRC=/ onerror=\"alert(String.fromCharCode(88,83,83))\"></img>", AssertExpression.NOT_CONTAINED, "alert(String.fromCharCode(88,83,83))"));
        xss.add(new XSSHolder("<IMG SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;&#116;&#58;&#97;&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;>", AssertExpression.NOT_CONTAINED,
            "SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;&#116;&#58;&#97;&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;"));
        xss.add(new XSSHolder("<IMG SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#"
            + "0000088&#0000083&#0000083&#0000039&#0000041>", AssertExpression.NOT_CONTAINED, "SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&#"
                + "0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041"));
        xss.add(new XSSHolder("<IMG SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>", AssertExpression.NOT_CONTAINED,
            "SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29"));
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
         * Malformed IMG tags
         */
        xss.add(new XSSHolder("<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\"", AssertExpression.NOT_CONTAINED, "<SCRIPT>alert(\"XSS\")</SCRIPT>"));
        /**
         * Malformed A tags
         */
        xss.add(new XSSHolder("<a onmouseover=\"alert(document.cookie)\">xxs link</a>", AssertExpression.NOT_CONTAINED, "onmouseover=\"alert(document.cookie)\""));
        xss.add(new XSSHolder("<a onmouseover=alert(document.cookie)>xxs link</a>", AssertExpression.NOT_CONTAINED, "onmouseover=alert(document.cookie)"));
        xss.add(new XSSHolder("<a title=\"harmless  SCRIPT&#61;javascript:alert(1) ignored&#61;ignored\"></a>", AssertExpression.NOT_CONTAINED, "javascript:alert(1)"));
        /**
         * Non-alpha-non-digit XSS
         */
        xss.add(new XSSHolder("<SCRIPT/XSS SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        xss.add(new XSSHolder("<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        /**
         * Non-alpha-non-digit XSS
         */;
        xss.add(new XSSHolder("<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>"));
        /**
         * Extraneous open brackets
         */
        xss.add(new XSSHolder("<<SCRIPT>alert(\"XSS\");//<</SCRIPT>script>"));
        /**
         * Protocol resolution in script tags
         */
        xss.add(new XSSHolder("<SCRIPT SRC=//ha.ckers.org/.j>"));
        /**
         * Half open HTML/JavaScript XSS vector
         */
        xss.add(new XSSHolder("<IMG SRC=\"javascript:alert('XSS')\""));
        /**
         * Double open angle brackets
         */
        xss.add(new XSSHolder("<iframe src=http://ha.ckers.org/scriptlet.html <"));
        /**
         * End title tag
         */
        xss.add(new XSSHolder("</TITLE><SCRIPT>alert(\"XSS\");</SCRIPT>", AssertExpression.NOT_CONTAINED, "<SCRIPT>alert(\"XSS\");</SCRIPT>"));
        /**
         * Input image
         */
        xss.add(new XSSHolder("<INPUT TYPE=\"IMAGE\" SRC=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * BODY image
         */
        xss.add(new XSSHolder("<BODY BACKGROUND=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * IMG Dynsrc
         */
        xss.add(new XSSHolder("<IMG DYNSRC=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * IMG lowsrc
         */
        xss.add(new XSSHolder("<IMG LOWSRC=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        /**
         * VBscript in an image
         */
        xss.add(new XSSHolder("<IMG SRC='vbscript:msgbox(\"XSS\")'>", AssertExpression.NOT_CONTAINED, "vbscript:msgbox(\"XSS\")"));
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
        xss.add(new XSSHolder("<LINK REL=\"stylesheet\" HREF=\"javascript:alert('XSS');\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS')"));
        /**
         * Remote style sheet
         */
        xss.add(new XSSHolder("<LINK REL=\"stylesheet\" HREF=\"http://ha.ckers.org/xss.css\">", AssertExpression.NOT_CONTAINED, "http://ha.ckers.org/xss.css"));
        xss.add(new XSSHolder("<STYLE>@import'http://ha.ckers.org/xss.css';</STYLE>", AssertExpression.NOT_CONTAINED, "http://ha.ckers.org/xss.css"));

        for (XSSHolder xssE : xss) {
            AssertionHelper.assertSanitized(getHtmlService(), xssE.getXssAttack(), xssE.getMalicious(), xssE.getAssertExpression());
        }
    }
}
