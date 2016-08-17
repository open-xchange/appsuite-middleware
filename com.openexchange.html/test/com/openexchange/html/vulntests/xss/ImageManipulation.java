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
 * {@link ImageManipulation}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ImageManipulation extends AbstractXSSVectors {

    @Test
    public void testImageManipulation() {
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
        xss.add(new XSSHolder("<IMG SRC=\"javascript:alert('XSS')\""));
        xss.add(new XSSHolder("<IMG DYNSRC=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG LOWSRC=\"javascript:alert('XSS')\">", AssertExpression.NOT_CONTAINED, "javascript:alert('XSS');"));
        xss.add(new XSSHolder("<IMG SRC='vbscript:msgbox(\"XSS\")'>", AssertExpression.NOT_CONTAINED, "vbscript:msgbox(\"XSS\")"));

        assertVectors();
    }
}
