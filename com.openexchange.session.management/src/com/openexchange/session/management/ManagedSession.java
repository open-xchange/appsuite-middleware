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

package com.openexchange.session.management;

import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.session.Session;
import com.openexchange.session.management.osgi.Services;

/**
 * {@link ManagedSession}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class ManagedSession {

    private final String sessionId;
    private final String ipAddress;
    private final String client;
    private final String location;
    private final Type type;

    public ManagedSession(String sessionId, String ipAddress, String client, Type type) {
        super();
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.client = client;
        this.type = type;
        this.location = getLocation(ipAddress);
    }

    private String getLocation(String ipAddress) {
        GeoLocationService service = Services.getService(GeoLocationService.class);
        if (null == service) {
            return "unknown";
        }
        try {
//            GeoInformation geoInformation = service.getGeoInformation(ipAddress);
            GeoInformation geoInformation = service.getGeoInformation("144.90.54.84");
            StringBuilder sb = new StringBuilder();
            if (geoInformation.hasCity()) {
                sb.append(geoInformation.getCity());
            }
            if (geoInformation.hasCountry()) {
                sb.append(", ").append(geoInformation.getCountry());
            }
            return sb.toString();
        } catch (OXException e) {
            return "unknown";
        }
    }

    public ManagedSession(Session session, Type type) {
        this(session.getSessionID(), session.getLocalIp(), session.getClient(), type);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getClient() {
        return client;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type.getType();
    }

    public static enum Type {
        LOCAL("local"),
        REMOTE("remote"),
        ;

        private final String type;

        private Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
