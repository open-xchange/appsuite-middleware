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
import com.openexchange.admin.dataSource.I_OXResource;

import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class Resource {
    
    private long id = -1;
    private long contextId = -1;
    
    private String displayName = null;
    private String email = null;
    private String description = null;
    private boolean available = false;
    private String identifier = null;
    
    
    /** Creates a new instance of Resource */
    public Resource() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContextId() {
        return contextId;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Hashtable xform2Data(){
        Hashtable ht = new Hashtable();
        
        if(this.getContextId()!=-1){
            ht.put(I_OXContext.CONTEXT_ID,(int)this.getContextId());
        }        
        if(this.getDescription()!=null){
            ht.put(I_OXResource.DESCRIPTION,this.getDescription());
        }
        if(this.getDisplayName()!=null){
            ht.put(I_OXResource.DISPLAYNAME,this.getDisplayName());
        }
        if(this.getEmail()!=null){
            ht.put(I_OXResource.PRIMARY_MAIL,this.getEmail());
        }
        if(this.getId()!=-1){
            ht.put(I_OXResource.RID_NUMBER,(int)this.getId());
        }
        if(this.getIdentifier()!=null){
            ht.put(I_OXResource.RID,this.getIdentifier());
        }
        ht.put(I_OXResource.AVAILABLE,this.isAvailable());
        
        return ht;
    }
    
    public void xform2Object(Hashtable ht){
        Boolean bv = (Boolean)ht.get(I_OXResource.AVAILABLE);
        this.setAvailable(bv.booleanValue());
        this.setContextId(Long.parseLong(ht.get(I_OXContext.CONTEXT_ID).toString()));
        if(ht.containsKey(I_OXResource.DESCRIPTION)){
            this.setDescription(ht.get(I_OXResource.DESCRIPTION).toString());
        }
        if(ht.containsKey(I_OXResource.DISPLAYNAME)){
            this.setDisplayName(ht.get(I_OXResource.DISPLAYNAME).toString());
        }
        if(ht.containsKey(I_OXResource.PRIMARY_MAIL)){
            this.setEmail(ht.get(I_OXResource.PRIMARY_MAIL).toString());
        }
        if(ht.containsKey(I_OXResource.RID_NUMBER)){
            this.setId(Long.parseLong(ht.get(I_OXResource.RID_NUMBER).toString()));
        }
        if(ht.containsKey(I_OXResource.RID)){
            this.setIdentifier(ht.get(I_OXResource.RID).toString());
        }
    }
    
}
