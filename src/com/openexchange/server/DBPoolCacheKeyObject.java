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



package com.openexchange.server;

import java.io.Serializable;


/**
   DBPoolCacheKeyObject
   @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
*/

public class DBPoolCacheKeyObject implements Serializable {

    private int cid, server_id;
    private boolean rw;
    public static final String IDENTIFIER = "DBPoolCacheKeyObject@";
    private String identifier;
    
    public DBPoolCacheKeyObject(int cid, int server_id, boolean rw) {
        this.cid = cid;
        this.server_id = server_id;
        this.rw = rw;
        identifier = IDENTIFIER+cid+server_id+rw;
    }
       
    public int getContextID() {
        return cid;
    }
    
    public int getServerID() {
        return server_id;
    }
    
    public boolean isTyperead() {
        return rw;
    }
    
    public int getHashCode() {
        return cid ^ server_id ^ getBooleanHashCode();
    }
    
    public int hashCode() {
        return getHashCode();
    }
    
    private final int getBooleanHashCode() {
        if (rw) return 1;
        return 0;
    }

    public Object getKeyObject() {
        return identifier;
    }
    
    public boolean equals(Object o) {
        if ( o == null )
            return false;
        if ( o == this )
            return true;
        if (!(o instanceof DBPoolCacheKeyObject))
            return false;
        DBPoolCacheKeyObject oo = (DBPoolCacheKeyObject)o;
        return this.cid == oo.cid && this.server_id == oo.server_id && this.rw == oo.rw;
  }
    
}
