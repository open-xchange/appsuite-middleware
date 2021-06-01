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

package com.openexchange.ajax.redirect;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.params.ClientPNames;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.redirect.actions.RedirectRequest;
import com.openexchange.ajax.redirect.actions.RedirectResponse;
import com.openexchange.exception.OXException;

/**
 * {@link Bug25140Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug25140Test extends AbstractAJAXSession {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getClient().getSession().getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    @Test
    public void testForArbitraryURLRedirect() throws OXException, IOException, JSONException {
        RedirectRequest request = new RedirectRequest("%0d/", "www.google.de");
        RedirectResponse response = getClient().execute(request);
        Assert.assertThat("Backend should return status code 400 if to another URL should be redirected.", I(response.getStatusCode()), equalTo(I(HttpServletResponse.SC_BAD_REQUEST)));
        Assert.assertThat("Backend should not return redirects to other URLs.", "//www.google.de", not(equalTo(response.getLocation())));
    }
}
