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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;

/**
 * The result of a {@link SearchRequest}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class SearchResult implements Serializable {

    /**
     * The empty search result.
     */
    public static final SearchResult EMPTY = new SearchResult(0, 0, Collections.<Document>emptyList(), Collections.<ActiveFacet>emptyList());

    private static final long serialVersionUID = -4937862789320521401L;

    private final int numFound;
    private final int start;
    private final List<Document> documents;
    private final List<ActiveFacet> facets;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link SearchResult}.
     *
     * @param numFound The total number of found documents.
     * @param start The start index within the set of total results
     * @param documents The resulting documents
     * @param facets The active facets
     */
    public SearchResult(int numFound, int start, List<Document> documents, List<ActiveFacet> facets) {
        this(numFound, start, documents, facets, null);
    }

    /**
     * Initializes a new {@link SearchResult}.
     *
     * @param numFound The total number of found documents.
     * @param start The start index within the set of total results
     * @param documents The resulting documents
     * @param facets The active facets
     * @param warnings The warnings to include
     */
    public SearchResult(int numFound, int start, List<Document> documents, List<ActiveFacet> facets, List<OXException> warnings) {
        super();
        this.numFound = numFound;
        this.start = start;
        this.documents = documents;
        this.facets = facets;
        this.warnings = warnings;
    }

    /**
     * The total number of found documents.
     * @return May be <code>-1</code> if unknown.
     */
    public int getNumFound() {
        return numFound;
    }

    /**
     * Used for pagination.
     * @return The start index within the set of total results.
     * Never negative.
     */
    public int getStart() {
        return start;
    }

    /**
     * Used for pagination.
     * @return The max. number of documents to return.
     * Never negative.
     */
    public int getSize() {
        return documents.size();
    }

    /**
     * The list of found documents.
     * @return May be empty but never <code>null</code>.
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * Gets the active facets that have been set on the according request.
     * @return May be empty but never <code>null</code>.
     */
    public List<ActiveFacet> getActiveFacets() {
        return facets;
    }

    /**
     * Gets a list of warnings that occurred during search.
     *
     * @return The warnings, or <code>null</code> if not set
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((documents == null) ? 0 : documents.hashCode());
        result = prime * result + ((facets == null) ? 0 : facets.hashCode());
        result = prime * result + numFound;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchResult other = (SearchResult) obj;
        if (documents == null) {
            if (other.documents != null) {
                return false;
            }
        } else if (!documents.equals(other.documents)) {
            return false;
        }
        if (facets == null) {
            if (other.facets != null) {
                return false;
            }
        } else if (!facets.equals(other.facets)) {
            return false;
        }
        if (numFound != other.numFound) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SearchResult [numFound=" + numFound + ", start=" + start + ", documents=" + documents + ", facets=" + facets + "]";
    }

}
