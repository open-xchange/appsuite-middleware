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
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;


/**
 * {@link CategoriesTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class CategoriesTerm extends AbstractStringSearchTerm {

    /**
     * Initializes a new {@link CategoriesTerm}.
     *
     * @param categories The categories string to look for
     */
    public CategoriesTerm(final String categories) {
        super(categories, true, true);
    }

    @Override
    public void visit(final SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(final Collection<Field> col) {
        if (null != col) {
            col.add(Field.CATEGORIES);
        }
    }

    @Override
    protected String getString(final File file) {
        return file.getCategories();
    }

}
