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

package com.openexchange.jump.json.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.jump.EndpointHandler;
import com.openexchange.jump.json.EndpointHandlerRegistry;
import com.openexchange.jump.json.JumpActionFactory;
import com.openexchange.jump.json.actions.AbstractJumpAction;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link JumpJsonActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JumpJsonActivator extends AJAXModuleActivator {

    private static final AtomicReference<BundleContext> START_BUNDLE_CONTEXT_REF = new AtomicReference<BundleContext>();

    /**
     * Gets the bundle context passed to this activator on bundle start-up.
     *
     * @return The bundle context or <code>null</code> if absent
     */
    public static BundleContext getBundleContextFromStartUp() {
        return START_BUNDLE_CONTEXT_REF.get();
    }

    // -------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link JumpJsonActivator}.
     */
    public JumpJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        START_BUNDLE_CONTEXT_REF.set(context);

        final RankingAwareNearRegistryServiceTracker<EndpointHandler> handlers = new RankingAwareNearRegistryServiceTracker<EndpointHandler>(context, EndpointHandler.class);
        rememberTracker(handlers);
        openTrackers();
        AbstractJumpAction.setEndpointHandlerRegistry(new EndpointHandlerRegistry(handlers));

        registerModule(new JumpActionFactory(this), "jump");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        AbstractJumpAction.setEndpointHandlerRegistry(null);
        START_BUNDLE_CONTEXT_REF.set(null);
    }

}
