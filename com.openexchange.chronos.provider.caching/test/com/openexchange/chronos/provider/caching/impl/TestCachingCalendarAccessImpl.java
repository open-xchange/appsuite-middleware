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

package com.openexchange.chronos.provider.caching.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TestCachingCalendarAccessImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class TestCachingCalendarAccessImpl extends BasicCachingCalendarAccess {

    private boolean configSaved = false;

    public TestCachingCalendarAccessImpl(Session session, CalendarAccount account, CalendarParameters parameters) {
        super(session, account, parameters);
    }

    boolean cacheUpdated = false;

    @Override
    protected void update() {
        cacheUpdated = true;
    }

    public boolean getCacheUpdated() {
        return cacheUpdated;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public long getRefreshInterval() {
        return 60;
    }

    @Override
    public ExternalCalendarResult getAllEvents() {
        return new ExternalCalendarResult(false, Collections.emptyList());
    }

    public boolean isConfigSaved() {
        return configSaved;
    }

    @Override
    public long getRetryAfterErrorInterval(OXException e) {
        return 1;
    }

    @Override
    public CalendarSettings getSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OXException> getWarnings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean acquireUpdateLock() {
        return true;
    }
}
