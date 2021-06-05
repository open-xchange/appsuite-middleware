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

package com.openexchange.ajax.user.actions;

import java.util.TimeZone;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.Params;

/**
 * {@link GetRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetRequest extends AbstractUserRequest<GetResponse> {

    private final int userId;
    private final TimeZone timeZone;
    private final boolean failOnError;

    public GetRequest(int userId, TimeZone timeZone, boolean failOnError) {
        super();
        this.userId = userId;
        this.timeZone = timeZone;
        this.failOnError = failOnError;
    }

    public GetRequest(TimeZone timeZone, boolean failOnError) {
        this(-1, timeZone, failOnError);
    }

    public GetRequest(int userId, TimeZone timeZone) {
        this(userId, timeZone, true);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, "get");
        if (0 < userId) {
            params.add(AJAXServlet.PARAMETER_ID, String.valueOf(userId));
        }
        return params.toArray();
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError, userId, timeZone);
    }
}
