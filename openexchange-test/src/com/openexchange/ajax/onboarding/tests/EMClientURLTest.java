/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.onboarding.tests;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractConfigAwareAjaxSession;
import com.openexchange.ajax.onboarding.actions.ExecuteRequest;
import com.openexchange.ajax.onboarding.actions.OnboardingTestResponse;


/**
 * {@link EMClientURLTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class EMClientURLTest extends AbstractConfigAwareAjaxSession {

    public EMClientURLTest(String name) {
        super(name);
    }

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
    protected void setUp() throws Exception {
        super.setUp();
        setUpConfiguration(client, false);
    }

    public void testEMClientURL() throws Exception {
        ExecuteRequest req = new ExecuteRequest("windows.desktop/emclientinstall", "link", null, false);
        OnboardingTestResponse response = client.execute(req);
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
