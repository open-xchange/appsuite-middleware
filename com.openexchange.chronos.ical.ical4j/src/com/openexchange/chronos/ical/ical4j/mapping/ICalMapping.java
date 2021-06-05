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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.List;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;

/**
 * {@link ICalMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface ICalMapping<T extends Component, U> {

    /**
     * Exports the mapped contact attributes into the supplied vCard.
     *
     * @param object The object to export
     * @param component The target iCal component
     * @param parameters Further options to use
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     */
    void export(U object, T component, ICalParameters parameters, List<OXException> warnings);

    /**
     * Imports the mapped vCard properties into the supplied contact
     *
     * @param component The iCal component to import
     * @param object The target object
     * @param parameters Further options to use
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     */
    void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings);

}
