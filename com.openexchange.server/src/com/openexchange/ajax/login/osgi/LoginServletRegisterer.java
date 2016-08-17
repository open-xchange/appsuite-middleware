/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.login.osgi;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ACTION;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionServletInterceptor;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.ShareLoginConfiguration.ShareLoginProperty;
import com.openexchange.ajax.login.session.CookieRefresher;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.InitProperty;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.osgi.ServiceSet;

/**
 * {@link LoginServletRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginServletRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginServletRegisterer.class);

    private final BundleContext context;
    private final Lock lock = new ReentrantLock();
    private final Map<String, LoginRequestHandler> handlers = new ConcurrentHashMap<String, LoginRequestHandler>();

    private ConfigurationService configService;
    private HttpService httpService;
    private DispatcherPrefixService prefixService;

    private LoginServlet login;
    private volatile String alias;
    private volatile ServiceRegistration<SessionServletInterceptor> interceptorRegistration;

    /**
     * Initializes a new {@link LoginServletRegisterer}.
     *
     * @param context The bundle context
     * @param rampUp The ramp-up service set
     */
    public LoginServletRegisterer(final BundleContext context, final ServiceSet<LoginRampUpService> rampUp) {
        super();
        this.context = context;
        LoginServlet.setRampUpServices(rampUp);
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        lock.lock();
        try {
            if (obj instanceof ConfigurationService) {
                configService = (ConfigurationService) obj;
                HashCalculator.getInstance().configure(configService);
            }
            if (obj instanceof HttpService) {
                httpService = (HttpService) obj;
            }
            if (obj instanceof DispatcherPrefixService) {
                prefixService = (DispatcherPrefixService) obj;
            }
            needsRegistration = null != configService && null != httpService && login == null && prefixService != null;
            if (needsRegistration) {
                login = new LoginServlet();
            }
        } finally {
            lock.unlock();
        }
        if (obj instanceof LoginRequestHandler) {
            Object tmp = reference.getProperty(PARAMETER_ACTION);
            if (null != tmp && tmp instanceof String) {
                String action = (String) tmp;
                LoginRequestHandler handler = (LoginRequestHandler) obj;
                addLoginRequestHandler(action, handler);
            }
        }
        if (needsRegistration) {
            for (Entry<String, LoginRequestHandler> entry : handlers.entrySet()) {
                login.addRequestHandler(entry.getKey(), entry.getValue());
            }
            final Dictionary<String, String> params = new Hashtable<String, String>(32);
            addProperty(params, Property.UI_WEB_PATH);
            addProperty(params, Property.COOKIE_HASH);
            addProperty(params, Property.COOKIE_TTL);
            addProperty(params, Property.COOKIE_FORCE_HTTPS);
            addProperty(params, Property.FORCE_HTTPS);
            addProperty(params, Property.IP_CHECK);
            addProperty(params, Property.IP_MASK_V4);
            addProperty(params, Property.IP_MASK_V6);
            addProperty(params, Property.IP_CHECK_WHITELIST);
            final String tmp = configService.getText("noipcheck.cnf");
            if (null != tmp) {
                params.put(ConfigurationProperty.NO_IP_CHECK_RANGE.getPropertyName(), tmp);
            }
            addProperty(params, ConfigurationProperty.SESSIOND_AUTOLOGIN);
            addProperty(params, ConfigurationProperty.HTTP_AUTH_AUTOLOGIN);
            addProperty(params, ConfigurationProperty.HTTP_AUTH_CLIENT);
            addProperty(params, ConfigurationProperty.HTTP_AUTH_VERSION);
            addProperty(params, ConfigurationProperty.ERROR_PAGE_TEMPLATE);
            addProperty(params, ConfigurationProperty.INSECURE);
            addProperty(params, ConfigurationProperty.REDIRECT_IP_CHANGE_ALLOWED);
            addProperty(params, ConfigurationProperty.DISABLE_TRIM_LOGIN);
            addProperty(params, ConfigurationProperty.FORM_LOGIN_WITHOUT_AUTHID);
            addProperty(params, ConfigurationProperty.RANDOM_TOKEN);
            addProperty(params, ConfigurationProperty.CHECK_PUNY_CODE_LOGIN);
            /*
             * add properties for share login configuration
             */
            for (ShareLoginProperty property : ShareLoginProperty.values()) {
                addProperty(params, property);
            }
            try {
                LOG.info("Registering login servlet.");
                String alias = prefixService.getPrefix() + LoginServlet.SERVLET_PATH_APPENDIX;
                httpService.registerServlet(alias, login, params, null);
                this.alias = alias;

                LoginConfiguration conf = LoginServlet.getLoginConfiguration();
                if (conf.isSessiondAutoLogin() || conf.getCookieExpiry() < 0) {
                    interceptorRegistration = context.registerService(SessionServletInterceptor.class, new CookieRefresher(conf), null);
                }
            } catch (final ServletException e) {
                LOG.error("Registering login servlet failed.", e);
            } catch (final NamespaceException e) {
                LOG.error("Registering login servlet failed.", e);
            }
        }
        return obj;
    }

    private void addProperty(final Dictionary<String, String> params, final Property property) {
        final String propertyName = property.getPropertyName();
        final String prop = configService.getProperty(propertyName);
        if (prop == null) {
            final String defaultValue = property.getDefaultValue();
            LOG.warn("Missing configuration property \"{}\". Using fall-back value: {}", propertyName, defaultValue);
            params.put(propertyName, defaultValue);
        } else {
            params.put(propertyName, prop);
        }
    }

    private void addProperty(Dictionary<String, String> params, InitProperty property) {
        String propertyName = property.getPropertyName();
        String value = configService.getProperty(propertyName, property.getDefaultValue());
        if (null != value) {
            params.put(propertyName, value.trim());
        }
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        HttpService unregister = null;
        String removeAction = null;
        lock.lock();
        try {
            if (service instanceof ConfigurationService) {
                configService = null;
            }
            if (service instanceof HttpService) {
                if (login != null) {
                    unregister = httpService;
                    login = null;
                }
                httpService = null;
            }
            if (service instanceof LoginRequestHandler) {
                Object tmp = reference.getProperty(PARAMETER_ACTION);
                if (null != tmp && tmp instanceof String) {
                    removeAction = (String) tmp;
                }
            }
        } finally {
            lock.unlock();
        }
        if (null != removeAction) {
            removeLoginRequestHandler(removeAction);
        }
        if (null != unregister) {
            LOG.info("Unregistering login servlet.");
            String alias = this.alias;
            if (null != alias) {
                unregister.unregister(alias);
                this.alias = null;
            }

            ServiceRegistration<SessionServletInterceptor> interceptorRegistration = this.interceptorRegistration;
            if (null != interceptorRegistration) {
                this.interceptorRegistration = null;
                interceptorRegistration.unregister();
            }
        }
        context.ungetService(reference);
    }

    public void addLoginRequestHandler(String action, LoginRequestHandler handler) {
        handlers.put(action, handler);
        if (null != login) {
            login.addRequestHandler(action, handler);
        }
    }

    public void removeLoginRequestHandler(String action) {
        handlers.remove(action);
        if (null != login) {
            login.removeRequestHandler(action);
        }
    }
}
