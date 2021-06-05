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

package com.openexchange.dav;

import java.util.Locale;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.java.Strings;


/**
 * {@link DAVClientInfo}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class DAVClientInfo implements ClientInfo {

    private final String app;
    private final String osFamily;
    private final String osVersion;
    private final String client;
    private final String clientVersion;
    private final String clientFamily;
    private final ClientInfoType type;

    public DAVClientInfo(String app, String clientFamily) {
        this(app, null, null, null, null, clientFamily);
    }

    public DAVClientInfo(String app, String osFamily, String osVersion, String client, String clientVersion, String clientFamily) {
        this(app, osFamily, osVersion, client, clientVersion, clientFamily, ClientInfoType.DAV);
    }

    public DAVClientInfo(String app, String osFamily, String osVersion, String client, String clientVersion, String clientFamily, ClientInfoType type) {
        super();
        this.app = app;
        this.osFamily = osFamily;
        this.osVersion = osVersion;
        this.client = client;
        this.clientVersion = clientVersion;
        this.clientFamily = clientFamily;
        this.type = type;
    }

    @Override
    public ClientInfoType getType() {
        return type;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return app;
    }

    @Override
    public String getOSFamily() {
        if (Strings.isNotEmpty(osFamily)) {
            return osFamily.toLowerCase();
        }
        return null;
    }

    @Override
    public String getOSVersion() {
        return osVersion;
    }

    @Override
    public String getClientName() {
        if (Strings.isNotEmpty(client)) {
            return client.toLowerCase();
        }
        return null;
    }

    @Override
    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public String getClientFamily() {
        return clientFamily;
    }

}
