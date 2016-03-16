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

package com.openexchange.find;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetType;


/**
 * An abstract class for find requests that support active facets and options.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractFindRequest implements Serializable {

    private static final long serialVersionUID = -3332517681701235711L;

    private final List<ActiveFacet> activeFacets;

    private final RequestOptions options;

    protected final Map<FacetType, List<ActiveFacet>> facetMap;

    /**
     * Initializes a new {@link AbstractFindRequest}.
     *
     * @param activeFacets The list of currently active facets; must not be <code>null</code>
     * @param optionMap A map containing client and module specific options; must not be <code>null</code>
     */
    protected AbstractFindRequest(final List<ActiveFacet> activeFacets, final Map<String, String> optionMap) {
        super();
        this.activeFacets = activeFacets;
        this.options = new RequestOptions(optionMap);
        facetMap = new HashMap<FacetType, List<ActiveFacet>>(activeFacets.size());
        for (ActiveFacet facet : activeFacets) {
            FacetType type = facet.getType();
            List<ActiveFacet> facetList = facetMap.get(type);
            if (facetList == null) {
                facetList = new LinkedList<ActiveFacet>();
                facetMap.put(type, facetList);
            }

            facetList.add(facet);
        }
    }

    /**
     * Gets a list of active facets that are currently set.
     *
     * @return The list of facets. May be empty but never <code>null</code>.
     */
    public List<ActiveFacet> getActiveFacets() {
        return activeFacets;
    }

    /**
     * Gets options that can be set by clients to set optional properties.
     *
     * @return The {@link RequestOptions}; never <code>null</code>.
     */
    public RequestOptions getOptions() {
        return options;
    }

    /**
     * Gets the active facets for the given type.
     * @return The facets or <code>null</code> if not present.
     */
    public List<ActiveFacet> getActiveFacets(FacetType type) {
        List<ActiveFacet> facets = facetMap.get(type);
        if (facets == null) {
            return null;
        }

        return Collections.unmodifiableList(facets);
    }

    /**
     * Gets the folder type set via a present facet of type {@link CommonFacetType#FOLDER_TYPE}.
     * @return The folder type as specified in {@link FolderType} or <code>null</code>.
     */
    public FolderType getFolderType() throws OXException {
        List<ActiveFacet> facets = facetMap.get(CommonFacetType.FOLDER_TYPE);
        if (facets == null || facets.isEmpty()) {
            return null;
        }

        String identifier = facets.get(0).getValueId();
        FolderType type = FolderType.getByIdentifier(identifier);
        if (type == null) {
            throw FindExceptionCode.INVALID_FOLDER_TYPE.create(identifier == null ? "null" : identifier);
        }

        return type;
    }

    /**
     * Gets the folder id set via a present facet of type {@link CommonFacetType#FOLDER}.
     * @return The folder id or <code>null</code> if folder facet is not present.
     */
    public String getFolderId() {
        List<ActiveFacet> facets = facetMap.get(CommonFacetType.FOLDER);
        if (facets == null || facets.isEmpty()) {
            return null;
        }

        return facets.get(0).getValueId();
    }

    public String getAccountId() {
        List<ActiveFacet> facets = facetMap.get(CommonFacetType.ACCOUNT);
        if (facets == null || facets.isEmpty()) {
            return null;
        }

        return facets.get(0).getValueId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activeFacets == null) ? 0 : activeFacets.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractFindRequest other = (AbstractFindRequest) obj;
        if (activeFacets == null) {
            if (other.activeFacets != null)
                return false;
        } else if (!activeFacets.equals(other.activeFacets))
            return false;
        if (options == null) {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        return true;
    }

}
