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

package com.openexchange.snippet;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link QuotaAwareSnippetService} - Extends {@link SnippetService} by methods to retrieves a list of file references as well as whether snippet files should be ignored for usage calculation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface QuotaAwareSnippetService extends SnippetService {

    /**
     * Retrieves a list of file references which should be ignored during usage calculation.
     *
     * @param contextId The context identifier
     * @return A list of file references
     * @throws OXException If file references cannot be returned
     */
    List<String> getFilesToIgnore(int contextId) throws OXException;

    /**
     * Checks whether snippet files should be ignored for usage calculation.
     * <p>
     * See {@link #getFilesToIgnore(Integer)} for more informations.
     *
     * @return <code>true</code> if snippet files should be ignore, <code>false</false> otherwise.
     */
    boolean ignoreQuota();

}
