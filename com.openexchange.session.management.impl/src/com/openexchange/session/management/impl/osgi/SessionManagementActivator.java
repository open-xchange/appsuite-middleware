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

package com.openexchange.session.management.impl.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.session.management.impl.LocalLastActiveTimestampSetter;
import com.openexchange.session.management.impl.SessionManagementProperty;
import com.openexchange.session.management.impl.SessionManagementServiceImpl;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link SessionManagementActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class SessionManagementActivator extends HousekeepingActivator implements Reloadable {

    private SessionManagementServiceImpl sessionManagementImpl;

    /**
     * Initializes a new {@link SessionManagementActivator}.
     */
    public SessionManagementActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SessiondService.class, LeanConfigurationService.class, UserService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        trackService(GeoLocationService.class);
        trackService(HazelcastInstance.class);
        openTrackers();

        registerService(SessionInspectorService.class, new LocalLastActiveTimestampSetter());

        SessionManagementServiceImpl sessionManagementImpl = new SessionManagementServiceImpl(this);
        this.sessionManagementImpl = sessionManagementImpl;
        registerService(SessionManagementService.class, sessionManagementImpl);

        registerService(Reloadable.class, this);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        this.sessionManagementImpl = null;
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        SessionManagementServiceImpl sessionManagementImpl = this.sessionManagementImpl;
        if (null != sessionManagementImpl) {
            sessionManagementImpl.reinitBlacklistedClients();
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            SessionManagementProperty.GLOBAL_LOOKUP.getFQPropertyName(),
            SessionManagementProperty.CLIENT_BLACKLIST.getFQPropertyName());
    }

}
