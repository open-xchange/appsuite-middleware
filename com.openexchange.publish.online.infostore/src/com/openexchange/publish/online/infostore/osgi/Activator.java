package com.openexchange.publish.online.infostore.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.online.infostore.InfostoreDocumentPublicationService;
import com.openexchange.publish.online.infostore.InfostorePublicationServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

public class Activator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private static final String ALIAS = InfostoreDocumentPublicationService.PREFIX+"*";
    private static final Class<?>[] NEEDED_SERVICES = {HttpService.class, PublicationDataLoaderService.class, ContextService.class, InfostoreFacade.class, UserService.class, UserConfigurationService.class };
    private ServiceRegistration serviceRegistration;
    private InfostorePublicationServlet servlet;


    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
            registerServlet();
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        InfostoreDocumentPublicationService infostorePublisher = new InfostoreDocumentPublicationService();
        InfostorePublicationServlet.setInfostoreDocumentPublicationService(infostorePublisher);
        serviceRegistration = context.registerService(PublicationService.class.getName(), infostorePublisher, null);
        
        registerServlet();

    }
    @Override
    protected void stopBundle() throws Exception {
        InfostorePublicationServlet.setInfostoreDocumentPublicationService(null);
        serviceRegistration.unregister();
        
        unregisterServlet();
    }
    
    private void unregisterServlet() {
        InfostorePublicationServlet.setContextService(null);
        InfostorePublicationServlet.setPublicationDataLoaderService(null);
        
        HttpService httpService = getService(HttpService.class);
        if(httpService != null && servlet != null) {
            httpService.unregister(ALIAS);
        }
        
    }

    private void registerServlet() {
        HttpService httpService = getService(HttpService.class);
        if(httpService == null) {
            return;
        }
        
        PublicationDataLoaderService dataLoader = getService(PublicationDataLoaderService.class);
        if(dataLoader == null) {
            return;
        }
        
        ContextService contexts = getService(ContextService.class);
        if(contexts == null) {
            return;
        }
        
        UserService users = getService(UserService.class);
        if(users == null) {
            return;
        }
        
        UserConfigurationService userConfigs = getService(UserConfigurationService.class);
        if(userConfigs == null) {
            return;
        }
        
        InfostoreFacade infostore = getService(InfostoreFacade.class);
        if(infostore == null) {
            return;
        }
        
        InfostorePublicationServlet.setContextService(contexts);
        InfostorePublicationServlet.setUserService(users);
        InfostorePublicationServlet.setUserConfigService(userConfigs);
        
        InfostorePublicationServlet.setInfostoreFacade(infostore);
        InfostorePublicationServlet.setPublicationDataLoaderService(dataLoader);
        
        if(servlet == null) {
            try {
                httpService.registerServlet(ALIAS, servlet = new InfostorePublicationServlet(), null, null);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        
    }
    

}
