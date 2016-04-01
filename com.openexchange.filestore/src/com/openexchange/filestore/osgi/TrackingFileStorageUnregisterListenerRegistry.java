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

package com.openexchange.filestore.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageUnregisterListener;
import com.openexchange.filestore.FileStorageUnregisterListenerRegistry;
import com.openexchange.java.SortableConcurrentList;


/**
 * {@link TrackingFileStorageUnregisterListenerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class TrackingFileStorageUnregisterListenerRegistry extends ServiceTracker<FileStorageUnregisterListener, FileStorageUnregisterListener> implements FileStorageUnregisterListenerRegistry {

    private final SortableConcurrentList<RankedService> services;

    /**
     * Initializes a new {@link TrackingFileStorageUnregisterListenerRegistry}.
     */
    public TrackingFileStorageUnregisterListenerRegistry(BundleContext context) {
        super(context, FileStorageUnregisterListener.class, null);
        services = new SortableConcurrentList<RankedService>();
    }

    @Override
    public List<FileStorageUnregisterListener> getListeners() throws OXException {
        List<FileStorageUnregisterListener> ret = new ArrayList<FileStorageUnregisterListener>(services.size());
        for (RankedService rs : services) {
            ret.add(rs.service);
        }
        return ret;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @Override
    public FileStorageUnregisterListener addingService(final ServiceReference<FileStorageUnregisterListener> reference) {
        FileStorageUnregisterListener service = context.getService(reference);
        int ranking = getRanking(reference, 0);
        RankedService rankedService = new RankedService(service, ranking);
        if (services.addAndSort(rankedService)) { // Append
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<FileStorageUnregisterListener> reference, FileStorageUnregisterListener service) {
        services.remove(new RankedService(service, getRanking(reference, 0)));
        context.ungetService(reference);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private static class RankedService implements Comparable<RankedService> {

        final FileStorageUnregisterListener service;
        final int ranking;
        private final int hash;

        RankedService(FileStorageUnregisterListener service, int ranking) {
            super();
            this.service = service;
            this.ranking = ranking;
            hash = 31 + ((service == null) ? 0 : service.hashCode());
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RankedService)) {
                return false;
            }
            final RankedService other = (RankedService) obj;
            if (service == null) {
                if (other.service != null) {
                    return false;
                }
            } else if (!service.equals(other.service)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(final RankedService o) {
            // Highest ranking first
            final int thisVal = this.ranking;
            final int anotherVal = o.ranking;
            return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder(128);
            builder.append("RankedService [");
            if (service != null) {
                builder.append("service=").append(service).append(", ");
            }
            builder.append("ranking=").append(ranking).append("]");
            return builder.toString();
        }
    }

    private static <S> int getRanking(final ServiceReference<S> reference, final int defaultRanking) {
        int ranking = defaultRanking;
        {
            final Object oRanking = reference.getProperty(Constants.SERVICE_RANKING);
            if (null != oRanking) {
                if (oRanking instanceof Integer) {
                    ranking = ((Integer) oRanking).intValue();
                } else {
                    try {
                        ranking = Integer.parseInt(oRanking.toString().trim());
                    } catch (final NumberFormatException e) {
                        ranking = defaultRanking;
                    }
                }
            }
        }
        return ranking;
    }

}
