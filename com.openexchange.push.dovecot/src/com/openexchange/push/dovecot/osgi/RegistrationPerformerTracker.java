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

package com.openexchange.push.dovecot.osgi;

import java.util.PriorityQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.push.dovecot.DovecotPushListener;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;


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
        RegistrationPerformer newUnused = DovecotPushListener.setIfHigherRanked(performer);
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
