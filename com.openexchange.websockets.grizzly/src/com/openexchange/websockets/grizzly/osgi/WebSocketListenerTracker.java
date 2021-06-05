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

package com.openexchange.websockets.grizzly.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.websockets.WebSocketListener;
import com.openexchange.websockets.grizzly.impl.DefaultGrizzlyWebSocketApplication;
import com.openexchange.websockets.grizzly.impl.WebSocketListenerAdapter;
import com.openexchange.websockets.grizzly.impl.WebSocketListenerRegistry;


/**
 * {@link WebSocketListenerTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketListenerTracker implements ServiceTrackerCustomizer<Object, Object>, WebSocketListenerRegistry {

    /*
     * Uses a ReadWriteLock to prevent from calling getListeners() while a new listener appears in addingService() call-back.
     * Non-synchronized access might end-up in WebSockets having duplicate listeners added.
     */

    private final BundleContext context;
    private final CopyOnWriteArrayList<org.glassfish.grizzly.websockets.WebSocketListener> adapters;
    private volatile DefaultGrizzlyWebSocketApplication application;

    /**
     * Initializes a new {@link WebSocketListenerTracker}.
     */
    public WebSocketListenerTracker(BundleContext context, WebSocketListener... initialListeners) {
        super();
        this.context = context;
        if (null != initialListeners && initialListeners.length > 0) {
            List<org.glassfish.grizzly.websockets.WebSocketListener> initialAdapters = new ArrayList<>(initialListeners.length);
            for (WebSocketListener listener : initialListeners) {
                initialAdapters.add(WebSocketListenerAdapter.newAdapterFor(listener));
            }
            adapters = new CopyOnWriteArrayList<>(initialAdapters);
        } else {
            adapters = new CopyOnWriteArrayList<>();
        }
    }

    /**
     * Sets the application
     *
     * @param application The application to set
     */
    public void setApplication(DefaultGrizzlyWebSocketApplication application) {
        this.application = application;
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        Object service = context.getService(reference);
        if (false == (service instanceof WebSocketListener)) {
            context.ungetService(reference);
            return null;
        }

        WebSocketListener listener = (WebSocketListener) service;
        WebSocketListenerAdapter adapter = WebSocketListenerAdapter.newAdapterFor(listener);
        if (adapters.addIfAbsent(adapter)) {
            DefaultGrizzlyWebSocketApplication application = this.application;
            if (null != application) {
                application.addWebSocketListener(adapter);
            }
            return listener;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        if (false == (service instanceof WebSocketListener)) {
            context.ungetService(reference);
            return;
        }

        WebSocketListener listener = (WebSocketListener) service;
        WebSocketListenerAdapter adapter = WebSocketListenerAdapter.newAdapterFor(listener);
        if (adapters.remove(adapter)) {
            DefaultGrizzlyWebSocketApplication application = this.application;
            if (null != application) {
                application.removeWebSocketListener(adapter);
            }
        }
        context.ungetService(reference);
    }

    @Override
    public List<org.glassfish.grizzly.websockets.WebSocketListener> getListeners() {
        return adapters;
    }

}
