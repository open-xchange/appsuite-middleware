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

package com.openexchange.find.json.actions;

import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.SearchService;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.json.FindRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AutocompleteAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class AutocompleteAction extends AbstractFindAction {

    /**
     * Initializes a new {@link AutocompleteAction}.
     *
     * @param services The service look-up
     */
    public AutocompleteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(FindRequest request) throws OXException {
        SearchService searchService = getSearchService();
        String prefix = request.requirePrefix();
        int limit = request.getIntParameter("limit");
        List<ActiveFacet> activeFacets = request.getActiveFacets();
        Map<String, String> options = request.getOptions();

        // Do the auto-complete
        AutocompleteResult result = searchService.autocomplete(
            new AutocompleteRequest(prefix, activeFacets, options, limit > 0 ? limit : 0),
            request.requireModule(),
            request.getServerSession());
        return new AJAXRequestResult(result, AutocompleteResult.class.getName());
    }

}
