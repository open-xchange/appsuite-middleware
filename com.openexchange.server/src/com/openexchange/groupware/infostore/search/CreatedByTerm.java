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

package com.openexchange.groupware.infostore.search;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;


/**
 * {@link CreatedByTerm}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class CreatedByTerm extends AbstractNumberSearchTerm {

    /**
     * Initializes a new {@link CreatedByTerm}.
     * @param pattern
     */
    public CreatedByTerm(ComparablePattern<Number> pattern) {
        super(pattern);
    }

    @Override
    public void visit(SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(Collection<Metadata> col) {
        if (null != col) {
            col.add(Metadata.CREATED_BY_LITERAL);
        }
    }

    @Override
    protected Number getNumber(DocumentMetadata file) {
        return I(file.getCreatedBy());
    }

    @Override
    protected boolean compareLongValues() {
        return false;
    }

}
