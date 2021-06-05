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

package com.openexchange.ajax.contact.action;

import java.util.Date;
import com.openexchange.ajax.framework.AbstractUpdatesRequest;
import com.openexchange.groupware.search.Order;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class UpdatesRequest extends AbstractUpdatesRequest<ContactUpdatesResponse> {

    private final int[] columns;

    /**
     * @param folderId
     * @param columns
     * @param sort
     * @param order
     * @param lastModified
     */
    public UpdatesRequest(final int folderId, final int[] columns, final int sort, final Order order, final Date lastModified) {
        this(folderId, columns, sort, order, lastModified, Ignore.DELETED);
    }

    public UpdatesRequest(final int folderId, final int[] columns, final int sort, final Order order, final Date lastModified, final Ignore ignore) {
        super(AbstractContactRequest.URL, folderId, columns, sort, order, lastModified, ignore, true);
        this.columns = columns;
    }

    @Override
    public ContactUpdatesParser getParser() {
        return new ContactUpdatesParser(true, columns);
    }
}
