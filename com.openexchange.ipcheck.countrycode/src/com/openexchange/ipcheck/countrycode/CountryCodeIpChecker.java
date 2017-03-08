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

package com.openexchange.ipcheck.countrycode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.ajax.ipcheck.IPCheckers;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMetrics;
import com.openexchange.management.MetricAware;
import com.openexchange.session.Session;

/**
 * {@link CountryCodeIpChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public class CountryCodeIpChecker implements IPChecker, MetricAware<IPCheckMetrics> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryCodeIpChecker.class);

    private final GeoLocationService service;
    private final IPCheckMetrics metrics;

    /**
     * Initializes a new {@link CountryCodeIpChecker}.
     */
    public CountryCodeIpChecker(GeoLocationService service) {
        super();
        this.service = service;
        metrics = new IPCheckMetrics();
    }

    @Override
    public void handleChangedIp(String current, String previous, Session session, IPCheckConfiguration configuration) throws OXException {
        metrics.incrementTotalIPChanges();
        if (!IPCheckers.isWhiteListed(current, previous, session, configuration)) {
            GeoInformation geoInformationCurrent = service.getGeoInformation(current);
            GeoInformation geoInformationPrevious = service.getGeoInformation(previous);

            boolean countryChanged = false;
            if (geoInformationPrevious.hasCountry() && geoInformationCurrent.hasCountry()) {
                countryChanged = !geoInformationPrevious.getCountry().equals(geoInformationCurrent.getCountry());
            }

            if (countryChanged) {
                LOGGER.info("Country was changed for session '{}' from '{}' to '{}'", session.getSessionID(), geoInformationPrevious.getCountry(), geoInformationCurrent.getCountry());
                IPCheckers.kick(current, session);
                metrics.incrementDeniedIPChanges();
            }
        }

        IPCheckers.apply(true, current, session, configuration);
        metrics.incrementAcceptedIPChanges();
    }

    @Override
    public String getId() {
        return "countrycode";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.management.MetricAware#getMetricsObject()
     */
    @Override
    public IPCheckMetrics getMetricsObject() {
        return metrics;
    }
}
