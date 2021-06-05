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

package com.openexchange.ajax.requesthandler;

import com.openexchange.exception.OXException;

/**
 * {@link NonAbortingDispatcherListener} - A dispatcher listener that is not supposed to abort disptacher processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class NonAbortingDispatcherListener implements DispatcherListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NonAbortingDispatcherListener.class);

    /**
     * Initializes a new {@link NonAbortingDispatcherListener}.
     */
    protected NonAbortingDispatcherListener() {
        super();
    }

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     * @throws OXException If this listener signals to abort further processing
     */
    @Override
    public void onRequestInitialized(AJAXRequestData requestData) {
        try {
            doOnRequestInitialized(requestData);
        } catch (Exception x) {
            LOG.error("Failed to execute dispatcher listener {}", this.getClass().getSimpleName(), x);
        }
    }

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     * @throws OXException If this listener signals to abort further processing
     */
    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        try {
            doOnRequestPerformed(requestData, requestResult, e);
        } catch (Exception x) {
            LOG.error("Failed to execute dispatcher listener {}", this.getClass().getSimpleName(), x);
        }
    }

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     * @throws Exception If the "request initialized" call-back cannot be successfully handled
     */
    protected abstract void doOnRequestInitialized(AJAXRequestData requestData) throws Exception;

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     * @throws Exception If the "request performed" call-back cannot be successfully handled
     */
    protected abstract void doOnRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) throws Exception;

}
