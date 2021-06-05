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

package com.openexchange.icap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.icap.ICAPResponse;

/**
 * {@link ICAPResponseBuilderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPResponseBuilderTest {

    /**
     * Initialises a new {@link ICAPResponseBuilderTest}.
     */
    public ICAPResponseBuilderTest() {
        super();
    }

    /**
     * Test for InSenSiTiVe header names
     */
    @Test
    public void testCaseInsensitiveHeaders() {
        ICAPResponse response = new ICAPResponse.Builder().addHeader("Some-Header", "Some value").addHeader("sOmE-OtHeR-HEADER", "With some other VALUE").build();
        String someHeaderValue = response.getHeader("soME-heADEr");
        String someOtherHeaderValue = response.getHeader("SOME-other-headeR");
        assertNotNull("The header's value is null", someHeaderValue);
        assertNotNull("The header's value is null", someOtherHeaderValue);

        assertEquals("The header's value does not match", someHeaderValue, "Some value");
        assertEquals("The header's value does not match", someOtherHeaderValue, "With some other VALUE");
    }
}
