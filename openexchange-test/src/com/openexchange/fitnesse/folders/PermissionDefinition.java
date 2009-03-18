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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.fitnesse.folders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.group.GroupResolver;
import com.openexchange.ajax.user.UserResolver;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.group.Group;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import static com.openexchange.server.impl.OCLPermission.*;

/**
 * {@link PermissionDefinition}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PermissionDefinition {

    private String name;

    private AJAXClient client;

    private ArrayList<OCLPermission> oclPermissions;

    private UserResolver userResolver;

    private GroupResolver groupResolver;

    /**
     * Initializes a new {@link PermissionDefinition}.
     */
    public PermissionDefinition(List<List<String>> table, AJAXClient client) throws FitnesseException {
        super();
        this.name = table.get(0).get(0);
        this.client = client;
        this.userResolver = new UserResolver(client);
        this.groupResolver = new GroupResolver(client);

        parsePermissionDefinitions(table.subList(1, table.size()));
    }

    protected void parsePermissionDefinitions(List<List<String>> permissions) throws FitnesseException {
        this.oclPermissions = new ArrayList<OCLPermission>();
        for (List<String> permissionLine : permissions) {
            String entity = permissionLine.get(0);
            String rights = permissionLine.get(1);
            OCLPermission oclPermission = parseRights(rights);
            resolveEntity(entity, oclPermission);
            oclPermissions.add(oclPermission);
        }
    }

    private void resolveEntity(String entity, OCLPermission permission) throws FitnesseException {
        boolean resolved = false;
        if (entity.equals("everybody")) {
            permission.setEntity(ALL_GROUPS_AND_USERS);
            permission.setGroupPermission(true);
            return;
        }
        if (entity.equals("myself")) {
            try {
                permission.setEntity(client.getValues().getUserId());
            } catch (AjaxException e) {
                throw new FitnesseException(e);
            } catch (IOException e) {
                throw new FitnesseException(e);
            } catch (SAXException e) {
                throw new FitnesseException(e);
            } catch (JSONException e) {
                throw new FitnesseException(e);
            }
            return;
        }
        if (entity.startsWith("group:")) {
            resolved = resolveGroup(entity.substring(6), permission);
        } else if (entity.startsWith("user:")) {
            resolved = resolveUser(entity.substring(5), permission);
        } else {
            if (!resolveUser(entity, permission)) {
                resolved = resolveGroup(entity, permission);
            } else {
                resolved = true;
            }
        }
        if (!resolved) {
            throw new FitnesseException("Could not resolve: " + entity);
        }
    }

    /**
     * @param substring
     * @param permission
     * @return
     */
    private boolean resolveUser(String searchPattern, OCLPermission permission) throws FitnesseException {
        try {
            User[] users = userResolver.resolveUser("*"+searchPattern+"*");
            if (users.length == 0) {
                return false;
            }
            permission.setEntity(users[0].getId());
            permission.setGroupPermission(false);
            return true;
        } catch (AjaxException e) {
            throw new FitnesseException(e);
        } catch (IOException e) {
            throw new FitnesseException(e);
        } catch (SAXException e) {
            throw new FitnesseException(e);
        } catch (JSONException e) {
            throw new FitnesseException(e);
        }
    }

    /**
     * @param substring
     * @param permission
     * @return
     * @throws FitnesseException
     */
    private boolean resolveGroup(String searchPattern, OCLPermission permission) throws FitnesseException {
        try {
            Group[] groups = groupResolver.resolveGroup("*"+searchPattern+"*");
            if (groups.length == 0) {
                return false;
            }
            permission.setEntity(groups[0].getIdentifier());
            permission.setGroupPermission(true);
            return true;
        } catch (AjaxException e) {
            throw new FitnesseException(e);
        } catch (OXJSONException e) {
            throw new FitnesseException(e);
        } catch (IOException e) {
            throw new FitnesseException(e);
        } catch (SAXException e) {
            throw new FitnesseException(e);
        } catch (JSONException e) {
            throw new FitnesseException(e);
        }
    }

    /**
     * @param rights
     * @return
     */
    protected OCLPermission parseRights(String rights) {
        OCLPermission oclPermission = new OCLPermission();
        oclPermission.setAllPermission(NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        // No Permissions! No Permissions! No Permissions! No Permissions! *jumps up and down*
        
        String[] permissionStrings = rights.split("\\s*,\\s*");
        for (String permissionString : permissionStrings) {
            if (permissionString.startsWith("read")) {
                if (permissionString.endsWith("own")) {
                    oclPermission.setReadObjectPermission(READ_OWN_OBJECTS);
                } else if (permissionString.endsWith("all")) {
                    oclPermission.setReadObjectPermission(READ_ALL_OBJECTS);
                } else if (permissionString.endsWith("admin")) {
                    oclPermission.setReadObjectPermission(ADMIN_PERMISSION);
                }
            } else if (permissionString.startsWith("write")) {
                if (permissionString.endsWith("own")) {
                    oclPermission.setWriteObjectPermission(WRITE_OWN_OBJECTS);
                } else if (permissionString.endsWith("all")) {
                    oclPermission.setWriteObjectPermission(WRITE_ALL_OBJECTS);
                } else if (permissionString.endsWith("admin")) {
                    oclPermission.setWriteObjectPermission(ADMIN_PERMISSION);
                }
            } else if (permissionString.startsWith("delete")) {
                if (permissionString.endsWith("own")) {
                    oclPermission.setDeleteObjectPermission(DELETE_OWN_OBJECTS);
                } else if (permissionString.endsWith("all")) {
                    oclPermission.setDeleteObjectPermission(DELETE_ALL_OBJECTS);
                } else if (permissionString.endsWith("admin")) {
                    oclPermission.setDeleteObjectPermission(ADMIN_PERMISSION);
                }
            } else if (permissionString.equals("folder_admin")) {
                oclPermission.setFolderAdmin(true);
                oclPermission.setFolderPermission(ADMIN_PERMISSION);
            } else if (permissionString.equals("see_folder")) {
                oclPermission.setFolderPermission(READ_FOLDER);
            } else if (permissionString.equals("create_objects")) {
                oclPermission.setFolderPermission(CREATE_OBJECTS_IN_FOLDER);
            } else if (permissionString.equals("create_subsfolders")) {
                oclPermission.setFolderPermission(CREATE_SUB_FOLDERS);
            } else if (permissionString.equals("grant_permissions")) {
                oclPermission.setFolderAdmin(true);
            }
        }
        return oclPermission;
    }

    public String getFixtureName() {
        return name;
    }

    public List<OCLPermission> getPermissions() {
        return oclPermissions;
    }

}
