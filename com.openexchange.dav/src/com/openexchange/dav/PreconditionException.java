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

package com.openexchange.dav;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PreconditionException}
 * <p/>
 * A "precondition" of a method describes the state of the server that
 * must be true for that method to be performed.  A "postcondition" of a
 * method describes the state of the server that must be true after that
 * method has been completed.  If a method precondition or postcondition
 * for a request is not satisfied, the response status of the request
 * MUST either be 403 (Forbidden), if the request should not be repeated
 * because it will always fail, or 409 (Conflict), if it is expected
 * that the user might be able to resolve the conflict and resubmit the
 * request.
 * <p/>
 * In order to allow better client handling of 403 and 409 responses, a
 * distinct XML element type is associated with each method precondition
 * and postcondition of a request.  When a particular precondition is
 * not satisfied or a particular postcondition cannot be achieved, the
 * appropriate XML element MUST be returned as the child of a top-level
 * DAV:error element in the response body, unless otherwise negotiated
 * by the request.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 * @see <a href="https://tools.ietf.org/html/rfc4791#section-1.3">RFC 4791, section 1.3</a>
 */
public class PreconditionException extends WebdavProtocolException {

    private static final long serialVersionUID = -6812444499133798978L;

    private final Element preconditionElement;

    /**
     * Initializes a new {@link PreconditionException}.
     *
     * @param namespace The namespace for the included precondition element
     * @param name The namespace for the included precondition element
     * @param url The WebDAV resource URL
     * @param status The HTTP response status; either {@link HttpServletResponse#SC_FORBIDDEN} if the request should not be repeated
     *        because it will always fail, or  {@link HttpServletResponse#SC_CONFLICT} if it is expected that the user might be able to
     *        resolve the conflict and resubmit the request
     */
    public PreconditionException(String namespace, String name, WebdavPath url, int status) {
        this(OXException.general(name), namespace, name, url, status);
    }

    /**
     * Initializes a new {@link PreconditionException}.
     *
     * @param cause The causing exception
     * @param namespace The namespace for the included precondition element
     * @param name The namespace for the included precondition element
     * @param url The WebDAV resource URL
     * @param status The HTTP response status; either {@link HttpServletResponse#SC_FORBIDDEN} if the request should not be repeated
     *            because it will always fail, or {@link HttpServletResponse#SC_CONFLICT} if it is expected that the user might be able to
     *            resolve the conflict and resubmit the request
     */
    public PreconditionException(OXException cause, String namespace, String name, WebdavPath url, int status) {
        super(url, status, cause);
        this.preconditionElement = new Element(name, namespace);
    }

    /**
     * Initializes a new {@link PreconditionException}.
     *
     * @param namespace The namespace for the included precondition element
     * @param name The namespace for the included precondition element
     * @param status The HTTP response status; either {@link HttpServletResponse#SC_FORBIDDEN} if the request should not be repeated
     *        because it will always fail, or  {@link HttpServletResponse#SC_CONFLICT} if it is expected that the user might be able to
     *        resolve the conflict and resubmit the request
     */
    public PreconditionException(String namespace, String name, int status) {
        this(namespace, name, new WebdavPath(), status);
    }

    public Element getPreconditionElement() {
        return preconditionElement;
    }

    public void sendError(HttpServletResponse response) {
        Element errorElement = new Element("error", DAVProtocol.DAV_NS);
        errorElement.addContent(preconditionElement);
        Document responseBody = new Document(errorElement);
        try {
            response.setStatus(getStatus());
            response.setContentType("text/xml; charset=UTF-8");
            new XMLOutputter(Format.getPrettyFormat()).output(responseBody, response.getOutputStream());
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(PreconditionException.class).warn("Error sending error response", e);
        }
    }

}
