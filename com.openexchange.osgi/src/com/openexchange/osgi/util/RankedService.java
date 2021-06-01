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

package com.openexchange.osgi.util;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import com.openexchange.osgi.Ranked;

/**
 * {@link RankedService} - A service plus its ranking.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RankedService<S> implements Comparable<RankedService<S>> {

    /**
     * Creates a new <code>RankedService</code> instance wrapping specified service using default ranking of <code>0</code> (zero).
     * <p>
     * Specified <code>ServiceReference</code> is used to determine <code>"service.ranking"</code> property.<br>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference to determine service ranking
     * @param service The service instance
     * @return The newly created ranked service
     */
    public static <S> RankedService<S> newRankedService(ServiceReference<S> reference, S service) {
        return newRankedService(reference, service, 0);
    }

    /**
     * Creates a new <code>RankedService</code> instance wrapping specified service using given default ranking.
     * <p>
     * Specified <code>ServiceReference</code> is used to determine <code>"service.ranking"</code> property.<br>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference to determine service ranking
     * @param service The service instance
     * @param defaultRanking The default ranking to use
     * @return The newly created ranked service
     */
    public static <S> RankedService<S> newRankedService(ServiceReference<S> reference, S service, int defaultRanking) {
        return new RankedService<S>(service, getRanking(service, reference, defaultRanking));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * The service.
     */
    public final S service;

    /**
     * The service ranking.
     */
    public final int ranking;

    private final int hash;

    /**
     * Initializes a new {@link RankedService}.
     *
     * @param service The associated service
     * @param ranking The service ranking
     */
    public RankedService(final S service, final int ranking) {
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
        final RankedService<?> other = (RankedService<?>) obj;
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
    public int compareTo(final RankedService<S> o) {
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

    // ----------------------------------------------------------------------------------------------- //

    /**
     * Gets the service ranking by look-up of <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference providing properties Dictionary object of the service
     * @return The ranking or <code>0</code> (zero) if absent
     * @see #getRanking(Object, ServiceReference, int)
     */
    public static <S> int getRanking(ServiceReference<S> reference) {
        return getRanking(reference, 0);
    }

    /**
     * Gets the service ranking by look-up of <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference providing properties Dictionary object of the service
     * @param defaultRanking The default ranking if {@link Constants#SERVICE_RANKING} property is absent
     * @return The ranking or given <code>defaultRanking</code> if absent
     * @see #getRanking(Object, ServiceReference, int)
     */
    public static <S> int getRanking(ServiceReference<S> reference, int defaultRanking) {
        return getRanking(null, reference, defaultRanking);
    }

    /**
     * Gets the service ranking either by given service instance (if not <code>null</code>) or by look-up of <code>"service.ranking"</code> property.
     * <p>
     * Service instance is checked if {@link Ranked} is implemented, if not, look-up falls-back to <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param service The (optional) obtained service
     * @param reference The service reference providing properties Dictionary object of the service
     * @param defaultRanking The default ranking if {@link Constants#SERVICE_RANKING} property is absent
     * @return The ranking or <code>0</code> (zero) if absent
     */
    public static <S> int getRanking(S service, ServiceReference<S> reference, int defaultRanking) {
        int ranking = defaultRanking;
        {
            if (service instanceof Ranked) {
                return ((Ranked) service).getRanking();
            }

            Object oRanking = reference.getProperty(Constants.SERVICE_RANKING);
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
