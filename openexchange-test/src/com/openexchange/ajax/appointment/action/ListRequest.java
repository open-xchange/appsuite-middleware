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

package com.openexchange.ajax.appointment.action;

import com.openexchange.ajax.framework.CommonListRequest;
import com.openexchange.ajax.framework.ListIDs;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (additional param)
 */
public class ListRequest extends CommonListRequest {

    public ListRequest(final ListIDs identifier, final int[] columns, final boolean failOnError) {
        super(AbstractAppointmentRequest.URL, identifier, columns, failOnError);
    }

    public ListRequest(final ListIDs identifier, final int[] columns) {
        this(identifier, columns, true);
    }

    public ListRequest(final ListIDs identifier, final String alias, final boolean failOnError) {
        super(AbstractAppointmentRequest.URL, identifier, alias, failOnError);
    }

    public ListRequest(final ListIDs identifier, final String alias) {
        this(identifier, alias, true);
    }

}
