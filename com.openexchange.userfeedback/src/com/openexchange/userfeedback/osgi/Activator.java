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

package com.openexchange.userfeedback.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userfeedback.FeedbackMode;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.FeedbackTypeRegistry;
import com.openexchange.userfeedback.internal.FeedbackServiceImpl;
import com.openexchange.userfeedback.internal.FeedbackTypeRegistryImpl;
import com.openexchange.userfeedback.internal.UserFeedbackProperty;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="vitali.sjablow.ruthmann@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
public class Activator extends HousekeepingActivator{

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ConfigViewFactory.class, DatabaseService.class, CapabilityService.class, ServerConfigService.class, LeanConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(FeedbackTypeRegistry.class, FeedbackTypeRegistryImpl.getInstance());
        registerService(FeedbackService.class, new FeedbackServiceImpl());
        
        {
            final String sCapability = "feedback";
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new CapabilityChecker() {
                @Override
                public boolean isEnabled(String capability, Session ses) throws OXException {
                    if (sCapability.equals(capability)) {
                        ServerSession session = ServerSessionAdapter.valueOf(ses);
                        User user = session.getUser();
                        if (session.isAnonymous() || user.isGuest()) {
                            return false;
                        }
                        LeanConfigurationService leanConfig = Services.getService(LeanConfigurationService.class);
                        return leanConfig.getBooleanProperty(user.getId(), session.getContextId(), UserFeedbackProperty.enabled);
                    }

                    return true;
                }
            }, properties);

            getService(CapabilityService.class).declareCapability(sCapability);
        }
        
        FeedbackMode feedbackMode = new FeedbackMode();
        registerService(PreferencesItemService.class, feedbackMode);
        registerService(ConfigTreeEquivalent.class, feedbackMode);
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
