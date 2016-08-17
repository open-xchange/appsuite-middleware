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

package com.openexchange.groupware.settings.extensions.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.extensions.ServicePublisher;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.strings.StringParser;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Activator extends HousekeepingActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private static final String PREFERENCE_PATH = "preferencePath";

    private static final String METADATA_PREFIX = "meta";

    private volatile ServicePublisher servicePublisher;

    private volatile ServiceTracker<ConfigViewFactory,ConfigViewFactory> serviceTracker;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { StringParser.class };
    }

    @Override
    protected void startBundle() throws Exception {
        servicePublisher = new OSGiServicePublisher(context);
        registerListenerForConfigurationService();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        unregisterListenerForConfigurationService();
        ServicePublisher servicePublisher = this.servicePublisher;
        if (null != servicePublisher) {
            this.servicePublisher = null;
            servicePublisher.removeAllServices();
        }
    }

    public void handleConfigurationUpdate(final ConfigViewFactory viewFactory) {
        LOG.info("Updating configtree");
        try {
            ConfigView view = viewFactory.getView();
            for (Map.Entry<String, ComposedConfigProperty<String>> entry : view.all().entrySet()) {
                String propertyName = entry.getKey();
                ComposedConfigProperty<String> property = entry.getValue();
                String[] possiblePath = generatePathFor(property);
                if (null != possiblePath) {
                    export(viewFactory, property, propertyName, possiblePath);
                }
            }
        } catch (final Throwable x) {
            LOG.error("", x);
        }
    }

    // Maybe that is an overuse of anonymous inner classes. Better get around to refactoring this at some point.

    private void export(final ConfigViewFactory viewFactory, final ComposedConfigProperty<String> property, final String propertyName, final String[] path) throws OXException {
        ServicePublisher servicePublisher = this.servicePublisher;
        if (null == servicePublisher) {
            LOG.warn("Unable to export config-cascade option {}. Bundle not initialized.", propertyName);
            return;
        }

        final PreferencesItemService prefItem = new PreferencesItemService() {

            private static final String UNDEFINED_STRING = "undefined";

            @Override
            public String[] getPath() {
                return path;
            }

            @Override
            public IValueHandler getSharedValue() {
                return new IValueHandler() {

                    @Override
                    public int getId() {
                        return NO_ID;
                    }

                    @Override
                    public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                            Object value = viewFactory.getView(user.getId(), ctx.getContextId()).get(propertyName, String.class);
                            if (UNDEFINED_STRING.equals(value)) {
                                //setting.setSingleValue(UNDEFINED);
                                return;
                            }
                            try {
                                // Let's turn this into a nice object, if it conforms to JSON
                                value = new JSONObject("{value: "+value+"}").get("value");

                            } catch (final JSONException x) {
                                // Ah well, let's pretend it's a string.
                            }

                            setting.setSingleValue(value);
                    }

                    @Override
                    public boolean isAvailable(final UserConfiguration userConfig) {
                        return true;
                    }

                    @Override
                    public boolean isWritable() {
                        try {
                            final String finalScope = property.get("final");
                            final String isProtected = property.get("protected");
                            return (finalScope == null || finalScope.equals("user")) && (isProtected == null || ! property.get("protected", boolean.class).booleanValue());
                        } catch (final OXException x) {
                            LOG.error("", x);
                            return false;
                        }
                    }

                    @Override
                    public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                        Object value = setting.getSingleValue();
                        if (value == null) {
                            final Object[] multiValue = setting.getMultiValue();
                            if (multiValue != null) {

                                final JSONArray arr = new JSONArray();
                                for (final Object o : multiValue) {
                                    arr.put(o);
                                }
                                value = arr.toString();
                            } else if (setting.isEmptyMultivalue()) {
                                value = "[]";
                            }
                        }
                        final String oldValue = viewFactory.getView(user.getId(), ctx.getContextId()).get(propertyName, String.class);
                        if (value != null) {
                            // Clients have a habit of dumping the config back at us, so we only save differing values.
                            if (!value.equals(oldValue)) {
                                viewFactory.getView(user.getId(), ctx.getContextId()).set("user", propertyName, value);
                            }

                        }
                    }

                };
            }

        };

        servicePublisher.publishService(PreferencesItemService.class, prefItem);

        // And let's publish the metadata as well
        final List<String> metadataNames = property.getMetadataNames();
        for (final String metadataName : metadataNames) {
            final String[] metadataPath = new String[path.length+2];
            System.arraycopy(path, 0, metadataPath, 1, path.length);
            metadataPath[metadataPath.length-1] = metadataName;
            metadataPath[0] = METADATA_PREFIX;


            final PreferencesItemService metadataItem = new PreferencesItemService() {

                @Override
                public String[] getPath() {
                    return metadataPath;
                }

                @Override
                public IValueHandler getSharedValue() {
                    return new IValueHandler() {

                        @Override
                        public int getId() {
                            return NO_ID;
                        }

                        @Override
                        public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                            final ComposedConfigProperty<String> prop = viewFactory.getView(user.getId(), ctx.getContextId()).property(propertyName, String.class);
                            Object value = prop.get(metadataName);
                            if (null != value) {
                                try {
                                    // Let's turn this into a nice object, if it conforms to JSON
                                    value = new JSONTokener(value.toString()).nextValue();
                                } catch (final JSONException x) {
                                    // Ah well, let's pretend it's a string.
                                }
                            }
                            setting.setSingleValue(value);
                        }

                        @Override
                        public boolean isAvailable(final UserConfiguration userConfig) {
                            return true;
                        }

                        @Override
                        public boolean isWritable() {
                            return false;
                        }

                        @Override
                        public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                            // IGNORE
                        }

                    };
                }

            };

            servicePublisher.publishService(PreferencesItemService.class, metadataItem);
        }

        // Lastly, let's publish configurability.
        final String[] configurablePath = new String[path.length+2];
        System.arraycopy(path, 0, configurablePath, 1, path.length);
        configurablePath[configurablePath.length-1] = "configurable";
        configurablePath[0] = METADATA_PREFIX;


        final PreferencesItemService configurableItem = new PreferencesItemService(){

            @Override
            public String[] getPath() {
                return configurablePath;
            }

            @Override
            public IValueHandler getSharedValue() {
                return new IValueHandler() {

                    @Override
                    public int getId() {
                        return NO_ID;
                    }

                    @Override
                    public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                        final String finalScope = property.get("final");
                        final String isProtected = property.get("protected");
                        final boolean writable = (finalScope == null || finalScope.equals("user")) && (isProtected == null || ! property.get("protected", boolean.class).booleanValue());
                        setting.setSingleValue(Boolean.valueOf(writable));
                    }

                    @Override
                    public boolean isAvailable(final UserConfiguration userConfig) {
                        return true;
                    }

                    @Override
                    public boolean isWritable() {
                        return false;
                    }

                    @Override
                    public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                        // IGNORE
                    }

                };
            }

        };

        servicePublisher.publishService(PreferencesItemService.class, configurableItem);
    }

    /**
     * Generates the appropriate path to be used in config tree in case provided property has a preference path setting.
     * <p>
     * <code>null</code> is returned to signal given property is not suitable to be placed into config tree.
     *
     * @param property The property to examine whether a preference path is available to generate a path for config tree
     * @return The path for config tree or <code>null</code> if property is not suitable
     * @throws OXException If preference path setting cannot be checked
     */
    private String[] generatePathFor(final ComposedConfigProperty<String> property) throws OXException {
        String preferencePath = property.get(PREFERENCE_PATH);
        if (null == preferencePath) {
            return null;
        }
        String[] path = preferencePath.split("/", 0);
        List<String> sanitizedPath = null;
        for (int i = 0; null == sanitizedPath && i < path.length; i++) {
            if (Strings.isEmpty(path[i])) {
                // Sanitizing needed...
                sanitizedPath = new ArrayList<String>(path.length);
                for (int k = 0; k < i; k++) {
                    sanitizedPath.add(path[k]);
                }
                for (int k = i+1; k < path.length; k++) {
                    String segment = path[k];
                    if (!Strings.isEmpty(segment)) {
                        sanitizedPath.add(segment);
                    }
                }
            }
        }
        return null == sanitizedPath ? path : sanitizedPath.toArray(new String[sanitizedPath.size()]);
    }

    private void registerListenerForConfigurationService() {
        ServiceTracker<ConfigViewFactory,ConfigViewFactory> serviceTracker = new ServiceTracker<ConfigViewFactory,ConfigViewFactory>(context, ConfigViewFactory.class, new ConfigurationTracker(context, this));
        serviceTracker.open();
        this.serviceTracker = serviceTracker;
    }

    private void unregisterListenerForConfigurationService() {
        ServiceTracker<ConfigViewFactory,ConfigViewFactory> serviceTracker = this.serviceTracker;
        if (null != serviceTracker) {
            this.serviceTracker = null;
            serviceTracker.close();
        }
    }


    private static final class ConfigurationTracker implements ServiceTrackerCustomizer<ConfigViewFactory,ConfigViewFactory> {
        private final BundleContext context;
        private final Activator activator;

        public ConfigurationTracker(final BundleContext context, final Activator activator) {
            this.context = context;
            this.activator = activator;

        }

        @Override
        public ConfigViewFactory addingService(final ServiceReference<ConfigViewFactory> serviceReference) {
            final ConfigViewFactory addedService = context.getService(serviceReference);
            activator.handleConfigurationUpdate(addedService);
            return addedService;
        }

        @Override
        public void modifiedService(final ServiceReference<ConfigViewFactory> serviceReference, final ConfigViewFactory o) {
            // IGNORE
        }

        @Override
        public void removedService(final ServiceReference<ConfigViewFactory> serviceReference, final ConfigViewFactory o) {
            context.ungetService(serviceReference);
        }
    }
}
