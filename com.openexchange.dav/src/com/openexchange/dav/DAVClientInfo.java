/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
