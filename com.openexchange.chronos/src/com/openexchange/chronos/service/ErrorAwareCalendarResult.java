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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ErrorAwareCalendarResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface ErrorAwareCalendarResult extends CalendarResult {

    /**
     * Gets the (fatal) error that prevented the operation from being completed successfully, or <code>null</code> if there is none
     *
     * @return The (fatal) error that prevented the operation from being completed successfully, or <code>null</code> if there is none
     */
    OXException getError();

    /**
     * Gets the list of (non-fatal) warnings that occurred during processing, or an empty list if there are none
     *
     * @return The list of (non-fatal) warnings that occurred during processing, or an empty list if there are none
     */
    List<OXException> getWarnings();

}
