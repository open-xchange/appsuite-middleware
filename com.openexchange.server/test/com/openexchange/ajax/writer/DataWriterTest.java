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

package com.openexchange.ajax.writer;

import java.io.StringWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link DataWriterTest} should test methods in {@link DataWriter}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.8.0
 */
@SuppressWarnings("static-method")
public class DataWriterTest {

    public DataWriterTest() {
        super();
    }

    /**
     * Test method for {@link com.openexchange.ajax.writer.DataWriter#writeParameter(java.lang.String, java.lang.Integer, org.json.JSONObject, boolean)}.
     * @throws JSONException
     */
     @Test
     public void testWriteParameterInteger() throws JSONException {
        JSONObject json = new JSONObject();
        DataWriter.writeParameter("testInteger", Integer.valueOf(0), json, true);
        Assert.assertTrue("JSON attribute was not written.", json.has("testInteger"));
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertEquals("Written JSON does not look as expected.", "{\"testInteger\":0}", sw.toString());
    }

    /**
     * Test method for {@link com.openexchange.ajax.writer.DataWriter#writeParameter(java.lang.String, java.lang.Integer, org.json.JSONObject, boolean)}.
     * @throws JSONException
     */
     @Test
     public void testWriteParameterIntegerNull() throws JSONException {
        JSONObject json = new JSONObject();
        DataWriter.writeParameter("testInteger", (Integer) null, json, true);
        Assert.assertTrue("JSON attribute was not written.", json.has("testInteger"));
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertEquals("Written JSON does not look as expected.", "{\"testInteger\":null}", sw.toString());
    }
}
