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
        return new IValueHandler(){

            public int getId() {
                return -1;
            }

            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws SettingException {
                long value = ServerUserSetting.getContactCollectionFolder(ctx.getContextId(), user.getId());
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
                    ServerUserSetting.setContactCollectionFolder(ctx.getContextId(), user.getId(), Long.parseLong((String) setting.getSingleValue()));
            }
            
        };
    }

}
