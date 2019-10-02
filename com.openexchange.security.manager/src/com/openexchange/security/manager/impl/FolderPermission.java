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

package com.openexchange.security.manager.impl;

import java.io.File;
import org.osgi.service.permissionadmin.PermissionInfo;

/**
 * {@link FolderPermission} is used for adding a folder/file to securityManager
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class FolderPermission {

    /**
     *
     * {@link Decision} If the rule should allow or deny the folder
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v7.10.3
     */
    public enum Decision {
        ALLOW("allow"),
        DENY("deny");

        private String value;

        Decision(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /**
     * What should be allowed in this folder.  Read/Write/ or both
     * {@link Allow}
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v7.10.3
     */
    public enum Allow {
        READ("READ"),
        WRITE("WRITE"),
        READ_WRITE("READ, WRITE");

        private String value;

        Allow(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /**
     * If this link refers to a file, just directory, or directory and all sub-directories(recursive)
     * {@link Type}
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v7.10.3
     */
    public enum Type {
        FILE, DIRECTORY, RECURSIVE;
    }

    private final Allow allow;
    private final Decision decision;
    private final String name;
    private final String directory;
    private final Type type;
    private final String FILE_SEPARATOR = File.separator;

    /**
     * Initializes a new {@link FolderPermission}.
     * @param name
     * @param directory
     * @param decision
     * @param allow
     */
    public FolderPermission(String name, String directory, Decision decision, Allow allow, Type type) {
        this.name = name;
        this.directory = constructDirectory(directory, type);
        this.decision = decision;
        this.allow = allow;
        this.type = type;
    }

    /**
     * Confirm that directory ends with file_separator
     *
     * @param dir
     * @param type
     */
    private String constructDirectory(String dir, Type type) {
        if (type == Type.FILE) {
            return dir;
        }
        if (!dir.endsWith(FILE_SEPARATOR)) {
            dir += FILE_SEPARATOR;
        }
        return dir;
    }

    /**
     * Return a new permission info populated with the folder information
     * @return
     */
    public PermissionInfo getPermissionInfo() {
        return new PermissionInfo("java.io.FilePermission", this.directory, this.allow.getValue());
    }

    /**
     * Return a new permission info populated with the folder information
     * @return
     */
    public PermissionInfo getRecursivePermissionInfo() {
        return new PermissionInfo("java.io.FilePermission", this.directory + "-", this.allow.getValue());
    }

    /**
     * Return the decision for this action
     *
     * @return
     */
    public String getDecision () {
        return this.decision.getValue();
    }

    /**
     * Get the name of this action.  Must be unique
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the name of the recursive action;
     *
     * @return the name plus "recursive"
     */
    public String getRecursiveName() {
        return this.name + "-recursive";
    }

    /**
     * Returns the type of FolderPermission
     *
     * @return
     */
    public Type getType() {
        return this.type;
    }
}
