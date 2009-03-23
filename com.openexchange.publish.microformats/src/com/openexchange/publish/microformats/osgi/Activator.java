
package com.openexchange.publish.microformats.osgi;

import javax.servlet.Servlet;
import org.osgi.service.http.HttpService;
import com.openexchange.context.ContextService;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.PublishMicroformatsServlet;
import com.openexchange.publish.microformats.internal.Contexts;
import com.openexchange.publish.microformats.internal.Users;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.user.UserService;

public class Activator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Activator.class);

    private static final String ALIAS = "ajax/sites*";

    private Servlet publishServlet;

    private static final Class<?>[] NEEDED_SERVICES = {
        HttpService.class, PublicationService.class, ContextService.class, UserService.class };

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        PublicationService publicationService = getService(PublicationService.class);
        PublishMicroformatsServlet.setPublicationService(publicationService);

        Contexts.setContextService(getService(ContextService.class));
        Users.setUserService(getService(UserService.class));

        final HttpService httpService = getService(HttpService.class);
        try {
            httpService.registerServlet(ALIAS, (publishServlet = new PublishMicroformatsServlet()), null, null);
            LOG.info(PublishMicroformatsServlet.class.getName() + " successfully re-registered due to re-appearing of " + clazz.getName());
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null && publishServlet != null) {
            httpService.unregister(ALIAS);
            publishServlet = null;
            LOG.info(PublishMicroformatsServlet.class.getName() + " unregistered due to disappearing of " + clazz.getName());
        }
    }

    @Override
    protected void startBundle() throws Exception {
        PublicationService publicationService = getService(PublicationService.class);
        if (publicationService == null) {
            return;
        }
        
        PublishMicroformatsServlet.setPublicationService(publicationService);

        ContextService contextService = getService(ContextService.class);
        if (contextService == null) {
            return;
        }

        Contexts.setContextService(contextService);

        UserService userService = getService(UserService.class);
        if (userService == null) {
            return;
        }
        Users.setUserService(userService);

        try {
            final HttpService httpService = getService(HttpService.class);
            if (httpService == null) {
                return;
            }
            httpService.registerServlet(ALIAS, (publishServlet = new PublishMicroformatsServlet()), null, null);
            LOG.info(PublishMicroformatsServlet.class.getName() + " successfully registered");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            final HttpService httpService = getService(HttpService.class);
            if (httpService != null && publishServlet != null) {
                httpService.unregister(ALIAS);
                publishServlet = null;
                LOG.info(PublishMicroformatsServlet.class.getName() + " unregistered due to bundle stop");
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }
}
