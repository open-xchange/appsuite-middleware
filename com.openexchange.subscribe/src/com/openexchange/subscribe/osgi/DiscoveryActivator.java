
package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.context.osgi.WhiteboardContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.datatypes.genericonf.storage.osgi.tools.WhiteboardGenericConfigurationStorageService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.osgi.WhiteboardDBProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.osgiservice.Whiteboard;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.FolderUpdaterService;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.helpers.DocumentMetadataHolder;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.DocumentMetadataHolderFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.subscribe.sql.SubscriptionSQLStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

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
        UserService users = whiteboard.getService(UserService.class);
        UserConfigurationService userConfigs = whiteboard.getService(UserConfigurationService.class);
        InfostoreFacade infostore = whiteboard.getService(InfostoreFacade.class);
        
        Hashtable discoveryDict = new Hashtable();
        discoveryDict.put(Constants.SERVICE_RANKING, 256);
        
        OSGiSubscriptionSourceDiscoveryCollector discoveryCollector = new OSGiSubscriptionSourceDiscoveryCollector(context);
        discoveryCollector.addSubscriptionSourceDiscoveryService(collector);
        
        discoveryRegistration = context.registerService(SubscriptionSourceDiscoveryService.class.getName(), discoveryCollector, discoveryDict);
        
        componentRegistration = new ComponentRegistration(context, "SUB", "com.openexchange.subscribe", SubscriptionErrorMessage.EXCEPTIONS);
        
        List<FolderUpdaterService> folderUpdaters = new ArrayList<FolderUpdaterService>(1);
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<DocumentMetadataHolder>(new DocumentMetadataHolderFolderUpdaterStrategy(users, userConfigs, infostore)));
        
        
        SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        executionRegistration = context.registerService(SubscriptionExecutionService.class.getName(), executor, null);
    
        DBProvider provider = whiteboard.getService(DBProvider.class);
        genconfStorage = new WhiteboardGenericConfigurationStorageService(context);
        SubscriptionSQLStorage storage = new SubscriptionSQLStorage(provider, genconfStorage, discoveryCollector);
    
        AbstractSubscribeService.STORAGE = storage;
        
        AbstractSubscribeService.CRYPTO = whiteboard.getService(CryptoService.class);
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
