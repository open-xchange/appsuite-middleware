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
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;

public class WebdavLockAction extends AbstractAction {

	private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavLockAction.class);

	@Override
	public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavProtocolException {
		final WebdavLock lock = new WebdavLock();

		lock.setTimeout(getTimeout(req.getHeader("Timeout")));
		lock.setDepth(getDepth(req.getHeader("Depth")));

		try {
		    if (req.hasBody()) {
		        configureLock(req, lock);
		    } else {
		        defaultLockParams(lock);
		    }
		    if (null == lock.getToken() && req.getUserInfo().containsKey("mentionedLocks")) {
		        List<String> mentionedLocks = (List<String>) req.getUserInfo().get("mentionedLocks");
		        if (1 == mentionedLocks.size()) {
		            lock.setToken(mentionedLocks.get(0));
		        }
		    }

            /*
             * (re-)check If-header against existing locks on requested resource, allowing multiple shared locks (RFC 4918, 9.10.5)
             */
            new WebdavIfAction(0, true, false, Scope.SHARED_LITERAL.equals(lock.getScope())).check(req, false);

			WebdavResource resource = req.getResource();

            if (null != lock.getToken()) {
                WebdavLock originalLock = resource.getLock(lock.getToken());
                copyOldValues(originalLock, lock);
            }

            final int status = resource.exists() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED;

			resource.lock(lock);

			// Reload, because it might have been switched to a lock null resource
			resource = req.getFactory().resolveResource(req.getUrl());

			res.setStatus(status);
            res.setHeader("Lock-Token", '<' + lock.getToken() + '>');
			res.setHeader("content-type", "application/xml");
			final WebdavProperty lockdiscovery = resource.getProperty("DAV:", "lockdiscovery");

			final Element lockDiscoveryElement = new PropertiesMarshaller(req.getCharset()).marshalProperty(lockdiscovery, resource.getProtocol());

			final Document responseDoc = new Document();
			final Element rootElement = new Element("prop",DAV_NS);

			rootElement.addContent(lockDiscoveryElement);

			responseDoc.setContent(rootElement);

            new XMLOutputter(Format.getPrettyFormat()).output(responseDoc, res.getOutputStream());

		} catch (JDOMException e) {
			LOG.error("JDOM Exception",e);
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			LOG.debug("Client gone?", e);
		}
	}

    private void copyOldValues(WebdavLock originalLock, WebdavLock lock) {
        if (lock.getOwner() == null) {
            lock.setOwner(originalLock.getOwner());
        }
    }

    /**
     * @param lock
     */
    private void defaultLockParams(WebdavLock lock) {
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
    }

    private void configureLock(final WebdavRequest req, final WebdavLock lock) throws JDOMException, IOException {
        final Element root = req.getBodyAsDocument().getRootElement();
        Element child = root.getChild("lockscope",DAV_NS);
        if (child != null) {
            final Element lockscope = child.getChildren().get(0);

            if (lockscope.getNamespace().equals(DAV_NS)) {
            	if (lockscope.getName().equalsIgnoreCase("shared")) {
            		lock.setScope(Scope.SHARED_LITERAL);
            	} else {
            		lock.setScope(Scope.EXCLUSIVE_LITERAL);
            	}
            }
        }

        lock.setType(Type.WRITE_LITERAL);

        final Element owner = root.getChild("owner",DAV_NS);

        if (owner != null) {
            lock.setOwner(new XMLOutputter().outputString(owner.cloneContent()));
        }
    }

	private int getDepth(final String header) {
        if (null == header) {
			return 0;
		}
		if (header.equalsIgnoreCase("infinity")) {
			return WebdavCollection.INFINITY;
		}

		return Integer.parseInt(header);
	}

	private long getTimeout(String header) {
		if (null == header) {
			return 600 * 1000;
		}
		if (header.indexOf(',') != -1) {
			header = header.substring(0,header.indexOf(',')).trim();
		}
		if (header.equalsIgnoreCase("infinite")) {
			return WebdavLock.NEVER;
		}

		return Long.parseLong(header.substring(7)) * 1000;
	}

}
