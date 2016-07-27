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

import javax.servlet.http.HttpServletResponse;
import org.jdom2.Namespace;
import com.openexchange.dav.reports.PrinicpalPropertySearchReport;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link DAVProtocol}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVProtocol extends Protocol {

    /** urn:ietf:params:xml:ns:caldav */
    public static final Namespace CAL_NS = Namespace.getNamespace("CAL", "urn:ietf:params:xml:ns:caldav");

    /** http://apple.com/ns/ical/ */
    public static final Namespace APPLE_NS = Namespace.getNamespace("APPLE", "http://apple.com/ns/ical/");

    /** http://calendarserver.org/ns/ */
    public static final Namespace CALENDARSERVER_NS = Namespace.getNamespace("CS", "http://calendarserver.org/ns/");

    /** urn:ietf:params:xml:ns:carddav */
    public static final Namespace CARD_NS = Namespace.getNamespace("CARD", "urn:ietf:params:xml:ns:carddav");

    /** HTTP/1.1 507 Insufficient Storage */
    public static final int SC_INSUFFICIENT_STORAGE = 507;

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVProtocol.class);

    @Override
    public WebdavAction getReportAction(String namespace, String name) {
        if (PrinicpalPropertySearchReport.NAMESPACE.equals(namespace) && PrinicpalPropertySearchReport.NAME.equals(name)) {
            return new PrinicpalPropertySearchReport(this);
        }
        return super.getReportAction(namespace, name);
    }

    /**
     * Generates a {@link WebdavProtocolException} with a generic error message signaling the specified HTTP status code.
     *
     * @param url The WebDAV path of the underlying resource, or <code>null</code> if unknown
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException}
     */
    public static WebdavProtocolException protocolException(WebdavPath url, int statusCode) {
        return protocolException(url, WebdavProtocolException.generalError(url, statusCode), statusCode);
    }

    /**
     * Generates a {@link WebdavProtocolException} with a generic error message signaling the specified HTTP status code, using the
     * supplied exception as root cause.
     *
     * @param url The WebDAV path of the underlying resource, or <code>null</code> if unknown
     * @param e The exception to get the protocol exception for
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException}
     */
    public static WebdavProtocolException protocolException(WebdavPath url, Exception e, int statusCode) {
        if (WebdavProtocolException.class.isInstance(e)) {
            return protocolException(url, (WebdavProtocolException) e, statusCode);
        }
        if (OXException.class.isInstance(e)) {
            return protocolException(url, (OXException) e, statusCode);
        }
        return protocolException(url, WebdavProtocolException.generalError(e, url, statusCode), statusCode);
    }

    /**
     * Generates a {@link WebdavProtocolException} with a generic error message signaling the specified HTTP status code, using the
     * supplied exception as root cause. Appropriate logging is performed implicitly based on the exception's category.
     *
     * @param url The WebDAV path of the underlying resource, or <code>null</code> if unknown
     * @param e The exception to get the protocol exception for
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException}
     */
    public static WebdavProtocolException protocolException(WebdavPath url, OXException e, int statusCode) {
        switch (e.getCategory().getLogLevel()) {
            case TRACE:
                LOG.trace("{}", url, e);
                break;
            case DEBUG:
                LOG.debug("{}", url, e);
                break;
            case INFO:
                LOG.info("{}", url, e);
                break;
            case WARNING:
                LOG.warn("{}", url, e);
                break;
            case ERROR:
                LOG.error("{}", url, e);
                break;
            default:
                break;
        }
        return WebdavProtocolException.class.isInstance(e) ? (WebdavProtocolException) e : WebdavProtocolException.generalError(e, url, statusCode);
    }

    /**
     * Generates a {@link WebdavProtocolException} with a generic error message signaling an appropriate HTTP status code, using the
     * supplied exception as root cause.
     *
     * @param url The WebDAV path of the underlying resource, or <code>null</code> if unknown
     * @param e The exception to get the protocol exception for
     * @return The appropriate {@link WebdavProtocolException}
     */
    public static WebdavProtocolException protocolException(WebdavPath url, Exception e) {
        if (OXException.class.isInstance(e)) {
            return protocolException(url, e, getStatusCode(((OXException) e).getCategory()));
        } else {
            return protocolException(url, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets an HTTP status code appropriate for the supplied exception category.
     *
     * @param category the exception category to get the status code for
     * @return The status code, or {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} if no suitable code available
     */
    private static int getStatusCode(Category category) {
        if (Category.CATEGORY_USER_INPUT.equals(category) || Category.CATEGORY_TRUNCATED.equals(category)) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }
        if (Category.CATEGORY_PERMISSION_DENIED.equals(category)) {
            return HttpServletResponse.SC_FORBIDDEN;
        }
        if (Category.CATEGORY_CONFLICT.equals(category)) {
            return HttpServletResponse.SC_CONFLICT;
        }
        if (Category.CATEGORY_SERVICE_DOWN.equals(category)) {
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
        if (Category.CATEGORY_CAPACITY.equals(category)) {
            return SC_INSUFFICIENT_STORAGE;
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

}
