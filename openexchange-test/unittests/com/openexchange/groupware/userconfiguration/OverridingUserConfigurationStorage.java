package com.openexchange.groupware.userconfiguration;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;

public class OverridingUserConfigurationStorage extends UserConfigurationStorage{

    protected UserConfigurationStorage delegate = null;

    public OverridingUserConfigurationStorage(UserConfigurationStorage delegate) {
        this.delegate = delegate;
    }

    protected void startInternal() throws AbstractOXException {
        delegate.startInternal();
    }

    protected void stopInternal() throws AbstractOXException {
        delegate.stopInternal();
    }

    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws UserConfigurationException {
        UserConfiguration config = getOverride(userId, groups, ctx);
        if( config != null) {
            return config;
        }
        return delegate.getUserConfiguration(userId, groups, ctx);
    }

    public void clearStorage() throws UserConfigurationException {
        delegate.clearStorage();
    }

    public void removeUserConfiguration(int userId, Context ctx) throws UserConfigurationException {
        delegate.removeUserConfiguration(userId,ctx);
    } 

    public UserConfiguration getOverride(int userId, int[] groups, Context ctx) throws UserConfigurationException {
        return null;
    }

    public void override() throws AbstractOXException {
        UserConfigurationStorage.setInstance(this);
    }

    public void takeBack() throws AbstractOXException {
        UserConfigurationStorage.setInstance(delegate);
    }
}
