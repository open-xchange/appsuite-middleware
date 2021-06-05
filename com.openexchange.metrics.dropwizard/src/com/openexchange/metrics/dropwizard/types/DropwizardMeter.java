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

import com.openexchange.metrics.types.Meter;

/**
 * {@link DropwizardMeter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMeter implements Meter {

    private final com.codahale.metrics.Meter delegate;

    /**
     * Initialises a new {@link DropwizardMeter}.
     * 
     * @param meter The delegate {@link com.codahale.metrics.Meter}
     */
    public DropwizardMeter(com.codahale.metrics.Meter meter) {
        super();
        this.delegate = meter;
    }

    @Override
    public void mark() {
        delegate.mark();
    }

    @Override
    public void mark(long n) {
        delegate.mark(n);
    }

    @Override
    public long getCount() {
        return delegate.getCount();
    }

    @Override
    public double getOneMinuteRate() {
        return delegate.getOneMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return delegate.getFiveMinuteRate();
    }

    @Override
    public double getFifteenMinuteRate() {
        return delegate.getFifteenMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return delegate.getMeanRate();
    }
}
