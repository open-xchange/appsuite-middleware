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
     public void testInsecureHref() throws Exception {
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

        Assert.assertTrue("Unexpected value: " + test, test.indexOf("javascript") < 0);
    }
}
