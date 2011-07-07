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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.webdav.acl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.groupware.ldap.User;
import com.openexchange.exception.OXException;
import com.openexchange.user.UserService;
import com.openexchange.webdav.acl.mixins.PrincipalURL;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;


/**
 * {@link RootPrincipal}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RootPrincipal extends AbstractCollection {

    private PrincipalWebdavFactory factory;
    private WebdavPath url;

    public RootPrincipal(PrincipalWebdavFactory factory) {
        super();
        this.factory = factory;
        this.url = new WebdavPath();
        
        includeProperties(new PrincipalURL(factory.getSessionHolder()));
    }

    @Override
    protected void internalDelete() throws OXException {
        throw new OXException(getUrl(), 403);
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws OXException {
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws OXException {

    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws OXException {

    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void setCreationDate(Date date) throws OXException {

    }

    public List<WebdavResource> getChildren() throws OXException {
        UserService users = factory.getUserService();
        try {
            User[] user = users.getUser(factory.getContext());
            List<WebdavResource> children = new ArrayList<WebdavResource>(user.length);
            for (User u : user) {
                children.add(new UserPrincipalResource(factory, u));
            }
            return children;
        } catch (OXException e) {
            throw new OXException(getUrl(), 403);
        }
    }
    
    public UserPrincipalResource resolveUser(WebdavPath url) throws OXException {
        String name = url.name();
        UserService users = factory.getUserService();
        try {
            int userId = users.getUserId(name, factory.getContext());
            User user = users.getUser(userId, factory.getContext());
            return new UserPrincipalResource(factory, user);
        } catch (OXException e) {
            throw new OXException(url, 500);
        }
    }

    public void create() throws OXException {
        // NOPE
    }

    public boolean exists() throws OXException {
        return true;
    }

    public Date getCreationDate() throws OXException {
        return new Date(0);
    }

    public String getDisplayName() throws OXException {
        return "";
    }

    public Date getLastModified() throws OXException {
        return new Date(0);
    }

    public WebdavLock getLock(String token) throws OXException {
        return null;
    }

    public List<WebdavLock> getLocks() throws OXException {
        return Collections.emptyList();
    }

    public WebdavLock getOwnLock(String token) throws OXException {
        return null;
    }

    public List<WebdavLock> getOwnLocks() throws OXException {
        return Collections.emptyList();
    }

    public String getSource() throws OXException {
        return null;
    }

    public WebdavPath getUrl() {
        return url;
    }

    public void lock(WebdavLock lock) throws OXException {
        //IGNORE
    }

    public void save() throws OXException {
        // IGNORE
    }

    public void setDisplayName(String displayName) throws OXException {
        // IGNORE
    }

    public void unlock(String token) throws OXException {
        // IGNORE
    }

}
