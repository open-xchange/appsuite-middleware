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

package com.openexchange.groupware.results;

import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.Customizer;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link CustomizableTimedResult}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CustomizableTimedResult<T> implements TimedResult<T> {

    private final TimedResult<T> result;
    private final Customizer<T> customizer;

    /**
     * Initializes a new {@link CustomizableTimedResult}.
     *
     * @param result The result to wrap
     * @param customizer The customizer to apply
     */
    public CustomizableTimedResult(TimedResult<T> result, Customizer<T> customizer) {
        super();
        this.result = result;
        this.customizer = customizer;
    }

    @Override
    public SearchIterator<T> results() throws OXException {
        return null == customizer ? result.results() : new CustomizableSearchIterator<T>(result.results(), customizer);
    }

    @Override
    public long sequenceNumber() throws OXException {
        return result.sequenceNumber();
    }

}
