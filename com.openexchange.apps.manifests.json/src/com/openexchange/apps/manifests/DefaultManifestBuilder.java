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

package com.openexchange.apps.manifests;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultManifestBuilder} - Build manifests based on manifest files and registered {@link ManifestContributor}s.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class DefaultManifestBuilder implements ManifestBuilder {

    final private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultManifestBuilder.class);

    private final AtomicReference<JSONArray> initialManifestsReference;
    private final ServiceListing<ManifestContributor> manifestContributors;
    private volatile JSONArray cachedResult;

    /**
     * Initializes a new {@link DefaultManifestBuilder}.
     *
     * @param manifests The JSON array providing the manifests
     * @param manifestContributorTracker The contributor tracker
     */
    public DefaultManifestBuilder(JSONArray manifests, ServiceListing<ManifestContributor> manifestContributors) {
        super();
        this.initialManifestsReference = new AtomicReference<JSONArray>(manifests);
        this.manifestContributors = manifestContributors;
    }

    /**
     * Reinitializes this builder with new initial manifests
     *
     * @param manifests The new initial manifests
     */
    public void reinitialize(JSONArray manifests) {
        this.initialManifestsReference.set(manifests);
        reset();
    }

    /**
     * Resets this manifest builder.
     */
    public void reset() {
        cachedResult = null;
    }

    @Override
    public JSONArray buildManifests(ServerSession session, String version) throws OXException {
        // Any contributors?
        boolean noContributors = false;
        List<ManifestContributor> contributors = Collections.emptyList();
        if (manifestContributors != null) {
            contributors = manifestContributors.getServiceList();
            noContributors = contributors.isEmpty();
        }
        if (noContributors) {
            // Check cached result
            JSONArray cached = cachedResult;
            if (null != cached) {
                return cached;
            }
        }

        // Fill with initial manifests
        JSONArray manifests = new JSONArray(initialManifestsReference.get());

        // Add contributions (if any)
        for (ManifestContributor contributor : contributors) {
            try {
                JSONArray additionalManifests = contributor.getAdditionalManifests(session);
                if (additionalManifests != null) {
                    for (int i = 0, size = additionalManifests.length(); i < size; i++) {
                        manifests.put(additionalManifests.get(i));
                    }
                }
            } catch (OXException | JSONException ex) {
                LOG.error("Error while trying to get additional manifests from contributor {} ", contributor, ex);
            }
        }

        // Build resulting manifests
        try {
            int size = manifests.length();
            JSONArray result = new JSONArray(size);
            for (int i = 0; i < size; i++) {
                // Put a copy into result
                JSONObject definition = manifests.getJSONObject(i);
                result.put(new JSONObject(definition));
            }

            // Caching only possible if no ManifestContributor available
            if (noContributors) {
                cachedResult = result;
            }

            return result;
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage(), x);
        }
    }

}
