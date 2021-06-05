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

package com.openexchange.tools.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * UploadServletException
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UploadServletException extends ServletException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5434167456158444917L;

    private String data;

    private transient HttpServletResponse res;

    public UploadServletException(final HttpServletResponse res, final String data) {
        super();
        this.data = data;
        this.res = res;
    }

    public UploadServletException(final HttpServletResponse res, final String data, final String message, final Throwable arg1) {
        super(message, arg1);
        this.data = data;
        this.res = res;
    }

    public UploadServletException(final HttpServletResponse res, final String data, final String message) {
        super(message);
        this.data = data;
        this.res = res;
    }

    public UploadServletException(final HttpServletResponse res, final String data, final Throwable arg0) {
        super(arg0);
        this.data = data;
        this.res = res;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public HttpServletResponse getRes() {
        return res;
    }

    public void setRes(final HttpServletResponse res) {
        this.res = res;
    }

}
