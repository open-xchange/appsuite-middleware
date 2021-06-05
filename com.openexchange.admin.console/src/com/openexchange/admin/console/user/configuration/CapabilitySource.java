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

package com.openexchange.admin.console.user.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link CapabilitySource}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CapabilitySource {

    public final static String DENIED_KEY = "denied";

    public final static String GRANTED_KEY = "granted";

    private final CapabilitySourceEnum source;

    private final List<String> grantedCapabilities;

    private final List<String> deniedCapabilities;

    public CapabilitySource(CapabilitySourceEnum source, Set<String> grantedCapabilities, Set<String> deniedCapabilities) {
        this.source = source;
        this.grantedCapabilities = new ArrayList<String>(grantedCapabilities);
        this.deniedCapabilities = new ArrayList<String>(deniedCapabilities);
    }

    /**
     * Gets the source
     *
     * @return The source
     */
    public CapabilitySourceEnum getSource() {
        return source;
    }

    public List<String> getGrantedCapabilities() {
        return grantedCapabilities;
    }

    public List<String> getDeniedCapabilities() {
        return deniedCapabilities;
    }
}
