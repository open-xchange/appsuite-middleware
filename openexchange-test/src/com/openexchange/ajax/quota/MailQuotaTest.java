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

package com.openexchange.ajax.quota;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link MailQuotaTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MailQuotaTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link MailQuotaTest}.
     *
     * @param name The test name
     */
    public MailQuotaTest() {
        super();
    }

    @Test
    public void testMailQuota() throws Exception {
        /*
         * get filestore usage quota
         */
        MailQuotaRequest request = new MailQuotaRequest();
        MailQuotaResponse response = getClient().execute(request);
        JSONObject jsonQuota = (JSONObject) response.getData();
        assertNotNull("No response data", jsonQuota);
        assertTrue("No use found", jsonQuota.hasAndNotNull("use"));
        assertTrue("No use found", jsonQuota.hasAndNotNull("quota"));
    }

}
