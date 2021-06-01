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

import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link ManifestProvider} provides alternative manifest files in form of a {@link ManifestBuilder}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@Service
public interface ManifestProvider {

    /**
     * Checks if this {@link ManifestProvider} is applicable.
     *
     * @param session The user session
     * @param version The requested version
     * @return <code>true</code> if it is applicable, <code>false</code> otherwise
     */
    boolean isApplicable(Session session, String version);

    /**
     * Gets the {@link ManifestBuilder}
     *
     * @return The {@link DefaultManifestBuilder}
     */
    ManifestBuilder getManifestBuilder();

}
