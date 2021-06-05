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

package com.openexchange.clientinfo.impl;

import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.ReadableUserAgent;


/**
 * {@link MailAppClientInfoProvider}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class MailAppClientInfoProvider implements ClientInfoProvider {

    private final ServiceLookup services;

    public MailAppClientInfoProvider(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        String clientId = session.getClient();
        if (Strings.isEmpty(clientId)) {
            return null;
        }

        UserAgentParser userAgentParser = services.getService(UserAgentParser.class);
        if (null == userAgentParser) {
            return getClientInfo(clientId);
        }

        ReadableUserAgent readableUserAgent = userAgentParser.parse((String) session.getParameter(Session.PARAM_USER_AGENT));
        String platform = readableUserAgent.getOperatingSystem().getFamilyName();
        String osVersionMajor = readableUserAgent.getOperatingSystem().getVersionNumber().getMajor();
        String osVersionMinor = readableUserAgent.getOperatingSystem().getVersionNumber().getMinor();
        String platformVersion = null;
        if (Strings.isNotEmpty(osVersionMajor)) {
            if (Strings.isNotEmpty(osVersionMinor)) {
                platformVersion = new StringBuilder(osVersionMajor).append('.').append(osVersionMinor).toString();
            } else {
                platformVersion = osVersionMajor;
            }
        }
        if (clientId.startsWith("open-xchange-mobile-api-facade")) {
            return new MailAppClientInfo("OX Mail App", null, platform, platformVersion);
        } else if (clientId.equals("open-xchange-mailapp")) {
            return new MailAppClientInfo("OX Mail App", null, platform, platformVersion);
        }
        return null;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return null;
        }

        if (clientId.startsWith("open-xchange-mobile-api-facade")) {
            return new MailAppClientInfo("OX Mail App");
        } else if (clientId.equals("open-xchange-mailapp")) {
            return new MailAppClientInfo("OX Mail App");
        }
        return null;
    }

}
