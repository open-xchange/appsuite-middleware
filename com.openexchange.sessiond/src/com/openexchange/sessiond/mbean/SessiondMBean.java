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

package com.openexchange.sessiond.mbean;

import java.util.Set;
import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;
import com.openexchange.sessiond.rest.SessiondRESTService;
import com.openexchange.sessiond.rmi.SessiondRMIService;

/**
 * {@link SessiondMBean} - The MBean for sessiond
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Use {@link SessiondRMIService} or {@link SessiondRESTService} instead.
 */
@Deprecated
public interface SessiondMBean {

    public static final String MBEAN_NAME = "SessionD Toolkit";
    public static final String SESSIOND_DOMAIN = "com.openexchange.sessiond";

    /**
     * Clears all sessions belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of removed sessions belonging to the user or <code>-1</code> if an error occurred
     */
    @MBeanMethodAnnotation(description = "Clears all sessions on running node belonging to the user identified by given user ID in specified context", parameters = { "userId", "contextId" }, parameterDescriptions = { "The user identifier", "The context identifier" })
    int clearUserSessions(int userId, int contextId);

    /**
     * Clears all sessions from cluster belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of removed sessions belonging to the user or <code>-1</code> if an error occurred
     * @throws MBeanException If operation fails
     */
    @MBeanMethodAnnotation(description = "Clears all sessions from cluster on running node belonging to the user identified by given user ID in specified context", parameters = { "userId", "contextId" }, parameterDescriptions = { "The user identifier", "The context identifier" })
    void clearUserSessionsGlobally(int userId, int contextId) throws MBeanException;

    /**
     * Gets the number of short-term sessions associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The number of user-associated sessions
     * @throws MBeanException If number of user-associated sessions cannot be returned
     */
    @MBeanMethodAnnotation(description = "Gets the number of short-term sessions associated with specified user", parameters = { "userId", "contextId" }, parameterDescriptions = { "The user identifier", "The context identifier" })
    int getNumberOfUserSessons(int userId, int contextId) throws MBeanException;

    /**
     * Clears all sessions belonging to specified context
     *
     * @param contextId The context ID
     */
    @MBeanMethodAnnotation(description = "Clears all sessions belonging to specified context", parameters = { "contextId" }, parameterDescriptions = { "The context identifier" })
    void clearContextSessions(int contextId);

    /**
     * Clears all sessions belonging to given contexts.
     *
     * @param contextId The context identifiers to remove sessions for
     */
    @MBeanMethodAnnotation(description = "Clears all sessions in whole cluster belonging to specified context identifiers", parameters = { "contextIds" }, parameterDescriptions = { "The context identifiers" })
    void clearContextSessionsGlobal(Set<Integer> contextIds) throws MBeanException;

    /**
     * Gets the number of short-term sessions.
     *
     * @return The number of short-term sessions
     */
    @MBeanMethodAnnotation(description = "Gets the number of short-term sessions.", parameters = {}, parameterDescriptions = {})
    int[] getNumberOfShortTermSessions();

    /**
     * Gets the number of long-term sessions.
     *
     * @return The number of long-term sessions
     */
    @MBeanMethodAnnotation(description = "Gets the number of long-term sessions.", parameters = {}, parameterDescriptions = {})
    int[] getNumberOfLongTermSessions();

    /**
     * Clear all sessions in central session storage. This does not affect the local short term session container.
     */
    @MBeanMethodAnnotation(description = "Clear all sessions in central session storage. This does not affect the local short term session container.", parameters = {}, parameterDescriptions = {})
    void clearSessionStorage() throws MBeanException;
}
