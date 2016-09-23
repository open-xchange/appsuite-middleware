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

package com.openexchange.find.spi;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.tools.session.ServerSession;

/**
 * A {@link ModuleSearchDriver} has to be implemented for every module that enables searching via the find API.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public interface ModuleSearchDriver {

    /** The superior ranking that must not be exceeded by a registered <code>ModuleSearchDriver</code> */
    public static final int RANKING_SUPERIOR = 100000;

    /**
     * Gets the module supported by this driver.
     *
     * @return The module supported by this driver.
     */
    Module getModule();

    /**
     * Checks if this driver applies to a given {@link ServerSession}.
     *
     * @param session The associated session
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session) throws OXException;

    /**
     * Checks if this driver applies to a given {@link ServerSession} and concrete find request.
     *
     * @param session The associated session
     * @param findRequest The current find request
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException;

    /**
     * Checks if this driver applies to a given {@link ServerSession} and concrete find request.
     *
     * @param session The associated session
     * @param facetInfos The current facet information
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session, List<FacetInfo> facetInfos) throws OXException;

    /**
     * Gets the driver-specific {@link SearchConfiguration}. May be individual for the
     * given session.
     *
     * @param session The associated session
     * @return The configuration; never <code>null</code>.
     */
    SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException;

    /**
     * Performs an auto-complete request.
     *
     * @param autocompleteRequest The associated request
     * @param session The associated session
     * @return The {@link AutocompleteResult}. Never <code>null</code>.
     */
    AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;

    /**
     * Performs a search request.
     *
     * @param searchRequest The associated request
     * @param session The associated session
     * @return The {@link SearchResult}. Never <code>null</code>.
     */
    SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException;

}
