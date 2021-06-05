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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAjaxSession;
import com.openexchange.ajax.onboarding.actions.ExecuteRequest;
import com.openexchange.ajax.onboarding.actions.OnboardingTestResponse;
import com.openexchange.config.cascade.ConfigViewScope;

/**
 * {@link EMClientURLTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class EMClientURLTest extends AbstractConfigAwareAjaxSession {

    public EMClientURLTest() {}

    private static Map<String, String> confs;

    static {
        confs = new HashMap<String, String>();
        confs.put("com.openexchange.client.onboarding.emclient.url", "http://www.open-xchange.com");
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return confs;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();
    }

    @Override
    public String getScope() {
        return ConfigViewScope.USER.getScopeName();
    }

    @Test
    public void testEMClientURL() throws Exception {
        ExecuteRequest req = new ExecuteRequest("windows.desktop/emclientinstall", "link", null, false);
        OnboardingTestResponse response = getAjaxClient().execute(req);
        assertNotNull("Response is empty!", response);
        if (response.hasError()) {
            fail("The response has an unexpected error: " + response.getException().getMessage());
        }
        Object data = response.getData();
        assertNotNull("Response has no data!", data);
        assertTrue("Unexpected response data type", data instanceof JSONObject);
        JSONObject jobj = ((JSONObject) data);
        Object linkObj = jobj.get("link");
        assertNotNull("Data object doesn't contain a link field", linkObj);
        assertTrue("Unexpected link field data type", linkObj instanceof String);
        String link = ((String) linkObj);
        assertTrue("The url " + link + " isn't valid!", UrlValidator.getInstance().isValid(link));
    }
}
