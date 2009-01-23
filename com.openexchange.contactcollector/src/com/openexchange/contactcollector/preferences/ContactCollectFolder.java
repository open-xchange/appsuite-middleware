
package com.openexchange.contactcollector.preferences;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.ServerUserSetting;
import com.openexchange.session.Session;

public class ContactCollectFolder implements PreferencesItemService {

    public String[] getPath() {
        return new String[] { "modules", "mail", "contactCollectFolder" };
    }

    public IValueHandler getSharedValue() {
        return new IValueHandler() {

            public int getId() {
                return -1;
            }

            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws SettingException {
                if (setting != null) {
                    final int value = ServerUserSetting.getContactCollectionFolder(ctx.getContextId(), user.getId());
                    setting.setSingleValue(Integer.valueOf(value));
                }
            }

            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail() && userConfig.hasContact();
            }

            public boolean isWritable() {
                return true;
            }

            // TODO folder identifier can only be integer.
            public void writeValue(final Context ctx, final User user, final Setting setting) throws SettingException {
                if (setting != null)
                    ServerUserSetting.setContactCollectionFolder(
                        ctx.getContextId(),
                        user.getId(),
                        Integer.parseInt(setting.getSingleValue().toString()));
            }

        };
    }

}
