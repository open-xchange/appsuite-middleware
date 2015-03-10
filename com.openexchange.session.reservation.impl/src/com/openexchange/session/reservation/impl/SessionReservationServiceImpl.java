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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.session.reservation.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.ReservationInfo;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.impl.portable.PortableReservation;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessiondService;

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
    private final ConcurrentMap<String, Reservation> reservations;
    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;

    /**
     * Initializes a new {@link SessionReservationServiceImpl}.
     */
    public SessionReservationServiceImpl(HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler) {
        super();
        IdleExpirationPolicy evictionPolicy = new IdleExpirationPolicy(TimeUnit.MINUTES.toMillis(5));
        reservations = new ConcurrentLinkedHashMap<String, Reservation>(1024, 0.75f, 16, Integer.MAX_VALUE, evictionPolicy);
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

    private Reservation peekReservation(String token) {
        blocker.acquire();
        try {
            return useHzMap ? peekFromHzMap(hzMapName, token) : reservations.get(token);
        } finally {
            blocker.release();
        }
    }

    private Reservation peekFromHzMap(String mapIdentifier, String token) {
        IMap<String, PortableReservation> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote reservations is not available.");
            return null;
        }

        PortableReservation portableReservation = hzMap.get(token);
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

    private Reservation pollReservation(String token) {
        blocker.acquire();
        try {
            return useHzMap ? pollFromHzMap(hzMapName, token) : reservations.remove(token);
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
    public Reservation getReservation(String token) throws OXException {
        Reservation reservation = peekReservation(token);
        if (null == reservation) {
            return null;
        }
        if ((System.currentTimeMillis() - reservation.getCreationStamp()) > reservation.getTimeoutMillis()) {
            pollReservation(token);
            return null;
        }

        return reservation;
    }

    @Override
    public Session redeemReservation(ReservationInfo reservationInfo) throws OXException {
        Reservation reservation = pollReservation(reservationInfo.getToken());
        if (null == reservation || ((System.currentTimeMillis() - reservation.getCreationStamp()) > reservation.getTimeoutMillis())) {
            return null;
        }

        SessiondService sessiondService = Services.getService(SessiondService.class);
        ContextService contextService = Services.getService(ContextService.class);
        AddSessionParameterImpl param = new AddSessionParameterImpl(reservation.getUserId(), contextService.getContext(reservation.getContextId()), reservationInfo);
        return sessiondService.addSession(param);
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
                for (Map.Entry<String, Reservation> entry : reservations.entrySet()) {
                    hzMap.put(entry.getKey(), new PortableReservation(entry.getValue()));
                }
                reservations.clear();
            }
            useHzMap = true;
            LOG.info("Reservations backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    private final class AddSessionParameterImpl implements AddSessionParameter {

        private final ReservationInfo reservationInfo;
        private final int userId;
        private final Context context;

        /**
         * Initializes a new {@link SessionReservationServiceImpl.AddSessionParameterImpl}.
         */
        AddSessionParameterImpl(int userId, Context context, ReservationInfo reservationInfo) {
            super();
            this.reservationInfo = reservationInfo;
            this.userId = userId;
            this.context = context;
        }

        @Override
        public boolean isTransient() {
            return reservationInfo.isTransient();
        }

        @Override
        public String getUserLoginInfo() {
            return reservationInfo.getUserLoginInfo();
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public String getPassword() {
            return reservationInfo.getPassword();
        }

        @Override
        public String getHash() {
            return reservationInfo.getHash();
        }

        @Override
        public String getFullLogin() {
            return reservationInfo.getFullLogin();
        }

        @Override
        public SessionEnhancement getEnhancement() {
            return reservationInfo.getEnhancement();
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public String getClientToken() {
            return null;
        }

        @Override
        public String getClientIP() {
            return reservationInfo.getClientIP();
        }

        @Override
        public String getClient() {
            return reservationInfo.getClient();
        }

        @Override
        public String getAuthId() {
            return reservationInfo.getAuthId();
        }
    }

}
