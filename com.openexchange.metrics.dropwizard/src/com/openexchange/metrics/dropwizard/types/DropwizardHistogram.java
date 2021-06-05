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

import com.codahale.metrics.Snapshot;
import com.openexchange.metrics.types.Histogram;

/**
 * {@link DropwizardHistogram}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardHistogram implements Histogram {

    private com.codahale.metrics.Histogram delegate;

    /**
     * Initialises a new {@link Dropwizard1Histogram}.
     */
    public DropwizardHistogram(com.codahale.metrics.Histogram histogram) {
        super();
        this.delegate = histogram;
    }

    @Override
    public void update(int value) {
        delegate.update(value);
    }

    @Override
    public void update(long value) {
        delegate.update(value);
    }

    @Override
    public long getCount() {
        return delegate.getCount();
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
