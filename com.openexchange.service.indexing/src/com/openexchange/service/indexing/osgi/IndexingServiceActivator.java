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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.osgi;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.internal.IndexingServiceImpl;
import com.openexchange.service.indexing.internal.IndexingServiceInit;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link IndexingServiceActivator} - The activator for indexing service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingServiceActivator extends HousekeepingActivator {

    private volatile IndexingServiceInit serviceInit;

    /**
     * Initializes a new {@link IndexingServiceActivator}.
     */
    public IndexingServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MQService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingServiceActivator.class));
        log.info("Starting bundle: com.openexchange.service.indexing");
        try {
            /*
             * IndexingService initialization
             */
            final int maxConcurrentJobs = 8;
            final IndexingServiceInit serviceInit = new IndexingServiceInit(maxConcurrentJobs, this);
            serviceInit.init();
            serviceInit.initReceiver();
            this.serviceInit = serviceInit;
            /*
             * Register service
             */
            registerService(IndexingService.class, new IndexingServiceImpl(serviceInit.getSender()));

            /*-
             * ------------------- Test ---------------------
             */
            //serviceInit.getSender().sendJobMessage(new DummyIndexingJob());
        } catch (final Exception e) {
            log.error("Error starting bundle: com.openexchange.service.indexing");
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingServiceActivator.class));
        log.info("Stopping bundle: com.openexchange.service.indexing");
        try {
            /*
             * Unregister service
             */
            unregisterServices();
            /*
             * IndexingService shut-down
             */
            final IndexingServiceInit serviceInit = this.serviceInit;
            if (null != serviceInit) {
                serviceInit.drop();
                this.serviceInit = null;
            }
            super.stopBundle();
        } catch (final Exception e) {
            log.error("Error stopping bundle: com.openexchange.service.indexing");
            throw e;
        }
    }

    private static final class DummyIndexingJob implements IndexingJob {

        private static final long serialVersionUID = 1L;

        private final Date stamp;

        /**
         * Initializes a new {@link DummyIndexingJob}.
         */
        public DummyIndexingJob() {
            super();
            stamp = new Date(System.currentTimeMillis());
        }

        @Override
        public boolean isDurable() {
            return false;
        }

        @Override
        public void performJob() throws OXException {
            System.out.println("\n\tPerformed dummy job created at " + stamp);
        }

        @Override
        public Behavior getBehavior() {
            return Behavior.CONSUMER_RUNS;
        }

    }

}
