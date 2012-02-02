package com.openexchange.config.cascade.user.osgi;

import java.util.Hashtable;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.user.UserConfigProvider;
import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

public class UserConfigCascadeActivator extends HousekeepingActivator {

    private static final Class<?>[] NEEDED = new Class[]{UserService.class, ContextService.class};

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        UserService users = getService(UserService.class);
        ContextService contexts = getService(ContextService.class);

        UserConfigProvider provider = new UserConfigProvider(users, contexts);

        Hashtable<String, Object> properties = new Hashtable<String,Object>();
        properties.put("scope", "user");

        registerService(ConfigProviderService.class, provider, properties);
    }


}
