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

package com.openexchange.file.storage.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;


/**
 * {@link OrTerm}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class OrTerm implements SearchTerm<List<SearchTerm<?>>> {

    private List<SearchTerm<?>> terms;

    /**
     * Initializes a new {@link OrTerm}.
     */
    public OrTerm(List<SearchTerm<?>> terms) {
        super();
        if (null != terms) {
            this.terms = terms;
        } else {
            this.terms = Collections.emptyList();
        }
    }

    @Override
    public List<SearchTerm<?>> getPattern() {
        return terms;
    }

    @Override
    public void visit(SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(Collection<Field> col) {
        for (SearchTerm<?> term : terms) {
            term.addField(col);
        }
    }

    @Override
    public boolean matches(File file) throws OXException {
        if (terms.isEmpty()) {
            return true;
        }
        boolean matches = terms.get(0).matches(file);
        for (int i = 1; !matches && i < terms.size(); i++) {
            matches |= terms.get(i).matches(file);
        }
        return matches;
    }

}
