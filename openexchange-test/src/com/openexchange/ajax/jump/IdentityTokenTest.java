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

package com.openexchange.ajax.jump;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.jump.actions.DummyRequest;
import com.openexchange.ajax.jump.actions.IdentityTokenRequest;
import com.openexchange.ajax.jump.actions.IdentityTokenResponse;

/**
 * {@link IdentityTokenTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IdentityTokenTest extends AbstractJumpTest {

    @Test
    public void testIdentityToken() {
        try {
            getClient().execute(new DummyRequest());

            IdentityTokenResponse identityTokenResponse = getClient().execute(new IdentityTokenRequest("dummy"));
            String token = identityTokenResponse.getToken();

            assertNotNull(token);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
