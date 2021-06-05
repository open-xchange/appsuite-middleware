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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link SimSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private final Map<String, SubscriptionSource> sources = new HashMap<String, SubscriptionSource>();

    private final List<String> loadedSources = new LinkedList<String>();

    private String lookupIdentifier;

    @Override
    public SubscriptionSource getSource(final String identifier) {
        loadedSources.add(identifier);
        return sources.get(identifier);
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        return new ArrayList<SubscriptionSource>(sources.values());
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return sources.containsKey(identifier);
    }

    public void addSource(final SubscriptionSource source) {
        sources.put(source.getId(), source);
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) {
        return getSource(lookupIdentifier);
    }

    public void setLookupIdentifier(final String lookupIdentifier) {
        this.lookupIdentifier = lookupIdentifier;
    }

    public List<String> getLoadedSources() {
        return loadedSources;
    }

    public void clearSim() {
        loadedSources.clear();
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return getSources(-1);
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return this;
    }

}
