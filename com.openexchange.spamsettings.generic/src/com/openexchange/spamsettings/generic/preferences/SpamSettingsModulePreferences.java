
package com.openexchange.spamsettings.generic.preferences;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;

/**
 * {@link SpamSettingsModulePreferences}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SpamSettingsModulePreferences implements PreferencesItemService {

    private static boolean MODULE = false;

    @Override
    public String[] getPath() {
        return new String[] { "modules", "com.openexchange.spamsettings.generic", "module" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return true;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                setting.setSingleValue(Boolean.valueOf(isModule()));
            }
        };
    }

    public static void setModule(final boolean module) {
        MODULE = module;
    }

    public static boolean isModule() {
        return MODULE;
    }

}
