
package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.context.osgi.WhiteboardContextService;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.subscribe.FolderUpdaterService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {


    private OSGiSubscriptionSourceCollector collector;
    private ServiceRegistration discoveryRegistration;
    private ServiceRegistration executionRegistration;
    private WhiteboardContextService contextService;
    
    public void start(BundleContext context) throws Exception {
        collector = new OSGiSubscriptionSourceCollector(context);
        contextService = new WhiteboardContextService(context);
        
        discoveryRegistration = context.registerService(SubscriptionSourceDiscoveryService.class.getName(), collector, null);
        
        List<FolderUpdaterService> folderUpdaters = new ArrayList<FolderUpdaterService>(1);
        folderUpdaters.add(new StrategyFolderUpdaterService<ContactObject>(new ContactFolderUpdaterStrategy()));
        SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        executionRegistration = context.registerService(SubscriptionExecutionService.class.getName(), executor, null);
    }

    public void stop(BundleContext context) throws Exception {
        collector.close();
        contextService.close();
        discoveryRegistration.unregister();
        executionRegistration.unregister();
    }

}
