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

package com.openexchange.caldav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.caldav.GroupwareCaldavFactory.State;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;


/**
 * A {@link CaldavCollection} bridges OX calendar folders to caldav collections. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavCollection extends AbstractCollection {
    
    private UserizedFolder folder;
    private GroupwareCaldavFactory factory;
    private WebdavPath url;
    private int id = -1;

    public CaldavCollection(AbstractStandardCaldavCollection parent, UserizedFolder folder, GroupwareCaldavFactory factory) {
        super();
        this.folder = folder;
        this.factory = factory;
        url = parent.getUrl().dup().append(folder.getName());

        includeProperties(
            new CurrentUserPrivilegeSet(folder.getOwnPermission()),
            new SupportedReportSet(),
            new SupportedCalendarComponentSet()
        );
        

    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        // throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
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
        // IGNORE
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    protected boolean isset(Property p) {
        if (p.getId() == Protocol.GETCONTENTLANGUAGE || p.getId() == Protocol.GETCONTENTLENGTH || p.getId() == Protocol.GETETAG) {
            return false;
        }
        return true;
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
        // IGNORE, this is not writable
    }

    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        State state = factory.getState();
        List<Appointment> appointments = state.getFolder(getId());
        List<WebdavResource> children = new ArrayList<WebdavResource>(appointments.size());

        for (Appointment appointment : appointments) {
            CaldavResource resource = new CaldavResource(this, appointment, factory);
            children.add(resource);
        }
        
        return children;
        
    }


    private static final Pattern NAME_PATTERN = Pattern.compile("(.+?)\\.ics");
    
    public CaldavResource getChild(String name) throws WebdavProtocolException {
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.find()) {
            throw new WebdavProtocolException(getUrl().dup().append(name), 404);
        }
        
        String uid = matcher.group(1);
        
        Appointment appointment = factory.getState().get(uid, getId());
        if (appointment == null) {
            // Not Found
            return new CaldavResource(this, getUrl().dup().append(name), factory);
        }
        return new CaldavResource(this, appointment, factory);
    }

    public void create() throws WebdavProtocolException {
        // throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    public boolean exists() throws WebdavProtocolException {
        return true;
    }

    public Date getCreationDate() throws WebdavProtocolException {
        return folder.getCreationDate();
    }

    public String getDisplayName() throws WebdavProtocolException {
        return folder.getName();
    }

    public Date getLastModified() throws WebdavProtocolException {
        return folder.getLastModified();
    }

    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    public String getSource() throws WebdavProtocolException {
        return null;
    }

    public WebdavPath getUrl() {
        return url;
    }

    public void lock(WebdavLock lock) throws WebdavProtocolException {
        // IGNORE
    }

    public void save() throws WebdavProtocolException {
//        throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    public void setDisplayName(String displayName) throws WebdavProtocolException {
        // IGNORE
    }

    public void unlock(String token) throws WebdavProtocolException {
        // IGNORE
    }
    
    public int getId() {
        if (id == -1) {
            return id = Integer.parseInt(folder.getID());
        }
        return id;
    }
    
    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType()+CaldavProtocol.CALENDAR;
    }


}
