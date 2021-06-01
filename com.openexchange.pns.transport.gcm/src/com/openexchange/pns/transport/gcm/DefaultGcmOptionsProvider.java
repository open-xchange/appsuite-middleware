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

package com.openexchange.pns.transport.gcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * {@link DefaultGcmOptionsProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultGcmOptionsProvider implements GcmOptionsProvider {

    private final Map<String, GcmOptions> options;

    /**
     * Initializes a new {@link DefaultGcmOptionsProvider}.
     */
    public DefaultGcmOptionsProvider(Map<String, GcmOptions> options) {
        super();
        this.options = options;
    }

    @Override
    public GcmOptions getOptions(String client) {
        return options.get(client);
    }

    @Override
    public Collection<GcmOptionsPerClient> getAvailableOptions() {
        Collection<GcmOptionsPerClient> col = new ArrayList<>(options.size());
        for (Map.Entry<String, GcmOptions> entry : options.entrySet()) {
            col.add(new GcmOptionsPerClient(entry.getKey(), entry.getValue()));
        }
        return col;
    }

}
