package com.openexchange.groupware.userconfiguration;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;

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
}
