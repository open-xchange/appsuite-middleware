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

package com.openexchange.service.indexing.hazelcast;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link JobInfo}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class JobInfo implements Serializable {
    
    private static final long serialVersionUID = 3704945446543513829L;
    
    private String jobClass;
    
    private int contextId;
    
    private int userId;
    
    private int module;
    
    private Map<String, Object> properties;    
    
    
    private JobInfo(String jobClass, int contextId, int userId, int module, Map<String, Object> properties) {
        super();
        this.jobClass = jobClass;
        this.contextId = contextId;
        this.userId = userId;
        this.module = module;
        this.properties = properties;            
    }
    
    public String getJobClass() {
        return jobClass;
    }
    
    public int getContextId() {
        return contextId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getModule() {
        return module;
    }
    
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    
    public static final class Builder {
        
        private String jobClass;
        
        private int contextId;
        
        private int userId;
        
        private int module;
        
        private Map<String, Object> properties = new HashMap<String, Object>();
        
        
        public Builder(String jobClass) {
            super();
            this.jobClass = jobClass;
        }
        
        public Builder setContextId(int contextId) {
            this.contextId = contextId;
            return this;
        }
        
        public Builder setUserId(int userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder setModule(int module) {
            this.module = module;
            return this;
        }
        
        public Builder addProperty(String key, Object value) {
            properties.put(key, value);
            return this;
        }
        
        public JobInfo build() {
            JobInfo jobInfo = new JobInfo(jobClass, contextId, userId, module, properties);
            return jobInfo;
        }
    }

}
