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

package com.openexchange.caldav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.caldav.mixins.CTag;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.caldav.mixins.SyncToken;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterAnalyzer;
import com.openexchange.caldav.query.FilterAnalyzerBuilder;
import com.openexchange.caldav.reports.FilteringResource;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

/**
 * A {@link CaldavCollection} bridges OX calendar folders to caldav collections. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CaldavCollection extends AbstractCollection implements FilteringResource {
    
    private final UserizedFolder folder;
    private final GroupwareCaldavFactory factory;
    private final WebdavPath url;
    private int id = -1;
    
    private static final Log LOG = LogFactory.getLog(CaldavCollection.class);

    public CaldavCollection(AbstractStandardCaldavCollection parent, UserizedFolder folder, GroupwareCaldavFactory factory) {
    	this(parent, folder, factory, -1);
    }

    public CaldavCollection(AbstractStandardCaldavCollection parent, UserizedFolder folder, GroupwareCaldavFactory factory, int order) {
        super();
        this.folder = folder;
        this.factory = factory;
        this.url = parent.getUrl().dup().append(folder.getID());
        includeProperties(
            new CurrentUserPrivilegeSet(folder.getOwnPermission()),
            new SupportedReportSet(),
            new SupportedCalendarComponentSet(),
            new CTag(getId(), factory),
            new SyncToken(getId(), factory)
        );
        if (-1 < order) {
            includeProperties(new CalendarOrder(order));
        }
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
    protected WebdavProperty internalGetProperty(final String namespace, final String name) throws WebdavProtocolException {
        return null;
    }

    @Override
    protected void internalPutProperty(final WebdavProperty prop) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    protected void internalRemoveProperty(final String namespace, final String name) throws WebdavProtocolException {
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
    public void setCreationDate(final Date date) throws WebdavProtocolException {
        // IGNORE, this is not writable
    }

    @Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
        final GroupwareCaldavFactory.State state = factory.getState();
        final List<Appointment> appointments = state.getFolder(getId());
        final List<WebdavResource> children = new ArrayList<WebdavResource>(appointments.size());

        for (final Appointment appointment : appointments) {
            final CaldavResource resource = new CaldavResource(this, appointment, factory);
            children.add(resource);
        }
        return children;
    }

    public CaldavResource getChild(String name) throws WebdavProtocolException {
    	Integer id = resolveID(name);
    	if (null != id) {
            Appointment appointment = factory.getState().get(id.intValue(), this.getId());
            if (null != appointment) {
            	/*
            	 * appointment resource
            	 */
                return new CaldavResource(this, appointment, factory);
            }
    	}
    	/*
    	 * resource not yet exists
    	 */
		return new CaldavResource(this, getUrl().dup().append(name), factory);
    }
    
    private Integer resolveID(String resourceName) {
    	if (null != resourceName && 0 < resourceName.length()) {
        	if (4 < resourceName.length() && resourceName.toLowerCase().endsWith(".ics")) {
        		resourceName = resourceName.substring(0, resourceName.length() - 4);
        	}
    		try {
    			return new Integer(resourceName);
    		} catch (NumberFormatException e) {
    			// not a number
    		}
            return factory.getState().resolveResource(resourceName, getId());
    	}
    	return null;
    }

    @Override
	public void create() throws WebdavProtocolException {
        // throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
	public boolean exists() throws WebdavProtocolException {
        return true;
    }

    @Override
	public Date getCreationDate() throws WebdavProtocolException {
        return folder.getCreationDate();
    }

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		if (this.exists()) {
	    	final Locale locale = this.factory.getSessionHolder().getUser().getLocale();
	    	final String name = null != locale ? this.folder.getLocalizedName(locale) : this.folder.getName();
	    	if (SharedType.getInstance().equals(this.folder.getType())) {
	    		String ownerName = null;
	            final Permission[] permissions = this.folder.getPermissions();
	            for (final Permission permission : permissions) {
	                if (permission.isAdmin()) {
	                    final int entity = permission.getEntity();
	                    try {
	                        ownerName = factory.resolveUser(entity).getDisplayName();
	                    } catch (final WebdavProtocolException e) {
	                        LOG.error(e.getMessage(), e);
	                        ownerName = new Integer(entity).toString();
	                    }
	                    break;
	                }
	            }	    		
	    		return String.format("%s (%s)", name, ownerName);
	    	} else {
	    		return name;
	    	}
		} else {
	    	return null;
		}
	}

    @Override
	public Date getLastModified() throws WebdavProtocolException {
        return folder.getLastModified();
    }

    @Override
	public WebdavLock getLock(final String token) throws WebdavProtocolException {
        return null;
    }

    @Override
	public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
	public WebdavLock getOwnLock(final String token) throws WebdavProtocolException {
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
	public void lock(final WebdavLock lock) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
	public void save() throws WebdavProtocolException {
//        throw new WebdavProtocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
	public void setDisplayName(final String displayName) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
	public void unlock(final String token) throws WebdavProtocolException {
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

    public boolean isShared() {
        return folder.getType() == SharedType.getInstance();
    }

    private static final FilterAnalyzer RANGE_QUERY_ANALYZER = new FilterAnalyzerBuilder()
        .compFilter("VCALENDAR")
            .compFilter("VEVENT")
                .timeRange().capture().end()
            .end()
        .end()
    .build();
    
    
    @Override
	public List<WebdavResource> filter(final Filter filter) throws WebdavProtocolException {
        final List<Object> arguments = new ArrayList<Object>(2);
        if (RANGE_QUERY_ANALYZER.match(filter, arguments) && ! arguments.isEmpty()) {
            final GroupwareCaldavFactory.State state = factory.getState();
            final Date start = toDate(arguments.get(0));
            final Date end = toDate(arguments.get(1));
            
            final List<Appointment> appointments = state.getAppointmentsInFolderAndRange(getId(), start, end);
            final List<WebdavResource> children = new ArrayList<WebdavResource>(appointments.size());

            for (final Appointment appointment : appointments) {
                final CaldavResource resource = new CaldavResource(this, appointment, factory);
                children.add(resource);
            }
            
            return children;

        }
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    private Date toDate(final Object object) {
        final long tstamp = (Long) object;
        if (tstamp == -1) {
            return null;
        }
        return new Date(tstamp);
    }

}
