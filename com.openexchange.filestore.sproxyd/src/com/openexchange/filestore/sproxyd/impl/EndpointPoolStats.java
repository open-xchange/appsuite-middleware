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

package com.openexchange.filestore.sproxyd.impl;

import java.util.List;

/**
 * {@link EndpointPoolStats}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class EndpointPoolStats {

    private final int numEndpoints;
    private final List<String> blacklist;

    /**
     * Initializes a new {@link EndpointPoolStats}.
     * @param numEndpoints
     * @param blacklisted
     */
    public EndpointPoolStats(int numEndpoints, List<String> blacklisted) {
        super();
        this.numEndpoints = numEndpoints;
        this.blacklist = blacklisted;
    }

    public int getTotalEndpoints() {
        return numEndpoints;
    }

    public int getAvailableEndpoints() {
        return numEndpoints - blacklist.size();
    }

    public int getBlacklistedEndpoints() {
        return blacklist.size();
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

}
