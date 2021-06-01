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

package com.openexchange.sessionstorage.hazelcast.osgi;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageService;

/**
 * {@link SessionToucher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SessionToucher implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionToucher.class);

    private final HazelcastSessionStorageService sessionStorage;

    /**
     * Initializes a new {@link SessionToucher}.
     *
     * @param sessionStorage The session storage service
     */
    public SessionToucher(HazelcastSessionStorageService sessionStorage) {
        super();
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void run() {
        /*
         * get a list of currently active sessions
         */
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (false == SessiondServiceExtended.class.isInstance(sessiondService)) {
            LOG.warn("", ServiceExceptionCode.absentService(SessiondServiceExtended.class));
            return;
        }
        /*
         * touch them in the session storage
         */
        long start = System.currentTimeMillis();
        LOG.debug("About to touch all active sessions in storage...");
        List<String> sessionIDs = ((SessiondServiceExtended) sessiondService).getActiveSessionIDs();
        if (null != sessionIDs && 0 < sessionIDs.size()) {
            int touched = 0;
            try {
                touched = sessionStorage.touch(sessionIDs);
            } catch (OXException e) {
                LOG.error("Error touching active sessions in storage", e);
            }
            LOG.info("Detected {} active sessions in local containers, thereof touched {} sessions in distributed session storage ({}ms elapsed).",
                I(sessionIDs.size()), I(touched), L((System.currentTimeMillis() - start)));
        }
    }

    /**
     * Gets the required touch period for sessions in the distributed storage based on the configured session default lifetime.
     *
     * @param configService A reference to the configuration service
     * @return The touch period in milliseconds
     */
    public static int getTouchPeriod(ConfigurationService configService) {
        int defaultValue = 60 * 60 * 1000;
        int value;
        if (null == configService) {
            LOG.warn("Unable to determine \"com.openexchange.sessiond.sessionDefaultLifeTime\", falling back to {}.", I(defaultValue));
            value = defaultValue;
        } else {
            value = configService.getIntProperty("com.openexchange.sessiond.sessionDefaultLifeTime", defaultValue);
        }
        return value;
    }

}
