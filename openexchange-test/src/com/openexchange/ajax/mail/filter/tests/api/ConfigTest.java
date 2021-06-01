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

package com.openexchange.ajax.mail.filter.tests.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.models.MailFilterConfigDatav2;
import com.openexchange.testing.httpclient.models.MailFilterConfigResponsev2;
import com.openexchange.testing.httpclient.modules.MailfilterApi;

/**
 * {@link ConfigTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class ConfigTest extends AbstractAPIClientSession {

    /**
     * Initializes a new {@link ConfigTest}.
     *
     * @param name The test case's name
     */
    public ConfigTest() {
        super();
    }

    /**
     * Test the GET /ajax/mailfilter/v2?action=config API call
     */
    @Test
    public void testConfig() throws Exception {
        MailfilterApi api = new MailfilterApi(getApiClient());
        MailFilterConfigResponsev2 response = api.getConfigV2(null);

        assertNotNull("The mail filter configuration response is null", response);
        assertNotNull("The mail filter configuration data is null", response.getData());
        MailFilterConfigDatav2 config = response.getData();
        assertNotNull("The 'tests' list is null", config.getTests());
        assertNotNull("The 'actionCommands' list is null", config.getActioncmds());

        assertFalse("The 'tests' list is empty", config.getTests().isEmpty());
        assertFalse("The 'actionCommands list is empty", config.getActioncmds().isEmpty());
    }
}
