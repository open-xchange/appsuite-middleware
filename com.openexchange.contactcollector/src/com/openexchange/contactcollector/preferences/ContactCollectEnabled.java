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

public class ContactCollectEnabled implements PreferencesItemService {

    public String[] getPath() {
        return new String[] { "modules", "mail", "contactCollectEnabled" };
    }

    public IValueHandler getSharedValue() {
        return new IValueHandler(){

            public int getId() {
                return -1;
            }

            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws SettingException {
                boolean value = ServerUserSetting.contactCollectionEnabled(ctx.getContextId(), user.getId());
                if(setting != null)
                    setting.setSingleValue(value);
            }

            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasWebMail() && userConfig.hasContact();
            }

            public boolean isWritable() {
                return true;
            }

            public void writeValue(Context ctx, User user, Setting setting) throws SettingException {
                if(setting != null)
                    ServerUserSetting.setContactColletion(ctx.getContextId(), user.getId(), (Boolean) setting.getSingleValue());
            }
            
        };
    }

}
