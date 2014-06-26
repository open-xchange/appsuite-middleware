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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link UndecidedFolderCollection}
 *
 * WebDAV task- and calendar-collections that are about to be created
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UndecidedFolderCollection extends CalDAVFolderCollection<CalendarObject> {

    private String displayName;
    private ContentType contentType;

    /**
     * Initializes a new {@link UndecidedFolderCollection}.
     *
     * @param factory The underlying CalDAV factory
     * @param url The target WebDAV path
     * @throws OXException
     */
    public UndecidedFolderCollection(GroupwareCaldavFactory factory, WebdavPath url) throws OXException {
        super(factory, url, null);
        this.contentType = CalendarContentType.getInstance();
        this.displayName = "New Folder";
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
        if (CaldavProtocol.CAL_NS.getURI().equals(prop.getNamespace()) && "supported-calendar-component-set".equals(prop.getName())) {
            String value = prop.getValue();
            if (prop.isXML()) {
                // try to extract comp attribute from xml fragment
                Pattern compNameRegex = Pattern.compile("name=\\\"(.+?)\\\"",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                Matcher regexMatcher = compNameRegex.matcher(value);
                if (regexMatcher.find()) {
                    value = regexMatcher.group(1);
                }
            }
            if (SupportedCalendarComponentSet.VTODO.equalsIgnoreCase(value)) {
                this.contentType = TaskContentType.getInstance();
            } else if (SupportedCalendarComponentSet.VEVENT.equalsIgnoreCase(value)) {
                this.contentType = CalendarContentType.getInstance();
            } else {
                throw protocolException(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    public void create() throws WebdavProtocolException {
        try {
            UserizedFolder parentFolder = factory.getFolderService().getDefaultFolder(factory.getUser(), factory.getState().getTreeID(),
                this.contentType, factory.getSession(), null);
            FolderObject newFolder = new FolderObject();
            newFolder.setFolderName(displayName);
            newFolder.setParentFolderID(Tools.parse(parentFolder.getID()));
            newFolder.setType(FolderObject.PRIVATE);
            newFolder.setModule(contentType.getModule());
            newFolder.addPermission(getDefaultPermissions());
            newFolder = OXFolderManager.getInstance(factory.getSession()).createFolder(newFolder, true, System.currentTimeMillis());
        } catch (OXException e) {
            throw protocolException(e, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return false;
    }

    @Override
    public void save() throws WebdavProtocolException {
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        this.displayName = displayName;
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
    }

    @Override
    protected boolean isSupported(CalendarObject object) throws OXException {
        return false;
    }

    @Override
    protected List<CalendarObject> getObjectsInRange(Date from, Date until) throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected Collection<CalendarObject> getModifiedObjects(Date since) throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected Collection<CalendarObject> getDeletedObjects(Date since) throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected Collection<CalendarObject> getObjects() throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected AbstractResource createResource(CalendarObject object, WebdavPath url) throws OXException {
        throw protocolException(HttpServletResponse.SC_FORBIDDEN);
    }

    private OCLPermission getDefaultPermissions() {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(factory.getUser().getId());
        permission.setFolderAdmin(true);
        permission.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        permission.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        permission.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        permission.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        permission.setGroupPermission(false);
        return permission;
    }

}
