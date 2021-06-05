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

package com.openexchange.jump.json.actions;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jump.Endpoint;
import com.openexchange.jump.EndpointHandler;
import com.openexchange.jump.json.JumpRequest;
import com.openexchange.jump.json.osgi.JumpJsonActivator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link DummyEndpointHandlerAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DummyEndpointHandlerAction extends AbstractJumpAction {

    /**
     * Initializes a new {@link DummyEndpointHandlerAction}.
     *
     * @param services
     */
    public DummyEndpointHandlerAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final JumpRequest request) throws OXException, JSONException {
        final BundleContext context = JumpJsonActivator.getBundleContextFromStartUp();

        if (null != context) {
            if (!getEndpointHandlerRegistry().hasHandlerFor("dummy")) {
                final EndpointHandler eh = new EndpointHandler() {

                    @Override
                    public Set<String> systemNamesOfInterest() {
                        return Collections.singleton("dummy");
                    }

                    @Override
                    public boolean handleEndpoint(final UUID token, final Endpoint endpoint, final Session session) {
                        return true;
                    }
                };
                context.registerService(EndpointHandler.class, eh, null);
            }
            if (!getJumpSerivce().getEndpoints().containsKey("dummy")) {
                final Endpoint dummyEndpoint = new Endpoint() {

                    @Override
                    public String getUrl() {
                        return "http://dummy.invalid/path/info";
                    }

                    @Override
                    public String getSystemName() {
                        return "dummy";
                    }

                    @Override
                    public Object getProperty(String propName) {
                        return null;
                    }

                    @Override
                    public Map<String, Object> getProperties() {
                        return Collections.emptyMap();
                    }
                };
                context.registerService(Endpoint.class, dummyEndpoint, null);
            }
        }

        return new AJAXRequestResult(Boolean.TRUE, "native");
    }

}
