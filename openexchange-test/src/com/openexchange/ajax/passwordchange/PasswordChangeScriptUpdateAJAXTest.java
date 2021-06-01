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

package com.openexchange.ajax.passwordchange;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeScriptResultRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeScriptResultResponse;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.exception.OXException;

/**
 * {@link PasswordChangeScriptUpdateAJAXTest} - Tests the UPDATE request on password
 * change servlet in combination with an external password change script. Especially
 * verify that UTF-8 characters reach the script.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 *
 */
public final class PasswordChangeScriptUpdateAJAXTest extends AbstractPasswordChangeAJAXTest {

    /**
     * Initializes a new {@link PasswordChangeScriptUpdateAJAXTest}
     *
     **/
    public PasswordChangeScriptUpdateAJAXTest() {
        super();
    }

    /**
     * Tests the <code>action=update</code> request
     * 
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testUpdate() throws OXException, IOException, JSONException {
        /*
         * Perform update request
         */
        final String newPassword = "(\u0298\u203f\u0298)";
        final String oldPassword = testUser.getPassword();
        Executor.execute(getSession(), new PasswordChangeUpdateRequest(newPassword, oldPassword, true));

        //verify file contains same text
        PasswordChangeScriptResultResponse resultResponse = Executor.execute(getSession(), new PasswordChangeScriptResultRequest());
        String resultPassword = resultResponse.getPassword();
        assertEquals("Passwords differ", newPassword, resultPassword);
    }
}
