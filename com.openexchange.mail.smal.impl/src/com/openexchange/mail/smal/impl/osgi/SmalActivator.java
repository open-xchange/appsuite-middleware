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

package com.openexchange.mail.smal.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.smal.SmalAccessService;
import com.openexchange.mail.smal.impl.SmalProvider;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.internal.SmalDeleteListenerImpl;
import com.openexchange.mail.smal.impl.internal.SmalUpdateTaskProviderServiceImpl;
import com.openexchange.mail.smal.impl.internal.tasks.DropMailSyncTable;
import com.openexchange.mail.smal.impl.internal.tasks.SMALCheckTableTask;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link SmalActivator} - The activator for Super-MAL bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SmalActivator extends HousekeepingActivator {

//    private volatile com.openexchange.mail.smal.impl.index.IndexEventHandler eventHandler;

    /**
     * Initializes a new {@link SmalActivator}.
     */
    public SmalActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, ConfigViewFactory.class, ThreadPoolService.class, TimerService.class, IndexingService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        SmalServiceLookup.getInstance().setServiceLookup(this);
        track(MailProvider.class, new SmalProviderServiceTracker(context));
        trackService(MailAccountStorageService.class);
        trackService(SessiondService.class);
        trackService(DatabaseService.class);
        // trackService(LanguageDetectionService.class);
        trackService(UserService.class);
        trackService(ContextService.class);
        trackService(IndexingService.class);
        trackService(IndexFacadeService.class);
        openTrackers();
        /*
         * Register SMAL provider
         */
        {
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", SmalProvider.PROTOCOL_SMAL.toString());
            registerService(MailProvider.class, SmalProvider.getInstance(), dictionary);

            registerService(SmalAccessService.class, new SmalAccessServiceImpl());
        }
//        /*
//         * Register event handlers
//         */
//        {
//            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
//            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
//            final IndexEventHandler eventHandler = this.eventHandler = new IndexEventHandler();
//            registerService(EventHandler.class, eventHandler, serviceProperties);
//        }
        /*
         * Register update task, create table job and delete listener
         */
        {
            registerService(UpdateTaskProviderService.class, new SmalUpdateTaskProviderServiceImpl(
                new SMALCheckTableTask(),
                new DropMailSyncTable()));
            registerService(DeleteListener.class, new SmalDeleteListenerImpl());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
//        final com.openexchange.mail.smal.impl.index.IndexEventHandler eventHandler = this.eventHandler;
//        if (null != eventHandler) {
//            eventHandler.close();
//            this.eventHandler = null;
//        }
        cleanUp();
        SmalServiceLookup.getInstance().setServiceLookup(null);
    }

}
