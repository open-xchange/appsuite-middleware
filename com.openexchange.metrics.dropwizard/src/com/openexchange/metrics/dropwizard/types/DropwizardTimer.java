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

package com.openexchange.metrics.dropwizard.types;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.codahale.metrics.Snapshot;
import com.openexchange.metrics.types.Timeable;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardTimer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardTimer implements Timer {

    private com.codahale.metrics.Timer delegate;

    /**
     * Initialises a new {@link DropwizardTimer}.
     */
    public DropwizardTimer(com.codahale.metrics.Timer timer) {
        this.delegate = timer;
    }

    @Override
    public void update(long duration, TimeUnit timeUnit) {
        delegate.update(duration, timeUnit);
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return delegate.time(event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends Exception> T time(Timeable<T, E> timeable) throws E {
        try {
            return delegate.time(() -> timeable.call());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw (E) e;
        }
    }

    @Override
    public <T> T timeSupplier(Supplier<T> event) {
        return delegate.timeSupplier(event);
    }

    @Override
    public void time(Runnable event) {
        delegate.time(event);
    }

    @Override
    public long getCount() {
        return delegate.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return delegate.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return delegate.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return delegate.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return delegate.getOneMinuteRate();
    }

    /**
     * Returns a snapshot of the values.
     * 
     * @return a snapshot of the values.
     */
    public Snapshot getSnapshot() {
        return delegate.getSnapshot();
    }
}
