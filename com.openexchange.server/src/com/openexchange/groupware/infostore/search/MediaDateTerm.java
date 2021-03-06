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
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;


/**
 * {@link MediaDateTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.2
 */
public class MediaDateTerm extends AbstractDateSearchTerm {

    // private final TimeZone timezone;

    /**
     * Initializes a new {@link MediaDateTerm}.
     */
    public MediaDateTerm(ComparablePattern<Date> pattern/*, TimeZone timezone*/) {
        super(pattern);
        //this.timezone = timezone;
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
            col.add(Metadata.LAST_MODIFIED_LITERAL);
            col.add(Metadata.CAPTURE_DATE_LITERAL);
        }
    }

    @Override
    protected Date getDate(DocumentMetadata file) {
        Date captureDate = file.getCaptureDate();
        return null == captureDate ? file.getLastModified() : captureDate;
    }

}
