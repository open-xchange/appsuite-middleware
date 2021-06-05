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

package com.openexchange.ajax.infostore.actions;

import com.openexchange.ajax.framework.AbstractAllRequest;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;

/**
 * {@link AllInfostoreRequest}
 *
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class AllInfostoreRequest extends AbstractAllRequest<AbstractColumnsResponse> {

    public static final int GUI_SORT = Metadata.TITLE;

    public static final Order GUI_ORDER = Order.ASCENDING;

    public AllInfostoreRequest(final int folderId, final int[] columns, final int sort, final Order order) {
        super(AbstractInfostoreRequest.INFOSTORE_URL, folderId, columns, sort, order, true);
    }

    public AllInfostoreRequest(final int folderId, final int[] columns, final int sort, final Order order, boolean failOnError) {
        super(AbstractInfostoreRequest.INFOSTORE_URL, folderId, columns, sort, order, failOnError);
    }

    public AllInfostoreRequest(final String folderId, final int[] columns, final int sort, final Order order) {
        super(AbstractInfostoreRequest.INFOSTORE_URL, folderId, columns, sort, order, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllInfostoreParser getParser() {
        return new AllInfostoreParser(isFailOnError(), getColumns());
    }

}
