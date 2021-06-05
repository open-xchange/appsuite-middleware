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

package com.openexchange.ajax.framework;

import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONObject;
import com.openexchange.ajax.framework.config.util.ChangePropertiesRequest;
import com.openexchange.ajax.framework.config.util.ChangePropertiesResponse;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.rest.AbstractRestTest;

/**
 * {@link AbstractConfigAwareAjaxSession} extends the AbstractAjaxSession with methods to preconfigure reloadable configurations before executing the tests.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public abstract class AbstractConfigAwareAjaxSession extends AbstractRestTest {

    /**
     * Initializes a new {@link AbstractConfigAwareAjaxSession}.
     *
     * @param name
     */
    protected AbstractConfigAwareAjaxSession() {}

    JSONObject oldData;

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }

    /**
     * Changes the configurations given by {@link #getNeededConfigurations()}.
     *
     * @throws Exception if changing the configuration fails
     */
    protected void setUpConfiguration() throws Exception {
        Map<String, String> map = getNeededConfigurations();
        if (!map.isEmpty()) {
            // change configuration to new values
            ChangePropertiesRequest req = new ChangePropertiesRequest(map, getScope(), getReloadables());
            ChangePropertiesResponse response = getAjaxClient().execute(req);
            oldData = ResponseWriter.getJSON(response.getResponse()).getJSONObject("data");
        }
    }

    /**
     *
     * Retrieves all needed configurations.
     *
     * Should be overwritten by child implementations to define necessary configurations.
     *
     * @return Needed configurations.
     */
    protected Map<String, String> getNeededConfigurations() {
        return Collections.emptyMap();
    }

    /**
     * Retrieves the scope to use for the configurations.
     *
     * Can be overwritten by child implementations to change the scope of the configurations. Defaults to "server".
     *
     * @return The scope for the configuration.
     */
    protected String getScope() {
        return ConfigViewScope.SERVER.getScopeName();
    }

    /**
     * Retrieves the the names of the reloadable classes which should be reloaded.
     *
     * Can be overwritten by child implementations. Defaults to null.
     *
     * @return A comma separated list of reloadable class names or null.
     */
    protected String getReloadables() {
        return null;
    }
}
