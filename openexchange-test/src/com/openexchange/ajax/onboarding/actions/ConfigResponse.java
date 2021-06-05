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
