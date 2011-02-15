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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.config.cascade.context;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.context.matching.ContextSetTerm;
import com.openexchange.config.cascade.context.matching.ContextSetTermParser;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link ContextSetConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextSetConfigProvider extends AbstractContextBasedConfigProvider {

    public static final String SCOPE = "contextSet";
    
    private List<ContextSetConfig> contextSetConfigs;

    public ContextSetConfigProvider(ContextService contexts, ConfigurationService config) {
        super(contexts);
        
        Map<String, Object> yamlInFolder = config.getYamlInFolder("contextSets");
        if(yamlInFolder != null) {
            prepare(yamlInFolder);
        } else {
            contextSetConfigs = Collections.emptyList();
        }
    }

    @Override
    protected BasicProperty get(final String property, Context context) {
        final List<Map<String, Object>> config = getConfigData(context);

        final String value = findFirst(config, property);
        
        return new BasicProperty() {

            public String get() {
                return value;
            }

            public String get(String metadataName) {
                return null;
            }

            public boolean isDefined() {
                return value != null;
            }

            public void set(String value) throws ConfigCascadeException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(property, SCOPE);
            }

            public void set(String metadataName, String value) throws ConfigCascadeException {
                throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, SCOPE);
            }

            public List<String> getMetadataNames() throws ConfigCascadeException {
                return Collections.emptyList();
            }
            
        };
    }


    @Override
    protected Collection<String> getAllPropertyNames(Context context) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String findFirst(List<Map<String, Object>> configData, String property) {
        for (Map<String, Object> map : configData) {
            Object object = map.get(property);
            if(object != null) {
                return object.toString();
            }
        }
        return null;
    }
    
    protected List<Map<String, Object>> getConfigData(Context ctx) {
        List<Map<String, Object>> retval = new LinkedList<Map<String, Object>>();
        for (ContextSetConfig c : contextSetConfigs) {
            if (c.matches(ctx)) {
                retval.add(c.getConfiguration());
            }
        }
        return retval;
    }
    
    protected void prepare(Map<String, Object> yamlFiles) {
        ContextSetTermParser parser = new ContextSetTermParser();
        contextSetConfigs = new LinkedList<ContextSetConfig>();
        for(Map.Entry<String, Object> file : yamlFiles.entrySet()) {
            String filename = file.getKey();
            Map<String, Map<String, Object>> content = (Map<String, Map<String, Object>>) file.getValue();
            for(Map.Entry<String, Map<String, Object>> configData : content.entrySet()) {
                String configName = configData.getKey();
                Map<String, Object> configuration = configData.getValue();
                
                String withTags = (String) configuration.get("withTags");
                if(withTags == null) {
                    throw new IllegalArgumentException("Missing withTags specification in configuration "+configName+" in file "+filename);
                }
                try {
                    ContextSetTerm term = parser.parse(withTags);
                    contextSetConfigs.add(new ContextSetConfig(term, configuration));
                } catch (IllegalArgumentException x) {
                    throw new IllegalArgumentException("Could not parse withTags expression '"+withTags+"' in configuration "+configName+" in file "+filename+": "+x.getMessage());
                }
            }

        }
    }
    
    private class ContextSetConfig {
        private ContextSetTerm term;
        private Map<String, Object> configuration;
        
        public ContextSetConfig(ContextSetTerm term, Map<String, Object> configuration) {
            super();
            this.term = term;
            this.configuration = configuration;
        }
       
        public boolean matches(Context ctx) {
            return term.matches(ctx);
        }
        
        public Map<String, Object> getConfiguration() {
            return configuration;
        }
        
    }

}
