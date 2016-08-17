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

package com.openexchange.dav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.mixins.CalendarColor;
import com.openexchange.dav.mixins.SupportedCalendarComponentSet;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link PlaceholderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class PlaceholderCollection<T extends CommonObject> extends CommonFolderCollection<T> {

    private String displayName;
    private ContentType contentType;
    private Map<String, Object> meta;
    private String treeID;

    /**
     * Initializes a new {@link PlaceholderCollection}.
     *
     * @param factory The underlying factory
     * @param url The target WebDAV path
     * @param contentType The default content type to use
     * @param treeID The tree identifier to use
     */
    public PlaceholderCollection(DAVFactory factory, WebdavPath url, ContentType contentType, String treeID) throws OXException {
        super(factory, url, null);
        this.displayName = url.name();
        this.contentType = contentType;
        this.treeID = treeID;
        this.meta = new HashMap<String, Object>();
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void putProperty(WebdavProperty prop) throws WebdavProtocolException {
        internalPutProperty(prop);
    }

    @Override
    protected void internalPutProperty(WebdavProperty property) throws WebdavProtocolException {
        Element element = DAVProperty.class.isInstance(property) ? ((DAVProperty) property).getElement() : null;
        if (DAVProtocol.CAL_NS.getURI().equals(property.getNamespace()) && "supported-calendar-component-set".equals(property.getName())) {
            String value = null;
            if (null != element) {
                for (Element compElement : element.getChildren("comp", DAVProtocol.CAL_NS)) {
                    String name = null != compElement.getAttribute("name") ? compElement.getAttribute("name").getValue() : null;
                    if (SupportedCalendarComponentSet.VTODO.equalsIgnoreCase(name) || SupportedCalendarComponentSet.VEVENT.equalsIgnoreCase(name)) {
                        value = name;
                    }
                }
            } else {
                value = property.getValue();
                if (property.isXML()) {
                    // try to extract comp attribute from xml fragment
                    Pattern compNameRegex = Pattern.compile("name=\\\"(.+?)\\\"",
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                    Matcher regexMatcher = compNameRegex.matcher(value);
                    if (regexMatcher.find()) {
                        value = regexMatcher.group(1);
                    }
                }
            }
            if (SupportedCalendarComponentSet.VTODO.equalsIgnoreCase(value)) {
                contentType = TaskContentType.getInstance();
            } else if (SupportedCalendarComponentSet.VEVENT.equalsIgnoreCase(value)) {
                contentType = CalendarContentType.getInstance();
            } else {
                throw protocolException(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
        } else if (DAVProtocol.DAV_NS.getURI().equals(property.getNamespace()) && "resourcetype".equals(property.getName()) && null != element) {
            if (null != element.getChild("addressbook", DAVProtocol.CARD_NS)) {
                contentType = ContactContentType.getInstance();
            } else if (null != element.getChild("calendar", DAVProtocol.CAL_NS)) {
                contentType = CalendarContentType.getInstance();
            } else {
                throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "valid-resourcetype", getUrl(), HttpServletResponse.SC_CONFLICT);
            }
        } else if (CalendarColor.NAMESPACE.getURI().equals(property.getNamespace()) && CalendarColor.NAME.equals(property.getName())) {
            String value = CalendarColor.parse(property);
            meta.put("color", value);
        } else if (DAVProtocol.DAV_NS.getURI().equals(property.getNamespace()) && "displayname".equals(property.getName())) {
            displayName = null != element ? element.getValue() : property.getValue();
        } else if (factory.getProtocol().isProtected(property.getNamespace(), property.getName())) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "cannot-modify-protected-property", getUrl(), HttpServletResponse.SC_CONFLICT);
        }
    }

    @Override
    public void create() throws WebdavProtocolException {
        try {
            FolderService folderService = factory.requireService(FolderService.class);
            UserizedFolder parentFolder = folderService.getDefaultFolder(factory.getUser(), treeID, contentType, factory.getSession(), null);
            AbstractFolder folder = getFolderToUpdate();
            folder.setParentID(parentFolder.getID());
            folder.setName(displayName);
            folder.setType(parentFolder.getType());
            folder.setContentType(contentType);
            folder.setTreeID(parentFolder.getTreeID());
            folder.setPermissions(new Permission[] { getDefaultAdminPermissions(factory.getUser().getId()) });
            meta.put("resourceName", getUrl().name());
            folder.setMeta(meta);
            factory.requireService(FolderService.class).createFolder(folder, factory.getSession(), null);
        } catch (OXException e) {
            if ("FLD-0092".equals(e.getErrorCode())) {
                /*
                 * 'Unsupported character "..." in field "Folder name".
                 */
                ProblematicAttribute[] problematics = e.getProblematics();
                if (null != problematics && 0 < problematics.length && null != problematics[0] && IncorrectString.class.isInstance(problematics[0])) {
                    IncorrectString incorrectString = ((IncorrectString) problematics[0]);
                    if (FolderObject.FOLDER_NAME == incorrectString.getId()) {
                        String correctedDisplayName = displayName.replace(incorrectString.getIncorrectString(), "");
                        if (false == correctedDisplayName.equals(displayName)) {
                            displayName = correctedDisplayName;
                            create();
                            return;
                        }
                    }
                }
            }
            throw protocolException(getUrl(), e);
        }
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return false;
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        this.displayName = displayName;
    }

    @Override
    protected Collection<T> getModifiedObjects(Date since) throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected Collection<T> getDeletedObjects(Date since) throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected Collection<T> getObjects() throws OXException {
        return Collections.emptyList();
    }

    @Override
    protected AbstractResource createResource(T object, WebdavPath url) throws OXException {
        throw protocolException(getUrl(), HttpServletResponse.SC_CONFLICT); // https://tools.ietf.org/html/rfc2518#section-8.7.1
    }

    @Override
    protected T getObject(String resourceName) throws OXException {
        return null;
    }

    @Override
    protected String getFileExtension() {
        return "";
    }

    protected Permission getDefaultAdminPermissions(int entity) {
        DefaultPermission permission = new DefaultPermission();
        permission.setMaxPermissions();
        permission.setEntity(entity);
        return permission;
    }

}
