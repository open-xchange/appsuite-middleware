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

package com.openexchange.sms.tools.internal;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sms.tools.SMSBucketExceptionCodes;
import com.openexchange.sms.tools.SMSBucketService;
import com.openexchange.sms.tools.SMSConstants;
import com.openexchange.sms.tools.osgi.Services;


/**
 * {@link SMSBucketServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class SMSBucketServiceImpl implements SMSBucketService {

    private IMap<String, SMSBucket> map;
    private static final String HZ_MAP_NAME = "SMS_Bucket";

    /**
     * Initializes a new {@link SMSBucketServiceImpl}.
     * 
     * @throws OXException
     */
    public SMSBucketServiceImpl() throws OXException {
        super();
        HazelcastInstance hz = Services.getService(HazelcastInstance.class);
        if (hz == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        map = hz.getMap(HZ_MAP_NAME);
    }

    /**
     * Initializes a new {@link SMSBucketServiceImpl} for testing purposes.
     * 
     * @throws OXException
     */
    public SMSBucketServiceImpl(HazelcastInstance hzInstance) throws OXException {
        super();
        HazelcastInstance hz = hzInstance;
        if (hz == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        map = hz.getMap(HZ_MAP_NAME);
    }

    @Override
    public int getSMSToken(Session session) throws OXException {
        String userIdentifier = session.getContextId()+"/"+session.getUserId();
        int limit = getUserLimit(session);
        if (!map.containsKey(userIdentifier)) {
            map.putIfAbsent(userIdentifier, new SMSBucket(limit));
        } else {
            SMSBucket bucket = map.get(userIdentifier);
            if (bucket.getBucketSize() != limit) {
                map.replace(userIdentifier, bucket, new SMSBucket(limit));
            }
        }
        ConfigViewFactory configFactory = Services.getService(ConfigViewFactory.class);
        if (configFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view =configFactory.getView(session.getUserId(), session.getContextId());
        int refreshInterval = 0;
        try {
            refreshInterval = Integer.valueOf(view.get(SMSConstants.SMS_USER_LIMIT_REFRESH_INTERVAL, String.class));
        } catch (NumberFormatException e) {
            throw new OXException(e);
        }
        
        for (;;) {
            SMSBucket oldBucket = map.get(userIdentifier);
            SMSBucket newBucket = oldBucket.clone();
            int amount = newBucket.removeToken(refreshInterval);

            if (amount == -1) {
                int hours = (int) Math.ceil(refreshInterval / 60d);
                throw SMSBucketExceptionCodes.SMS_LIMIT_REACHED.create(hours);
            }

            if (map.replace(userIdentifier, oldBucket, newBucket)) {
                return amount;
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
    public boolean isEnabled(Session session) throws OXException {
        ConfigViewFactory configFactory = Services.getService(ConfigViewFactory.class);
        if (configFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view = configFactory.getView(session.getUserId(), session.getContextId());
        return view.get(SMSConstants.SMS_USER_LIMIT_ENABLED, boolean.class);
    }

    @Override
    public int getRefreshInterval(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        int hours = (int) Math.ceil(Integer.valueOf(view.property("com.openexchange.sms.userlimit.refreshInterval", String.class).get()) / 60d);
        return hours;
    }

}
