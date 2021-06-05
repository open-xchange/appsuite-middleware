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

package com.openexchange.grizzly;

import static org.junit.Assert.assertEquals;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import com.openexchange.ajax.simple.AbstractSimpleClientTest;
import com.openexchange.ajax.simple.SimpleOXClient;

/**
 * {@link OmitUserInputTest} - Check that user input via request url isn't echoed to the client.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.3
 */
public class OmitUserInputTest extends AbstractSimpleClientTest {

    private static String USER_INPUT = "i_do_not_exist_at_all";

    @Test
    public void test() throws Exception {
        SimpleOXClient oxClient = createClient();
        HttpClient httpClient = oxClient.getClient();
        HttpMethod getMissing = new GetMethod("/servlet/" + USER_INPUT);
        int status = httpClient.executeMethod(getMissing);

        assertEquals(404, status);
    }
}
