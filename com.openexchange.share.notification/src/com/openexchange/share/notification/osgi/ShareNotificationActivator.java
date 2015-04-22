package com.openexchange.share.notification.osgi;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationHandler;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.impl.DefaultNotificationService;
import com.openexchange.share.notification.mail.impl.MailNotificationHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

public class ShareNotificationActivator extends HousekeepingActivator {

    final private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareNotificationActivator.class);
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {ConfigurationService.class, ServerConfigService.class, UserService.class, TemplateService.class,
            HtmlService.class, ModuleSupport.class, TranslatorFactory.class, ConfigViewFactory.class
            };
    }

    @Override
    protected void startBundle() throws Exception {
        
        context.addFrameworkListener(new FrameworkListener() {

            @Override
            public void frameworkEvent(FrameworkEvent event) {
                if (event.getBundle().getSymbolicName().equalsIgnoreCase("com.openexchange.share.notification")) {
                    int eventType = event.getType();
                    if (eventType == FrameworkEvent.ERROR) {
                        LOG.error(event.toString(), event.getThrowable());
                    } else {
                        LOG.info(event.toString(), event.getThrowable());
                    }
                }
            }
        });
        
        // Initialize share notification service
        final DefaultNotificationService defaultNotificationService = new DefaultNotificationService(this);

        // Add in-place handlers
        defaultNotificationService.add(new MailNotificationHandler(this));

        // track additional share notification handlers
        track(ShareNotificationHandler.class, new ServiceTrackerCustomizer<ShareNotificationHandler, ShareNotificationHandler>() {

            @Override
            public ShareNotificationHandler addingService(ServiceReference<ShareNotificationHandler> reference) {
                ShareNotificationHandler handler = context.getService(reference);
                defaultNotificationService.add(handler);
                return handler;
            }

            @Override
            public void modifiedService(ServiceReference<ShareNotificationHandler> reference, ShareNotificationHandler service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<ShareNotificationHandler> reference, ShareNotificationHandler service) {
                defaultNotificationService.remove(service);
                context.ungetService(reference);
            }
        });
        
        registerService(ShareNotificationService.class, defaultNotificationService);
        openTrackers();
    }

}
