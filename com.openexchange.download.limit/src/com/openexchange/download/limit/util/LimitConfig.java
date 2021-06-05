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

package com.openexchange.download.limit.util;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.download.limit.internal.Services;

/**
 *
 * {@link LimitConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class LimitConfig implements Reloadable {

    public static final String LIMIT_ENABLED = "com.openexchange.download.limit.enabled";

    public static final String SIZE_LIMIT_GUESTS = "com.openexchange.download.limit.size.guests";
    public static final String COUNT_LIMIT_GUESTS = "com.openexchange.download.limit.count.guests";
    public static final String TIME_FRAME_GUESTS = "com.openexchange.download.limit.timeFrame.guests";

    public static final String SIZE_LIMIT_LINKS = "com.openexchange.download.limit.size.links";
    public static final String COUNT_LIMIT_LINKS = "com.openexchange.download.limit.count.links";
    public static final String TIME_FRAME_LINKS = "com.openexchange.download.limit.timeFrame.links";

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
                    this.enabled = tmp;
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

    private static final String[] PROPERTIES = new String[] {
        "com.openexchange.download.limit.enabled",
        "com.openexchange.download.limit.timeFrame.guests",
        "com.openexchange.download.limit.timeFrame.links",
        "com.openexchange.download.limit.size.guests",
        "com.openexchange.download.limit.size.links",
        "com.openexchange.download.limit.count.guests",
        "com.openexchange.download.limit.count.links"
    };

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(PROPERTIES).build();
    }
}
