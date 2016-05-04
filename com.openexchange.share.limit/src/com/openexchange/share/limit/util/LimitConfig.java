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

package com.openexchange.share.limit.util;

import com.openexchange.config.ConfigurationService;
import com.openexchange.share.limit.internal.Services;

/**
 * 
 * {@link LimitConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class LimitConfig {

    public static final String SIZE_LIMIT = "com.openexchange.share.servlet.limit.size";
    public static final String COUNT_LIMIT = "com.openexchange.share.servlet.limit.count";
    public static final String TIME_FRAME = "com.openexchange.share.servlet.limit.timeFrame";

    private static volatile Integer countLimit;

    public static int countLimit() {
        Integer tmp = countLimit;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = countLimit;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 100;
                    }
                    tmp = Integer.valueOf(service.getProperty(COUNT_LIMIT, "100"));
                    countLimit = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Long sizeLimit;

    public static long sizeLimit() {
        Long tmp = sizeLimit;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = sizeLimit;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 1073741824; // 1GB
                    }
                    tmp = Long.valueOf(service.getProperty(SIZE_LIMIT, "1073741824"));
                    sizeLimit = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Integer timeFrame;

    public static int timeFrame() {
        Integer tmp = timeFrame;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = timeFrame;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 300000;
                    }
                    tmp = Integer.valueOf(service.getProperty(TIME_FRAME, "300000"));
                    timeFrame = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link LimitConfig}.
     */
    private LimitConfig() {
        super();
    }
}
