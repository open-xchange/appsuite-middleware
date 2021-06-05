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

package com.openexchange.websockets.cap.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.websockets.cap.WebSocketsCapabilityTracker;


/**
 * {@link WebSocketsCapabilityActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketsCapabilityActivator implements BundleActivator {

    private ServiceTracker<Object, Object> tracker;

    /**
     * Initializes a new {@link WebSocketsCapabilityActivator}.
     */
    public WebSocketsCapabilityActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        WebSocketsCapabilityTracker t = new WebSocketsCapabilityTracker(context);
        Filter filter = t.getFilter();
        ServiceTracker<Object, Object> tracker = new ServiceTracker<>(context, filter, t);
        tracker.open();
        this.tracker = tracker;
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        ServiceTracker<Object, Object> tracker = this.tracker;
        if (null != tracker) {
            this.tracker = null;
            tracker.close();
        }
    }

}
