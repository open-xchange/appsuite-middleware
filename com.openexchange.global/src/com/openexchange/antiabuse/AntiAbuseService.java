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

package com.openexchange.antiabuse;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AntiAbuseService} - The service for anti-abuse checking and reporting.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@SingletonService
public interface AntiAbuseService {

    /**
     * Performs the <code>"allow"</code> request.
     *
     * @param parameters The parameters to use
     * @return The status response
     * @throws OXException If allow request fails
     */
    Status allow(AllowParameters parameters) throws OXException;

    /**
     * Performs the <code>"report"</code> request.
     *
     * @param parameters The parameters to use
     * @return The status response
     * @throws OXException If report request fails
     */
    void report(ReportParameters parameters) throws OXException;

}
