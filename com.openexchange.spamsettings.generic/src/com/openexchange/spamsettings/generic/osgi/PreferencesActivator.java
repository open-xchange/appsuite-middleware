
package com.openexchange.spamsettings.generic.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;


/**
 * {@link PreferencesActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class PreferencesActivator implements BundleActivator {

    private ServiceRegistration<PreferencesItemService> userConfigFlagRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        userConfigFlagRegistration = context.registerService(PreferencesItemService.class, new SpamSettingsModulePreferences(), null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        userConfigFlagRegistration.unregister();
        userConfigFlagRegistration = null;
    }

}
