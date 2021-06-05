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

package com.openexchange.capabilities;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * A {@link CapabilityChecker} can check {@link CapabilityService#declareCapability(String) previously declared} capabilities.
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
 * <b>Note</b>: It does only apply to declared ones!
 * </div>
 * <p>
 * OSGi-wise registration of a {@code CapabilityChecker} may be accompanied by service properties providing the <code>"capabilities"</code>
 * property that specifies to what capabilities that check applies.<br>
 * See <code>org.osgi.framework.BundleContext.registerService(Class,&nbsp;S,&nbsp;<b>Dictionary&lt;String,&nbsp;?&gt;</b>)</code>.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CapabilityChecker {

    /** The name for optional <code>"capabilities"</code> property */
    public static final String PROPERTY_CAPABILITIES = "capabilities";

    /**
     * Check whether the capability should be awarded for a certain user
     *
     * @param capability The capability to check
     * @param session Provides the users session for which to check
     * @return Whether to award this capability or not
     * @throws OXException If check fails
     */
    boolean isEnabled(String capability, Session session) throws OXException;
}
