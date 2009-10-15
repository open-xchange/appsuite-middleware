package com.openexchange.groupware.userconfiguration;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

public class OverridingUserConfigurationStorage extends UserConfigurationStorage{

    protected UserConfigurationStorage delegate = null;

    public OverridingUserConfigurationStorage(final UserConfigurationStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void startInternal() throws AbstractOXException {
        delegate.startInternal();
    }

    @Override
    protected void stopInternal() throws AbstractOXException {
        delegate.stopInternal();
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx) throws UserConfigurationException {
        final UserConfiguration config = getOverride(userId, groups, ctx);
        if( config != null) {
            return config;
        }
        return delegate.getUserConfiguration(userId, groups, ctx);
    }

    @Override
    public UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws UserConfigurationException {
        List<UserConfiguration> retval = new ArrayList<UserConfiguration>();
        for (User user : users) {
            retval.add(getUserConfiguration(user.getId(), user.getGroups(), ctx));
        }
        return retval.toArray(new UserConfiguration[retval.size()]);
    }

    @Override
    public void clearStorage() throws UserConfigurationException {
        delegate.clearStorage();
    }

    @Override
    public void removeUserConfiguration(final int userId, final Context ctx) throws UserConfigurationException {
        delegate.removeUserConfiguration(userId,ctx);
    } 

    public UserConfiguration getOverride(final int userId, final int[] groups, final Context ctx) throws UserConfigurationException {
        return null;
    }

    public void override() throws AbstractOXException {
        UserConfigurationStorage.setInstance(this);
    }

    public void takeBack() throws AbstractOXException {
        UserConfigurationStorage.setInstance(delegate);
    }

    @Override
    public void saveUserConfiguration(final int permissionBits, final int userId, final Context ctx) throws UserConfigurationException {
        delegate.saveUserConfiguration(permissionBits, userId, ctx);
    }
}
