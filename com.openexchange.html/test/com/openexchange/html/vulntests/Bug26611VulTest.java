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

package com.openexchange.html.vulntests;

import org.junit.Assert;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug26611VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug26611VulTest extends AbstractSanitizing {
     @Test
     public void testSanitize() throws Exception{
        String content = "foo <object/data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiWFNTIFNjaHdhY2hzdGVsbGUiKTwvc2NyaXB0Pg==\"<!-- --></object//-->> bar";
        String test = getHtmlService().sanitize(content, null, false, null, null);
        Assert.assertFalse("Sanitized content still contains object tag.", test.contains("<object"));
        Assert.assertFalse("Sanitized content still contains object tag.", test.contains("</object>"));
        Assert.assertFalse("Sanitized content still contains base64 string.", test.contains("PHNjcmlwdD5hbGVydCgiWFNTIFNjaHdhY2hzdGVsbGUiKTwvc2NyaXB0Pg=="));
        Assert.assertFalse("Sanitized content still contains data type.", test.contains("text/html"));
        Assert.assertFalse("Sanitized content still contains base64 encoding description.", test.contains("base64"));
    }
}
