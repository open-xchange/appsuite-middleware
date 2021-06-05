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
