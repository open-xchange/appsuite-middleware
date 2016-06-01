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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    /** The queue for added listeners */
    protected final Queue<RenderListener> renderListenerRegistry;

    /** The flag signaling is there is any registered listener */
    protected volatile boolean hasRenderListeners;

    /**
     * Initializes a new {@link AbstractListenerCollectingResponseRenderer}.
     */
    protected AbstractListenerCollectingResponseRenderer() {
        super();
        this.renderListenerRegistry = new ConcurrentLinkedQueue<RenderListener>();
    }

    @Override
    public void addRenderListener(RenderListener listener) {
        if (null != listener && renderListenerRegistry.add(listener)) {
            hasRenderListeners = true;
        }
    }

    @Override
    public void removeRenderListener(RenderListener listener) {
        if (null != listener && renderListenerRegistry.remove(listener)) {
            hasRenderListeners = (false == renderListenerRegistry.isEmpty());
        }
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        // Fast check for available listeners
        if (false == hasRenderListeners) {
            // No listeners...
            actualWrite(request, result, req, resp);
            return;
        }

        // Collect applicable listeners
        List<RenderListener> applicableListeners = new LinkedList<RenderListener>();
        for (RenderListener renderListener : this.renderListenerRegistry) {
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
            if (renderListener.handles(requestData)) {
                renderListener.onBeforeWrite(requestData, result, req, resp);
            }
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
            if (renderListener.handles(requestData)) {
                renderListener.onAfterWrite(requestData, result, writeException);
            }
        }
    }

}
