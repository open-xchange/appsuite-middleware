package com.openexchange.share.notification.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.group.GroupService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.impl.DefaultNotificationService;
import com.openexchange.share.notification.impl.ShareNotificationHandler;
import com.openexchange.share.notification.impl.mail.MailNotificationHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

public class ShareNotificationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ContextService.class,
            UserService.class,
            GroupService.class,
            ServerConfigService.class,
            TranslatorFactory.class,
            ModuleSupport.class,
            ConfigurationService.class,
            ConfigViewFactory.class,
            TemplateService.class,
            HtmlService.class,
            ShareService.class,
            NotificationMailFactory.class
        };
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void startBundle() throws Exception {
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

        trackService(ContactCollectorService.class);
        trackService(ObjectUseCountService.class);

        registerService(ShareNotificationService.class, defaultNotificationService);
        openTrackers();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

}
