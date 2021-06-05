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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVUserAgent;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.folderstorage.Permission;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.session.Session;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link DAVCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVCollection extends AbstractCollection {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVCollection.class);

    private final DAVFactory factory;
    private final WebdavPath url;

    private DAVUserAgent userAgent;

    /**
     * Initializes a new {@link DAVCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     */
    protected DAVCollection(DAVFactory factory, WebdavPath url) {
        super();
        this.factory = factory;
        this.url = url;
        includeProperties(new CurrentUserPrincipal(factory));
        LOG.debug("{}: initialized.", getUrl());
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

    /**
     * Gets the topic this collection uses for push notifications.
     * <p/>
     * The default implementation returns <code>null</code> (no push available), so override if applicable.
     *
     * @return The push topic, or <code>null</code> if not available for this collection
     */
    public String getPushTopic() {
        return null;
    }

    /**
     * Gets the collection's sync token based.
     *
     * @return The sync token
     */
    public abstract String getSyncToken() throws WebdavProtocolException;

    /**
     * Gets a child resource from this collection by name. If the resource
     * does not yet exists, a placeholder resource is created.
     *
     * @param name the name of the resource
     * @return the child resource
     */
    public abstract AbstractResource getChild(String name) throws WebdavProtocolException;

    /**
     * Gets the underlying permissions applicable for this WebDAV collection.
     *
     * @return The permissions
     */
    public abstract Permission[] getPermissions();

    /**
     * Constructs a {@link WebdavPath} for a child resource of this
     * collection with the supplied file name.
     *
     * @param fileName the file name of the resource
     * @return the path
     */
    protected WebdavPath constructPathForChildResource(String fileName) {
        return this.getUrl().dup().append(fileName);
    }

    @Override
    public WebdavPath getUrl() {
        return url;
    }

    @Override
    public DAVFactory getFactory() {
        return factory;
    }

    @Override
    protected boolean isset(Property p) {
        int id = p.getId();
        return Protocol.GETCONTENTLANGUAGE != id && Protocol.GETETAG != id;
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        return Long.valueOf(0);
    }

    @Override
    public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void lock(WebdavLock lock) throws WebdavProtocolException {
    }

    @Override
    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    public void unlock(String token) throws WebdavProtocolException {
        // no
    }

    @Override
    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        // no
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
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
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        return null;
    }

    @Override
    public void create() throws WebdavProtocolException {
        // no
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return true;
    }

    @Override
    public void save() throws WebdavProtocolException {
        // no
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        // no
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
        // no
    }

}
