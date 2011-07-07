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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.caldav.GroupwareCaldavFactory.State;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;


/**
 * A {@link CaldavCollection} bridges OX calendar folders to caldav collections. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavCollection extends AbstractCollection {
    
    private final UserizedFolder folder;
    private final GroupwareCaldavFactory factory;
    private final WebdavPath url;
    private int id = -1;
    
    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(CaldavCollection.class));

    public CaldavCollection(final AbstractStandardCaldavCollection parent, final UserizedFolder folder, final GroupwareCaldavFactory factory) {
        super();
        this.folder = folder;
        this.factory = factory;
        url = parent.getUrl().dup().append(getFolderName(folder));

        includeProperties(
            new CurrentUserPrivilegeSet(folder.getOwnPermission()),
            new SupportedReportSet(),
            new SupportedCalendarComponentSet()
        );
        

    }
    private String getFolderName(final UserizedFolder f) {
        final Type type = f.getType();
        if (type.equals(SharedType.getInstance())) {
            return f.getName()+" ("+getOwnerName(f)+")";
        } 
        return f.getName();
    }

    private String getOwnerName(final UserizedFolder f) {
        final Permission[] permissions = f.getPermissions();
        for (final Permission permission : permissions) {
            if (permission.isAdmin()) {
                final int entity = permission.getEntity();
                try {
                    return factory.resolveUser(entity).getDisplayName();
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                    return new Integer(entity).toString();
                }
            }
        }
        
        return null;
    }
    @Override
    protected void internalDelete() throws OXException {
        // throw new OXException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
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
    protected WebdavProperty internalGetProperty(final String namespace, final String name) throws OXException {
        return null;
    }

    @Override
    protected void internalPutProperty(final WebdavProperty prop) throws OXException {
        // IGNORE
    }

    @Override
    protected void internalRemoveProperty(final String namespace, final String name) throws OXException {
        // IGNORE
    }

    @Override
    protected boolean isset(final Property p) {
        if (p.getId() == Protocol.GETCONTENTLANGUAGE || p.getId() == Protocol.GETCONTENTLENGTH || p.getId() == Protocol.GETETAG) {
            return false;
        }
        return true;
    }

    @Override
    public void setCreationDate(final Date date) throws OXException {
        // IGNORE, this is not writable
    }

    public List<WebdavResource> getChildren() throws OXException {
        final State state = factory.getState();
        final List<Appointment> appointments = state.getFolder(getId());
        if (appointments == null) {
            final int a = 12;
            System.out.println(a);
        }
        final List<WebdavResource> children = new ArrayList<WebdavResource>(appointments.size());

        for (final Appointment appointment : appointments) {
            final CaldavResource resource = new CaldavResource(this, appointment, factory);
            children.add(resource);
        }
        
        return children;
        
    }


    private static final Pattern NAME_PATTERN = Pattern.compile("(.+?)\\.ics");
    
    public CaldavResource getChild(final String name) throws OXException {
        final Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.find()) {
            throw new OXException(getUrl().dup().append(name), 404);
        }
        
        final String uid = matcher.group(1);
        
        final Appointment appointment = factory.getState().get(uid, getId());
        if (appointment == null) {
            // Not Found
            return new CaldavResource(this, getUrl().dup().append(name), factory);
        }
        return new CaldavResource(this, appointment, factory);
    }

    public void create() throws OXException {
        // throw new OXException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    public boolean exists() throws OXException {
        return true;
    }

    public Date getCreationDate() throws OXException {
        return folder.getCreationDate();
    }

    public String getDisplayName() throws OXException {
        return getFolderName(folder);
    }

    public Date getLastModified() throws OXException {
        return folder.getLastModified();
    }

    public WebdavLock getLock(final String token) throws OXException {
        return null;
    }

    public List<WebdavLock> getLocks() throws OXException {
        return Collections.emptyList();
    }

    public WebdavLock getOwnLock(final String token) throws OXException {
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

    public void lock(final WebdavLock lock) throws OXException {
        // IGNORE
    }

    public void save() throws OXException {
//        throw new OXException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    public void setDisplayName(final String displayName) throws OXException {
        // IGNORE
    }

    public void unlock(final String token) throws OXException {
        // IGNORE
    }
    
    public int getId() {
        if (id == -1) {
            return id = Integer.parseInt(folder.getID());
        }
        return id;
    }
    
    @Override
    public String getResourceType() throws OXException {
        return super.getResourceType()+CaldavProtocol.CALENDAR;
    }


}
