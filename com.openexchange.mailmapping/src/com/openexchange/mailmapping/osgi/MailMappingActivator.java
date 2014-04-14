package com.openexchange.mailmapping.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import com.openexchange.context.ContextService;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.impl.DefaultMailMappingService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

public class MailMappingActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{UserService.class, ContextService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
        registerService(MailResolver.class, new DefaultMailMappingService(this), props);
    }


}
