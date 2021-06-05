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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug25923Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug25923Test extends AbstractSanitizing {

     @Test
     public void testCheckValidBaseTag() {
        String htmlContent = "<html><head><title>Foo Bar</title><base href=\"http://www.foobar.invalid\"></head><body><img src=\"image.png\"></body></html>";
        String actual = getHtmlService().checkBaseTag(htmlContent, true);
        String expected = "<html><head><title>Foo Bar</title></head><body><img src=\"http://www.foobar.invalid/image.png\"\"></body></html>";
        assertFalse("The parsed HTML still contains the <base> tag", actual.contains("base"));
        assertEquals("Unexpected output", expected, actual);
    }
}
