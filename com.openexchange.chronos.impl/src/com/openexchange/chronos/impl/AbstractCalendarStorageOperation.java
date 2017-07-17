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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.LegacyCalendarStorageFactory;
import com.openexchange.chronos.storage.ReplayingCalendarStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractCalendarStorageOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
abstract class AbstractCalendarStorageOperation<T> extends AbstractStorageOperation<CalendarStorage, T> {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCalendarStorageOperation.class);

    /** The default maximum number of retry attempts */
    private static final int DEFAULT_RETRIES = 3;

    /** The base number of milliseconds to wait until retrying */
    private static final int RETRY_BASE_DELAY = 500;

    /** The random number generator */
    private static final Random RANDOM = new Random();

    private final int maxRetries;
    private int retryCount;

    /**
     * Initializes a new {@link AbstractCalendarStorageOperation}.
     *
     * @param session The calendar session
     */
    public AbstractCalendarStorageOperation(CalendarSession session) throws OXException {
        this(session, DEFAULT_RETRIES);
    }

    /**
     * Initializes a new {@link AbstractCalendarStorageOperation}.
     *
     * @param session The calendar session
     * @param maxRetries The maximum number of retry attempts when encountering recoverable storage errors, or <code>0</code> for no retries
     */
    public AbstractCalendarStorageOperation(CalendarSession session, int maxRetries) throws OXException {
        super(session);
        this.maxRetries = maxRetries;
    }

    @Override
    protected CalendarStorage initStorage(DBProvider dbProvider) throws OXException {
        if (session.getConfig().isReplayToLegacyStorage()) {
            return Services.getService(ReplayingCalendarStorageFactory.class).create(context, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        } else if (session.getConfig().isUseLegacyStorage()) {
            return Services.getService(LegacyCalendarStorageFactory.class).create(context, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        } else {
            return Services.getService(CalendarStorageFactory.class).create(context, 0, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        }
    }

    @Override
    public T executeUpdate() throws OXException {
        while (true) {
            try {
                return super.executeUpdate();
            } catch (OXException e) {
                if (tryAgain(e)) {
                    continue;
                }
                throw e;
            }
        }
    }

    /**
     * Checks if the operation may be tried again in case of recoverable errors, based on the excecption's category and the current retry
     * count. In case it's worth to try again, the thread is sent to sleep for a certain timespan to mimic some kind of exponential
     * backoff before trying again.
     *
     * @param e The encountered exception
     * @return <code>true</code> if the operation may be tried again, <code>false</code>, otherwise
     */
    private boolean tryAgain(OXException e) {
        if (retryCount > maxRetries || false == mayTryAgain(e)) {
            return false;
        }
        retryCount++;
        int delay = RETRY_BASE_DELAY * retryCount + RANDOM.nextInt(RETRY_BASE_DELAY);
        LOG.info("Error performing storage operation (\"{}\"), trying again in {}ms ({}/{})...", e.getMessage(), I(delay), I(retryCount), I(maxRetries));
        LockSupport.parkNanos(delay * 1000000L);
        return true;
    }

    /**
     * Gets a value indicating whether the storage operation may be tried again for an occurred exception or not.
     *
     * @param e The exception to check
     * @return <code>true</code> if the operation may be tried again, <code>false</code>, otherwise
     */
    protected boolean mayTryAgain(OXException e) {
        if (null == e) {
            return false;
        }
        return Category.CATEGORY_TRY_AGAIN.equals(e.getCategory());
    }

}
