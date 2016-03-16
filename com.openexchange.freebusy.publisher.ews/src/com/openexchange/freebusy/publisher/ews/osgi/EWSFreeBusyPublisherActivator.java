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

package com.openexchange.freebusy.publisher.ews.osgi;

import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.ews.EWSFactoryService;
import com.openexchange.freebusy.provider.InternalFreeBusyProvider;
import com.openexchange.freebusy.publisher.ews.Tools;
import com.openexchange.freebusy.publisher.ews.internal.EWSFreeBusyPublisherLookup;
import com.openexchange.freebusy.publisher.ews.internal.Publisher;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link EWSFreeBusyPublisherActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EWSFreeBusyPublisherActivator extends HousekeepingActivator {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EWSFreeBusyPublisherActivator.class);

    private ScheduledTimerTask publishTask = null;

    /**
     * Initializes a new {@link EWSFreeBusyPublisherActivator}.
     */
    public EWSFreeBusyPublisherActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, InternalFreeBusyProvider.class, UserService.class, ContextService.class,
            EWSFactoryService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.freebusy.publisher.ews");
            EWSFreeBusyPublisherLookup.set(this);
            int initialDelay = Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.initialDelay", 5);
            int delay = Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.delay", 15);
            publishTask = EWSFreeBusyPublisherLookup.getService(TimerService.class).scheduleWithFixedDelay(
                new Publisher(), initialDelay, delay, TimeUnit.MINUTES);
            LOG.info("Scheduled first publication cycle to run in {} minutes, then repeating with a delay of {} minutes.", initialDelay, delay);
        } catch (Exception e) {
            LOG.error("error starting com.openexchange.freebusy.publisher.ews", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.freebusy.publisher.ews");
        if (null != publishTask) {
            LOG.info("Stopping publication cycle.");
            publishTask.cancel(true);
        }
        super.stopBundle();
    }

}
