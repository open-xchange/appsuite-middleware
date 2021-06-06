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
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.io.SizeAwareInputStream;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavPutAction extends AbstractAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavPutAction.class);

	@Override
	public void perform(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
		final WebdavResource resource = req.getResource();
		if (null != req.getHeader("content-length")) {
			resource.setLength(new Long(req.getHeader("content-length")));
		}
		String contentType = MimeType2ExtMap.getContentType(resource.getUrl().name());
		if ("application/octet-stream".equals(contentType)) {
		    contentType = req.getHeader("content-type");
		}
		if (contentType == null) {
		    contentType = "application/octet-stream";
		}
		resource.setContentType(contentType);

		SizeExceededInputStream in = null;
		try {
			InputStream data = null;
			if (-1 != getMaxSize()) {
				data = in = new SizeExceededInputStream(req.getBody(), getMaxSize());
			} else {
				data = req.getBody();
			}

			resource.putBodyAndGuessLength(data);
			if (resource.exists() && ! resource.isLockNull()) {
				resource.save();
			} else {
				resource.create();
			}
			res.setStatus(HttpServletResponse.SC_CREATED);
		} catch (IOException e) {
			LOG.debug("Client Gone?", e);
		} catch (WebdavProtocolException x) {
			if (in != null && in.hasExceeded()) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			}
            throw x;
		}

	}

	// Override for specific upload quota handling
	public long getMaxSize() {
		return -1;
	}

	public static final class SizeExceededInputStream extends SizeAwareInputStream {

		private boolean exceeded;
		private final long maxSize;

		public SizeExceededInputStream(final InputStream delegate, final long maxSize) {
			super(delegate);
			this.maxSize = maxSize;
		}

		@Override
		protected void size(final long size) throws IOException {
			if (size > maxSize) {
				exceeded = true;
				throw new IOException("Exceeded max upload size of "+maxSize);
			}
		}

		public boolean hasExceeded(){
			return exceeded;
		}
	}
}
