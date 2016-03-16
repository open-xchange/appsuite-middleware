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

package com.openexchange.exception.interception.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.exception.interception.OXExceptionInterceptor;
import com.openexchange.exception.interception.Responsibility;

/**
 * Registry that handles all registered {@link OXExceptionInterceptor} for processing within {@link OXException} creation
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Thread-safe collection
 * @since 7.6.1
 */
public class OXExceptionInterceptorRegistration {

    /** Logger for this class **/
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OXExceptionInterceptorRegistration.class);

    /** Singleton instance for this registration **/
    private static volatile OXExceptionInterceptorRegistration instance;

    /**
     * Initializes the instance using given configuration service instance
     */
    public static void initInstance() {
        instance = new OXExceptionInterceptorRegistration();
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        instance = null;
    }

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static OXExceptionInterceptorRegistration getInstance() {
        if (instance == null) {
            initInstance();
        }
        return instance;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /** Comparator to sort registered {@link OXExceptionInterceptor} **/
    private final Comparator<OXExceptionInterceptor> comparator;

    /** List with all registered interceptors **/
    private volatile List<OXExceptionInterceptor> interceptors;

    private OXExceptionInterceptorRegistration() {
        super();
        this.interceptors = new LinkedList<OXExceptionInterceptor>();

        comparator = new Comparator<OXExceptionInterceptor>() {

            @Override
            public int compare(OXExceptionInterceptor o1, OXExceptionInterceptor o2) {
                int rank1 = o1.getRanking();
                int rank2 = o2.getRanking();
                return (rank1 < rank2 ? -1 : (rank1 == rank2 ? 0 : 1));
            }
        };
    }

    /**
     * Adds an {@link OXExceptionInterceptor} to intercept exception throwing. If an interceptor should be added where a similar one is
     * already registered for (means ranking, module and action is equal) it won't be added.
     *
     * @param {@link OXExceptionInterceptor} to add
     * @return <code>true</code> if interceptor is added; otherwise <code>false</code>
     */
    public synchronized boolean put(OXExceptionInterceptor interceptor) {
        if (interceptor == null) {
            LOG.error("Interceptor to add might not be null!");
            return false;
        }

        if (isResponsibleInterceptorRegistered(interceptor)) {
            LOG.error("Interceptor for the given ranking " + interceptor.getRanking() + " and desired module/action combination already registered! Discard the new one from type: " + interceptor.getClass());
            return false;
        }
        this.interceptors.add(interceptor);
        return true;
    }

    /**
     * Checks if an {@link OXExceptionInterceptor} with same ranking and module/action combination is already registered
     *
     * @param interceptorCandidate The {@link OXExceptionInterceptor} that might be added
     * @return boolean<code>true</code> if a {@link OXExceptionInterceptor} is already registered for the given ranking and module/action combination, otherwise <code>false</code>
     */
    public boolean isResponsibleInterceptorRegistered(OXExceptionInterceptor interceptorCandidate) {
        for (OXExceptionInterceptor interceptor : this.interceptors) {
            if (interceptor.getRanking() != interceptorCandidate.getRanking()) {
                continue;
            }
            for (Responsibility responsibility : interceptor.getResponsibilities()) {
                for (Responsibility candidateResponsibility : interceptorCandidate.getResponsibilities()) {
                    if (responsibility.implies(candidateResponsibility)) {
                        // There is another interceptor with the same ranking covering the same responsibility
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes an {@link OXExceptionInterceptor} to not intercept exception throwing
     *
     * @param interceptor - {@link OXExceptionInterceptor} to remove
     */
    public synchronized void remove(final OXExceptionInterceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    /**
     * Returns all registered {@link OXExceptionInterceptor}s ranked
     *
     * @return a ranked list with all registered {@link OXExceptionInterceptor}s
     */
    public List<OXExceptionInterceptor> getRegisteredInterceptors() {
        // Add all interceptors
        List<OXExceptionInterceptor> lInterceptors = new ArrayList<OXExceptionInterceptor>(this.interceptors);

        // Now order them according to service ranking
        Collections.sort(lInterceptors, comparator);
        return lInterceptors;
    }

    /**
     * Returns all {@link OXExceptionInterceptor}s that are responsible for this module/action combination ranked.
     *
     * @param module - the module to get the responsible interceptors for
     * @param action - the action to get the responsible interceptors for
     * @return a ranked list with all registered {@link OXExceptionInterceptor}s that are responsible for the given module/action
     *         combination
     */
    public List<OXExceptionInterceptor> getResponsibleInterceptors(String module, String action) {
        // Collect responsible interceptors
        List<OXExceptionInterceptor> lInterceptors = new LinkedList<OXExceptionInterceptor>();
        for (OXExceptionInterceptor interceptor : this.interceptors) {
            if (interceptor.isResponsible(module, action)) {
                lInterceptors.add(interceptor);
            }
        }

        // Sort by ranking & return
        Collections.sort(lInterceptors, comparator);
        return lInterceptors;
    }
}
