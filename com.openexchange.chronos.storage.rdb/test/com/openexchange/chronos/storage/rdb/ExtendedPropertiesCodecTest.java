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

package com.openexchange.chronos.storage.rdb;

import static org.junit.Assert.assertEquals;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.java.Streams;

/**
 * {@link ExtendedPropertiesCodecTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class ExtendedPropertiesCodecTest {

    @Test
    public void testUmlautInPropertyValue() throws Exception {
        ExtendedProperties properties = new ExtendedProperties();
        properties.add(new ExtendedProperty("test", "mütze"));
        byte[] encodedProperties = ExtendedPropertiesCodec.encode(properties);
        ExtendedProperties decodedProperties = ExtendedPropertiesCodec.decode(Streams.newByteArrayInputStream(encodedProperties));
        assertEquals("mütze", decodedProperties.get(0).getValue());
    }

    @Test
    public void testUmlautInParameterValue() throws Exception {
        ExtendedPropertyParameter parameter = new ExtendedPropertyParameter("test", "höhle");
        ExtendedProperties properties = new ExtendedProperties();
        properties.add(new ExtendedProperty("test", "test", Collections.singletonList(parameter)));
        byte[] encodedProperties = ExtendedPropertiesCodec.encode(properties);
        ExtendedProperties decodedProperties = ExtendedPropertiesCodec.decode(Streams.newByteArrayInputStream(encodedProperties));
        assertEquals("höhle", decodedProperties.get(0).getParameter("test").getValue());
    }

}
