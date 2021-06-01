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

package com.openexchange.groupware.settings.extensions.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.extensions.ServicePublisher;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.user.User;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Activator extends HousekeepingActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private static final String PREFERENCE_PATH = "preferencePath";

    private static final String METADATA_PREFIX = "meta";

    private ServicePublisher servicePublisher;
    private ServiceTracker<ConfigViewFactory,ConfigViewFactory> serviceTracker;

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
    protected synchronized void startBundle() throws Exception {
        servicePublisher = new OSGiServicePublisher(context);
        registerListenerForConfigurationService();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        unregisterListenerForConfigurationService();
        ServicePublisher servicePublisher = this.servicePublisher;
        if (null != servicePublisher) {
            this.servicePublisher = null;
            servicePublisher.removeAllServices();
        }
    }

    public synchronized void handleConfigurationUpdate(final ConfigViewFactory viewFactory) {
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
        } catch (Throwable x) {
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

                            // Let's turn this into a nice object, if it conforms to JSON
                            value = toPojo(value);

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
                            return (finalScope == null || finalScope.equals(ConfigViewScope.USER.getScopeName())) && (isProtected == null || !Boolean.parseBoolean(isProtected));
                        } catch (OXException x) {
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
                                viewFactory.getView(user.getId(), ctx.getContextId()).set(ConfigViewScope.USER.getScopeName(), propertyName, value);
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
                                // Let's turn this into a nice object, if it conforms to JSON
                                value = toPojo(value);
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
                        final boolean writable = (finalScope == null || finalScope.equals(ConfigViewScope.USER.getScopeName())) && (isProtected == null || !Boolean.parseBoolean(isProtected));
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
     * Tries to convert given value's string representation to an appropriate Java object.
     *
     * @param value The value to parse
     * @return The appropriate Java object or the value itself
     */
    static Object toPojo(Object value) {
        if (!(value instanceof String)) {
            return value;
        }

        try {
            // Let's turn this into a nice object, if it conforms to JSON
            return new JSONObject("{value: "+value+"}").get("value");
        } catch (JSONException e) {
            // Ah well, let's pretend it's a string.
            return value;
        }
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
                    if (Strings.isNotEmpty(segment)) {
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
