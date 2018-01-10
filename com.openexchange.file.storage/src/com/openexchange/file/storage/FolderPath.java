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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.java.Strings;

/**
 * {@link FolderPath} - A path for a folder consisting of the individual names from the folder to the root folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class FolderPath implements Iterable<String> {

    /** The folder path type determines to what starting folder a path belongs */
    public static enum FolderPathType {
        /** The folder path starts from user's default file storage folder */
        PRIVATE,
        /** The folder path starts from public file storage folder */
        PUBLIC,
        /** The folder path starts from shared file storage folder */
        SHARED,
        /** The folder path starts from undefined location, in which case it falls-back to user's default file storage folder */
        UNDEFINED;
    }

    /** The empty folder path */
    public static final FolderPath EMPTY_PATH = new FolderPath(ImmutableList.of()) {

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }
    };

    /**
     * Parses the folder path from specified path string
     *
     * @param folderPath The path string
     * @return The parsed instance
     */
    public static FolderPath parseFrom(String folderPath) {
        if (Strings.isEmpty(folderPath)) {
            return EMPTY_PATH;
        }

        return new FolderPath(Strings.splitBy(folderPath, '/', true));
    }

    /**
     * Creates a folder path consisting of the names from specified <code>Iterable</code> instance
     *
     * @param names The names to add
     * @return The folder path
     */
    public static FolderPath copyOf(Iterable<String> names) {
        if (null == names) {
            return null;
        }

        return new FolderPath(names);
    }

    /**
     * Creates a new builder instance.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for an instance of <code>FolderPath</code> */
    public static class Builder {

        private final List<String> names;

        Builder() {
            super();
            names = new ArrayList<>(8);
        }

        /**
         * Adds specified (untranslated) folder name to this builder.
         *
         * @param name The name to add
         * @return This builder
         */
        public Builder addNameToPath(String name) {
            if (null == name) {
                throw new IllegalArgumentException("Name must not be null");
            }
            names.add(name);
            return this;
        }

        /**
         * Builds the instance of <code>FolderPath</code> from this builder's arguments
         *
         * @return The instance of <code>FolderPath</code>
         */
        public FolderPath build() {
            return names.isEmpty() ? EMPTY_PATH : new FolderPath(names);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    private final static String PUBLIC_INFOSTORE = "15"; // aligned to 'c.o.groupware.container.FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID'
    private final static String SHARED_INFOSTORE = "10"; // aligned to 'c.o.groupware.container.FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID'

    private static FolderPathType determineType(List<String> names) {
        FolderPathType type = FolderPathType.UNDEFINED;
        if (false == names.isEmpty()) {
            String firstName = names.get(0);
            if (Strings.isNotEmpty(firstName)) {
                if (PUBLIC_INFOSTORE.equals(firstName)) {
                    type = FolderPathType.PUBLIC;
                } else if (SHARED_INFOSTORE.equals(firstName)) {
                    type = FolderPathType.SHARED;
                } else {
                    type = FolderPathType.PRIVATE;
                }
            }
        }
        return type;
    }

    private final List<String> names;
    private final int size;
    private final FolderPathType type;

    /**
     * Initializes a new {@link FolderPath}.
     */
    FolderPath(List<String> names) {
        super();
        this.names = ImmutableList.copyOf(names);
        size = names.size();
        this.type = determineType(this.names);
    }

    /**
     * Initializes a new {@link FolderPath}.
     */
    FolderPath(Iterable<String> names) {
        super();
        ImmutableList.Builder<String> b = ImmutableList.builder();
        for (String name : names) {
            b.add(name);
        }
        this.names = b.build();
        size = this.names.size();
        this.type = determineType(this.names);
    }

    /**
     * Initializes a new {@link FolderPath}.
     */
    FolderPath(String[] names) {
        super();
        this.names = ImmutableList.copyOf(names);
        size = names.length;
        this.type = determineType(this.names);
    }

    @Override
    public Iterator<String> iterator() {
        return names.iterator();
    }

    @Override
    public String toString() {
        return Strings.join(names, "/");
    }

    /**
     * Checks if this folder path is empty
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return names.isEmpty();
    }

    /**
     * Gets the number of name elements in this folder path.
     *
     * @return The number of name elements
     */
    public int size() {
        return size;
    }

    /**
     * Gets the type of this folder path.
     *
     * @return The type
     */
    public FolderPathType getType() {
        return type;
    }

    /**
     * Gets the path to use for restoring.
     *
     * @return The path for restoring
     */
    public Iterable<String> getPathForRestore() {
        if (size <= 1) {
            return Collections.emptyList();
        }

        return names.subList(1, size);
    }

}
