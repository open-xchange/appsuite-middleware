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

import java.net.Inet4Address;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.InetAddresses;
import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.ajax.ipcheck.IPCheckers;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationExceptionCodes;
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

        boolean whiteListedClient = IPCheckers.isWhitelistedClient(session, configuration);
        // ACCEPT: If session-associated client is white-listed
        if (whiteListedClient) {
            accept(current, previous, session, true, AcceptReason.WHITE_LISTED);
            return;
        }

        // ACCEPT: If one of the given IP address lies with in the private range
        if (isPrivateV4Address(current) || isPrivateV4Address(previous)) {
            accept(current, previous, session, whiteListedClient, AcceptReason.PRIVATE_IPV4);
            return;
        }

        // ACCEPT: if the IP address are white-listed
        if (IPCheckers.isWhiteListedAddress(current, previous, configuration)) {
            accept(current, previous, session, whiteListedClient, AcceptReason.WHITE_LISTED);
            return;
        }

        try {
            GeoInformation geoInformationCurrent = service.getGeoInformation(current);
            GeoInformation geoInformationPrevious = service.getGeoInformation(previous);

            boolean countryChanged = true;
            if (geoInformationPrevious.hasCountry() && geoInformationCurrent.hasCountry()) {
                countryChanged = !geoInformationPrevious.getCountry().equals(geoInformationCurrent.getCountry());
            }
            // DENY: if country code did change
            if (countryChanged) {
                deny(current, previous, session, DenyReason.COUNTRY_CHANGE);
            }
        } catch (OXException e) {
            if (!e.getPrefix().equals(GeoLocationExceptionCodes.PREFIX)) {
                throw e;
            }
            String message = e.getMessage();
            LOGGER.error("{}", message, e);
            deny(current, previous, session, DenyReason.EXCEPTION, e);
        }

        // ACCEPT
        accept(current, previous, session, whiteListedClient, AcceptReason.ELIGIBLE);
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

    ///////////////////////////////////////////// HELPERS //////////////////////////////////////////////////

    /**
     * Accepts the IP change and applies it to the specified {@link Session}
     *
     * @param current The current IP address
     * @param session The {@link Session}
     * @param whiteListedClient Whether client is white-listed
     * @param acceptReason The accept reason
     */
    private void accept(String current, String previous, Session session, boolean whiteListedClient, AcceptReason acceptReason) {
        LOGGER.debug("The IP change from '{}' to '{}' was accepted. Reason: '{}'", previous, current, acceptReason.getMessage());
        IPCheckers.updateIPAddress(current, session, true, whiteListedClient);
        switch (acceptReason) {
            case PRIVATE_IPV4:
                metrics.incrementAcceptedPrivateIP();
                break;
            case WHITE_LISTED:
                metrics.incrementAcceptedWhiteListed();
                break;
            case ELIGIBLE:
            default:
                metrics.incrementAcceptedEligibleIPChange();
        }
        metrics.incrementAcceptedIPChanges();
    }

    /**
     * Denies the IP change and kicks the specified {@link Session}
     *
     * @param current The current IP
     * @param session The {@link Session}
     * @throws OXException To actually kick the session
     */
    private void deny(String current, String previous, Session session, DenyReason reason) throws OXException {
        deny(current, previous, session, reason, null);
    }

    /**
     * Denies the IP change and kicks the specified {@link Session}
     *
     * @param current The current IP
     * @param session The {@link Session}
     * @param t The exception
     * @throws OXException To actually kick the session
     */
    private void deny(String current, String previous, Session session, DenyReason reason, Throwable t) throws OXException {
        if (null == t) {
            LOGGER.debug("The IP change from '{}' to '{}' was denied. Reason: '{}'", previous, current, reason.getMessage());
        } else {
            LOGGER.debug("The IP change from '{}' to '{}' was denied. Reason: '{}', '{}'", previous, current, reason.getMessage(), t.getMessage());
        }

        switch (reason) {
            case EXCEPTION:
                metrics.incrementDeniedException();
                break;
            case COUNTRY_CHANGE:
                metrics.incrementDeniedCountryChange();
                break;
            default:
                break;
        }
        metrics.incrementDeniedIPChanges();
        IPCheckers.kick(current, session);
    }

    private static final Cache<String, Boolean> CACHE_PRIVATE_IPS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(90, TimeUnit.MINUTES).build();

    /**
     * Checks whether the specified IP address lies in the private range.
     *
     * @param ip The IP address to check
     * @return <code>true</code> if the IP address lies within the private range of class A, B, or C; <code>false</code> otherwise
     */
    private boolean isPrivateV4Address(final String ip) {
        Boolean isPrivate = CACHE_PRIVATE_IPS.getIfPresent(ip);
        if (null != isPrivate) {
            return isPrivate.booleanValue();
        }

        Callable<Boolean> loader = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(doCheckForPrivateV4Address(ip));
            }
        };

        try {
            return CACHE_PRIVATE_IPS.get(ip, loader).booleanValue();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException ? (RuntimeException) cause : new IllegalStateException(cause);
        }
    }

    /**
     * <p>Checks whether the specified IP address lies in the private range.</p>
     *
     * <ul>
     * <li>Class A range: 10.0.0.0 - 10.255.255.255</li>
     * <li>Class B range: 172.16.0.0 - 172.31.255.255</li>
     * <li>Class C range: 192.168.0.0 - 192.168.255.255</li>
     * </ul>
     *
     * <p>
     * Based on {@link Inet4Address#isSiteLocalAddress()}, with addition of the check for the 172.16.0.0 - 172.31.255.255 block
     * </p>
     *
     * @param ip The IP address to check
     * @return <code>true</code> if the IP address lies within the private range of class A, B, or C;
     *         <code>false</code> otherwise
     */
    static boolean doCheckForPrivateV4Address(String ip) {
        int address = InetAddresses.coerceToInteger(InetAddresses.forString(ip));
        return (((address >>> 24) & 0xFF) == 10) // Class A
            || ((((address >>> 24) & 0xFF) == 172) && ((address >>> 16) & 0xFF) >= 16 && ((address >>> 16) & 0xFF) <= 31) // Class B
            || ((((address >>> 24) & 0xFF) == 192) && (((address >>> 16) & 0xFF) == 168)); // Class C
    }
}
