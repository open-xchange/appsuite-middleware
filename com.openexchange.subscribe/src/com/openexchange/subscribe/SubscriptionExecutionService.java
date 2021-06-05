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

package com.openexchange.subscribe;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SubscriptionExecutionService} - The subscription service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
@SingletonService
public interface SubscriptionExecutionService {

    /**
     * Executes the denoted subscription
     *
     * @param sourceId The source identifier
     * @param session The associated session
     * @param subscriptionId The identifier of the subscription to execute
     * @return The number of stored data elements
     * @throws OXException If subscription cannot be executed
     */
    int executeSubscription(String sourceId, ServerSession session, int subscriptionId) throws OXException;

    /**
     * Executes the denoted subscription
     *
     * @param session The associated session
     * @param subscriptionId The identifier of the subscription to execute
     * @return The number of stored data elements
     * @throws OXException If subscription cannot be executed
     */
    int executeSubscription(ServerSession session, int subscriptionId) throws OXException;

    /**
     * Executes the specified subscriptions
     * <p>
     * The caller may influence whether a fall-back to one-by-one storing is performed by passing a {@code Collection} instance for
     * <code>optErrors</code> parameter.
     *
     * @param subscriptionsToRefresh The subscriptions to execute
     * @param session The associated session
     * @param optErrors The optional {@code Collection} instance for collecting possible errors;
     *                  if set to non-<code>null</code> a fall-back to one-by-one storing is performed in case batch-store fails
     * @return The number of stored data elements
     * @throws OXException If subscriptions cannot be executed
     */
    int executeSubscriptions(List<Subscription> subscriptionsToRefresh, ServerSession session, Collection<OXException> optErrors) throws OXException;

}
