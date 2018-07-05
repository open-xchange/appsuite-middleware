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

package com.openexchange.chronos.provider.google.config;

import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarConfig {

    private static final Long REFRESH_INTERVAL = new Long(10);

    private static final Long RETRY_ON_ERROR_INTERVAL = new Long(1800);

    private static final Property REFRESH_INTERVAL_PROP = DefaultProperty.valueOf("com.openexchange.calendar.provider.google.refreshInterval", REFRESH_INTERVAL);

    private static final Property RETRY_ON_ERROR_INTERVAL_PROP = DefaultProperty.valueOf("com.openexchange.calendar.provider.google.retryOnErrorInterval", RETRY_ON_ERROR_INTERVAL);

    public static long getResfrehInterval(Session session) {

        LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
        if (service == null) {
            return REFRESH_INTERVAL.longValue();
        }
        return service.getLongProperty(session.getUserId(), session.getContextId(), REFRESH_INTERVAL_PROP);

    }

    public static long getRetryOnErrorInterval(Session session) {

        LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
        if (service == null) {
            return RETRY_ON_ERROR_INTERVAL.longValue();
        }
        return service.getLongProperty(session.getUserId(), session.getContextId(), RETRY_ON_ERROR_INTERVAL_PROP);

    }
}
