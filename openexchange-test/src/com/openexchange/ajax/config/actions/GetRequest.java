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

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GetRequest extends AbstractConfigRequest<GetResponse> {

    private final String path;

    private final boolean failOnError;

    public GetRequest(final Tree param) {
        super();
        this.path = param.getPath();
        this.failOnError = true;
    }

    public GetRequest(final Tree param, final boolean failOnError) {
        super();
        this.path = param.getPath();
        this.failOnError = failOnError;
    }

    public GetRequest(final String path) {
        super();
        this.path = path;
        this.failOnError = true;
    }

    public GetRequest(final String path, final boolean failOnError) {
        super();
        this.path = path;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return super.getServletPath() + path;
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
        return new Parameter[0];
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError);
    }
}
