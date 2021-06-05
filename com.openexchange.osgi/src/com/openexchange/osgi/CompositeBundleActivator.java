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

package com.openexchange.osgi;

import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * With this abstract class multiple activators in a bundle can be joined.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class CompositeBundleActivator implements BundleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositeBundleActivator.class);

    /**
     * The stack of activated {@link BundleActivator}s.
     */
    private final Stack<BundleActivator> activated;

    /**
     * Initializes a new {@link CompositeBundleActivator}.
     */
    protected CompositeBundleActivator() {
        super();
        activated = new Stack<BundleActivator>();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        Exception first = null;
        for (final BundleActivator activator : getActivators()) {
            try {
                activator.start(context);
                activated.push(activator);
            } catch (Exception e) {
                if (null == first) {
                    first = e;
                }
                LOG.error("Exception while running activator {}", activator.getClass().getName(), e);
            }
        }
        if (null != first) {
            throw first;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        Exception first = null;
        while (!activated.isEmpty()) {
            final BundleActivator activator = activated.pop();
            try {
                activator.stop(context);
            } catch (Exception e) {
                if (null == first) {
                    first = e;
                }
                LOG.error("Exception while stopping activator {}", activator.getClass().getName(), e);
            }
        }
        if (null != first) {
            throw first;
        }
    }

    /**
     * Gets the joined {@link BundleActivator activators} which shall be started sequentially.
     *
     * @return The joined {@link BundleActivator activators}
     */
    protected abstract BundleActivator[] getActivators();

}
