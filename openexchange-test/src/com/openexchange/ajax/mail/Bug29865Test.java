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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link Bug29865Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug29865Test extends AbstractMailTest {

    private static String attachment = readFile("attachment.base64");

    private static String eml = readFile("bug29865.eml");

    private UserValues values;

    /**
     * Initializes a new {@link Bug29865Test}.
     *
     * @param name
     */
    public Bug29865Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testGetStructure() throws OXException, IOException, JSONException {

        final NewMailRequest newMailRequest = new NewMailRequest(null, eml.replaceAll("#ADDR#", values.getSendAddress()), -1, true);
        final NewMailResponse newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());

        final GetRequest newGetRequest = new GetRequest(newMailResponse.getFolder(), newMailResponse.getId(), true, true);
        final GetResponse newGetResponse = getClient().execute(newGetRequest);

        String actualAttachment = ((JSONObject) newGetResponse.getData()).getJSONArray("body").getJSONObject(1).getJSONObject("body").getString("data");
        assertEquals("Attachment has been modified", attachment.replaceAll("(\\r|\\n)", ""), actualAttachment);
    }

    private static String readFile(String fileName) {
        try {
            @SuppressWarnings("resource") BufferedReader br = new BufferedReader(new FileReader(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
