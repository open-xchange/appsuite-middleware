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

package com.openexchange.apps.manifests.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.apps.manifests.ManifestBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ManifestActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ManifestActionFactory implements AJAXActionServiceFactory {

    private static final String MODULE = "apps/manifests";

    /**
     * Gets the <code>"apps/manifests"</code> module identifier.
     *
     * @return The module identifier
     */
    public static String getModule() {
        return MODULE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link ManifestActionFactory}.
     *
     * @param services The {@link ServiceLookup}
     * @param manifestBuilder The {@link ManifestBuilder}
     */
    public ManifestActionFactory(ServiceLookup services, ManifestBuilder manifestBuilder) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builderWithExpectedSize(2);
        actions.put("all", new AllAction( manifestBuilder));
        actions.put("config", new ConfigAction(services, manifestBuilder));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService actionService = actions.get(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, MODULE);
        }
        return actionService;
    }

}
