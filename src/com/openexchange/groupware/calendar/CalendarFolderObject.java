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

package com.openexchange.groupware.calendar;

import com.openexchange.groupware.container.FolderObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CalendarFolderObject
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarFolderObject implements Serializable {
    
    private int uid;
    private int cid;
    
    private ArrayList<Integer> privatefolder;
    private ArrayList<Integer> publicfolder;
    private ArrayList<Integer> sharedfolder;
    
    private ArrayList<Integer> private_read_all;
    private ArrayList<Integer> private_read_own;
    private ArrayList<Integer> public_read_all;
    private ArrayList<Integer> public_read_own;
    private ArrayList<Integer> shared_read_all;
    private ArrayList<Integer> shared_read_own;
    
    private Object private_read_all_sorted[];
    private Object private_read_own_sorted[];
    private Object public_read_all_sorted[];
    private Object public_read_own_sorted[];
    private Object shared_read_all_sorted[];
    private Object shared_read_own_sorted[];
    
    
    private boolean fill_shared;
    private static final Object[] EMPTY = new Object[0];
    
    private static final transient Log LOG = LogFactory.getLog(CalendarCommonCollection.class);
    
    public static final String IDENTIFIER = "CalendarFolderObject@";
    
    
    public CalendarFolderObject(final int uid, final int cid, final boolean fill_shared) {
        this.uid = uid;
        this.cid = cid;
        this.fill_shared = fill_shared;
    }
    
    void addFolder(final boolean readall, final boolean readown, final boolean shared, final int folderid, final int type) {
        final Integer folderID = Integer.valueOf(folderid);
        if (!shared) {
            if (type == FolderObject.PRIVATE) {
                if (privatefolder == null) {
                    privatefolder = new ArrayList<Integer>(4);
                }
                privatefolder.add(folderID);
                if (readall) {
                    if (private_read_all == null) {
                        private_read_all = new ArrayList<Integer>(4);
                    }
                    private_read_all.add(folderID);
                } else if (readown) {
                    if (private_read_own == null) {
                        private_read_own = new ArrayList<Integer>(4);
                    }
                    private_read_own.add(folderID);
                }
            } else if (type == FolderObject.PUBLIC) {
                if (publicfolder == null) {
                    publicfolder = new ArrayList<Integer>(4);
                }
                if (readall) {
                    if (public_read_all == null) {
                        public_read_all = new ArrayList<Integer>(4);
                    }
                    public_read_all.add(folderID);
                } else if (readown) {
                    if (public_read_own == null) {
                        public_read_own = new ArrayList<Integer>(4);
                    }
                    public_read_own.add(folderID);
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Got an unknown folder type :"+type+" for folderid "+folderid);
                }
            }
        } else if (fill_shared) {
            if (sharedfolder == null) {
                sharedfolder = new ArrayList<Integer>(4);
            }
            sharedfolder.add(folderID);
            if (readall) {
                if (shared_read_all == null) {
                    shared_read_all = new ArrayList<Integer>(4);
                }
                shared_read_all.add(folderID);
            } else if (readown) {
                if (shared_read_own == null) {
                    shared_read_own = new ArrayList<Integer>(4);
                }
                shared_read_own.add(folderID);
            }
        }
    }
    
    public final Object[] getPrivateFolderList() {
        if (privatefolder != null) {
            return privatefolder.toArray();
        }
        return EMPTY;
    }
    
    public final Object[] getPublicFolderList() {
        if (publicfolder != null) {
            return publicfolder.toArray();
        }
        return EMPTY;
    }
    
    public final Object[] getSharedFolderList() {
        if (sharedfolder != null) {
            return sharedfolder.toArray();
        }
        return EMPTY;
    }
    
    public final Object[] getPrivateReadableAll() {
        if (private_read_all != null) {
            if (private_read_all_sorted == null) {
                private_read_all_sorted = private_read_all.toArray();
                Arrays.sort(private_read_all_sorted);
            }
            return private_read_all_sorted;
        }
        return EMPTY;
    }
    
    public final Object[] getPrivateReadableOwn() {
        if (private_read_own != null) {
            if (private_read_own_sorted == null) {
                private_read_own_sorted = private_read_own.toArray();
                Arrays.sort(private_read_own_sorted);
            }
            return private_read_own_sorted;
        }
        return EMPTY;
    }
    
    public final Object[] getPublicReadableAll() {
        if (public_read_all != null) {
            if (public_read_all_sorted == null) {
                public_read_all_sorted= public_read_all.toArray();
                Arrays.sort(public_read_all_sorted);
            }
            return public_read_all_sorted;
        }
        return EMPTY;
    }
    
    public final Object[] getPublicReadableOwn() {
        if (public_read_own != null) {
            if (public_read_own_sorted == null) {
                public_read_own_sorted =  public_read_own.toArray();
                Arrays.sort(public_read_own_sorted);
            }
            return public_read_own_sorted;
        }
        return EMPTY;
    }
    
    public final Object[] getSharedReadableAll() {
        if (shared_read_all != null) {
            if (shared_read_all_sorted == null) {
                shared_read_all_sorted =  shared_read_all.toArray();
                Arrays.sort(shared_read_all_sorted);
            }
            return shared_read_all_sorted;
        }
        return EMPTY;
    }
    
    public final Object[] getSharedReadableOwn() {
        if (shared_read_own != null) {
            if (shared_read_own_sorted == null) {
                shared_read_own_sorted =  shared_read_own.toArray();
                Arrays.sort(shared_read_own_sorted);
            }
            return shared_read_own_sorted;
        }
        return EMPTY;
    }
    
    
    @Override
    public int hashCode() {
        return uid ^ cid ^ (fill_shared ? 1 : 0);
    }
    
    @Override
    public boolean equals(final Object o) {
        if ( o == null ) {
            return false;
        }
        if ( o == this ) {
            return true;
        }
        if (!(o instanceof CalendarFolderObject)) {
            return false;
        }
        final CalendarFolderObject oo = (CalendarFolderObject)o;
        return this.uid == oo.uid && this.cid == oo.cid && this.fill_shared == oo.fill_shared;
    }
    
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
    
    public String getGroupKey() {
        final StringBuilder key = new StringBuilder(IDENTIFIER);
        key.append('.');
        key.append(cid);
        return key.toString();
    }
    
    public static final String createGroupKeyFromContextID(final int cid) {
        final StringBuilder key = new StringBuilder(IDENTIFIER);
        key.append('.');
        key.append(cid);
        return key.toString();
    }
    
    
    
}
