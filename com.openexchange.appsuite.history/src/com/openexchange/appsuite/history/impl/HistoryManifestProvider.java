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

package com.openexchange.appsuite.history.impl;

import com.openexchange.apps.manifests.ManifestBuilder;
import com.openexchange.apps.manifests.ManifestProvider;
import com.openexchange.session.Session;

/**
 * {@link HistoryManifestProvider} - is a {@link ManifestProvider} which provides manifest for older appsuite versions
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HistoryManifestProvider implements ManifestProvider {

    private final String version;
    private final ManifestBuilder builder;

    /**
     * Initializes a new {@link HistoryManifestProvider}.
     *
     * @param version The version of this provider
     * @param builder The {@link ManifestBuilder}
     */
    public HistoryManifestProvider(String version, ManifestBuilder builder) {
        super();
        this.version = version;
        this.builder = builder;
    }

    @Override
    public boolean isApplicable(Session session, String version) {
        return this.version.equalsIgnoreCase(version);
    }

    @Override
    public ManifestBuilder getManifestBuilder() {
        return builder;
    }

}
