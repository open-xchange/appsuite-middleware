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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.acl.mixins.AddressbookHomeSet;
import com.openexchange.webdav.acl.mixins.CalendarHomeSet;
import com.openexchange.webdav.acl.mixins.CurrentUserPrincipal;
import com.openexchange.webdav.acl.mixins.PrincipalURL;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;


/**
 * {@link UserPrincipalResource}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserPrincipalResource extends AbstractResource {

    private final PrincipalWebdavFactory factory;
    private final User user;
    private final WebdavPath url;

    public UserPrincipalResource(PrincipalWebdavFactory factory, User u, WebdavPath url) {
        this.factory = factory;
        this.user = u;
        this.url = url;
        includeProperties(new PrincipalURL(factory.getSessionHolder()), new CurrentUserPrincipal(factory.getSessionHolder()));
        includeProperties(new AddressbookHomeSet(), new CalendarHomeSet());
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return"<D:resourcetype><D:principal /></D:resourcetype>";
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return false;
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
        // IGNORE
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void create() throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void delete() throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return true;
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
        return user.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        return "http://www.open-xchange.com/webdav/users/"+user.getId();
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
        // IGNORE
    }

    @Override
    public void save() throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setContentType(String type) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setLength(Long length) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    public void unlock(String token) throws WebdavProtocolException {
        // IGNORE
    }

}
