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

import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class Context {
    
    private long contextId = -1;
    private String contextName = null;
    
    private long contextLockId = -1;
    private boolean contextLocked = false;
    
    private String filestoreUsername = null;
    private String filestorePassword = null;
    private String filestoreName = null;
    private long filestoreId = -1;
    private long filestoerQuotaMax = -1;
    private long filestoreQuotaUsed = -1;
    
    private long contextDBWritePoolId = -1;
    private long contextDBReadPoolId = -1;
    private String contextDBSchemaName = null;
    
    private User adminUser = null;
    
    
    /** Creates a new instance of Context */
    public Context() {
    }
    
    public Context(User adminuser) {
        this.setAdminUser(adminuser);
    }
    
    public long getContextId() {
        return contextId;
    }
    
    public void setContextId(long contextId) {
        this.contextId = contextId;
    }
    
    public String getContextName() {
        return contextName;
    }
    
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }
    
    public long getContextLockId() {
        return contextLockId;
    }
    
    public void setContextLockId(long contextLockId) {
        this.contextLockId = contextLockId;
    }
    
    public boolean isContextLocked() {
        return contextLocked;
    }
    
    public void setContextLocked(boolean contextLocked) {
        this.contextLocked = contextLocked;
    }
    
    public String getFilestoreUsername() {
        return filestoreUsername;
    }
    
    public void setFilestoreUsername(String filestoreUsername) {
        this.filestoreUsername = filestoreUsername;
    }
    
    public String getFilestorePassword() {
        return filestorePassword;
    }
    
    public void setFilestorePassword(String filestorePassword) {
        this.filestorePassword = filestorePassword;
    }
    
    public String getFilestoreName() {
        return filestoreName;
    }
    
    public void setFilestoreName(String filestoreName) {
        this.filestoreName = filestoreName;
    }
    
    public long getFilestoreId() {
        return filestoreId;
    }
    
    public void setFilestoreId(long filestoreId) {
        this.filestoreId = filestoreId;
    }
    
    public long getFilestoreQuotaMax() {
        return filestoerQuotaMax;
    }
    
    public void setFilestoreQuotaMax(long filestoerQuotaMax) {
        this.filestoerQuotaMax = filestoerQuotaMax;
    }
    
    public long getFilestoreQuotaUsed() {
        return filestoreQuotaUsed;
    }
    
    public void setFilestoreQuotaUsed(long filestoreQuotaUsed) {
        this.filestoreQuotaUsed = filestoreQuotaUsed;
    }
    
    public long getContextDBWritePoolId() {
        return contextDBWritePoolId;
    }
    
    public void setContextDBWritePoolId(long contextDBWritePoolId) {
        this.contextDBWritePoolId = contextDBWritePoolId;
    }
    
    public long getContextDBReadPoolId() {
        return contextDBReadPoolId;
    }
    
    public void setContextDBReadPoolId(long contextDBReadPoolId) {
        this.contextDBReadPoolId = contextDBReadPoolId;
    }
    
    public Hashtable xForm2AdminUserData(){        
        return this.getAdminUser().xForm2Userdata();        
    }
    
    public User getAdminUser() {
        return adminUser;
    }
    
    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }
    
    public Hashtable xform2Data(){
        Hashtable ht = new Hashtable();
        if(this.getContextDBReadPoolId()!=-1){
            ht.put(I_OXContext.CONTEXT_READ_POOL_ID,this.getContextDBReadPoolId());
        }
        if(this.getContextDBWritePoolId()!=-1){
            ht.put(I_OXContext.CONTEXT_WRITE_POOL_ID,this.getContextDBWritePoolId());
        }
        if(this.getContextId()!=-1){
            ht.put(I_OXContext.CONTEXT_ID,this.getContextId());
        }        
        if(this.getContextLockId()!=-1){
            ht.put(I_OXContext.CONTEXT_LOCKED_TXT_ID,this.getContextLockId());
        }
        if(this.getContextName()!=null){
            ht.put(I_OXContext.CONTEXT_NAME,this.getContextName());
        }
        if(this.getFilestoreId()!=-1){
            ht.put(I_OXContext.CONTEXT_FILESTORE_ID,this.getFilestoreId());
        }
        if(this.getFilestoreName()!=null){
            ht.put(I_OXContext.CONTEXT_FILESTORE_NAME,this.getFilestoreName());
        }
        if(this.getFilestorePassword()!=null){
            ht.put(I_OXContext.CONTEXT_FILESTORE_PASSWORD,this.getFilestorePassword());
        }
        if(this.getFilestoreUsername()!=null){
            ht.put(I_OXContext.CONTEXT_FILESTORE_USERNAME,this.getFilestoreUsername());
        }
        if(this.getFilestoreQuotaMax()!=-1){
            ht.put(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX,this.getFilestoreQuotaMax());
        }
        if(this.getFilestoreQuotaUsed()!=-1){
            ht.put(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED,this.getFilestoreQuotaUsed());
        }
        return ht;
    }

    public String getContextDBSchemaName() {
        return contextDBSchemaName;
    }

    public void setContextDBSchemaName(String contextDBSchemaName) {
        this.contextDBSchemaName = contextDBSchemaName;
    }
    
}
