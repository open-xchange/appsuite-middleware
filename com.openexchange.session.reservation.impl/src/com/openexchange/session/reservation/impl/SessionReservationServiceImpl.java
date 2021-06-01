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

package com.openexchange.session.reservation.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.impl.portable.PortableReservation;

/**
 * {@link SessionReservationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class SessionReservationServiceImpl implements SessionReservationService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionReservationServiceImpl.class);

    private volatile String hzMapName;
    private volatile boolean useHzMap = false;

    private final Blocker blocker = new ConcurrentBlocker();
    private final Cache<String, Reservation> reservations;
    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;

    /**
     * Initializes a new {@link SessionReservationServiceImpl}.
     */
    public SessionReservationServiceImpl(HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler) {
        super();
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .maximumSize(Integer.MAX_VALUE)
            .initialCapacity(1024)
            .expireAfterAccess(5, TimeUnit.MINUTES)
        ;
        Cache<String, Reservation> cache = cacheBuilder.build();
        reservations = cache;
        this.notActiveExceptionHandler = notActiveExceptionHandler;
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error.", HazelcastInstanceNotActiveException.class.getSimpleName());
        changeBackingMapToLocalMap();

        HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler = this.notActiveExceptionHandler;
        if (null != notActiveExceptionHandler) {
            notActiveExceptionHandler.propagateNotActive(e);
        }
    }

    /**
     * Gets the Hazelcast map or <code>null</code> if unavailable.
     */
    private IMap<String, PortableReservation> hzMap(String mapIdentifier) {
        if (null == mapIdentifier) {
            LOG.trace("Name of Hazelcast map is missing for token login service.");
            return null;
        }
        final HazelcastInstance hazelcastInstance = Services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOG.trace("Hazelcast instance is not available.");
            return null;
        }
        try {
            return hazelcastInstance.getMap(mapIdentifier);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        }
    }

    /**
     * Sets the name for the Hazelcast map
     *
     * @param hzMapName The map name to set
     */
    public void setHzMapName(String hzMapName) {
        this.hzMapName = hzMapName;
    }

    private void putReservation(Reservation reservation) {
        blocker.acquire();
        try {
            if (useHzMap) {
                putToHzMap(hzMapName, reservation);
            } else {
                reservations.put(reservation.getToken(), reservation);
            }
        } finally {
            blocker.release();
        }
    }

    private void putToHzMap(String mapIdentifier, Reservation reservation) {
        final IMap<String, PortableReservation> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote reservations is not available.");
        } else {
            hzMap.put(reservation.getToken(), new PortableReservation(reservation));
        }
    }

    private Reservation pollReservation(String token) {
        blocker.acquire();
        try {
            return useHzMap ? pollFromHzMap(hzMapName, token) : reservations.asMap().remove(token);
        } finally {
            blocker.release();
        }
    }

    private Reservation pollFromHzMap(String mapIdentifier, String token) {
        IMap<String, PortableReservation> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote reservations is not available.");
            return null;
        }

        PortableReservation portableReservation = hzMap.remove(token);
        if (null == portableReservation) {
            return null;
        }
        ReservationImpl reservation = new ReservationImpl();
        reservation.setContextId(portableReservation.getContextId());
        reservation.setUserId(portableReservation.getUserId());
        reservation.setTimeoutMillis(portableReservation.getTimeout());
        reservation.setCreationStamp(portableReservation.getCreationStamp());
        reservation.setToken(token);
        reservation.setState(portableReservation.getState());
        return reservation;
    }

    // ---------------------------------------------------------------------------------------------------

    @Override
    public String reserveSessionFor(int userId, int contextId, long timeout, TimeUnit unit, Map<String, String> optState) throws OXException {
        ReservationImpl reservation = new ReservationImpl();
        reservation.setContextId(contextId);
        reservation.setUserId(userId);
        reservation.setTimeoutMillis(unit.toMillis(timeout));
        reservation.setCreationStamp(System.currentTimeMillis());
        reservation.setToken(UUIDs.getUnformattedString(UUID.randomUUID()) + "-" + UUIDs.getUnformattedString(UUID.randomUUID()));
        reservation.setState(optState);

        putReservation(reservation);

        return reservation.getToken();
    }

    @Override
    public Reservation removeReservation(String token) throws OXException {
        Reservation reservation = pollReservation(token);
        if (null == reservation) {
            return null;
        }
        if ((System.currentTimeMillis() - reservation.getCreationStamp()) > reservation.getTimeoutMillis()) {
            pollReservation(token);
            return null;
        }

        return reservation;
    }

    // ---------------------------------------------------------------------------------------------------

   /**
    *
    */
    public void changeBackingMapToLocalMap() {
        blocker.block();
        try {
            // This happens if Hazelcast is removed in the meantime. We cannot copy any information back to the local map.
            useHzMap = false;
            LOG.info("Reservations backing map changed to local");
        } finally {
            blocker.unblock();
        }
    }

   /**
    *
    */
    public void changeBackingMapToHz() {
        blocker.block();
        try {
            if (useHzMap) {
                return;
            }

            final IMap<String, PortableReservation> hzMap = hzMap(hzMapName);
            if (null == hzMap) {
                LOG.trace("Hazelcast map is not available.");
            } else {
                // This MUST be synchronous!
                for (Map.Entry<String, Reservation> entry : reservations.asMap().entrySet()) {
                    hzMap.put(entry.getKey(), new PortableReservation(entry.getValue()));
                }
                reservations.invalidateAll();
            }
            useHzMap = true;
            LOG.info("Reservations backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }

}
