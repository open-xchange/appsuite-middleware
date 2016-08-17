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

package com.openexchange.html.bugtests;

import org.junit.Assert;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug28094Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug28094Test extends AbstractSanitizing {
    @Test
    public void testInsecureHref() {
        String content = "<a href=\"http://www.raumausstatter-innung-schwalm-eder.de/"
            + "index.php?eID=tx_cms_showpic&amp;file=uploads%2Fpics%2F13-06-Raumausstatter-JHV.jpg"
            + "&amp;width=500m&amp;height=500&amp;bodyTag=%3Cbody%20bgColor%3D%22%23ffffff%22%3E"
            + "&amp;wrap=%3Ca%20href%3D%22javascript%3Aclose%28%29%3B%22%3E%20%7C%20%3C%2Fa%3E"
            + "&amp;md5=a0a07697cb8be1898b5e9ec79d249de2\">"
            + "<span style='mso-fareast-font-family:\"Times New Roman\";color:windowtext;mso-fareast-language:DE;mso-no-proof:yes;text-decoration:none;text-underline:none'>"
            + "<img border=0 width=144 height=76 id=\"Bild_x0020_9\" src=\"cid:image004.jpg@01CE6E59.FDD59220\" alt=\"http://www.raumausstatter-innung-schwalm-eder.de/typo3temp/pics/3794d580f5.jpg\">"
            + "</span>"
            + "</a>";

        String test = getHtmlService().sanitize(content, null, true, null, null);

        Assert.assertTrue("Unexpected value: " + test, test.indexOf("href=\"") < 0);
    }
}
