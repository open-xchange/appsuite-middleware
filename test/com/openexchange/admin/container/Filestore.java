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
package com.openexchange.admin.container;

import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXUtil;

import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class Filestore {
    
    private long filestoreId = -1;
    private String filestoreUrl = null;
    private long filestoreSize = -1;
    private long filestoreMaxContext = -1;
    
    /** Creates a new instance of Filestore */
    public Filestore() {
    }

    public long getFilestoreId() {
        return filestoreId;
    }

    public void setFilestoreId(long filestoreId) {
        this.filestoreId = filestoreId;
    }

    public String getFilestoreUrl() {
        return filestoreUrl;
    }

    public void setFilestoreUrl(String filestoreUrl) {
        this.filestoreUrl = filestoreUrl;
    }

    public long getFilestoreSize() {
        return filestoreSize;
    }

    public void setFilestoreSize(long filestoreSize) {
        this.filestoreSize = filestoreSize;
    }

    public long getFilestoreMaxContext() {
        return filestoreMaxContext;
    }

    public void setFilestoreMaxContext(long filestoreMaxContext) {
        this.filestoreMaxContext = filestoreMaxContext;
    }
    
    public Hashtable xform2Data(){
        Hashtable ht = new Hashtable();
        if(this.getFilestoreId()!=-1){
            ht.put(I_OXContext.CONTEXT_FILESTORE_ID,this.getFilestoreId());
        }
        if(this.getFilestoreMaxContext()!=-1){
            ht.put(I_OXUtil.STORE_MAX_CONTEXT,this.getFilestoreMaxContext());
        }
        if(this.getFilestoreSize()!=-1){
            ht.put(I_OXUtil.STORE_SIZE,this.getFilestoreSize());
        }
        if(this.getFilestoreUrl()!=null){
        ht.put(I_OXUtil.STORE_URL,this.getFilestoreUrl());
        }
        return ht;
    }
    
}
