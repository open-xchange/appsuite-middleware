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

package com.openexchange.appsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import com.openexchange.ajax.simple.AbstractSimpleClientTest;
import com.openexchange.ajax.simple.SimpleOXClient;

/**
 * {@link Bug50721Test}
 *
 * Reflected content for /api/apps/load
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug50721Test extends AbstractSimpleClientTest {

    @Test
    public void testReflectedContent() throws Exception {
        String uri = "/appsuite/api/apps/load/7.8.3-5.20161130.145751,/text;+++++++++++++++++++++++++++++++++++++++++However.it.has.been.moved.to.our.new.website.at.WWW.TTACKER.COM";
        SimpleOXClient oxClient = createClient();
        HttpMethod getMethod = new GetMethod(uri);
        oxClient.getClient().executeMethod(getMethod);
        assertEquals(HttpServletResponse.SC_OK, getMethod.getStatusCode());
        String response = getMethod.getResponseBodyAsString();
        getMethod.releaseConnection();
        assertNotNull(response);
        assertFalse(response.contains("ACKER"));

    }

}
