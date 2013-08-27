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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.management.ManagementService;
import com.openexchange.subscribe.crawler.commandline.CrawlerUpdateMBean;
import com.openexchange.subscribe.crawler.commandline.CrawlerUpdateMBeanImpl;


/**
 * {@link CrawlerMBeanRegisterer}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerMBeanRegisterer implements ServiceTrackerCustomizer<Object,Object>{

    private final BundleContext context;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CrawlerMBeanRegisterer.class));

    private ManagementService managementService;

    private ConfigurationService configurationService;

    private final Activator activator;

    private final Lock lock = new ReentrantLock();

    public CrawlerMBeanRegisterer (final BundleContext context, final Activator activator){
        this.context = context;
        this.activator = activator;
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        lock.lock();
        boolean registeringMBeanPossible = false;
        try {
            if (obj instanceof ManagementService) {
                managementService = (ManagementService) obj;
            }
            if (obj instanceof ConfigurationService) {
                configurationService = (ConfigurationService) obj;
            }
            registeringMBeanPossible = null != managementService && null != configurationService;
        } finally {
            lock.unlock();
        }
        if (registeringMBeanPossible){
            try {
                final ObjectName objectName = new ObjectName(CrawlerUpdateMBean.DOMAIN_NAME , "name", "CrawlerUpdateMBeanImpl");
                managementService.registerMBean(objectName, new CrawlerUpdateMBeanImpl(configurationService, activator));
            } catch (final MalformedObjectNameException e) {
                LOG.error(e.getMessage(), e);
            } catch (final NotCompliantMBeanException e) {
                LOG.error(e.getMessage(), e);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return obj;
    }


    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // nothing to do here
    }


    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        lock.lock();
        try {
            if (service instanceof ManagementService) {
                try {
                    managementService.unregisterMBean(new ObjectName(CrawlerUpdateMBean.DOMAIN_NAME , "name", "CrawlerUpdateMBeanImpl"));
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final MalformedObjectNameException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                }
                managementService = null;
            }
            if (service instanceof ConfigurationService) {
                configurationService = null;
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

}
