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
