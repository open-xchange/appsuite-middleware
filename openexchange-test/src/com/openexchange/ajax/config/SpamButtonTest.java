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

package com.openexchange.ajax.config;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;

public class SpamButtonTest extends AbstractAJAXSession {

    /**
     * Path to the configuration parameter.
     */
    private static final String PATH = "/mail/spambutton";

    /**
     * Tests if the spam button option is sent to the GUI.
     * 
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testSpamButton() throws Throwable {
        final String value = getClient().execute(new GetRequest(PATH)).getData().toString();
        assertTrue("Got no value for the spam button configuration parameter.", Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value));
    }
}
