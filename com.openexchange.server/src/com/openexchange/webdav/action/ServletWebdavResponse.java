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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

public class ServletWebdavResponse implements WebdavResponse {

	private final HttpServletResponse res;
	private int status;

	public ServletWebdavResponse(final HttpServletResponse res) {
		this.res = res;
	}

	@Override
    public OutputStream getOutputStream() throws IOException {
		return res.getOutputStream();
	}

	@Override
    public void setHeader(final String header, final String value) {
		res.setHeader(header, value);
	}

	@Override
    public void setStatus(final int status) {
		res.setStatus(status);
		this.status = status;
	}

	@Override
    public int getStatus() {
		return status;
	}

	@Override
    public void setContentType(final String s) {
		res.setContentType(s);
	}

    @Override
    public void sendString(final String notFound) throws IOException {
        final byte[] bytes = notFound.getBytes("UTF-8");
        setHeader("Content-Length", String.valueOf(bytes.length));
        getOutputStream().write(bytes);
    }

}
