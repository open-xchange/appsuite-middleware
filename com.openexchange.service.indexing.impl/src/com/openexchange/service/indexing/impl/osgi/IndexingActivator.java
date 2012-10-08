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

package com.openexchange.service.indexing.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.quartz.service.QuartzService;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.service.MailService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.IndexingServiceMBean;
import com.openexchange.service.indexing.impl.internal.IndexingServiceImpl;
import com.openexchange.service.indexing.impl.internal.IndexingServiceMBeanImpl;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.groupware.SessionEventHandler;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link IndexingActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexingActivator extends HousekeepingActivator {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingActivator.class));
    
    private ObjectName indexingMBeanName;

    private IndexingServiceMBeanImpl indexingMBean;
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, 
            IndexFacadeService.class, 
            HazelcastInstance.class, 
            QuartzService.class, 
            MailService.class,
            FolderService.class,
            InfostoreFacade.class,
            ContextService.class,
            UserService.class,
            UserConfigurationService.class };
    }
    
    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.service.indexing");
        Services.setServiceLookup(this);
        IndexingService serviceImpl = new IndexingServiceImpl();
        addService(IndexingService.class, serviceImpl);
        registerService(IndexingService.class, serviceImpl);
        
        Dictionary<String, Object> sessionProperties = new Hashtable<String, Object>(1);
        sessionProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
        registerService(EventHandler.class, new SessionEventHandler(), sessionProperties);
        
        registerMBean((IndexingServiceImpl) serviceImpl);
        openTrackers();
    }
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        
        ManagementService managementService = Services.optService(ManagementService.class);
        if (managementService != null && indexingMBeanName != null) {
            managementService.unregisterMBean(indexingMBeanName);
            indexingMBean = null;
        }        
    }
    
    private void registerMBean(IndexingServiceImpl indexingService) {
        try {
            indexingMBeanName = new ObjectName(IndexingServiceMBean.DOMAIN, "name", "Indexing Service");
            indexingMBean = new IndexingServiceMBeanImpl(indexingService);
            track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

                @Override
                public void added(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.registerMBean(indexingMBeanName, indexingMBean);
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.unregisterMBean(indexingMBeanName);
                    } catch (OXException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
        } catch (MalformedObjectNameException e) {
            LOG.error(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
}
