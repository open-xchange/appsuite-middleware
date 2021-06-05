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

import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.removePathPrefixFromPath;
import static com.openexchange.dav.DAVTools.removePrefixFromPath;
import static com.openexchange.dav.DAVTools.startsWithPrefix;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * {@link DAVFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVFactory extends AbstractWebdavFactory implements SessionHolder, ServiceLookup {

    public static final WebdavPath ROOT_URL = new WebdavPath();

    private final Protocol protocol;
    private final SessionHolder sessionHolder;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DAVFactory}.
     *
     * @param protocol The protocol
     * @param services A service lookup reference
     * @param sessionHolder The session holder to use
     */
    public DAVFactory(Protocol protocol, ServiceLookup services, SessionHolder sessionHolder) {
        super();
        this.protocol = protocol;
        this.sessionHolder = sessionHolder;
        this.services = services;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public Context getContext() {
        return sessionHolder.getContext();
    }

    @Override
    public Session getSessionObject() {
        return sessionHolder.getSessionObject();
    }

    @Override
    public Session getSession() {
        return getSessionObject();
    }

    @Override
    public User getUser() {
        return sessionHolder.getUser();
    }

    public UserConfiguration getUserConfiguration() {
        return ServerSessionAdapter.valueOf(getSession(), getContext(), getUser()).getUserConfiguration();
    }

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }

    public <S> S requireService(Class<? extends S> clazz) throws OXException {
        S service = services.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.absentService(clazz);
        }
        return service;
    }

    /**
     * Gets a value indicating whether the supplied WebDAV path denotes the root path or not.
     *
     * @param url The WebDAV path to check
     * @return <code>true</code> if it's the root path, <code>false</code>, otherwise
     */
    protected boolean isRoot(WebdavPath url) {
        return 0 == url.size();
    }

    /**
     * Sanitizes the supplied WebDAV path by stripping the implicit URL prefix of the DAV servlet this factory is responsible for.
     *
     * @param url The WebDAV path to sanitize
     * @return The sanitized path
     */
    protected WebdavPath sanitize(WebdavPath url) {
        ConfigViewFactory configViewFactory = getService(ConfigViewFactory.class);

        /*
         * Build relative URLs
         */
        String prefix = removePathPrefixFromPath(configViewFactory, getURLPrefix());
        String urlPath = removePathPrefixFromPath(configViewFactory, url.toString());

        /*
         * Remove the path this factory is responsible for, if present
         */
        if (Strings.isNotEmpty(prefix) && false == prefix.equals("/") && startsWithPrefix(urlPath, prefix)) {
            return new WebdavPath(removePrefixFromPath(prefix, urlPath));
        }
        return new WebdavPath(urlPath);
    }

    /**
     * Gets the URL prefix of the DAV servlet this factory is responsible for.
     *
     * @return The URL prefix, e.g. <code>/principals/users/</code>
     */
    public abstract String getURLPrefix();

    /**
     * Gets the full qualified path this factory is responsible for.
     *
     * @param path The relative path of factory is responsible for.
     * @return The full qualified path considering configuration.
     */
    protected String getURLPrefix(String path) {
        return getExternalPath(getService(ConfigViewFactory.class), path);
    }

}
