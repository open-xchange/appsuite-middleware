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

package com.openexchange.ajax.requesthandler;

import org.fishwife.jrugged.CircuitBreaker;
import com.openexchange.exception.OXException;


/**
 * {@link CircuitBreakerAJAXActionServiceFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class CircuitBreakerAJAXActionServiceFactory implements AJAXActionServiceFactory {

    /**
     * Initializes a new {@link CircuitBreakerAJAXActionServiceFactory}.
     */
    protected CircuitBreakerAJAXActionServiceFactory() {
        super();
    }

    @Override
    public final AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService actionService = doCreateActionService(action);
        CircuitBreaker circuitBreaker = getCircuitBreaker();
        return null == circuitBreaker ? actionService : new CircuitBreakerAJAXActionService(circuitBreaker, actionService);
    }

    /**
     * Gets the circuit break to apply for this action factory.
     *
     * @return The circuit breaker
     */
    protected abstract CircuitBreaker getCircuitBreaker();

    /**
     * Creates the action service for performing the request.
     *
     * @param action The name of the action
     * @return The action service implementation and never <code>null</code>.
     * @throws OXException If an action service can not be created for the given name.
     */
    protected abstract AJAXActionService doCreateActionService(String action) throws OXException;

}
