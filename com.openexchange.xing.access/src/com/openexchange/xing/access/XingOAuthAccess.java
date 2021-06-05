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

package com.openexchange.xing.access;

import com.openexchange.exception.OXException;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.session.WebAuthSession;


/**
 * {@link XingOAuthAccess} - A XING OAuth access obtained by {@link XingOAuthAccessProvider}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface XingOAuthAccess {

    /**
     * Gets the XING API reference.
     *
     * @return The XING API reference
     */
    XingAPI<WebAuthSession> getXingAPI() throws OXException;

    /**
     * Disposes this XING OAuth access.
     */
    void dispose();

    /**
     * Gets the XING user identifier.
     *
     * @return The XING user identifier
     */
    String getXingUserId();

    /**
     * Gets the XING user's display name.
     *
     * @return The XING user's display name.
     */
    String getXingUserName();

}
