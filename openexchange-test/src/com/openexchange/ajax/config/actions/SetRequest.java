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

package com.openexchange.ajax.config.actions;

import org.json.JSONObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SetRequest extends AbstractConfigRequest<SetResponse> {

    private final Tree param;

    private final Object value;

    private final boolean failOnError;

    public SetRequest(final Tree param, final Object value) {
        this(param, value, true);
    }

    public SetRequest(final Tree param, final Object value, final boolean failOnError) {
        super();
        this.param = param;
        this.value = value;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return super.getServletPath() + param.getPath();
    }

    @Override
    public Object getBody() {
        return null == value ? JSONObject.NULL : value;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public SetParser getParser() {
        return new SetParser(failOnError);
    }
}
