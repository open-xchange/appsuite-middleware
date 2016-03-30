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

package com.openexchange.exception.interception;

import java.util.Collection;
import com.openexchange.exception.OXException;

/**
 * {@link OXExceptionInterceptor} interface that might be implemented to register a new interceptor for exception handling.
 * <p>
 * Have a look at {@link AbstractOXExceptionInterceptor} that defines a default implementation.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @since 7.6.1
 */
public interface OXExceptionInterceptor {

    /**
     * Gets the module / action combinations this interceptor is responsible for.
     *
     * @return The responsibilities for this interceptor
     */
    Collection<Responsibility> getResponsibilities();

    /**
     * Adds a new {@link Responsibility} to the interceptor
     *
     * @param responsibility The module/action combination the interceptor should be responsible for
     */
    void addResponsibility(Responsibility responsibility);

    /**
     * Intercepts the given {@link OXException} for the defined module / action. Previously check if the given
     * {@link OXExceptionInterceptor} is responsible for the module / action combination by using {@link #isResponsible(String, String)}
     *
     * @param oxException The {@link OXException} to intercept
     * @return {@link OXExceptionArguments} that was processed by this interceptor or <code>null</code> to signal no intervention
     */
    OXExceptionArguments intercept(OXException oxException);

    /**
     * Checks if the interceptor is responsible for the given module and action combination
     *
     * @param module The module that should be tested
     * @param action The action that should be tested
     * @return <code>true</code> if the interceptor is responsible (means that {@link #intercept(OXException)} will be executed); otherwise <code>false</code>
     */
    boolean isResponsible(String module, String action);

    /**
     * Returns the ranking of this {@link OXExceptionInterceptor}
     *
     * @return An <code>int</code> value representing the interceptor's ranking
     */
    int getRanking();

}
