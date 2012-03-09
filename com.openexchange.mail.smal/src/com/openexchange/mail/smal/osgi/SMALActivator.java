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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.langdetect.LanguageDetectionService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.smal.SMALProvider;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.adapter.IndexService;
import com.openexchange.mail.smal.adapter.internal.IndexEventHandler;
import com.openexchange.mail.smal.adapter.internal.IndexServiceImpl;
import com.openexchange.mail.smal.adapter.solrj.SolrAdapter;
import com.openexchange.mail.smal.internal.SMALDeleteListener;
import com.openexchange.mail.smal.internal.SMALUpdateTaskProviderService;
import com.openexchange.mail.smal.internal.tasks.CreateMailSyncTable;
import com.openexchange.mail.smal.internal.tasks.SMALCheckTableTask;
import com.openexchange.mail.smal.internal.tasks.SMALCreateTableTask;
import com.openexchange.mail.smal.jobqueue.JobQueue;
import com.openexchange.mail.smal.jobqueue.internal.JobQueueEventHandler;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link SMALActivator} - The activator for Super-MAL bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMALActivator extends HousekeepingActivator {

    private IndexService indexService;
    private JobQueueEventHandler eventHandler;

    /**
     * Initializes a new {@link SMALActivator}.
     */
    public SMALActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        SMALServiceLookup.getInstance().setServiceLookup(this);
        track(MailProvider.class, new SMALProviderServiceTracker(context));
        trackService(MailAccountStorageService.class);
        trackService(SessiondService.class);
        trackService(DatabaseService.class);
        trackService(ConfigIndexService.class);
        trackService(LanguageDetectionService.class);
        trackService(UserService.class);
        trackService(ContextService.class);
        openTrackers();
        JobQueue.getInstance();
        /*
         * Register SMAL provider
         */
        {
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", SMALProvider.PROTOCOL_SMAL.toString());
            registerService(MailProvider.class, SMALProvider.getInstance(), dictionary);
        }
        /*
         * Register index service
         */
        {
            final ConfigurationService cs = getService(ConfigurationService.class);
            final String className = cs.getProperty("com.openexchange.mail.smal.adapter", SolrAdapter.class.getName());
            final Class<? extends IndexAdapter> clazz = Class.forName(className).asSubclass(IndexAdapter.class);
            indexService = new IndexServiceImpl(clazz.newInstance());
            /*-
             * 
             * TODO: Enable
             * 
             * 
            registerService(IndexService.class, indexService);
            if (!addService(IndexService.class, indexService)) {
                com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SMALActivator.class)).error(
                    "IndexService could not be added.");
            }
             * 
             */
        }
        /*
         * Register event handlers
         */
        {
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new IndexEventHandler(), serviceProperties);
        }
        {
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            eventHandler = new JobQueueEventHandler();
            registerService(EventHandler.class, eventHandler, serviceProperties);
        }
        /*
         * Register update task, create table job and delete listener
         */
        {
            registerService(CreateTableService.class, new CreateMailSyncTable());
            registerService(UpdateTaskProviderService.class, new SMALUpdateTaskProviderService(new SMALCreateTableTask(), new SMALCheckTableTask()));
            registerService(DeleteListener.class, new SMALDeleteListener());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        JobQueue.dropInstance();
        if (null != eventHandler) {
            eventHandler.close();
            eventHandler = null;
        }
        cleanUp();
        if (null != indexService) {
            indexService.getAdapter().stop();
            indexService = null;
        }
        SMALServiceLookup.getInstance().setServiceLookup(null);
    }

}
