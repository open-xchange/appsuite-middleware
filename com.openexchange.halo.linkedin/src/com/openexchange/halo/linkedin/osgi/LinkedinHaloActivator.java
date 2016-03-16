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

package com.openexchange.halo.linkedin.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.linkedin.AbstractLinkedinDataSource;
import com.openexchange.halo.linkedin.LinkedinInboxDataSource;
import com.openexchange.halo.linkedin.LinkedinProfileDataSource;
import com.openexchange.halo.linkedin.LinkedinUpdatesDataSource;
import com.openexchange.halo.linkedin.helpers.LinkedinPlusChecker;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link LinkedinHaloActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LinkedinHaloActivator extends HousekeepingActivator {

    private static final String PLUS_CAPABILITY = "linkedinPlus";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            LinkedInService.class, OAuthService.class, ContactService.class,
            UserService.class, ConfigurationService.class, CapabilityService.class,
            ConfigViewFactory.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        final LinkedinPlusChecker plusChecker = new LinkedinPlusChecker(this);
        getService(CapabilityService.class).declareCapability(PLUS_CAPABILITY);
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, PLUS_CAPABILITY);
        registerService(CapabilityChecker.class, new CapabilityChecker() {
            @Override
            public boolean isEnabled(String capability, Session session) throws OXException {
                if (PLUS_CAPABILITY.equals(capability)) {
                    final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                    if (serverSession.isAnonymous() || serverSession.getUser().isGuest()) {
                        return false;
                    }
                    return plusChecker.hasPlusFeatures(new ServerSessionAdapter(serverSession));
                }

                return true;
            }
        }, properties);

        AbstractLinkedinDataSource profile = new LinkedinProfileDataSource(this);
        profile.setPlusChecker(plusChecker);
        AbstractLinkedinDataSource inbox = new LinkedinInboxDataSource(this);
        inbox.setPlusChecker(plusChecker);
        AbstractLinkedinDataSource updates = new LinkedinUpdatesDataSource(this);
        updates.setPlusChecker(plusChecker);

        registerService(HaloContactDataSource.class, profile);
        registerService(HaloContactDataSource.class, inbox);
        registerService(HaloContactDataSource.class, updates);
    }

}
