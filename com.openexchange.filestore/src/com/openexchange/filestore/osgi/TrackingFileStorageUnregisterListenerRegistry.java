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
                    } catch (NumberFormatException e) {
                        ranking = defaultRanking;
                    }
                }
            }
        }
        return ranking;
    }

}
