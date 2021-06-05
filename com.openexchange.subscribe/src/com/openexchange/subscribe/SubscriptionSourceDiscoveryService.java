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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * {@link SubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface SubscriptionSourceDiscoveryService {

    /**
     * Retrieves all {@link SubscriptionSource}s
     * 
     * @return A list of {@link SubscriptionSource}s
     */
    public List<SubscriptionSource> getSources();

    /**
     * Retrieves all {@link SubscriptionSource}s of the given folder module
     * 
     * @param folderModule The folder module
     * @return A list of {@link SubscriptionSource}s
     */
    public List<SubscriptionSource> getSources(int folderModule);

    /**
     * Gets the {@link SubscriptionSource} for the given identifier
     * 
     * @param identifier The {@link SubscriptionSource} identifier
     * @return The {@link SubscriptionSource}
     */
    public SubscriptionSource getSource(String identifier);

    /**
     * Gets a {@link SubscriptionSource} for a given subscription
     * 
     * @param context The context
     * @param subscriptionId The id of the subscription
     * @return The subscription source
     * @throws OXException
     */
    public SubscriptionSource getSource(Context context, int subscriptionId) throws OXException;

    /**
     * Checks if the given identifier is a known {@link SubscriptionSource}
     * 
     * @param identifier The identifier
     * @return true if it is known, false otherwise
     */
    public boolean knowsSource(String identifier);

    /**
     * Gets a {@link SubscriptionSourceDiscoveryService} which filters {@link SubscriptionSource}s based on user and context.
     *
     * @param user The user id
     * @param context The context id
     * @return The {@link SubscriptionSourceDiscoveryService}
     * @throws OXException
     */
    public SubscriptionSourceDiscoveryService filter(int user, int context) throws OXException;
}
