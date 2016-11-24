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

package com.openexchange.dav.actions;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.dav.CUType;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.Privilege;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link ACLAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ACLAction extends DAVAction {

    /**
     * Initializes a new {@link ACLAction}.
     *
     * @param protocol The underlying protocol
     */
    public ACLAction(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * check if applicable for resource
         */
        WebdavResource resource = request.getResource();
        if (null == resource || false == resource.isCollection() || false == FolderCollection.class.isInstance(resource)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        FolderCollection<?> folderCollection = (FolderCollection<?>) resource;
        UserizedFolder folder = folderCollection.getFolder();
        if (false == folder.getOwnPermission().isAdmin()) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        /*
         * parse target permissions
         */
        Document requestBody = requireRequestBody(request);
        Element aclElement = requestBody.getRootElement();
        if (null == aclElement || false == "acl".equals(aclElement.getName()) || false == DAVProtocol.DAV_NS.equals(aclElement.getNamespace())) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        List<Permission> permissions = parsePermissions(aclElement.getChildren("ace", DAVProtocol.DAV_NS));
        if (null == permissions || 0 == permissions.size()) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        /*
         * perform the folder update
         */
        AbstractFolder updatableFolder = new AbstractFolder() {

            private static final long serialVersionUID = 9014282914929095490L;

            @Override
            public boolean isGlobalID() {
                return false;
            }
        };
        updatableFolder.setID(folder.getID());
        updatableFolder.setTreeID(folder.getTreeID());
        updatableFolder.setType(folder.getType());
        updatableFolder.setParentID(folder.getParentID());
        updatableFolder.setPermissions(permissions.toArray(new Permission[permissions.size()]));
        DAVFactory factory = folderCollection.getFactory();
        try {
            factory.requireService(FolderService.class).updateFolder(updatableFolder, folder.getLastModifiedUTC(), factory.getSession(), null);
        } catch (OXException e) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private List<Permission> parsePermissions(List<Element> aceElements) throws WebdavProtocolException {
        if (null == aceElements) {
            return null;
        }
        List<Permission> permissions = new ArrayList<Permission>(aceElements.size());
        for (Element aceElement : aceElements) {
            permissions.add(parsePermission(aceElement));
        }
        return permissions;
    }

    private Permission parsePermission(Element aceElement) throws WebdavProtocolException {
        if (null == aceElement) {
            return null;
        }
        /*
         * parse granted privileges
         */
        List<Element> grantElements = aceElement.getChildren("grant", DAVProtocol.DAV_NS);
        if (null == grantElements || 0 == grantElements.size() || null != aceElement.getChild("deny", DAVProtocol.DAV_NS)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "grant-only", HttpServletResponse.SC_FORBIDDEN);
        }
        if (null != aceElement.getChild("invert", DAVProtocol.DAV_NS)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-invert", HttpServletResponse.SC_FORBIDDEN);
        }
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (Element grantElement : grantElements) {
            for (Element privilegeElement : grantElement.getChildren("privilege", DAVProtocol.DAV_NS)) {
                for (Element element : privilegeElement.getChildren()) {
                    Privilege privilege = Privilege.parse(element.getName());
                    if (null == privilege) {
                        throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "not-supported-privilege", HttpServletResponse.SC_FORBIDDEN);
                    }
                    privileges.add(privilege);
                }
            }
        }
        Permission permission = Privilege.getApplying(privileges);
        /*
         * parse targeted principal
         */
        Element principalElement = aceElement.getChild("principal", DAVProtocol.DAV_NS);
        if (null == principalElement) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "missing-required-principal", HttpServletResponse.SC_FORBIDDEN);
        }
        PrincipalURL principalURL = PrincipalURL.parse(principalElement.getValue());
        if (null == principalURL) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "recognized-principal", HttpServletResponse.SC_FORBIDDEN);
        }
        if (false == CUType.INDIVIDUAL.equals(principalURL.getType()) && false == CUType.GROUP.equals(principalURL.getType())) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "allowed-principal", HttpServletResponse.SC_FORBIDDEN);
        }
        permission.setEntity(principalURL.getPrincipalID());
        permission.setGroup(CUType.GROUP.equals(principalURL.getType()));
        return permission;
    }

}
