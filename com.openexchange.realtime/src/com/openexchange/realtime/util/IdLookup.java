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

package com.openexchange.realtime.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.osgi.RealtimeServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link IdLookup} Utility class to do various Id lookups based on a {@link com.openexchange.realtime.packet.ID}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IdLookup {

    private static final Logger LOG = LoggerFactory.getLogger(IdLookup.class);
    
    /**
     * Utility method to get contextId and userId from a com.openexchange.realtime.packet.ID
     * If either userId or contextId can be parsed into an Integer no lookup will be done for that field but the parsed value will be used.
     * As realtime IDs may represent synthetic users without valid user and contextIds the parsing or lookup may fail.
     * 
     * @param id the realtime ID
     * @return a UserAndContext bean containing the requested information
     * @throws OXException if the needed services for the lookup are missing or the lookup for the ID fails.
     */
    public static UserAndContext getUserAndContextIDs(ID id) throws OXException {
        int contextId = -1;
        int userId = -1;
        
        ContextService contextService;
        UserService userService;
        String context = id.getContext();
        String user = id.getUser();

        try {
            contextId = Integer.parseInt(context);
            userId = Integer.parseInt(user);
        } catch (NumberFormatException nfe) {
            LOG.debug("Failed to parse id components of {} to int, will continue string based lookup", id);
        }

        if(contextId < 1 ) {
            if (context == null || context.equals("")) {
                LOG.debug("Context missing from id representation {}. Setting to 'defaultcontext'");
                context = "defaultcontext";
            }
            contextService = getContextService();
            contextId = contextService.getContextId(context);
            if (contextId == -1) {
                RealtimeExceptionCodes.INVALID_ID.create("Unable to lookup context: " + context);
            }
        }

        if(userId < 1 ) {
            contextService = getContextService();
            userService = getUserService();
            Context contextObject = contextService.getContext(contextId);
            userId = userService.getUserId(user, contextObject);
        }

        return new UserAndContext(contextId, userId);
    }

    private static ContextService getContextService() throws RealtimeException {
        ServiceLookup serviceLookup = RealtimeServiceRegistry.getInstance();
        ContextService contextService = serviceLookup.getService(ContextService.class);
        if (contextService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ContextService.class.getName());
        }
        return contextService;
    }

    private static UserService getUserService() throws RealtimeException {
        ServiceLookup serviceLookup = RealtimeServiceRegistry.getInstance();
        UserService userService = serviceLookup.getService(UserService.class);
        if (userService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(UserService.class.getName());
        }
        return userService;
    }

    /**
     * {@link UserAndContext} - Simple bean to carry contextId and userId.
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public static final class UserAndContext {

        private int contextId;

        private int userId;

        /**
         * Initializes a new {@link UserAndContext}.
         * 
         * @param contextId the contextId
         * @param userId the userId
         */
        public UserAndContext(int contextId, int userId) {
            this.contextId = contextId;
            this.userId = userId;
        }

        /**
         * Gets the contextId
         * 
         * @return The contextId
         */
        public int getContextId() {
            return contextId;
        }

        /**
         * Sets the contextId
         * 
         * @param contextId The contextId to set
         */
        public void setContextId(int contextId) {
            this.contextId = contextId;
        }

        /**
         * Gets the userId
         * 
         * @return The userId
         */
        public int getUserId() {
            return userId;
        }

        /**
         * Sets the userId
         * 
         * @param userId The userId to set
         */
        public void setUserId(int userId) {
            this.userId = userId;
        }

    }
}
