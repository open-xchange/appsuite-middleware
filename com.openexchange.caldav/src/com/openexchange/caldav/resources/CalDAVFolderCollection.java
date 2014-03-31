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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.openexchange.caldav.CalDAVPermission;
import com.openexchange.caldav.CalDAVServiceLookup;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.AllowedSharingModes;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.CalendarOwner;
import com.openexchange.caldav.mixins.Invite;
import com.openexchange.caldav.mixins.MaxDateTime;
import com.openexchange.caldav.mixins.MinDateTime;
import com.openexchange.caldav.mixins.Organizer;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterAnalyzer;
import com.openexchange.caldav.query.FilterAnalyzerBuilder;
import com.openexchange.caldav.reports.FilteringResource;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.xml.jdom.JDOMParser;

/**
 * {@link CalDAVFolderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public abstract class CalDAVFolderCollection<T extends CalendarObject> extends CommonFolderCollection<T> implements FilteringResource {

    protected static final int NO_ORDER = -1;
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^\\d+$");

    protected GroupwareCaldavFactory factory;

    public CalDAVFolderCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }

    public CalDAVFolderCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        includeProperties(new SupportedReportSet(), new MinDateTime(this), new MaxDateTime(this), new Invite(factory, this),
            new AllowedSharingModes(factory.getSession()), new CalendarOwner(this), new Organizer(this));
        if (NO_ORDER != order) {
            includeProperties(new CalendarOrder(order));
        }
    }

    protected abstract boolean isSupported(T object) throws OXException;

    @Override
    protected String getFileExtension() {
        return CalDAVResource.EXTENSION_ICS;
    }

    public Date getIntervalStart() {
        return factory.getState().getMinDateTime();
    }

    public Date getIntervalEnd() {
        return factory.getState().getMaxDateTime();
    }

    @Override
    public User getOwner() throws OXException {
        if (PrivateType.getInstance().equals(this.folder.getType())) {
            return factory.getUser();
        } else if (null != folder.getPermissions()) {
            for (Permission permission : folder.getPermissions()) {
                if (permission.isAdmin() && false == permission.isGroup()) {
                    return factory.resolveUser(permission.getEntity());
                }
            }
        }
        return null;
    }

    @Override
    public void putBody(final InputStream data, final boolean guessSize) throws WebdavProtocolException {
        try {
            Document document = CalDAVServiceLookup.getService(JDOMParser.class).parse(data);
            if (null == document.getRootElement() || false == "share".equals(document.getRootElement().getName()) ||
                false == CaldavProtocol.CALENDARSERVER_NS.equals(document.getRootElement().getNamespace())) {
                throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
            if (false == getFolder().getOwnPermission().isAdmin()) {
                throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
            List<Permission> permissionsToSet = extractPermissionsToSet(document);
            Set<Integer> permissionsEntitiesToRemove = extractPermissionsToRemove(document);
            boolean hasChanged = false;
            List<Permission> updatedPermissions = new ArrayList<Permission>();
            Permission[] permissions = getFolder().getPermissions();
            // remove / update
            for (Permission permission : permissions) {
                if (permission.isAdmin()) {
                    updatedPermissions.add(permission);
                    continue;
                }
                if (permissionsEntitiesToRemove.contains(Integer.valueOf(permission.getEntity()))) {
                    // don't add
                    hasChanged = true;
                    continue;
                }
                Iterator<Permission> iterator = permissionsToSet.iterator();
                while (iterator.hasNext()) {
                    Permission permissionToSet = iterator.next();
                    if (permission.getEntity() == permissionToSet.getEntity()) {
                        // update
                        permission = permissionToSet;
                        iterator.remove();
                        hasChanged = true;
                        break;
                    }
                }
                updatedPermissions.add(permission);
            }
            // add remaining
            if (0 < permissionsToSet.size()) {
                updatedPermissions.addAll(permissionsToSet);
                hasChanged = true;
            }
            if (hasChanged) {
                UserizedFolder folder = getFolder();
                AbstractFolder updatedFolder = new AbstractFolder() {

                    private static final long serialVersionUID = -367640273380922433L;

                    @Override
                    public boolean isGlobalID() {
                        return false;
                    }
                };
                updatedFolder.setID(folder.getID());
                updatedFolder.setTreeID(folder.getTreeID());
                updatedFolder.setType(folder.getType());
                updatedFolder.setParentID(folder.getParentID());
                updatedFolder.setPermissions(updatedPermissions.toArray(new Permission[updatedPermissions.size()]));
                factory.getFolderService().updateFolder(updatedFolder, folder.getLastModifiedUTC(), factory.getSession(), null);
            }
        } catch (JDOMException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (OXException e) {
            if (e.getCategory().equals(Category.CATEGORY_PERMISSION_DENIED)) {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_FORBIDDEN);
            } else {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    /**
     * Gets a list of permission entities that are requested to remove in the supplied "share" document.
     *
     * @param document The document body of the request
     * @return A set of permission entities to remove, or an empty set if there are none
     */
    private Set<Integer> extractPermissionsToRemove(Document document) {
        Set<Integer> permissionsEntitiesToRemove = new HashSet<Integer>();
        for (Element element : document.getRootElement().getChildren("remove", CaldavProtocol.CALENDARSERVER_NS)) {
            try {
                Element hrefElement = element.getChild("href", CaldavProtocol.DAV_NS);
                int entity = discoverUserID(hrefElement);
                if (-1 == entity) {
                    entity = discoverGroupID(hrefElement);
                    if (-1 == entity) {
                        continue;
                    }
                }
                permissionsEntitiesToRemove.add(entity);
            } catch (OXException e) {
                LOG.warn("Error resolving share entitites", e);
            }
        }
        return permissionsEntitiesToRemove;
    }

    /**
     * Gets a list of permissions that are requested to set in the supplied "share" document.
     *
     * @param document The document body of the request
     * @return The list of permissions to set, or an empty list if there are none
     */
    private List<Permission> extractPermissionsToSet(Document document) {
        List<Permission> permissionsToSet = new ArrayList<Permission>();
        for (Element element : document.getRootElement().getChildren("set", CaldavProtocol.CALENDARSERVER_NS)) {
            try {
                Element hrefElement = element.getChild("href", CaldavProtocol.DAV_NS);
                boolean group = false;
                int entity = discoverUserID(hrefElement);
                if (-1 == entity) {
                    entity = discoverGroupID(hrefElement);
                    if (-1 == entity) {
                        continue;
                    }
                    group = true;
                }
                if (null != element.getChild("read", CaldavProtocol.CALENDARSERVER_NS)) {
                    permissionsToSet.add(CalDAVPermission.createReadOnlyForEntity(entity, group));
                } else if (null != element.getChild("read-write", CaldavProtocol.CALENDARSERVER_NS)) {
                    permissionsToSet.add(CalDAVPermission.createReadWriteForEntity(entity, group));
                }
            } catch (OXException e) {
                LOG.warn("Error resolving share entitites", e);
            }
        }
        return permissionsToSet;
    }

    private int discoverGroupID(Element hrefElement) throws OXException {
        if (null != hrefElement.getText() && null != hrefElement.getText()) {
            String text = hrefElement.getText();
            if (text.startsWith("/principals/groups/")) {
                String groupId = text.substring(19);
                if ('/' == groupId.charAt(groupId.length() - 1)) {
                    groupId = groupId.substring(0, groupId.length() - 1);
                }
                try {
                    return Integer.valueOf(groupId);
                } catch (NumberFormatException e) {
                    LOG.debug("Error parsing group identifier '{}'", groupId, e);
                }
            }
        }
        return -1;
    }

    private int discoverUserID(Element hrefElement) throws OXException {
        if (null != hrefElement.getText() && null != hrefElement.getText()) {
            String text = hrefElement.getText();
            if (text.startsWith("mailto:")) {
                String mail = text.substring(7);
                return CalDAVServiceLookup.getService(UserService.class).searchUser(mail, factory.getContext()).getId();
            } else if (text.startsWith("/principals/users/")) {
                String loginName = text.substring(18);
                if ('/' == loginName.charAt(loginName.length() - 1)) {
                    loginName = loginName.substring(0, loginName.length() - 1);
                }
                return CalDAVServiceLookup.getService(UserService.class).getUserId(loginName, factory.getContext());
            }
        }
        return -1;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        return Streams.newByteArrayInputStream(new byte[0]);
    }

    @Override
    public void setLength(final Long l) throws WebdavProtocolException {

    }

    protected static <T extends CalendarObject>boolean isInInterval(T object, Date intervalStart, Date intervalEnd) {
        return null != object &&
            (null == object.getEndDate() || object.getEndDate().after(intervalStart)) &&
            (null == object.getStartDate() || object.getStartDate().before(intervalEnd));
    }

    protected List<T> filter(SearchIterator<T> searchIterator) throws OXException {
        List<T> list = new ArrayList<T>();
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    T t = searchIterator.next();
                    if (isSupported(t)) {
                        list.add(t);
                    }
                }
            } finally {
                searchIterator.close();
            }
        }
        return list;
    }

    @Override
    protected T getObject(String resourceName) throws OXException {
        Collection<T> objects = this.getObjects();
        if (null != objects && 0 < objects.size()) {
            /*
             * try filename and uid properties
             */
            for (T t : objects) {
                if (resourceName.equals(t.getFilename()) || resourceName.equals(t.getUid())) {
                    return t;
                }
            }
            /*
             * try object id as fallback to support previous implementation
             */
            Matcher matcher = OBJECT_ID_PATTERN.matcher(resourceName);
            if (matcher.find()) {
                try {
                    int objectID = Integer.parseInt(matcher.group(0));
                    for (T t : objects) {
                        if (objectID == t.getObjectID()) {
                            return t;
                        }
                    }
                } catch (NumberFormatException e) {
                    // not an ID
                }
            }
        }
        return null;
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        StringBuilder StringBuilder = new StringBuilder(super.getResourceType());
        StringBuilder.append('<').append(CaldavProtocol.CAL_NS.getPrefix()).append(":calendar/>");
        if (null != this.folder) {
            if (SharedType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by another user and is being shared to the current user.
                StringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared/>");
            } else if (PrivateType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by the current user and is being shared by them.
                StringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared-owner/>");
            } else if (PublicType.getInstance().equals(folder.getType())) {
                // evaluate own permission if folder shares can be edited or not
                StringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix())
                    .append(folder.getOwnPermission().isAdmin() ? ":shared-owner/>" : ":shared/>");
            }
        }
        return StringBuilder.toString();
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        Locale locale = this.factory.getUser().getLocale();
        String name = null != locale ? folder.getLocalizedName(locale) : folder.getName();
        String ownerName = SharedType.getInstance().equals(this.folder.getType()) ? getOwnerName() : null;
        if (null != ownerName && 0 < ownerName.length()) {
            return String.format("%s (%s)", name, ownerName);
        } else {
            return name;
        }
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        if (false == this.folder.isDefault() && PrivateType.getInstance().equals(this.folder.getType())) {
            this.folder.setName(displayName);
            try {
                factory.getFolderService().updateFolder(folder, this.folder.getLastModified(), factory.getSession(), null);
            } catch (OXException e) {
                throw protocolException(e);
            }
        } else {
            throw protocolException(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        if (null == this.folder) {
            throw protocolException(HttpServletResponse.SC_NOT_FOUND);
        } else if (this.folder.isDefault()) {
            throw protocolException(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                factory.getFolderService().deleteFolder(factory.getState().getTreeID(), this.folder.getID(),
                    this.folder.getLastModifiedUTC(), factory.getSession(), null);
            } catch (OXException e) {
                throw protocolException(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

    protected abstract List<T> getObjectsInRange(Date from, Date until) throws OXException;

    private static FilterAnalyzer VEVENT_RANGE_QUERY_ANALYZER = new FilterAnalyzerBuilder()
        .compFilter("VCALENDAR")
            .compFilter("VEVENT")
                .timeRange().capture().end()
            .end()
        .end()
    .build();

    private static FilterAnalyzer VTODO_RANGE_QUERY_ANALYZER = new FilterAnalyzerBuilder()
        .compFilter("VCALENDAR")
            .compFilter("VTODO")
                .timeRange().capture().end()
            .end()
        .end()
    .build();


    @Override
    public List<WebdavResource> filter(Filter filter) throws WebdavProtocolException {
        List<Object> arguments = new ArrayList<Object>(2);
        if (VEVENT_RANGE_QUERY_ANALYZER.match(filter, arguments) || VTODO_RANGE_QUERY_ANALYZER.match(filter, arguments)) {
            Date from = arguments.isEmpty() ? null : toDate(arguments.get(0));
            if (null == from || from.before(getIntervalStart())) {
                from = getIntervalStart();
            }
            Date until = arguments.isEmpty() ? null : toDate(arguments.get(1));
            if (null == until || until.after(getIntervalEnd())) {
                until = getIntervalEnd();
            }
            try {
                List<T> objects = this.getObjectsInRange(from, until);
                if (null == objects || 0 == objects.size()) {
                    return Collections.emptyList();
                } else {
                    List<WebdavResource> resources = new ArrayList<WebdavResource>(objects.size());
                    for (T object : this.getObjectsInRange(from, until)) {
                        resources.add(createResource(object, constructPathForChildResource(object)));
                    }
                    return resources;
                }
            } catch (OXException e) {
                throw protocolException(e);
            }
        } else {
            throw protocolException(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private static Date toDate(Object object) {
        long tstamp = (Long) object;
        if (tstamp == -1) {
            return null;
        }
        return new Date(tstamp);
    }

    private String getOwnerName() {
        User owner = null;
        try {
            owner = this.getOwner();
        } catch (OXException e) {
            LOG.error("Error resolving owner", e);
        }
        return null != owner ? owner.getDisplayName() : null;
    }

}
