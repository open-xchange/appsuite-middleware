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

package com.openexchange.dav.resources;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVUserAgent;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.session.Session;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link DAVResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVResource extends AbstractResource {

    protected final DAVFactory factory;
    protected final WebdavPath url;

    private DAVUserAgent userAgent;

    /**
     * Initializes a new {@link DAVResource}.
     *
     * @param factory The factory
     * @param url The WebDAV path of the resource
     */
    protected DAVResource(DAVFactory factory, WebdavPath url) {
        super();
        this.factory = factory;
        this.url = url;
        includeProperties(new CurrentUserPrincipal(factory));
    }

    /**
     * Gets the host data valid for the currently executed WebDAV request, throwing an exception if no host data could be looked up.
     *
     * @return The host data
     */
    protected HostData getHostData() throws WebdavProtocolException {
        /*
         * get host data from request context or session parameter
         */
        com.openexchange.framework.request.RequestContext requestContext = RequestContextHolder.get();
        if (null != requestContext) {
            return requestContext.getHostData();
        }
        Session session = factory.getSession();
        if (null != session) {
            HostData hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
            if (null != hostData) {
                return hostData;
            }
        }
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets the user agent derived from the request's <code>user-agent</code> header.
     *
     * @return The user agent, or {@link DAVUserAgent#UNKNOWN} if not set or unknown
     */
    protected DAVUserAgent getUserAgent() {
        if (null == userAgent) {
            String value = null;
            com.openexchange.framework.request.RequestContext requestContext = RequestContextHolder.get();
            if (null != requestContext) {
                value = requestContext.getUserAgent();
            } else {
                Session session = factory.getSession();
                if (null != session) {
                    value = (String) session.getParameter("user-agent");
                }
            }
            userAgent = DAVUserAgent.parse(value);
        }
        return userAgent;
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
        // no
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
        // no
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
        // no
    }

    @Override
    public void create() throws WebdavProtocolException {
        // no
    }

    @Override
    public void delete() throws WebdavProtocolException {
        // no
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return true;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return false;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        return null;
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return null;
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return null;
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        return null;
    }

    @Override
    public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        return null;
    }

    @Override
    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
    public WebdavPath getUrl() {
        return url;
    }

    @Override
    public void lock(WebdavLock lock) throws WebdavProtocolException {
        // no
    }

    @Override
    public void save() throws WebdavProtocolException {
        // no
    }

    @Override
    public void setContentType(String type) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setLength(Long length) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {
        // no
    }

    @Override
    public void unlock(String token) throws WebdavProtocolException {
        // no
    }

}
