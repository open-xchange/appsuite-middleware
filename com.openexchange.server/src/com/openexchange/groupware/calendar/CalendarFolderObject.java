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

package com.openexchange.groupware.calendar;

import gnu.trove.TCollections;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.Serializable;
import com.openexchange.groupware.container.FolderObject;

/**
 * CalendarFolderObject The CalendarFolderObject represents the sets of folders a user may have access to. This is cached by the calendar
 * subsystem to optimize its permission queries.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarFolderObject implements Serializable {

    private static final TIntSet EMPTY = TCollections.unmodifiableSet(new TIntHashSet(0));

    private static final long serialVersionUID = -2356348744702379243L;

    private final int uid;

    private final int cid;

    private TIntSet privatefolder = EMPTY;

    private TIntSet publicfolder = EMPTY;

    private TIntSet sharedfolder = EMPTY;

    private final boolean fill_shared;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarFolderObject.class);

    public static final String IDENTIFIER = "CalendarFolderObject@";

    private TIntSet publicReadableAllSet = EMPTY;

    private TIntSet publicReadableOwnSet = EMPTY;

    private TIntSet privateReadableAllSet = EMPTY;

    private TIntSet privateReadableOwnSet = EMPTY;

    private TIntSet sharedReadableAllSet = EMPTY;

    private TIntSet sharedReadableOwnSet = EMPTY;

    /**
     * Constructs a CalendarFolderObject. Note that it still has to be filled using {@link #addFolder(boolean, boolean, boolean, int, int)}
     * The CFO contains all folder a certain user (represented by his or her uid in a given context (cid)) has access to. Initializes a new
     * {@link CalendarFolderObject}.
     *
     * @param uid The User ID of the user the CFO describes
     * @param cid The ContextID
     * @param fill_shared set to true to have the CFO cache shared folders as well.
     */
    public CalendarFolderObject(final int uid, final int cid, final boolean fill_shared) {
        this.uid = uid;
        this.cid = cid;
        this.fill_shared = fill_shared;
    }

    /**
     * Add a folder description as described by the boolean parameters.
     *
     * @param readall Set to true if the user may read all entries in the given folder
     * @param readown Set to true if the user may read her own in the given folder
     * @param shared Set to true if this is a shared folder
     * @param folderid The ID of the folder
     * @param type The folder type as per the type constants in the {@link FolderObject}
     */
    public void addFolder(final boolean readall, final boolean readown, final boolean shared, final int folderid, final int type) {
        if (!shared) {
            if (type == FolderObject.PRIVATE) {
                if (privatefolder == EMPTY) {
                    privatefolder = new TIntHashSet(4);
                }
                privatefolder.add(folderid);
                if (readall) {
                    if (privateReadableAllSet == EMPTY) {
                        privateReadableAllSet = new TIntHashSet(4);
                    }
                    privateReadableAllSet.add(folderid);
                } else if (readown) {
                    if (privateReadableOwnSet == EMPTY) {
                        privateReadableOwnSet = new TIntHashSet(4);
                    }
                    privateReadableOwnSet.add(folderid);
                }
            } else if (type == FolderObject.PUBLIC) {
                if (publicfolder == EMPTY) {
                    publicfolder = new TIntHashSet(4);
                }
                if (readall) {
                    if (publicReadableAllSet == EMPTY) {
                        publicReadableAllSet = new TIntHashSet(4);
                    }
                    publicReadableAllSet.add(folderid);
                } else if (readown) {
                    if (publicReadableOwnSet == EMPTY) {
                        publicReadableOwnSet = new TIntHashSet(4);
                    }
                    publicReadableOwnSet.add(folderid);
                }
            } else {
                LOG.warn("Got an unknown folder type :{} for folderid {}", type, folderid);
            }
        } else if (fill_shared) {
            if (sharedfolder == EMPTY) {
                sharedfolder = new TIntHashSet(4);
            }
            sharedfolder.add(folderid);
            if (readall) {
                if (sharedReadableAllSet == EMPTY) {
                    sharedReadableAllSet = new TIntHashSet(4);
                }
                sharedReadableAllSet.add(folderid);
            } else if (readown) {
                if (sharedReadableOwnSet == EMPTY) {
                    sharedReadableOwnSet = new TIntHashSet(4);
                }
                sharedReadableOwnSet.add(folderid);
            }
        }
    }

    /**
     * @return a set of all private calendar folders that this user can see
     */
    public final TIntSet getPrivateFolders() {
        return privatefolder;
    }

    /**
     * @return a set of all public calendar folders that this user can see
     */
    public final TIntSet getPublicFolders() {
        return publicfolder;
    }

    /**
     * a set of all shared calendar folders the user can see
     *
     * @return
     */
    public final TIntSet getSharedFolderList() {
        return sharedfolder;
    }

    /**
     * @return a set of all private calendar folders in which the user may read all entries
     */
    public final TIntSet getPrivateReadableAll() {
        return privateReadableAllSet;
    }

    /**
     * @return a set of all private calendar folders in which the user may read her own objects
     */
    public final TIntSet getPrivateReadableOwn() {
        return privateReadableOwnSet;
    }

    /**
     * @return a set of all public calendar folders in which the user may read all entries
     */
    public final TIntSet getPublicReadableAll() {
        return publicReadableAllSet;
    }

    /**
     * @return a set of all public folders in which the user may read her own entries
     */
    public final TIntSet getPublicReadableOwn() {
        return publicReadableOwnSet;
    }

    /**
     * @return a set of all shared folders in which the user may read all entries
     */
    public final TIntSet getSharedReadableAll() {
        return sharedReadableAllSet;
    }

    /**
     * @return a set of all shared folders in which the user may read her own entries
     */
    public final TIntSet getSharedReadableOwn() {
        return sharedReadableOwnSet;
    }

    @Override
    public int hashCode() {
        return uid ^ cid ^ (fill_shared ? 1 : 0);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof CalendarFolderObject)) {
            return false;
        }
        final CalendarFolderObject oo = (CalendarFolderObject) o;
        return this.uid == oo.uid && this.cid == oo.cid && this.fill_shared == oo.fill_shared;
    }

    /**
     * Creates an identity for cacheing purposes.
     *
     * @return A cache key to uniquely identify this CFO
     */
    public String getObjectKey() {
        final StringBuilder key = new StringBuilder(IDENTIFIER);
        key.append('.');
        key.append(uid);
        key.append('.');
        key.append(cid);
        key.append('.');
        key.append(fill_shared);
        return key.toString();
    }

    /**
     * Returns a cacheing identifier to denote the group (context) to which this CFO belongs.
     *
     * @return
     */
    public String getGroupKey() {
        final StringBuilder key = new StringBuilder(IDENTIFIER);
        key.append('.');
        key.append(cid);
        return key.toString();
    }

    /**
     * Creates the group key for a given context id for lookup.
     *
     * @param cid
     * @return
     */
    public static final String createGroupKeyFromContextID(final int cid) {
        final StringBuilder key = new StringBuilder(IDENTIFIER);
        key.append('.');
        key.append(cid);
        return key.toString();
    }

    /**
     * Denotes whether the given folder id belongs to a public folder in which the user can read all entries
     *
     * @param fid The folder id to check
     */
    public boolean canReadAllInPublicFolder(int fid) {
        return publicReadableAllSet.contains(fid);
    }

    /**
     * Denotes whether the given folder id belongs to a public folder in which the user can read her own
     *
     * @param fid The folder id to check
     */
    public boolean canReadOwnInPublicFolder(int fid) {
        return publicReadableOwnSet.contains(fid);
    }

    /**
     * Denotes whether the given folder id belongs to a private folder in which the user can read all entries
     *
     * @param fid The folder id to check
     */
    public boolean canReadAllInPrivateFolder(int fid) {
        return privateReadableAllSet.contains(fid);
    }

    /**
     * Denotes whether the given folder id belongs to a private folder in which the user can read her own entries
     *
     * @param fid The folder id to check
     */
    public boolean canReadOwnInPrivateFolder(int fid) {
        return privateReadableOwnSet.contains(fid);
    }

    /**
     * Denotes whether the given folder id belongs to a shared folder in which the user can read all entries
     *
     * @param fid The folder id to check
     */
    public boolean canReadAllInSharedFolder(int fid) {
        return sharedReadableAllSet.contains(fid);
    }

    /**
     * Denotes whether the given folder id belongs to a shared folder in which the user can read her own entries
     *
     * @param fid The folder id to check
     */
    public boolean canReadOwnInSharedFolder(int fid) {
        return sharedReadableOwnSet.contains(fid);
    }

}
