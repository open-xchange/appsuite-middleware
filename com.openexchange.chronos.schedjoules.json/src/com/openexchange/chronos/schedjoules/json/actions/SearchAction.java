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

package com.openexchange.chronos.schedjoules.json.actions;

import java.util.Collections;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.SchedJoulesPageField;
import com.openexchange.chronos.schedjoules.json.actions.parameter.SchedJoulesSearchParameter;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SearchAction extends AbstractSchedJoulesAction implements AJAXActionService {

    /**
     * Initialises a new {@link SearchAction}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public SearchAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // Get the mandatory 'query' parameter
        String query = requestData.nonEmptyParameter(SchedJoulesSearchParameter.QUERY);
        // Get optional 'maxRows', 'language', 'countryId' and 'categoryId' parameters
        int maxRows = requestData.getIntParameter(SchedJoulesSearchParameter.MAX_ROWS);
        String locale = getLanguage(requestData, session);
        int countryId = requestData.getIntParameter(SchedJoulesSearchParameter.COUNTRY_ID);
        int categoryId = requestData.getIntParameter(SchedJoulesSearchParameter.CATEGORY_ID);

        // Execute
        SchedJoulesService service = services.getService(SchedJoulesService.class);
        return new AJAXRequestResult(service.search(session.getContextId(), query, locale, countryId, categoryId, maxRows, Collections.singleton(SchedJoulesPageField.URL)).getData());
    }
}
