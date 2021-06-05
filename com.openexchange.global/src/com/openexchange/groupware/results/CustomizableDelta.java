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
 * {@link CustomizableDelta}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CustomizableDelta<T> implements Delta<T>{
    private final Delta<T> delta;
    private final Customizer<T> customizer;

    public CustomizableDelta(Delta<T> delta, Customizer<T> customizer) {
        super();
        this.delta = delta;
        this.customizer = customizer;
    }

    @Override
    public SearchIterator<T> getDeleted() {
        return new CustomizableSearchIterator<T>(delta.getDeleted(), customizer);
    }

    @Override
    public SearchIterator<T> getModified() {
        return new CustomizableSearchIterator<T>(delta.getModified(), customizer);
    }

    @Override
    public SearchIterator<T> getNew() {
        return new CustomizableSearchIterator<T>(delta.getNew(), customizer);
    }

    @Override
    public SearchIterator<T> results() throws OXException {
        return new CustomizableSearchIterator<T>(delta.results(), customizer);
    }

    @Override
    public long sequenceNumber() throws OXException {
        return delta.sequenceNumber();
    }


}
