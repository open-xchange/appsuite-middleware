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

package com.openexchange.find.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.SearchService;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.tools.session.ServerSession;


/**
 * The implementation of the {@link SearchService} interface.
 * Collects all {@link ModuleSearchDriver} implementations and
 * chooses an appropriate one on every request for a given module
 * and session.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SearchServiceImpl implements SearchService {

    private final SearchDriverManager driverManager;

    public SearchServiceImpl(final SearchDriverManager driverManager) {
        super();
        this.driverManager = driverManager;
    }

    @Override
    public AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, Module module, ServerSession session) throws OXException {
        try {
            return requireDriver(session, module, new LookUpInfo(autocompleteRequest, null)).autocomplete(autocompleteRequest, session);
        } catch (RuntimeException e) {
            throw FindExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, Module module, ServerSession session) throws OXException {
        try {
            return requireDriver(session, module, new LookUpInfo(searchRequest, null)).search(searchRequest, session);
        } catch (RuntimeException e) {
            throw FindExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public ModuleSearchDriver getDriver(List<FacetInfo> facetInfos, Module module, ServerSession session) throws OXException {
        return requireDriver(session, module, new LookUpInfo(null, facetInfos));
    }

    private ModuleSearchDriver requireDriver(ServerSession session, Module module, LookUpInfo lookUpInfo) throws OXException {
        ModuleSearchDriver determined = driverManager.determineDriver(session, module, lookUpInfo, true);
        if (determined == null) {
            throw FindExceptionCode.MISSING_DRIVER.create(module.getIdentifier(), I(session.getUserId()), I(session.getContextId()));
        }
        return determined;
    }

}
