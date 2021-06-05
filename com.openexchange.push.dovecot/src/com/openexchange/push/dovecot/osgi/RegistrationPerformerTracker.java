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

package com.openexchange.push.dovecot.osgi;

import java.util.PriorityQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.push.dovecot.AbstractDovecotPushListener;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.stateful.DovecotPushListener;


/**
 * {@link RegistrationPerformerTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RegistrationPerformerTracker implements ServiceTrackerCustomizer<RegistrationPerformer, RegistrationPerformer> {

    private final BundleContext context;
    private final PriorityQueue<RegistrationPerformerWrapper> performers;

    /**
     * Initializes a new {@link RegistrationPerformerTracker}.
     */
    public RegistrationPerformerTracker(BundleContext context) {
        super();
        this.context = context;
        performers = new PriorityQueue<RegistrationPerformerWrapper>(2);
    }

    @Override
    public synchronized RegistrationPerformer addingService(ServiceReference<RegistrationPerformer> reference) {
        RegistrationPerformer performer = context.getService(reference);
        RegistrationPerformer newUnused = AbstractDovecotPushListener.setIfHigherRanked(performer);
        if (null != newUnused) {
            performers.offer(new RegistrationPerformerWrapper(newUnused));
        }
        return performer;
    }

    @Override
    public void modifiedService(ServiceReference<RegistrationPerformer> reference, RegistrationPerformer service) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<RegistrationPerformer> reference, RegistrationPerformer performer) {
        context.ungetService(reference);
        if (performers.remove(new RegistrationPerformerWrapper(performer))) {
            // Was not active
            return;
        }

        RegistrationPerformerWrapper next = performers.poll();
        DovecotPushListener.replaceIfActive(performer, null == next ? null : next.performer);
    }

    // -----------------------------------------------------------------------------

    private static class RegistrationPerformerWrapper implements Comparable<RegistrationPerformerWrapper> {

        final RegistrationPerformer performer;
        private final int ranking;

        RegistrationPerformerWrapper(RegistrationPerformer performer) {
            super();
            this.performer = performer;
            this.ranking = performer.getRanking();
        }

        @Override
        public int compareTo(final RegistrationPerformerWrapper o) {
            // Highest ranking first
            final int thisVal = this.ranking;
            final int anotherVal = o.ranking;
            return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ranking;
            result = prime * result + ((performer == null) ? 0 : performer.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RegistrationPerformerWrapper)) {
                return false;
            }
            RegistrationPerformerWrapper other = (RegistrationPerformerWrapper) obj;
            if (ranking != other.ranking) {
                return false;
            }
            if (performer == null) {
                if (other.performer != null) {
                    return false;
                }
            } else if (!performer.equals(other.performer)) {
                return false;
            }
            return true;
        }
    }

}
