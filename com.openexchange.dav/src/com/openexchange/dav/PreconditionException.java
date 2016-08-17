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

package com.openexchange.dav;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
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
    private static final XMLOutputter OUTPUTTER = new XMLOutputter();

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
        super(url, status, OXException.general(namespace + ':' + name));
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
            OUTPUTTER.output(responseBody, response.getOutputStream());
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(PreconditionException.class).warn("Error sending error response", e);
        }
    }

}
