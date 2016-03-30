/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
		if(null != req.getHeader("content-length")) {
			resource.setLength(new Long(req.getHeader("content-length")));
		}
		String contentType = MimeType2ExtMap.getContentType(resource.getUrl().name());
		if("application/octet-stream".equals(contentType)) {
		    contentType = req.getHeader("content-type");
		}
		if(contentType == null) {
		    contentType = "application/octet-stream";
		}
		resource.setContentType(contentType);

		SizeExceededInputStream in = null;
		try {
			InputStream data = null;
			if(-1 != getMaxSize()) {
				data = in = new SizeExceededInputStream(req.getBody(), getMaxSize());
			} else {
				data = req.getBody();
			}

			resource.putBodyAndGuessLength(data);
			if(resource.exists() && ! resource.isLockNull()) {
				resource.save();
			} else {
				resource.create();
			}
			res.setStatus(HttpServletResponse.SC_CREATED);
		} catch (final IOException e) {
			LOG.debug("Client Gone?", e);
		} catch (final WebdavProtocolException x) {
			if (in != null && in.hasExceeded()) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			} else {
				throw x;
			}
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
		public void size(final long size) throws IOException {
			if(size > maxSize) {
				exceeded = true;
				throw new IOException("Exceeded max upload size of "+maxSize);
			}
		}

		public boolean hasExceeded(){
			return exceeded;
		}
	}
}
