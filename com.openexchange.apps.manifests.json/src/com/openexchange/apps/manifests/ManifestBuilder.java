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

import org.json.JSONArray;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ManifestBuilder} provides a {@link JSONArray} of manifest entries 
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public interface ManifestBuilder {

    /**
     * Compute the manifests from files and {@link ManifestContributor}s.
     *
     * @param session The users {@link ServerSession}
     * @param version The requested version
     * @throws OXException
     */
    JSONArray buildManifests(ServerSession session, String version) throws OXException;

}
