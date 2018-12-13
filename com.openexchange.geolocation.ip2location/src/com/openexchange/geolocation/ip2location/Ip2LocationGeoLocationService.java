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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.geolocation.ip2location;

import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link Ip2LocationGeoLocationService} - The ip[2Location Geo location service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public class Ip2LocationGeoLocationService implements GeoLocationService {

    private final Ip2LocationSQLStorage storage;

    /**
     * Initializes a new {@link Ip2LocationGeoLocationService}.
     */
    public Ip2LocationGeoLocationService(ServiceLookup services) {
        super();
        this.storage = new Ip2LocationSQLStorage(services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.GeoLocationService#getGeoInformation(com.openexchange.session.Session, java.lang.String)
     */
    @Override
    public GeoInformation getGeoInformation(Session session, String ipAddress) throws OXException {
        return storage.getGeoInformation(session, GeoLocationUtils.convertIp(ipAddress));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.GeoLocationService#getGeoInformation(com.openexchange.session.Session, double, double, double)
     */
    @Override
    public GeoInformation getGeoInformation(Session session, double latitude, double longitude, int radius) throws OXException {
        return storage.getGeoInformation(session, latitude, longitude, radius);
    }
}
