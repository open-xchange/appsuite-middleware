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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.HttpErrorCodeException;
import com.openexchange.ajax.requesthandler.ListenerCollectingResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.servlet.StatusKnowing;

/**
 * {@link AbstractListenerCollectingResponseRenderer}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public abstract class AbstractListenerCollectingResponseRenderer implements ListenerCollectingResponseRenderer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractListenerCollectingResponseRenderer.class);

    /** The reference for listeners */
    private final AtomicReference<List<RenderListener>> listenersReference;

    /**
     * Initializes a new {@link AbstractListenerCollectingResponseRenderer}.
     */
    protected AbstractListenerCollectingResponseRenderer() {
        super();
        this.listenersReference = new AtomicReference<List<RenderListener>>(null);
    }

    @Override
    public void addRenderListener(RenderListener listener) {
        if (listener == null) {
            return;
        }

        List<RenderListener> listeners;
        List<RenderListener> newListeners;
        do {
            listeners = listenersReference.get();
            newListeners = listeners == null ? ImmutableList.of(listener) : ImmutableList.<RenderListener> builderWithExpectedSize(listeners.size() + 1).addAll(listeners).add(listener).build();
        } while (false == listenersReference.compareAndSet(listeners, newListeners));
    }

    @Override
    public void removeRenderListener(RenderListener listener) {
        if (listener == null) {
            return;
        }

        List<RenderListener> listeners;
        List<RenderListener> newListeners;
        do {
            listeners = listenersReference.get();
            if (listeners == null) {
                // Cannot remove from empty list. Leave as-is.
                return;
            }

            // Copy to local list for removal
            newListeners = new ArrayList<>(listeners);
            boolean removed = newListeners.remove(listener);
            if (removed == false) {
                // Nothing removed. Leave as-is.
                return;
            }

            newListeners = newListeners.isEmpty() ? null : ImmutableList.copyOf(newListeners);
        } while (false == listenersReference.compareAndSet(listeners, newListeners));
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        // Fast check for available listeners
        List<RenderListener> listeners = listenersReference.get();
        if (listeners == null) {
            // No listeners...
            actualWrite(request, result, req, resp);
            return;
        }

        // Collect applicable listeners
        List<RenderListener> applicableListeners = new LinkedList<RenderListener>();
        for (RenderListener renderListener : listeners) {
            if (renderListener.handles(request)) {
                applicableListeners.add(renderListener);
            }
        }

        if (applicableListeners.isEmpty()) {
            // No applicable listeners...
            actualWrite(request, result, req, resp);
            return;
        }

        StatusKnowing statusKnowing = null;
        if (resp instanceof StatusKnowing) {
            statusKnowing = (StatusKnowing) resp;
        }

        // Trigger onBeforeWrite()
        try {
            beforeWrite(request, result, req, resp, applicableListeners);
            if (null != statusKnowing && isError(statusKnowing.getStatus())) {
                // onBeforeWrite() set a HTTP error
                return;
            }
        } catch (OXException e) {
            LOG.error("Skipped using renderer '{}' due to an error.", this.getClass().getName(), e);
            return;
        }

        // Do the actual write and trigger onAfterWrite() afterwards
        Exception exceptionCausedByActualWrite = null;
        try {
            actualWrite(request, result, req, resp);

            // Assign a suitable exception in case a non-OK status is set
            if (null != statusKnowing) {
                int status = ((StatusKnowing) resp).getStatus();
                if (isError(status)) {
                    // Assume error was returned
                    exceptionCausedByActualWrite = new HttpErrorCodeException(status);
                }
            }
        } catch (RuntimeException e) {
            exceptionCausedByActualWrite = e;
            LOG.error("Skipped using renderer '{}' due to an error.", this.getClass().getName(), e);
        } finally {
            // Trigger onAfterWrite()
            try {
                afterWrite(request, result, exceptionCausedByActualWrite, applicableListeners);
            } catch (Exception e) {
                LOG.error("Skipped using renderer '{}' due to an error.", this.getClass().getName(), e);
            }
        }
    }

    private boolean isError(int status) {
        return ((status >= 400 && status <= 499) || (status >= 500 && status <= 599));
    }

    /**
     * The actual call to the write implementation.
     * <p>
     * Gets invoked after AbstractResponseRenderer.beforeWrite(AJAXRequestData) and before AbstractResponseRenderer.afterWrite(AJAXRequestData, AJAXRequestResult) is called.
     *
     * @param request The request data
     * @param result The result
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public abstract void actualWrite(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp);

    /**
     * Triggers the {@link RenderListener#onBeforeWrite(AJAXRequestData) onBeforeWrite()} method of registered listeners.
     *
     * @param requestData The AJAX request data to pass
     * @param resp The result
     * @param req The HTTP request
     * @param result The HTTP response
     * @param listeners The render listeners to trigger
     * @throws OXException If a registered listener signals to abort further processing
     */
    protected void beforeWrite(AJAXRequestData requestData, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp, Collection<RenderListener> listeners) throws OXException {
        for (RenderListener renderListener : listeners) {
            renderListener.onBeforeWrite(requestData, result, req, resp);
        }
    }

    /**
     * Triggers the {@link RenderListener#onAfterWrite(AJAXRequestData, AJAXRequestResult, Exception) onAfterWrite()} method of registered listeners.
     *
     * @param requestData The AJAX request data to pass
     * @param result The result data to pass
     * @param writeException The optional unexpected exception
     * @param listeners The render listeners to trigger
     * @throws OXException If a registered listener signals to abort further processing
     */
    public void afterWrite(AJAXRequestData requestData, AJAXRequestResult result, Exception writeException, Collection<RenderListener> listeners) throws OXException {
        for (RenderListener renderListener : listeners) {
            renderListener.onAfterWrite(requestData, result, writeException);
        }
    }

}
