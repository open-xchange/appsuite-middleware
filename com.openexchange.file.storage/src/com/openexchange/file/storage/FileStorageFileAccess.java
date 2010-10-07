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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.file.storage.meta.FileComparator;
import com.openexchange.tx.TransactionAware;

/**
 * A {@link FileStorageFileAccess} provides access to files in a file hierarchy.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface FileStorageFileAccess extends TransactionAware {

    /**
     * A version number pointing at the current version of a file
     */
    public static final int CURRENT_VERSION = -1;

    /**
     * An ID value denoting a newly created file, or a file that should be created
     */
    public static final String NEW = null;

    /**
     * An undefined last modified value
     */
    public static final long UNDEFINED_SEQUENCE_NUMBER = -1;

    /**
     * A sequence number that can be considered larger than all others
     */
    public static final long DISTANT_FUTURE = Long.MAX_VALUE;

    /**
     * Denotes a {@link SortDirection}
     */
    public static enum SortDirection {
        /**
         * Sort ascendingly
         */
        ASC,
        /**
         * Sort descendingly
         */
        DESC;

        /**
         * The default SortDirection
         */
        public static final SortDirection DEFAULT = ASC;

        public Comparator<File> comparatorBy(File.Field by) {
            FileComparator fileComparator = new FileComparator(by);
            switch (this) {
            case ASC:
                return fileComparator;
            case DESC:
                return new InverseComparator(fileComparator);
            }
            return null;
        }

        public Comparator<File> comparatorBy(File.Field by, Comparator comparator) {
            FileComparator fileComparator = new FileComparator(by, comparator);
            switch (this) {
            case ASC:
                return fileComparator;
            case DESC:
                return new InverseComparator(fileComparator);
            }
            return null;
        }
        
        public void sort(List<File> collection, File.Field by) {
            Collections.sort(collection, comparatorBy(by));
        }

        public void sort(List<File> collection, File.Field by, Comparator comparator) {
            Collections.sort(collection, comparatorBy(by, comparator));
        }

        private static final class InverseComparator implements Comparator<File> {

            private Comparator<File> delegate = null;

            public InverseComparator(Comparator<File> delegate) {
                this.delegate = delegate;
            }

            public int compare(File o1, File o2) {
                return -delegate.compare(o1, o2);
            }
        }

    }

    /**
     * Find out whether the file with a given ID exists or not.
     * 
     * @param id The ID to check for
     * @param version The version to check for
     * @return true when the file exists and is readable, false otherwise.
     */
    public boolean exists(String id, int version);
    
    
    // TODO: Define more methods
    
    
}
