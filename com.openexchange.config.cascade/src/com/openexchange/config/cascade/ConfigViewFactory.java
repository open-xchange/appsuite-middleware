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

package com.openexchange.config.cascade;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ConfigViewFactory} - The factory to yield {@link ConfigView}s.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface ConfigViewFactory {

    /**
     * Gets a user-specific configuration view in following order:
     * <ol>
     * <li>USER; if not available, falls-back to:</li>
     * <li>CONTEXT; if not available, falls-back to:</li>
     * <li>RESELLER; if not available, falls-back to:</li>
     * <li>CONTEXT-SET; if not available, falls-back to:</li>
     * <li>SERVER</li>
     * </ol>
     *
     * @param user The user identifier
     * @param context The context identifier
     * @return The user-sensitive configuration view
     * @throws OXException If user-sensitive configuration view cannot be returned
     */
    ConfigView getView(int user, int context) throws OXException;

    /**
     * Gets a server/global configuration view.
     *
     * @return The configuration view
     * @throws OXException If configuration view cannot be returned
     */
    ConfigView getView() throws OXException;

    /**
     * The search path order.
     *
     * @return The search path order
     * @throws OXException If search path cannot be returned
     */
    String[] getSearchPath() throws OXException;
}
