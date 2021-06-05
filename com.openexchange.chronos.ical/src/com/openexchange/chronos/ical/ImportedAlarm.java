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

package com.openexchange.chronos.ical;

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.DelegatingAlarm;
import com.openexchange.exception.OXException;

/**
 * {@link ImportedAlarm}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ImportedAlarm extends DelegatingAlarm implements ImportedComponent {

    private final int index;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link ImportedAlarm}.
     *
     * @param index The component's index in the parent iCalendar structure.
     * @param alarm The imported alarm object
     * @param warnings A list of parser- and conversion warnings.
     */
    public ImportedAlarm(int index, Alarm alarm, List<OXException> warnings) {
        super(alarm);
        this.warnings = warnings;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

}
