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

package org.dmfs.rfc5545.recurrenceset;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.recurrenceset.AbstractRecurrenceAdapter.InstanceIterator;

/**
 * {@link ForwardingRecurrenceSet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ForwardingRecurrenceSet {

    private final List<AbstractRecurrenceAdapter> mInstances = new ArrayList<AbstractRecurrenceAdapter>();

    private List<AbstractRecurrenceAdapter> mExceptions = null;

    /**
     * Add instances to the set of instances.
     *
     * @param adapter
     *            An {@link AbstractRecurrenceAdapter} that defines instances.
     */
    public void addInstances(AbstractRecurrenceAdapter adapter) {
        mInstances.add(adapter);
    }

    /**
     * Add exceptions to the set of instances (i.e. effectively remove instances from the instance set).
     *
     * @param adapter
     *            An {@link AbstractRecurrenceAdapter} that defines instances.
     */
    public void addExceptions(AbstractRecurrenceAdapter adapter) {
        if (mExceptions == null) {
            mExceptions = new ArrayList<AbstractRecurrenceAdapter>();
        }
        mExceptions.add(adapter);
    }

    /**
     * Get an iterator for the specified start time.
     *
     * @param timezone
     *            The {@link TimeZone} of the first instance.
     * @param start
     *            The start time in milliseconds since the epoch.
     *
     * @return A {@link RecurrenceSetIterator} that iterates all instances.
     */
    public RecurrenceSetIterator iterator(TimeZone timezone, long start) {
        return iterator(timezone, start, Long.MAX_VALUE);
    }

    /**
     * Return a new {@link RecurrenceSetIterator} for this recurrence set.
     *
     * @param timezone
     *         The {@link TimeZone} of the first instance.
     * @param start
     *         The start time in milliseconds since the epoch.
     * @param end
     *         The end of the time range to iterate in milliseconds since the epoch.
     *
     * @return A {@link RecurrenceSetIterator} that iterates all instances.
     */
    public RecurrenceSetIterator iterator(TimeZone timezone, long start, long end) {
        List<InstanceIterator> instances = new ArrayList<InstanceIterator>(mInstances.size());
        // make sure we add the start as the first instance
        // instances.add(new RecurrenceList(new long[] { start }).getIterator(timezone, start));
        for (AbstractRecurrenceAdapter adapter : mInstances)
        {
            instances.add(adapter.getIterator(timezone, start));
        }

        List<InstanceIterator> exceptions = null;
        if (mExceptions != null)
        {
            exceptions = new ArrayList<InstanceIterator>(mExceptions.size());
            for (AbstractRecurrenceAdapter adapter : mExceptions)
            {
                exceptions.add(adapter.getIterator(timezone, start));
            }
        }
        return new RecurrenceSetIterator(instances, exceptions).setEnd(end);
    }

}
