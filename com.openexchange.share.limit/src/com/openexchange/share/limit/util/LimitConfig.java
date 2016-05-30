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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.share.limit.internal.Services;

/**
 * 
 * {@link LimitConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class LimitConfig implements Reloadable {

    public static final String LIMIT_ENABLED = "com.openexchange.share.servlet.limit.enabled";

    public static final String SIZE_LIMIT_GUESTS = "com.openexchange.share.servlet.limit.size.guests";
    public static final String COUNT_LIMIT_GUESTS = "com.openexchange.share.servlet.limit.count.guests";
    public static final String TIME_FRAME_GUESTS = "com.openexchange.share.servlet.limit.timeFrame.guests";

    public static final String SIZE_LIMIT_LINKS = "com.openexchange.share.servlet.limit.size.links";
    public static final String COUNT_LIMIT_LINKS = "com.openexchange.share.servlet.limit.count.links";
    public static final String TIME_FRAME_LINKS = "com.openexchange.share.servlet.limit.timeFrame.links";

    private static final LimitConfig INSTANCE = new LimitConfig();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static LimitConfig getInstance() {
        return INSTANCE;
    }

    private volatile Integer countLimitGuests;

    public int countLimitGuests() {
        Integer tmp = this.countLimitGuests;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.countLimitGuests;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 100;
                    }
                    tmp = Integer.valueOf(service.getProperty(COUNT_LIMIT_GUESTS, "100"));
                    this.countLimitGuests = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private volatile Integer countLimitLinks;

    public int countLimitLinks() {
        Integer tmp = this.countLimitLinks;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.countLimitLinks;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 100;
                    }
                    tmp = Integer.valueOf(service.getProperty(COUNT_LIMIT_LINKS, "100"));
                    this.countLimitLinks = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private volatile Long sizeLimitGuests;

    public long sizeLimitGuests() {
        Long tmp = this.sizeLimitGuests;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.sizeLimitGuests;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 1073741824L; // 1GB
                    }
                    tmp = Long.valueOf(service.getProperty(SIZE_LIMIT_GUESTS, "1073741824"));
                    this.sizeLimitGuests = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private volatile Long sizeLimitLinks;

    public long sizeLimitLinks() {
        Long tmp = this.sizeLimitLinks;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.sizeLimitLinks;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 1073741824L; // 1GB
                    }
                    tmp = Long.valueOf(service.getProperty(SIZE_LIMIT_LINKS, "1073741824"));
                    this.sizeLimitLinks = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private volatile Integer timeFrameGuests;

    public int timeFrameGuests() {
        Integer tmp = this.timeFrameGuests;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.timeFrameGuests;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 0;
                    }
                    tmp = Integer.valueOf(service.getProperty(TIME_FRAME_GUESTS, "3600000"));
                    this.timeFrameGuests = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private volatile Integer timeFrameLinks;

    public int timeFrameLinks() {
        Integer tmp = this.timeFrameLinks;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.timeFrameLinks;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 0;
                    }
                    tmp = Integer.valueOf(service.getProperty(TIME_FRAME_LINKS, "3600000"));
                    this.timeFrameLinks = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private volatile Boolean enabled;

    public boolean isEnabled() {
        Boolean tmp = this.enabled;
        if (null == tmp) {
            synchronized (LimitConfig.class) {
                tmp = this.enabled;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return false;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty(LIMIT_ENABLED, false));
                    this.enabled = tmp.booleanValue();
                }
            }
        }
        return tmp.booleanValue();
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link LimitConfig}.
     */
    private LimitConfig() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        this.enabled = null;
        this.countLimitGuests = null;
        this.countLimitLinks = null;
        this.sizeLimitGuests = null;
        this.sizeLimitLinks = null;
        this.timeFrameGuests = null;
        this.timeFrameLinks = null;
    }

    private static final String CONFIGFILE = "share.properties";

    private static final String[] PROPERTIES = new String[] { 
        "com.openexchange.share.servlet.limit.enabled",
        "com.openexchange.share.servlet.limit.timeFrame.guests",
        "com.openexchange.share.servlet.limit.timeFrame.links",
        "com.openexchange.share.servlet.limit.size.guests",
        "com.openexchange.share.servlet.limit.size.links",
        "com.openexchange.share.servlet.limit.count.guests",
        "com.openexchange.share.servlet.limit.count.links"
    };

    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put(CONFIGFILE, PROPERTIES);
        return map;
    }
}
