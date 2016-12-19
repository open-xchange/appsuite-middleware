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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.startup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link StaticSignalStartedService} - The singleton {@link SignalStartedService}, which also provides a start-up state.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public final class StaticSignalStartedService implements SignalStartedService {

    /** A known state associated with a server that completed start-up */
    public static enum State {

        /**
         * The start-up state signaling all fine.
         */
        OK("ok"),
        /**
         * The start-up state signaling that configuration could not be parsed/read correctly.
         * <p>
         * As a result the server might not run as expected and/or miss needed services.
         */
        INVALID_CONFIGURATION("invalid_configuration"),
        /**
         * A general error occurred that prevents the server from working correctly.
         */
        GENERAL_ERROR("general_error"),
        ;

        private final String identifier;

        private State(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the state identifier
         *
         * @return The state identifier
         */
        public String getIdentifier() {
            return identifier;
        }
    }

    /** A known information associated with a server start-up state */
    public static interface Info<E> {

        /**
         * Gets the state identifier
         *
         * @return The state identifier
         */
        String getIdentifier();

        Class<E> getInfoType();
    }

    /** References the exception instance associated with a state */
    public static final Info<Throwable> INFO_EXCEPTION = new Info<Throwable>() {

        @Override
        public String getIdentifier() {
            return "__exception";
        }

        @Override
        public java.lang.Class<Throwable> getInfoType() {
            return Throwable.class;
        }
    };

    /** References the message associated with a state */
    public static final Info<String> INFO_MESSAGE = new Info<String>() {

        @Override
        public String getIdentifier() {
            return "__message";
        }

        @Override
        public java.lang.Class<String> getInfoType() {
            return String.class;
        }
    };

    private static final StaticSignalStartedService INSTANCE = new StaticSignalStartedService();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static StaticSignalStartedService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------

    private final ConcurrentMap<String, Object> stateInfo;
    private final AtomicReference<State> stateReference;

    private StaticSignalStartedService() {
        super();
        stateInfo = new ConcurrentHashMap<>(4, 0.9F, 1);
        stateReference = new AtomicReference<StaticSignalStartedService.State>(State.OK);
    }

    /**
     * Gets the currently active start-up state.
     *
     * @return The start-up state
     */
    public State getState() {
        return stateReference.get();
    }

    /**
     * Gets the denoted start-up info (if available)
     *
     * @param info The info name
     * @return The info or <code>null</code>
     */
    public <V> V getStateInfo(Info<V> info) {
        if (null == info) {
            return null;
        }
        return getStateInfo(info.getIdentifier());
    }

    /**
     * Gets the denoted start-up info (if available)
     *
     * @param info The info name
     * @return The info or <code>null</code>
     */
    public <V> V getStateInfo(String info) {
        if (null == info) {
            return null;
        }

        try {
            return (V) stateInfo.get(info);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Sets the start-up to given non-OK state.
     *
     * @param state The state to set
     * @param t The {@code Throwable} instance which caused the provided state to be entered
     */
    public void setState(State state, Throwable t) {
        setState(state, t, null);
    }

    /**
     * Sets the start-up to given non-OK state.
     *
     * @param state The state to set
     * @param t The {@code Throwable} instance which caused the provided state to be entered
     * @param message The message associated with the state that should be output to logging system
     */
    public void setState(State state, Throwable t, String message) {
        Map<String, Object> info = null;

        if (null != t) {
            info = new HashMap<>(2);
            info.put(INFO_EXCEPTION.getIdentifier(), t);
        }

        if (null != message) {
            if (null == info) {
                info = new HashMap<>(2);
            }
            info.put(INFO_MESSAGE.getIdentifier(), message);
        }

        setState(state, info);
    }

    /**
     * Sets the start-up to given non-OK state.
     *
     * @param state The state to set
     * @param optInfo The optional information associated with the state
     * @return <code>true</code> if state could be successfully set; otherwise <code>false</code>
     */
    public boolean setState(State state, Map<String, Object> optInfo) {
        if (null == state || State.OK == state) {
            return false;
        }

        State cur;
        do {
            cur = stateReference.get();
            if (cur != State.OK) {
                // Already set to a non-OK state
                return false;
            }
        } while (!stateReference.compareAndSet(cur, state));

        if (null != optInfo) {
            for (Map.Entry<String,Object> entry : optInfo.entrySet()) {
                stateInfo.put(entry.getKey(), entry.getValue());
            }
        }

        return true;
    }

}
