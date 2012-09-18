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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.impl.infostore;

import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link InfostoreJobInfo}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreJobInfo extends JobInfo {
    
    private static final long serialVersionUID = 2186833384874500056L;
    
    public final String account;
    
    public final long folder;
    
    public boolean force;
    
    public boolean deleteFolder;
    
    private String uniqueId = null;

    /**
     * Initializes a new {@link InfostoreJobInfo}.
     * @param jobClass
     * @param contextId
     * @param userId
     */
    private InfostoreJobInfo(Builder builder) {
        super(builder);
        account = builder.account;
        folder = builder.folder;
        force = builder.force;
        deleteFolder = builder.deleteFolder;
    }

    @Override
    public String toUniqueId() {
        if (uniqueId == null) {
            StringBuilder sb = new StringBuilder(jobClass.getName());
            sb.append('/');
            sb.append(contextId);
            sb.append('/');
            sb.append(userId);
            
            if (account != null) {
                sb.append('/');
                sb.append(account);
            }
            
            sb.append('/');
            sb.append(folder);
            
            uniqueId = sb.toString();
        }
        
        return uniqueId;
    }
    
    @Override
    public String toString() {
        return toUniqueId();
    }
    
    public static Builder newBuilder(Class<? extends IndexingJob> jobClass) {
        return new Builder(jobClass);
    }
    
    public static final class Builder extends JobInfoBuilder<Builder> {

        protected String account;
        
        protected long folder;
        
        protected boolean force = false;
        
        protected boolean deleteFolder = false;

        /**
         * Initializes a new {@link Builder}.
         * @param jobClass
         */
        public Builder(Class<? extends IndexingJob> jobClass) {
            super(jobClass);
        }
        
        public Builder account(String account) {
            this.account = account;
            return this;
        }
        
        public Builder folder(long folder) {
            this.folder = folder;
            return this;
        }
        
        public Builder force() {
            force = true;
            return this;
        }
        
        public Builder delete() {
            this.deleteFolder = true;
            return this;
        }

        @Override
        public JobInfo build() {
            return new InfostoreJobInfo(this);
        }
        
    }

}
