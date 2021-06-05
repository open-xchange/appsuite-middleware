/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.sms.tools.internal;

import static com.openexchange.java.Autoboxing.I;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
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

    private final IMap<String, SMSBucket> map;
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
            refreshInterval = Integer.parseInt(view.get(SMSConstants.SMS_USER_LIMIT_REFRESH_INTERVAL, String.class));
        } catch (NumberFormatException e) {
            throw new OXException(e);
        }

        for (;;) {
            SMSBucket oldBucket = map.get(userIdentifier);
            SMSBucket newBucket = oldBucket.clone();
            int amount = newBucket.removeToken(refreshInterval);

            if (amount == -1) {
                int hours = (int) Math.ceil(refreshInterval / 60d);
                throw SMSBucketExceptionCodes.SMS_LIMIT_REACHED.create(I(hours));
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
        return Integer.parseInt(view.get(SMSConstants.SMS_USER_LIMIT_PROPERTY, String.class));
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        ConfigViewFactory configFactory = Services.getService(ConfigViewFactory.class);
        if (configFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view = configFactory.getView(session.getUserId(), session.getContextId());
        return view.get(SMSConstants.SMS_USER_LIMIT_ENABLED, boolean.class).booleanValue();
    }

    @Override
    public int getRefreshInterval(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        int hours = (int) Math.ceil(Double.parseDouble(view.property("com.openexchange.sms.userlimit.refreshInterval", String.class).get()) / 60d);
        return hours;
    }

}
