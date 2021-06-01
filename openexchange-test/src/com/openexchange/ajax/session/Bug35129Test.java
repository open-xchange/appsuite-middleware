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

package com.openexchange.ajax.session;

import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.params.ClientPNames;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.EmptyHttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthResponse;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * Test proper server response if Authentication header is not sent.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug35129Test extends AbstractAJAXSession {

    private AJAXClient myClient;

    @Override
    @Before
    public void setUp() throws Exception {
        AJAXConfig.init();
        super.setUp();
        myClient = getClient();
        myClient.getSession().getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }


    @Test
    public void test4UnauthorizedResponse() throws Exception {
        HttpAuthResponse response = myClient.execute(new EmptyHttpAuthRequest(false, false, false));
        Assert.assertEquals("Missing Authorization header should give according status code.", HttpServletResponse.SC_UNAUTHORIZED, response.getStatusCode());
        Assert.assertEquals("Authorization Required!", response.getReasonPhrase());
    }
}
