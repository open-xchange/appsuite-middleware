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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.sms.tools.internal;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sms.tools.SMSBucketService;
import com.openexchange.sms.tools.SMSConstants;
import com.openexchange.sms.tools.osgi.Services;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link SMSBucketServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class SMSBucketServiceImpl implements SMSBucketService {

    private static final Logger LOG = LoggerFactory.getLogger(SMSBucketService.class);
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map;
    private ArrayList<ScheduledTimerTask> tasks;

    /**
     * Initializes a new {@link SMSBucketServiceImpl}.
     */
    public SMSBucketServiceImpl() {
        super();
        map = new ConcurrentHashMap<>();
        tasks = new ArrayList<>();

    }

    @Override
    public int getSMSToken(Session session) throws OXException {
        if (!map.containsKey(session.getContextId())) {
            map.putIfAbsent(session.getContextId(), new ConcurrentHashMap<Integer, Integer>());
        }
        ConcurrentHashMap<Integer, Integer> userMap = map.get(session.getContextId());
        if (!userMap.containsKey(session.getUserId())) {
            int tokens = getUserLimit(session);
            userMap.putIfAbsent(session.getUserId(), tokens - 1);
            return tokens;
        }
        for (;;) {
            int result = userMap.get(session.getUserId()).intValue();
            if (result == 0) {
                return 0;
            }
            if (userMap.replace(session.getUserId(), result, result - 1)) {
                return result;
            }
        }
    }

    private int getUserLimit(Session session) throws OXException {
        ConfigViewFactory configFactory = Services.getService(ConfigViewFactory.class);
        if (configFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view = configFactory.getView(session.getUserId(), session.getContextId());
        return Integer.valueOf(view.get(SMSConstants.SMS_USER_LIMIT_PROPERTY, String.class));
    }

    @Override
    public void refillAllBuckets() {
        map.clear();
    }

    @Override
    public void refillBucket(int contextId) {
        map.get(contextId).clear();
    }

    @Override
    public void startRefreshTask() throws OXException {

        TimerService timerService = Services.getService(TimerService.class);
        if (timerService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(TimerService.class.getName());
        }
        
        ConfigurationService config = Services.getService(ConfigurationService.class);
        if (config == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
        }
        int interval = config.getIntProperty(SMSConstants.SMS_USER_LIMIT_REFRESH_INTERVAL, 1440);
        tasks.add(timerService.scheduleAtFixedRate(new RefreshSMSBucketsTask(this), interval, interval, TimeUnit.MINUTES));
        LOG.info("Started general SMSBucketRefreshTask with interval " + interval);
    }
    
    @Override
    public void startRefreshTask(int contextId) throws OXException {

        TimerService timerService = Services.getService(TimerService.class);
        if (timerService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(TimerService.class.getName());
        }
        
        ConfigViewFactory config = Services.getService(ConfigViewFactory.class);
        if (config == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view = config.getView(-1, contextId);
        int interval = view.get(SMSConstants.SMS_USER_LIMIT_REFRESH_INTERVAL, Integer.class);
        tasks.add(timerService.scheduleAtFixedRate(new RefreshSMSBucketsTask(this, contextId), interval, interval, TimeUnit.MINUTES));
        LOG.info("Started SMSBucketRefreshTask for context " + contextId + " with interval " + interval);
    }

    @Override
    public void stopRefreshTasks() {
        for (ScheduledTimerTask task : tasks) {
            task.cancel();
        }

        tasks.clear();
    }

    private class RefreshSMSBucketsTask implements Runnable {

        private SMSBucketService service;
        private Integer context;

        /**
         * Initializes a new {@link SMSBucketServiceImpl.RefreshSMSBucketsTask}.
         * 
         * @throws OXException
         */
        public RefreshSMSBucketsTask(SMSBucketService service) throws OXException {
            super();
            this.service = service;
            if (this.service == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SMSBucketService.class.getName());
            }
        }
        
        /**
         * Initializes a new {@link SMSBucketServiceImpl.RefreshSMSBucketsTask}.
         * 
         * @throws OXException
         */
        public RefreshSMSBucketsTask(SMSBucketService service, int context) throws OXException {
            super();
            this.service = service;
            if (this.service == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SMSBucketService.class.getName());
            }
            this.context=context;
        }

        @Override
        public void run() {
            if (context == null) {
                service.refillAllBuckets();
            } else {
                service.refillBucket(context);
            }
        }

    }

    @Override
    public boolean isEnabled() throws OXException {
        ConfigurationService config = Services.getService(ConfigurationService.class);
        if (config == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
        }
        return config.getBoolProperty(SMSConstants.SMS_USER_LIMIT_ENABLED, true);
    }

}
