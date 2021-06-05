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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12437Test {

    private AJAXClient client;

    private String login;

    private String password;

    public Bug12437Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        AJAXConfig.init();
        final AJAXSession session = new AJAXSession();
        client = new AJAXClient(session, true);
        login = "some invalid login";
        password = "some invalid password";
    }

    @After
    public void tearDown() {
        client.getSession().getConversation().clearContents();
    }

    /**
     * Checks if login with wrong credentials gives LGI-0006.
     */
    @Test
    public void testWrongErrorCode() throws Throwable {
        LoginRequest request = new LoginRequest(login, password, LoginTools.generateAuthId(), Bug12437Test.class.getName(), "6,15.0", false);
        final LoginResponse response = client.execute(request);
        assertTrue("Wrong credentials are not detected.", response.hasError());
        final OXException exc = response.getException();
        assertEquals("Wrong exception message.", Category.CATEGORY_USER_INPUT, exc.getCategory());
        assertEquals("Wrong exception message.", 6, exc.getCode());
    }
}
