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

package com.openexchange.html.vulntests;

import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;


/**
 * {@link Bug22284VulTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug22284VulTest extends AbstractSanitizing {
    @Test
    public void testSanitize() {
        String content = "<HTML><HEAD><STYLE " +
            "id=\"styletagforeditor\">body{background-color:rgb(255,255,255);direction:ltr;font-family:times " +
            "new " +
            "roman;font-size:12pt;line-height:1.2;padding-top:0.787in;padding-right:0.787in;padding-bottom:0.787i " +
            "n;padding-left:0.787in;margin:0in;} " +
            "p{margin-top:0pt;margin-bottom:0pt;}</STYLE><STYLE " +
            "id=\"styletagtwoforeditor\" type=\"text/css\">table { font-size: 12pt } " +
            "table p, li p { margin : 0px; }</STYLE></HEAD><BODY><P><IMG  " +
            "align=\"bottom\" alt=\"onerror=``alert(1)\"  " +
            "src=\"http://localhost\"></P></BODY></HTML>";

         AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "onerror=``alert(1)");
    }
}
