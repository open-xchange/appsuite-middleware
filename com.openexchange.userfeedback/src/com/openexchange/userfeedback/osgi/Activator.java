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

package com.openexchange.userfeedback.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceReference;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.userfeedback.FeedbackMode;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.FeedbackStoreListener;
import com.openexchange.userfeedback.FeedbackType;
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
        track(FeedbackType.class, new SimpleRegistryListener<FeedbackType>() {

            @Override
            public void added(ServiceReference<FeedbackType> ref, FeedbackType type) {
                FeedbackTypeRegistryImpl.getInstance().registerType(type);
            }

            @Override
            public void removed(ServiceReference<FeedbackType> ref, FeedbackType type) {
                FeedbackTypeRegistryImpl.getInstance().unregisterType(type);
            }});
        ServiceSet<FeedbackStoreListener> storeListeners = new ServiceSet<FeedbackStoreListener>();
        track(FeedbackStoreListener.class, storeListeners);
        openTrackers();
        registerService(FeedbackService.class, new FeedbackServiceImpl(storeListeners));

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
