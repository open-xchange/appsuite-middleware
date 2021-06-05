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

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

/**
 * {@link NumberOfVersionsTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NumberOfVersionsTerm implements SearchTerm<ComparablePattern<Number>> {

    private final ComparablePattern<Number> pattern;

    /**
     * Initializes a new {@link NumberOfVersionsTerm}.
     */
    public NumberOfVersionsTerm(final ComparablePattern<Number> pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public ComparablePattern<Number> getPattern() {
        return pattern;
    }

    @Override
    public void visit(final SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(final Collection<Metadata> col) {
        if (null != col) {
            col.add(Metadata.NUMBER_OF_VERSIONS_LITERAL);
        }
    }

    @Override
    public boolean matches(final DocumentMetadata file) throws OXException {
        final int numberOfVersions = file.getNumberOfVersions();
        switch (pattern.getComparisonType()) {
        case EQUALS:
            return numberOfVersions == pattern.getPattern().intValue();
        case LESS_THAN:
            return numberOfVersions < pattern.getPattern().intValue();
        case GREATER_THAN:
            return numberOfVersions > pattern.getPattern().intValue();
        default:
            return false;
        }
    }

}
