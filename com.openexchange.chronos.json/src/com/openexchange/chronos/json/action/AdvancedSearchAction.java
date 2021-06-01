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

package com.openexchange.chronos.json.action;

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.json.converter.EventsPerFolderResultConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AdvancedSearchAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.READ)
public class AdvancedSearchAction extends ChronosAction {

    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, PARAM_RANGE_END);
    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_EXPAND, PARAM_ORDER_BY, PARAM_ORDER, PARAM_FIELDS);

    /**
     * Initialises a new {@link AdvancedSearchAction}.
     * 
     * @param services A service lookup reference
     */
    public AdvancedSearchAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        return new AJAXRequestResult(calendarAccess.searchEvents(getFolderIds(requestData), getSearchTerm(requestData)), EventsPerFolderResultConverter.INPUT_FORMAT);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }
}
