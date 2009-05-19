
package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.context.osgi.WhiteboardContextService;
import com.openexchange.datatypes.genericonf.storage.osgi.tools.WhiteboardGenericConfigurationStorageService;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.osgi.WhiteboardDBProvider;
import com.openexchange.server.osgiservice.Whiteboard;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.FolderUpdaterService;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.subscribe.sql.SubscriptionSQLStorage;

import com.openexchange.exceptions.osgi.ComponentRegistration;;
/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DiscoveryActivator implements BundleActivator {


    private OSGiSubscriptionSourceCollector collector;
    private ServiceRegistration discoveryRegistration;
    private ServiceRegistration executionRegistration;
    private WhiteboardContextService contextService;
    private ComponentRegistration componentRegistration;
    private WhiteboardGenericConfigurationStorageService genconfStorage;
    
    private Whiteboard whiteboard;
    
    public void start(BundleContext context) throws Exception {
        whiteboard = new Whiteboard(context);
        collector = new OSGiSubscriptionSourceCollector(context);
        contextService = new WhiteboardContextService(context);
        Hashtable discoveryDict = new Hashtable();
        discoveryDict.put(Constants.SERVICE_RANKING, 256);
        discoveryRegistration = context.registerService(SubscriptionSourceDiscoveryService.class.getName(), collector, discoveryDict);
        
        componentRegistration = new ComponentRegistration(context, "SUB", "com.openexchange.subscribe", SubscriptionErrorMessage.EXCEPTIONS);
        
        List<FolderUpdaterService> folderUpdaters = new ArrayList<FolderUpdaterService>(1);
        folderUpdaters.add(new StrategyFolderUpdaterService<ContactObject>(new ContactFolderUpdaterStrategy()));
        SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        executionRegistration = context.registerService(SubscriptionExecutionService.class.getName(), executor, null);
    
        DBProvider provider = whiteboard.getService(DBProvider.class);
        genconfStorage = new WhiteboardGenericConfigurationStorageService(context);
        SubscriptionSQLStorage storage = new SubscriptionSQLStorage(provider, genconfStorage, collector);
    
        AbstractSubscribeService.STORAGE = storage;
    }

    public void stop(BundleContext context) throws Exception {
        whiteboard.close();
        genconfStorage.close();
        componentRegistration.unregister();
        collector.close();
        contextService.close();
        discoveryRegistration.unregister();
        executionRegistration.unregister();
    }

}
