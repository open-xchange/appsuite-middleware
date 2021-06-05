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

package com.openexchange.mailfilter;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;

/**
 * {@link MailFilterInterceptorRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MailFilterInterceptorRegistry {

    /**
     * Registers the specified {@link MailFilterInterceptor}. The ranking of the {@link MailFilterInterceptor}
     * defines the execution order.
     * 
     * @param interceptor The {@link MailFilterInterceptor} to register
     */
    void register(MailFilterInterceptor interceptor);

    /**
     * Executes all registered {@link MailFilterInterceptor}s.
     * 
     * This interception call happens right after the sieve script is read from the sieve server, but BEFORE
     * any processing begins on the middleware side.
     * 
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param rules A {@link List} of {@link Rule}s
     * 
     * @throws OXException if an error is occurred
     */
    void executeBefore(int userId, int contextId, List<Rule> rules) throws OXException;

    /**
     * Executes all registered {@link MailFilterInterceptor}s.
     * 
     * This interception call happens before writing the sieve script to the sieve server, but AFTER
     * any processing happened on the middleware side.
     * 
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param rules A {@link List} of {@link Rule}s
     * @throws OXException if an error is occurred
     */
    void executeAfter(int userId, int contextId, List<Rule> rules) throws OXException;
}
