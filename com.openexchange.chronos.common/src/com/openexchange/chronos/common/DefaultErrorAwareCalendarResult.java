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

package com.openexchange.chronos.common;

import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.ErrorAwareCalendarResult;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultErrorAwareCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultErrorAwareCalendarResult extends DefaultCalendarResult implements ErrorAwareCalendarResult {

    private final OXException error;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link DefaultErrorAwareCalendarResult}.
     *
     * @param result The underlying calendar result
     * @param warnings The list of (non-fatal) warnings that occurred during processing, or an empty list if there are none
     * @param error The (fatal) error that prevented the operation from being completed successfully, or null if there is none
     */
    public DefaultErrorAwareCalendarResult(CalendarResult result, List<OXException> warnings, OXException error) {
        super(result.getSession(), result.getCalendarUser(), result.getFolderID(), result.getCreations(), result.getUpdates(), result.getDeletions());
        this.warnings = warnings;
        this.error = error;
    }

    @Override
    public OXException getError() {
        return error;
    }


    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

}
