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

package com.openexchange.caldav.resources;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CommonCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CommonCollection extends AbstractCollection {
    
    protected static Log LOG = com.openexchange.log.Log.loggerFor(CommonCollection.class);
    
    private final WebdavFactory factory;
    private final WebdavPath url;

    /**
     * Initializes a new {@link CommonCollection}.
     * 
     * @param factory the factory
     */
    public CommonCollection(WebdavFactory factory, WebdavPath url) {
        super();
        this.factory = factory;
        this.url = url;
        LOG.debug(getUrl() + ": initialized.");
    }
    
    protected WebdavProtocolException protocolException(Throwable t) {
        return protocolException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected WebdavProtocolException protocolException(Throwable t, int statusCode) {
        LOG.error(this.getUrl() + ": " + t.getMessage(), t);
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, t);
    }
    
    protected WebdavProtocolException protocolException(int statusCode) {
        return protocolException(new Throwable(), statusCode);
    }
    
    /**
     * Gets a child resource from this collection by name. If the resource 
     * does not yet exists, a placeholder resource is created.
     * 
     * @param name the name of the resource
     * @return the child resource
     * @throws WebdavProtocolException
     */
    public abstract AbstractResource getChild(String name) throws WebdavProtocolException;

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
        return this.url;
    }
    
    @Override
    protected WebdavFactory getFactory() {
        return this.factory;
    }

    @Override
    protected boolean isset(Property p) {
        int id = p.getId();
        return Protocol.GETCONTENTLANGUAGE != id && Protocol.GETCONTENTLENGTH != id && Protocol.GETETAG != id;
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
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        return null;
    }

    @Override
    public void create() throws WebdavProtocolException {
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return true;
    }

    @Override
    public void save() throws WebdavProtocolException {
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
    }

}
