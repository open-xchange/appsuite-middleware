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

package com.openexchange.health;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link AbstractCachingMWHealthCheck} - Abstract class for health checks, that cache their response for specified time-to-live prior to
 * actually re-checking.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public abstract class AbstractCachingMWHealthCheck implements MWHealthCheck {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractCachingMWHealthCheck.class);

    /** The reference to cached result wrapped by a Future */
    protected final AtomicReference<StampedFutureTask> stammpedResponseReference;

    /** The time-to-live duration in milliseconds */
    protected final long timeToLiveMillis;

    /**
     * Initializes a new {@link AbstractCachingMWHealthCheck}.
     *
     * @param timeToLiveMillis The time-to-live in milliseconds for a cached response, if elapsed the response is considered as expired/invalid
     */
    protected AbstractCachingMWHealthCheck(long timeToLiveMillis) {
        super();
        this.timeToLiveMillis = timeToLiveMillis;
        stammpedResponseReference = new AtomicReference<>();
    }

    @Override
    public final MWHealthCheckResponse call() {
        StampedFutureTask stampedResponse = stammpedResponseReference.get();
        boolean executed = false;
        if (stampedResponse == null || stampedResponse.isExpired(timeToLiveMillis)) {
            StampedFutureTask ft = new StampedFutureTask(new HealthCheckCallable(this));
            if (stammpedResponseReference.compareAndSet(stampedResponse, ft)) {
                LOGGER.debug("Going to execute health check \"{}\"", getName());
                ft.run();
                stampedResponse = ft;
                executed = true;
            } else {
                stampedResponse = stammpedResponseReference.get();
            }
        }

        try {
            if (!executed) {
                LOGGER.debug("Fetching cached result for health check \"{}\"", getName());
            }
            return stampedResponse.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stampedResponse.cancel(true);
            throw new IllegalStateException("Wait for health check \"" + getName() + "\" aborted");
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Failure while trying to perform health check \"" + getName() +  "\"", t);
            }
        }
    }

    /**
     * Executes the health check.
     *
     * @return The health check response
     */
    protected abstract MWHealthCheckResponse doCall();

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class StampedFutureTask extends FutureTask<MWHealthCheckResponse> {

        final long checkTimeStamp;

        /**
         * Initializes a new {@link StampedFutureTask}.
         *
         * @param The health check callable
         */
        StampedFutureTask(HealthCheckCallable callable) {
            super(callable);
            checkTimeStamp = System.currentTimeMillis();
        }

        boolean isExpired(long timeToLiveMillis) {
            return (System.currentTimeMillis() - checkTimeStamp) > timeToLiveMillis;
        }
    }

    private static class HealthCheckCallable implements Callable<MWHealthCheckResponse> {

        private final AbstractCachingMWHealthCheck instance;

        HealthCheckCallable(AbstractCachingMWHealthCheck instance) {
            super();
            this.instance = instance;
        }

        @Override
        public MWHealthCheckResponse call() throws Exception {
            return instance.doCall();
        }
    }

}
