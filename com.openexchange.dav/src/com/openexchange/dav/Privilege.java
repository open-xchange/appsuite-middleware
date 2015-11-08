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

package com.openexchange.dav;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.folderstorage.Permission;

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
    WRITE("write", "Write any object"),
    WRITE_PROPERTIES("write-properties", "Write properties"),
    WRITE_CONTENT("write-content", "Write resource content"),
    UNLOCK("unlock", "Unlock resource"),
    READ_ACL("read-acl", "Read ACL"),
    READ_CURRENT_USER_PRIVILEGE_SET("read-current-user-privilege-set", "Read current user privilege set property"),
    WRITE_ACL("write-acl", "Write ACL"),
    BIND("bind", "Add resources to the collection"),
    UNBIND("unbind", "Remove resources from the collection"),
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

}
