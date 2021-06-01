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

package com.openexchange.pns.transport.apns_http2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import com.openexchange.pns.transport.apns_http2.util.ApnsHttp2Options;
import com.openexchange.pns.transport.apns_http2.util.ApnsHttp2OptionsPerClient;
import com.openexchange.pns.transport.apns_http2.util.ApnsHttp2OptionsProvider;


/**
 * {@link DefaultApnsHttp2OptionsProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultApnsHttp2OptionsProvider implements ApnsHttp2OptionsProvider {

    private final Map<String, ApnsHttp2Options> options;

    /**
     * Initializes a new {@link DefaultApnsHttp2OptionsProvider}.
     */
    public DefaultApnsHttp2OptionsProvider(Map<String, ApnsHttp2Options> options) {
        super();
        this.options = options;
    }

    @Override
    public ApnsHttp2Options getOptions(String client) {
        return options.get(client);
    }

    @Override
    public Collection<ApnsHttp2OptionsPerClient> getAvailableOptions() {
        Collection<ApnsHttp2OptionsPerClient> col = new ArrayList<>(options.size());
        for (Map.Entry<String, ApnsHttp2Options> entry : options.entrySet()) {
            col.add(new ApnsHttp2OptionsPerClient(entry.getKey(), entry.getValue()));
        }
        return col;
    }

}
