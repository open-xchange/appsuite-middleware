
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
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.tx.DBProvider;
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
    
  
    public void start(final BundleContext context) throws Exception {
        whiteboard = new Whiteboard(context);
        collector = new OSGiSubscriptionSourceCollector(context);
        contextService = new WhiteboardContextService(context);
        final UserService users = whiteboard.getService(UserService.class);
        final UserConfigurationService userConfigs = whiteboard.getService(UserConfigurationService.class);
        final InfostoreFacade infostore = whiteboard.getService(InfostoreFacade.class);
        
        final Hashtable discoveryDict = new Hashtable();
        discoveryDict.put(Constants.SERVICE_RANKING, 256);
        
        final OSGiSubscriptionSourceDiscoveryCollector discoveryCollector = new OSGiSubscriptionSourceDiscoveryCollector(context);
        discoveryCollector.addSubscriptionSourceDiscoveryService(collector);
        
        discoveryRegistration = context.registerService(SubscriptionSourceDiscoveryService.class.getName(), discoveryCollector, discoveryDict);
        
        FolderFieldActivator.DISCOVERY = discoveryCollector;
        
        componentRegistration = new ComponentRegistration(context, "SUB", "com.openexchange.subscribe", SubscriptionErrorMessage.EXCEPTIONS);
        
        final List<FolderUpdaterService> folderUpdaters = new ArrayList<FolderUpdaterService>(1);
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<DocumentMetadataHolder>(new DocumentMetadataHolderFolderUpdaterStrategy(users, userConfigs, infostore)));
        
        
        final SubscriptionExecutionServiceImpl executor = new SubscriptionExecutionServiceImpl(collector, folderUpdaters, contextService);
        executionRegistration = context.registerService(SubscriptionExecutionService.class.getName(), executor, null);
    
        final DBProvider provider = whiteboard.getService(DBProvider.class);
        genconfStorage = new WhiteboardGenericConfigurationStorageService(context);
        final SubscriptionSQLStorage storage = new SubscriptionSQLStorage(provider, genconfStorage, discoveryCollector);
    
        AbstractSubscribeService.STORAGE = storage;
        
        AbstractSubscribeService.CRYPTO = whiteboard.getService(CryptoService.class);
    }

    public void stop(final BundleContext context) throws Exception {
        whiteboard.close();
        whiteboard = null;
        genconfStorage.close();
        genconfStorage = null;
        componentRegistration.unregister();
        componentRegistration = null;
        collector.close();
        collector = null;
        contextService.close();
        contextService = null;
        discoveryRegistration.unregister();
        discoveryRegistration = null;
        executionRegistration.unregister();
        executionRegistration = null;
    }

}
