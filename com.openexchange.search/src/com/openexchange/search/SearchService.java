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

package com.openexchange.search;

import java.util.Collection;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link SearchService} - The search service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface SearchService {

    /**
     * Tests if specified candidate satisfies given search term.
     *
     * @param candidate The candidate to check
     * @param searchTerm The search term to satisfy
     * @param attributeFetcher The attribute fetcher for specified candidate
     * @return <code>true</code> if specified candidate satisfies given search term; otherwise <code>false</code>.
     */
    <C> boolean matches(C candidate, SearchTerm<?> searchTerm, SearchAttributeFetcher<C> attributeFetcher);

    /**
     * Filters specified candidates by given search term.
     *
     * @param candidates The candidates to check
     * @param searchTerm The search term to used as filter
     * @param attributeFetcher The attribute fetcher for specified candidates
     * @return The filtered candidates which satisfy given search term.
     */
    <C> Collection<C> filter(Collection<C> candidates, SearchTerm<?> searchTerm, SearchAttributeFetcher<C> attributeFetcher);

}
