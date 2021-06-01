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

package com.openexchange.ajax.framework;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;

/**
 * Tests for the {@link Params} class.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ParamsTest {

    protected Params defaultParams;

    @Before
    public void setUp() {
        defaultParams = new Params();
        defaultParams.add("key1", "value1");
        defaultParams.add(new Parameter("key2", "value2"));
        defaultParams.add("key3", "value3", "key4", "value4");
        defaultParams.add(new Parameter("key5", "value5"), new Parameter("key6", "value6"));
    }

    @Test
    public void testToString() {
        assertEquals("?key1=value1&key2=value2&key3=value3&key4=value4&key5=value5&key6=value6", defaultParams.toString());
    }
}
