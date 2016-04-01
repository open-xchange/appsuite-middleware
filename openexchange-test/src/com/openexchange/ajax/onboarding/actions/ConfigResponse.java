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

package com.openexchange.ajax.onboarding.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Platform;
import com.openexchange.client.onboarding.Scenario;


/**
 * {@link ConfigResponse}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class ConfigResponse extends AbstractAJAXResponse {

    protected ConfigResponse(Response response) {
        super(response);
    }
    
    public List<Platform> getPlatforms() throws Exception {
        JSONObject json = (JSONObject) getData();
        List<Platform> platforms = new ArrayList<Platform>();
        JSONArray jsonPlatforms = json.getJSONArray("platforms");
        for (int i = 0; i < jsonPlatforms.length(); i++) {
            JSONObject platform = jsonPlatforms.getJSONObject(i);
            platforms.add(Platform.platformFor(platform.getString("id")));
        }
        return platforms;
    }
    
    public List<Device> getDevices() throws Exception {
        JSONObject json = (JSONObject) getData();
        List<Device> devices = new ArrayList<Device>();
        JSONArray jsonDevices = json.getJSONArray("devices");
        for (int i = 0; i < jsonDevices.length(); i++) {
            JSONObject device = jsonDevices.getJSONObject(i);
            devices.add(Device.deviceFor(device.getString("id")));
        }
        return devices;
    }
    
    public List<Scenario> getScenario() throws Exception {
        JSONObject json = (JSONObject) getData();
        List<Scenario> scenarios = new ArrayList<Scenario>();
        JSONArray jsonScenarios = json.getJSONArray("scenarios");
        for (int i = 0; i < jsonScenarios.length(); i++) {
//            JSONArray scenario = jsonScenarios.getJSONObject(i);
//            scenarios.add(new Sc)
        }
        return scenarios;
    }
    
    public Map<String, List<String>> getMatching() throws Exception {
        JSONObject json = (JSONObject) getData();
        JSONArray matching = json.getJSONArray("matching");
        Map<String, List<String>> result = new HashMap<String, List<String>>(matching.length());
        for (int i = 0; i < matching.length(); i++) {
            JSONObject match = matching.getJSONObject(i);
            JSONArray jsonActions = match.getJSONArray("actions");
            List<String> actions = new ArrayList<String>(jsonActions.length());
            for (int j = 0; j < jsonActions.length(); j++) {
                String action = jsonActions.getString(j);
                actions.add(action);
            }
            result.put(match.getString("id"), actions);
        }
        return result;
    }
    
}
