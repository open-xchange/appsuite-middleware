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

package com.openexchange.metrics.noop;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.openexchange.metrics.types.Timeable;
import com.openexchange.metrics.types.Timer;


/**
 * {@link NoopTimer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class NoopTimer implements Timer {

    private static final NoopTimer INSTANCE = new NoopTimer();

    private NoopTimer() {
        super();
    }

    public static NoopTimer getInstance() {
        return INSTANCE;
    }

    @Override
    public void update(long duration, TimeUnit timeUnit) {

    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return event.call();
    }

    @Override
    public <T, E extends Exception> T time(Timeable<T, E> timeable) throws E {
        return timeable.call();
    }

    @Override
    public <T> T timeSupplier(Supplier<T> event) {
        return event.get();
    }

    @Override
    public void time(Runnable event) {
        event.run();
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getFifteenMinuteRate() {
        return 0;
    }

    @Override
    public double getFiveMinuteRate() {
        return 0;
    }

    @Override
    public double getMeanRate() {
        return 0;
    }

    @Override
    public double getOneMinuteRate() {
        return 0;
    }

}
