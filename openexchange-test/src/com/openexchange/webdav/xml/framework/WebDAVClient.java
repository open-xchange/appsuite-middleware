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

package com.openexchange.webdav.xml.framework;

import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom2.JDOMException;
import com.openexchange.configuration.WebDAVConfig;
import com.openexchange.configuration.WebDAVConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.xml.folder.FolderTools;
import com.openexchange.webdav.xml.user.GroupUserTools;

/**
 * This class implements the temporary memory of a WebDAV client and provides
 * some convenience methods to determine user specific values for running some
 * tests more easily.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class WebDAVClient {

    private WebDAVSession session;

    private FolderTools folderTools;

    private GroupUserTools groupUserTools;

    /**
     * Default constructor.
     */
    public WebDAVClient(final WebDAVSession session) {
        super();
        this.session = session;
    }

    public WebDAVClient(final User user) throws OXException {
        this(new WebDAVSession());
        WebDAVConfig.init();
        final String login = WebDAVConfig.getProperty(user.getLogin());
        final String password = WebDAVConfig.getProperty(user.getPassword());
        setAuth(login, password);
    }

    public WebDAVClient(final String login, final String password) {
        this(new WebDAVSession());
        setAuth(login, password);
    }

    public final void setAuth(final String login, final String password) {
        final HttpClient client = session.getClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
    }

    public enum User {
        User1(Property.LOGIN, Property.PASSWORD),
        User2(Property.SECONDUSER, Property.PASSWORD);

        private Property login;
        private Property password;

        private User(final Property login, final Property password) {
            this.login = login;
            this.password = password;
        }

        public Property getLogin() {
            return login;
        }

        public Property getPassword() {
            return password;
        }
    }

    /**
     * @return the session
     */
    public WebDAVSession getSession() {
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            logout();
        } finally {
            super.finalize();
        }
    }

    public void logout() {
        session = new WebDAVSession();
    }

    public <T extends AbstractWebDAVResponse> T execute(final WebDAVRequest<T> request) throws IOException, JDOMException, OXException, OXException {
         return Executor.execute(this, request);
    }

    public <T extends AbstractWebDAVResponse> T execute(final String host, final WebDAVRequest<T> request) throws IOException, JDOMException, OXException, OXException {
        if (null == host) {
            return execute(request);
        }
        return Executor.execute(this, host, request);
   }

    public FolderTools getFolderTools() {
        if (null == folderTools) {
            folderTools = new FolderTools(this);
        }
        return folderTools;
    }

    public GroupUserTools getGroupUserTools() {
        if (null == groupUserTools) {
            groupUserTools = new GroupUserTools(this);
        }
        return groupUserTools;
    }
}
