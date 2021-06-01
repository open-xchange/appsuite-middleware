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

package com.openexchange.apps.manifests.json;

import org.json.JSONArray;
import com.openexchange.apps.manifests.ManifestBuilder;
import com.openexchange.apps.manifests.ManifestProvider;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ProviderAwareManifestBuilder}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class ProviderAwareManifestBuilder implements ManifestBuilder {

    private final RankingAwareNearRegistryServiceTracker<ManifestProvider> providerTracker;
    private final ManifestBuilder delegate;

    /**
     * Initializes a new {@link ProviderAwareManifestBuilder}.
     *
     * @param providerTracker The tracker for possibly registered providers
     * @param delegate The delegate manifest builder to use when there is no suitable manifest provider
     */
    public ProviderAwareManifestBuilder(RankingAwareNearRegistryServiceTracker<ManifestProvider> providerTracker, ManifestBuilder delegate) {
        super();
        this.providerTracker = providerTracker;
        this.delegate = delegate;
    }

    @Override
    public JSONArray buildManifests(ServerSession session, String version) throws OXException {
        for (ManifestProvider provider : providerTracker.getServiceList()) {
            if (provider.isApplicable(session, version)) {
                return provider.getManifestBuilder().buildManifests(session, version);
            }
        }
        return delegate.buildManifests(session, version);
    }

}
