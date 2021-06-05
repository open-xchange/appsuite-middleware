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

package com.openexchange.ajax.framework;

import com.openexchange.groupware.search.Order;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonAllRequest extends AbstractAllRequest<CommonAllResponse> {

    public CommonAllRequest(final String servletPath, final int folderId, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderId, columns, sort, order, failOnError);
    }

    public CommonAllRequest(final String servletPath, final String folderPath, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderPath, columns, sort, order, failOnError);
    }

    public CommonAllRequest(final String servletPath, final int folderId, final String alias, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderId, alias, sort, order, failOnError);
    }

    public CommonAllRequest(final String servletPath, final String folderPath, final String alias, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderPath, alias, sort, order, failOnError);
    }

    @Override
    public CommonAllParser getParser() {
        return new CommonAllParser(isFailOnError(), getColumns());
    }
}
