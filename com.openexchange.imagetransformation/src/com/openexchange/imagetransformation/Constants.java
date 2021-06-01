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

package com.openexchange.imagetransformation;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.imagetransformation.osgi.Services;


/**
 * {@link Constants} - Provides constants for <i>com.openexchange.imagetransformation</i> package.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class Constants {

    /**
     * Initializes a new {@link Constants}.
     */
    private Constants() {
        super();
    }

    static {
        ImageTransformationReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                maxHeight = null;
                maxWidth = null;
            }

            @Override
            public Interests getInterests() {
                return DefaultInterests.builder().propertiesOfInterest("com.openexchange.tools.images.maxHeight", "com.openexchange.tools.images.maxWidth").build();
            }
        });
    }

    private static volatile Integer maxHeight;

    /**
     * The max. allowed height for image transformation.
     */
    public static int getMaxHeight() {
        Integer tmp = maxHeight;
        if (null == tmp) {
            synchronized (Constants.class) {
                tmp = maxHeight;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    int defaultMaxHeight = 4096;
                    if (null == service) {
                        return defaultMaxHeight;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.tools.images.maxHeight", defaultMaxHeight));
                    maxHeight = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer maxWidth;

    /**
     * The max. allowed width for image transformation.
     */
    public static int getMaxWidth() {
        Integer tmp = maxWidth;
        if (null == tmp) {
            synchronized (Constants.class) {
                tmp = maxWidth;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    int defaultMaxWidth = 4096;
                    if (null == service) {
                        return defaultMaxWidth;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.tools.images.maxWidth", defaultMaxWidth));
                    maxWidth = tmp;
                }
            }
        }
        return tmp.intValue();
    }

}
