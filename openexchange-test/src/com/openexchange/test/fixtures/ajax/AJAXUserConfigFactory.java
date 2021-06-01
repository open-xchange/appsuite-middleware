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

package com.openexchange.test.fixtures.ajax;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.SimpleCredentials;
import com.openexchange.test.fixtures.TestUserConfig;
import com.openexchange.test.fixtures.TestUserConfigFactory;

public class AJAXUserConfigFactory implements TestUserConfigFactory {

    @Override
    public TestUserConfig create(SimpleCredentials credentials) {
        AJAXSession session = new AJAXSession();
        try {
            AJAXClient client = new AJAXClient(session, true);
            session.setId(client.execute(new LoginRequest(credentials.getLogin(), credentials.getPassword(), LoginTools.generateAuthId(), AJAXUserConfigFactory.class.getName(), "6.15.0")).getSessionId());
            return new AJAXUserConfig(client);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
