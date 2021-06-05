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
