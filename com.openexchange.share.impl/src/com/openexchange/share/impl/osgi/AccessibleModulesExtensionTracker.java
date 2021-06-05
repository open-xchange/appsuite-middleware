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

package com.openexchange.share.impl.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.share.groupware.spi.AccessibleModulesExtension;


/**
 * {@link AccessibleModulesExtensionTracker}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since 7.8.4
 */
public class AccessibleModulesExtensionTracker extends RankingAwareNearRegistryServiceTracker<AccessibleModulesExtension> {

    /**
     * Initializes a new {@link AccessibleModulesExtensionTracker}.
     *
     * @param context The OSGi bundle execution context
     */
    AccessibleModulesExtensionTracker(BundleContext context) {
        super(context, AccessibleModulesExtension.class, 0);
    }
}
