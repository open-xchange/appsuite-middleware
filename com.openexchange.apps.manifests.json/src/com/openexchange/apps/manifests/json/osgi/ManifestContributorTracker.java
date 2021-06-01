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

package com.openexchange.apps.manifests.json.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import com.openexchange.apps.manifests.DefaultManifestBuilder;
import com.openexchange.apps.manifests.ManifestContributor;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;


/**
 * {@link ManifestContributorTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ManifestContributorTracker extends RankingAwareNearRegistryServiceTracker<ManifestContributor> {

    private final AtomicReference<DefaultManifestBuilder> builderReference;

    /**
     * Initializes a new {@link ManifestContributorTracker}.
     *
     * @param context The bundle context
     */
    public ManifestContributorTracker(BundleContext context) {
        super(context, ManifestContributor.class);
        builderReference = new AtomicReference<DefaultManifestBuilder>();
    }

    /**
     * Sets the associated manifest builder instance.
     *
     * @param manifestBuilder The manifest builder instance
     */
    public void setManifestBuilder(DefaultManifestBuilder manifestBuilder) {
        builderReference.set(manifestBuilder);
    }

    @Override
    protected void onServiceAdded(ManifestContributor service) {
        super.onServiceAdded(service);
        resetBuilder();
    }

    @Override
    protected void onServiceRemoved(ManifestContributor service) {
        super.onServiceRemoved(service);
        resetBuilder();
    }

    private void resetBuilder() {
        DefaultManifestBuilder manifestBuilder = builderReference.get();
        if (null != manifestBuilder) {
            manifestBuilder.reset();
        }
    }

}
