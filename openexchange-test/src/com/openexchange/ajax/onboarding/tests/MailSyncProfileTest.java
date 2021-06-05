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

package com.openexchange.ajax.onboarding.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractSmtpAJAXSession;
import com.openexchange.ajax.onboarding.actions.ExecuteRequest;
import com.openexchange.ajax.onboarding.actions.OnboardingTestResponse;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link MailSyncProfileTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class MailSyncProfileTest extends AbstractSmtpAJAXSession {

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().build();
    }

    @Test
    public void testIMAPSyncProfileViaEmail() throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", getClient().getValues().getDefaultAddress());
        ExecuteRequest req = new ExecuteRequest("apple.mac/mailsync", "email", body, false);
        getClient().execute(req);

        assertEquals(1, mailManager.getMailCount());
    }

    @Test
    public void testIMAPSyncProfileViaDisplay() throws Exception {
        ExecuteRequest req = new ExecuteRequest("apple.mac/mailmanual", "display", null, false);
        OnboardingTestResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        JSONObject json = (JSONObject) resp.getData();
        assertTrue(json.hasAndNotNull("imapLogin"));
        assertTrue(json.hasAndNotNull("imapServer"));
        assertTrue(json.hasAndNotNull("imapPort"));
        assertTrue(json.hasAndNotNull("imapSecure"));
        assertTrue(json.hasAndNotNull("smtpLogin"));
        assertTrue(json.hasAndNotNull("smtpServer"));
        assertTrue(json.hasAndNotNull("smtpPort"));
        assertTrue(json.hasAndNotNull("smtpSecure"));
    }

}
