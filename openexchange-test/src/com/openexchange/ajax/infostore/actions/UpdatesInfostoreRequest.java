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

import java.util.Date;
import com.openexchange.ajax.framework.AbstractUpdatesRequest;
import com.openexchange.groupware.search.Order;

/**
 * {@link UpdatesInfostoreRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdatesInfostoreRequest extends AbstractUpdatesRequest<UpdatesInfostoreResponse> {

    public UpdatesInfostoreRequest(int folderId, int[] columns, int sort, Order order) {
        this(folderId, columns, sort, order, Ignore.NONE, new Date(), true);
    }

    public UpdatesInfostoreRequest(int folderId, int[] columns, int sort, Order order, Ignore ignore) {
        this(folderId, columns, sort, order, ignore, new Date(), true);
    }

    public UpdatesInfostoreRequest(int folderId, int[] columns, int sort, Order order, Ignore ignore, boolean failOnError) {
        this(folderId, columns, sort, order, ignore, new Date(), failOnError);
    }

    public UpdatesInfostoreRequest(int folderId, int[] columns, int sort, Order order, Ignore ignore, Date lastModified, boolean failOnError) {
        super("/ajax/infostore", folderId, columns, sort, order, lastModified, ignore, failOnError);
    }

    @Override
    public UpdatesInfostoreParser getParser() {
        return new UpdatesInfostoreParser(isFailOnError());
    }

}
