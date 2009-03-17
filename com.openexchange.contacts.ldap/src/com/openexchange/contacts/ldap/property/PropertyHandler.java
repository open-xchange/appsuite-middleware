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

package com.openexchange.contacts.ldap.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;
import com.openexchange.contacts.ldap.osgi.ServiceRegistry;

/**
 * A class which will deal with all property related actions.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class PropertyHandler {

    public enum AuthType {
        AdminDN("AdminDN"),
        anonymous("anonymous"),
        user("user");
        
        private final String type;
        
        private AuthType(final String type) {
            this.type = type;
        }

        
        public final String getType() {
            return type;
        }
        
    }
    
    public enum SearchScope {
        base("base"),
        one("one"),
        sub("sub");
        
        private final String type;
        
        private SearchScope(final String type) {
            this.type = type;
        }

        public final String getType() {
            return type;
        }
        
    }

    public enum Sorting {
        groupware,
        server;
    }
    
    private enum Parameters {
        AdminBindPW("AdminBindPW"),
        AdminDN("AdminDN"),
        authtype("authtype"),
        baseDN("baseDN"),
        contexts("contexts"),
        mappingfile("mappingfile"),
        memorymapping("memorymapping"),
        pagesize("pagesize"),
        searchScope("searchScope"),
        sorting("sorting"),
        uri("uri");
        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        
        public final String getName() {
            return bundlename + name;
        }
        
    }
    
    public static final String bundlename = "com.openexchange.contacts.ldap.";
    
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PropertyHandler.class);
    
    private final static String PROPFILE = "contacts-ldap.properties";

    private static PropertyHandler singleton = new PropertyHandler();
    
    private String adminBindPW;
    
    private String adminDN;
    
    private AuthType authtype;

    private String baseDN;
    
    private Map<Integer, ContextProperties> contextdetails = new ConcurrentHashMap<Integer, ContextProperties>();
    
    private List<Integer> contexts;
    
    private AtomicBoolean loaded = new AtomicBoolean();
    
    private Mappings mappings;

    private boolean memorymapping;

    private int pagesize;

    private Properties properties;
    
    
    private SearchScope searchScope;
    
    private Sorting sorting;

    private String uri;
    
    public static String checkStringProperty(Properties props, final String name) throws LdapConfigurationException {
        final String property = props.getProperty(name);
        if (null == property) {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, name);
        } else {
            return property;
        }
        
    }

    public static PropertyHandler getInstance() {
        return singleton;
    }

    public final String getAdminBindPW() {
        return adminBindPW;
    }

    public final String getAdminDN() {
        return adminDN;
    }

    
    public final AuthType getAuthtype() {
        return authtype;
    }

    
    public String getBaseDN() {
        return this.baseDN;
    }

    
    public final Map<Integer, ContextProperties> getContextdetails() {
        return contextdetails;
    }
    
    public final List<Integer> getContexts() {
        return contexts;
    }

    
    public final Mappings getMappings() {
        return mappings;
    }

    public int getPagesize() {
        return pagesize;
    }

    public final SearchScope getSearchScope() {
        return searchScope;
    }
    
    public final Sorting getSorting() {
        return sorting;
    }

    public final String getUri() {
        return uri;
    }

    public final boolean isMemorymapping() {
        return memorymapping;
    }
    
    public void loadProperties() throws LdapConfigurationException {
        final StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\nLoading Contacts-LDAP properties...\n");
        final ConfigurationService configuration = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        this.properties = configuration.getFile(PROPFILE);
        
        // Here we iterate over all properties...
        this.uri = checkStringProperty(this.properties, Parameters.uri.getName());
        logBuilder.append("\tUri: ").append(this.uri).append('\n');
        
        this.baseDN = checkStringProperty(this.properties, Parameters.baseDN.getName());
        logBuilder.append("\tBaseDN: ").append(this.baseDN).append('\n');
        
        this.adminDN = checkStringProperty(this.properties, Parameters.AdminDN.getName());
        logBuilder.append("\tAdminDN: ").append(this.adminDN).append('\n');
        
        this.adminBindPW = checkStringProperty(this.properties, Parameters.AdminBindPW.getName());
        
        final String searchScopeString = checkStringProperty(this.properties, Parameters.searchScope.getName());
        try {
            this.searchScope = SearchScope.valueOf(searchScopeString);
            logBuilder.append("\tsearchScope: ").append(this.searchScope).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SEARCH_SCOPE_WRONG, searchScopeString);
        }
        
        final String authstring = checkStringProperty(this.properties, Parameters.authtype.getName());
        try {
            this.authtype  = AuthType.valueOf(authstring);
            logBuilder.append("\tauthtype: ").append(this.authtype).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.AUTH_TYPE_WRONG, authstring);
        }
        
        this.contexts = getContexts(Parameters.contexts.getName());
        
        final String sortingString = checkStringProperty(this.properties, Parameters.sorting.getName());
        try {
            this.sorting = Sorting.valueOf(sortingString);
            logBuilder.append("\tsorting: ").append(this.sorting).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SORTING_WRONG, authstring);
        }
        
        final String memoryMappingString = checkStringProperty(this.properties, Parameters.memorymapping.getName());
        
        // TODO: Throws no error, so use an error checking method
        this.memorymapping = Boolean.parseBoolean(memoryMappingString);
        logBuilder.append("\tmemorymapping: ").append(this.memorymapping).append('\n');

        final String pagesizestring = checkStringProperty(this.properties, Parameters.pagesize.getName());
        try {
            this.pagesize = Integer.parseInt(pagesizestring);
            logBuilder.append("\tpagesize: ").append(this.pagesize).append('\n');
        } catch (final NumberFormatException e) {
            throw new LdapConfigurationException(Code.INVALID_PAGESIZE, pagesizestring);
        }
        
        
        final String mappingfile = checkStringProperty(this.properties, Parameters.mappingfile.getName());
        
        final Properties mapprops = configuration.getFile(mappingfile);
        if (mapprops.isEmpty()) {
            throw new LdapConfigurationException(Code.INVALID_MAPPING_FILE, mappingfile);
        } else {
            this.mappings = Mappings.getMappingsFromProperties(mapprops);
        }

        for (final Integer ctx : this.contexts) {
            final String stringctx = String.valueOf(ctx);
            final Properties file = configuration.getFile(stringctx + ".properties");
            this.contextdetails.put(ctx, ContextProperties.getContextPropertiesFromProperties(file, stringctx));
        }
        this.loaded.set(true);
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    public void reloadProperties() {
        
    }

    private List<Integer> getContexts(String name) throws LdapConfigurationException {
        final String property = this.properties.getProperty(name);
        if (null != property) {
            final List<Integer> retval = new ArrayList<Integer>();
            final String[] split = property.split(",");
            for (final String ctx : split) {
                try {
                    retval.add(Integer.parseInt(ctx));
                } catch (final NumberFormatException e) {
                    throw new LdapConfigurationException(Code.NO_INTEGER_VALUE, ctx);
                }
            }
            return retval;
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, name);
        }
    }
}
