/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.find.internal;

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
        } catch (final RuntimeException e) {
            throw FindExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, Module module, ServerSession session) throws OXException {
        try {
            return requireDriver(session, module, new LookUpInfo(searchRequest, null)).search(searchRequest, session);
        } catch (final RuntimeException e) {
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
            throw FindExceptionCode.MISSING_DRIVER.create(module.getIdentifier(), session.getUserId(), session.getContextId());
        }
        return determined;
    }

}
