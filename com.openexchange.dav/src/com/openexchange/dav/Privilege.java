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

package com.openexchange.dav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link Privilege}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public enum Privilege {

    /**
     * <b>DAV:read Privilege</b>
     * <p/>
     * The read privilege controls methods that return information about the
     * state of the resource, including the resource's properties.  Affected
     * methods include GET and PROPFIND.  Any implementation-defined
     * privilege that also controls access to GET and PROPFIND must be
     * aggregated under DAV:read - if an ACL grants access to DAV:read, the
     * client may expect that no other privilege needs to be granted to have
     * access to GET and PROPFIND.  Additionally, the read privilege MUST
     * control the OPTIONS method.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.1">RFC 3744, section 3.1</a>
     */
    READ("read", "Read any object"),

    /**
     * <b>DAV:write Privilege</b>
     * <p/>
     * The write privilege controls methods that lock a resource or modify
     * the content, dead properties, or (in the case of a collection)
     * membership of the resource, such as PUT and PROPPATCH.  Note that
     * state modification is also controlled via locking (see section 5.3 of
     * [RFC2518]), so effective write access requires that both write
     * privileges and write locking requirements are satisfied.  Any
     * implementation-defined privilege that also controls access to methods
     * modifying content, dead properties or collection membership must be
     * aggregated under DAV:write, e.g., if an ACL grants access to
     * DAV:write, the client may expect that no other privilege needs to be
     * granted to have access to PUT and PROPPATCH.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.2">RFC 3744, section 3.2</a>
     */
    WRITE("write", "Write any object"),

    /**
     * <b>DAV:write-properties Privilege</b>
     * <p/>
     * The DAV:write-properties privilege controls methods that modify the
     * dead properties of the resource, such as PROPPATCH.  Whether this
     * privilege may be used to control access to any live properties is
     * determined by the implementation.  Any implementation-defined
     * privilege that also controls access to methods modifying dead
     * properties must be aggregated under DAV:write-properties - e.g., if
     * an ACL grants access to DAV:write-properties, the client can safely
     * expect that no other privilege needs to be granted to have access to
     * PROPPATCH.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.3">RFC 3744, section 3.3</a>
     */
    WRITE_PROPERTIES("write-properties", "Write properties"),

    /**
     * <b>DAV:write-content Privilege</b>
     * <p/>
     * The DAV:write-content privilege controls methods that modify the
     * content of an existing resource, such as PUT.  Any implementation-
     * defined privilege that also controls access to content must be
     * aggregated under DAV:write-content - e.g., if an ACL grants access to
     * DAV:write-content, the client can safely expect that no other
     * privilege needs to be granted to have access to PUT.  Note that PUT -
     * when applied to an unmapped URI - creates a new resource and
     * therefore is controlled by the DAV:bind privilege on the parent
     * collection.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.4">RFC 3744, section 3.4</a>
     */
    WRITE_CONTENT("write-content", "Write resource content"),

    /**
     * <b>DAV:unlock Privilege</b>
     * <p/>
     * The DAV:unlock privilege controls the use of the UNLOCK method by a
     * principal other than the lock owner (the principal that created a
     * lock can always perform an UNLOCK).  While the set of users who may
     * lock a resource is most commonly the same set of users who may modify
     * a resource, servers may allow various kinds of administrators to
     * unlock resources locked by others.  Any privilege controlling access
     * by non-lock owners to UNLOCK MUST be aggregated under DAV:unlock.
     *
     * A lock owner can always remove a lock by issuing an UNLOCK with the
     * correct lock token and authentication credentials.  That is, even if
     * a principal does not have DAV:unlock privilege, they can still remove
     * locks they own.  Principals other than the lock owner can remove a
     * lock only if they have DAV:unlock privilege and they issue an UNLOCK
     * with the correct lock token.  Lock timeout is not affected by the
     * DAV:unlock privilege.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.5">RFC 3744, section 3.5</a>
     */
    UNLOCK("unlock", "Unlock resource"),

    /**
     * <b>DAV:read-acl Privilege</b>
     * <p/>
     * The DAV:read-acl privilege controls the use of PROPFIND to retrieve
     * the DAV:acl property of the resource.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.6">RFC 3744, section 3.6</a>
     */
    READ_ACL("read-acl", "Read ACL"),

    /**
     * <b>DAV:read-current-user-privilege-set Privilege</b>
     * <p/>
     * The DAV:read-current-user-privilege-set privilege controls the use of
     * PROPFIND to retrieve the DAV:current-user-privilege-set property of
     * the resource.
     *
     * Clients are intended to use this property to visually indicate in
     * their UI items that are dependent on the permissions of a resource,
     * for example, by graying out resources that are not writable.
     *
     * This privilege is separate from DAV:read-acl because there is a need
     * to allow most users access to the privileges permitted the current
     * user (due to its use in creating the UI), while the full ACL contains
     * information that may not be appropriate for the current authenticated
     * user.  As a result, the set of users who can view the full ACL is
     * expected to be much smaller than those who can read the current user
     * privilege set, and hence distinct privileges are needed for each.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.7">RFC 3744, section 3.7</a>
     */
    READ_CURRENT_USER_PRIVILEGE_SET("read-current-user-privilege-set", "Read current user privilege set property"),

    /**
     * <b>DAV:write-acl Privilege</b>
     * <p/>
     * The DAV:write-acl privilege controls use of the ACL method to modify
     * the DAV:acl property of the resource.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.8">RFC 3744, section 3.8</a>
     */
    WRITE_ACL("write-acl", "Write ACL"),

    /**
     * <b>DAV:bind Privilege</b>
     * <p/>
     * The DAV:bind privilege allows a method to add a new member URL to the
     * specified collection (for example via PUT or MKCOL).  It is ignored
     * for resources that are not collections.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.9">RFC 3744, section 3.9</a>
     */
    BIND("bind", "Add resources to the collection"),

    /**
     * <b>DAV:unbind Privilege</b>
     * <p/>
     * The DAV:unbind privilege allows a method to remove a member URL from
     * the specified collection (for example via DELETE or MOVE).  It is
     * ignored for resources that are not collections.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.10">RFC 3744, section 3.10</a>
     */
    UNBIND("unbind", "Remove resources from the collection"),

    /**
     * <b>DAV:share Privilege</b>
     * <p/>
     * The share element is a WebDAV ACL [RFC3744] privilege that allows a
     * client to inspect whether a user may be allowed to share a resource.
     *
     * @see <a href="https://tools.ietf.org/html/draft-pot-webdav-resource-sharing-03#section-5.2">draft-pot-webdav-resource-sharing, section 5.2</a>
     */
    SHARE("share", "Share the collection"),

    /**
     * <b>DAV: Privilege</b>
     * <p/>
     * DAV:all is an aggregate privilege that contains the entire set of
     * privileges that can be applied to the resource.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3744#section-3.11">RFC 3744, section 3.11</a>
     */
    ALL("all", "All privileges"),
    ;

    /**
     * Gets the DAV privilege name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a human-readable description of the ACL.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    private final String name;
    private final String description;

    /**
     * Initializes a new {@link Privilege}.
     *
     * @param name The DAV privilege name
     * @param description A human-readable description of the ACL
     */
    private Privilege(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Privilege parse(String name) {
        for (Privilege privilege : Privilege.values()) {
            if (privilege.getName().equals(name)) {
                return privilege;
            }
        }
        return null;
    }

    /**
     * Gets the granted privileges applying to a specific folder permission.
     *
     * @param permission The permission to get the applying privileges for
     * @return The granted privileges in a list
     */
    public static List<Privilege> getApplying(Permission permission) {
        List<Privilege> applying = new ArrayList<Privilege>();
        applying.add(READ_ACL);
        applying.add(READ_CURRENT_USER_PRIVILEGE_SET);
        if (permission.getReadPermission() >= Permission.READ_OWN_OBJECTS) {
            applying.add(READ);
        }
        if (permission.getWritePermission() >= Permission.WRITE_OWN_OBJECTS) {
            applying.add(WRITE_CONTENT);
        }
        if (permission.isAdmin()) {
            applying.add(WRITE);
            applying.add(WRITE_PROPERTIES);
            applying.add(WRITE_ACL);
        }
        if (permission.getFolderPermission() >= Permission.CREATE_OBJECTS_IN_FOLDER) {
            applying.add(BIND);
        }
        if (permission.getDeletePermission() > Permission.DELETE_OWN_OBJECTS) {
            applying.add(UNBIND);
        }
        return applying;
    }

    public static Permission getApplying(List<Privilege> privileges) throws WebdavProtocolException {
        DefaultPermission permission = new DefaultPermission();
        HashSet<Privilege> setPrivileges = new HashSet<Privilege>(privileges);
        if (setPrivileges.contains(ALL)) {
            if (1 < setPrivileges.size()) {
                throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", new WebdavPath(), HttpServletResponse.SC_FORBIDDEN);
            }
            permission.setMaxPermissions();
        }
        if (false == setPrivileges.contains(READ_ACL) || false == setPrivileges.contains(READ_CURRENT_USER_PRIVILEGE_SET)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "not-supported-privilege", new WebdavPath(), HttpServletResponse.SC_FORBIDDEN);
        }
        if (setPrivileges.contains(READ)) {
            permission.setFolderPermission(Permission.READ_FOLDER);
            permission.setReadPermission(Permission.READ_ALL_OBJECTS);
        }
        if (setPrivileges.contains(WRITE_CONTENT)) {
            permission.setFolderPermission(Permission.READ_FOLDER);
            permission.setWritePermission(Permission.WRITE_ALL_OBJECTS);
        }
        if (setPrivileges.contains(WRITE_ACL) || setPrivileges.contains(WRITE) || setPrivileges.contains(WRITE_PROPERTIES)) {
            if (false == setPrivileges.containsAll(Arrays.asList(WRITE, WRITE_ACL, WRITE_PROPERTIES))) {
                throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "no-ace-conflict", new WebdavPath(), HttpServletResponse.SC_FORBIDDEN);
            }
            permission.setAdmin(true);
        }
        if (setPrivileges.contains(BIND)) {
            permission.setFolderPermission(Permission.CREATE_SUB_FOLDERS);
        }
        if (setPrivileges.contains(UNBIND)) {
            permission.setDeletePermission(Permission.DELETE_ALL_OBJECTS);
        }
        return permission;
    }

}
