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

package org.glassfish.grizzly.http.server;

import java.util.concurrent.Semaphore;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.filterchain.FilterChainEvent;
import org.glassfish.grizzly.filterchain.TransportFilter;

/**
 * {@link HttpRequestGuard} - A guard that controls the number of threads that are allowed to concurrently process a HTTP request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class HttpRequestGuard {

    /** The effectively no-op HTTP request guard */
    public static final HttpRequestGuard NO_GUARD = new HttpRequestGuard() {

        @Override
        public boolean mayHandle(Request newRequest) {
            return true;
        }

        @Override
        public String toString() {
            return "allow all";
        }
    };

    /**
     * Gets the appropriate guard for specified initial permits.
     *
     * @param permits The initial permits
     * @return The guard
     */
    public static HttpRequestGuard guardFor(int permits) {
        if (permits <= 0) {
            return NO_GUARD;
        }

        Semaphore semaphore = new Semaphore(permits);
        SemaphoreHandler semaphoreHandler = new SemaphoreHandler(semaphore);
        return new SemaphoreHttpRequestGuard(semaphore, semaphoreHandler);
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link HttpRequestGuard}.
     */
    protected HttpRequestGuard() {
        super();
    }

    /**
     * Checks if this guard allows that the given <i>new</i> HTTP request should be handled.
     *
     * @return <code>true</code> if the HTTP request is allowed being handled and <code>false</code> otherwise
     */
    public abstract boolean mayHandle(Request newRequest);

    // -----------------------------------------------------------------------------------------------------------

    private static final class SemaphoreHttpRequestGuard extends HttpRequestGuard {

        private final Semaphore semaphore;
        private final SemaphoreHandler semaphoreHandler;

        /**
         * Initializes a new {@link SemaphoreHttpRequestGuard}.
         *
         * @param semaphore The semaphore managing the permits
         * @param semaphoreHandler The handler caring about releasing a previously acquired permit
         */
        SemaphoreHttpRequestGuard(Semaphore semaphore, SemaphoreHandler semaphoreHandler) {
            super();
            this.semaphore = semaphore;
            this.semaphoreHandler = semaphoreHandler;
        }

        @Override
        public boolean mayHandle(Request newRequest) {
            boolean allowed = semaphore.tryAcquire();
            if (allowed) {
                newRequest.addAfterServiceListener(semaphoreHandler);
            }
            return allowed;
        }

        @Override
        public String toString() {
            return semaphore.toString();
        }
    }

    private static final class SemaphoreHandler extends EmptyCompletionHandler<Object> implements AfterServiceListener {

        private final FilterChainEvent event;
        private final Semaphore semaphore;

        SemaphoreHandler(Semaphore semaphore) {
            super();
            this.semaphore = semaphore;
            event = TransportFilter.createFlushEvent(this);
        }

        @Override
        public void cancelled() {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void failed(final Throwable throwable) {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void completed(final Object result) {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void onAfterService(final Request request) {
            // Same as request.getContext().flush(this), but less garbage
            request.getContext().notifyDownstream(event);
        }

        /**
         * Will be called, once HTTP request processing is complete and response is flushed.
         */
        private void onRequestCompleteAndResponseFlushed() {
            semaphore.release();
        }
    }

}
