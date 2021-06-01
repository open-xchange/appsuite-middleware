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

package com.openexchange.pns.transport.apn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import com.openexchange.pns.transport.apns_http2.util.ApnOptions;
import com.openexchange.pns.transport.apns_http2.util.ApnOptionsPerClient;
import com.openexchange.pns.transport.apns_http2.util.ApnOptionsProvider;


/**
 * {@link DefaultApnOptionsProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultApnOptionsProvider implements ApnOptionsProvider {

    private final Map<String, ApnOptions> options;

    /**
     * Initializes a new {@link DefaultApnOptionsProvider}.
     */
    public DefaultApnOptionsProvider(Map<String, ApnOptions> options) {
        super();
        this.options = options;
    }

    @Override
    public ApnOptions getOptions(String client) {
        return options.get(client);
    }

    @Override
    public Collection<ApnOptionsPerClient> getAvailableOptions() {
        Collection<ApnOptionsPerClient> col = new ArrayList<>(options.size());
        for (Map.Entry<String, ApnOptions> entry : options.entrySet()) {
            col.add(new ApnOptionsPerClient(entry.getKey(), entry.getValue()));
        }
        return col;
    }

}
