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

import java.util.Properties;
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
    
    private static final String bundlename = "com.openexchange.contacts.ldap.";
    
    private enum Parameters {
        uri("uri"),
        baseDN("baseDN"),
        AdminDN("AdminDN"),
        AdminBindPW("AdminBindPW"),
        searchScope("searchScope"),
        authtype("authtype"),
        // Here we begin with the mapping entries
        uniqueid("uniqueid"),
        displayname("displayname"),
        givenname("givenname"),
        surname("surname"),
        password("password"),
        email("email"),
        language("language"),
        timezone("timezone"),
        department("department"),
        company("company"),
        aliases("aliases"),
        
        accessCombinationName("access-combination-name"),
        email1("email1"),
        mailenabled("mailenabled");
        ;
        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        
        public final String getName() {
            return bundlename + name;
        }
        
    }
    
    public enum AuthType {
        anonymous("anonymous"),
        AdminDN("AdminDN"),
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
        sub("sub"),
        base("base"),
        one("one");
        
        private final String type;
        
        private SearchScope(final String type) {
            this.type = type;
        }

        public final String getType() {
            return type;
        }
        
    }
    
    private AtomicBoolean loaded = new AtomicBoolean();

    private Properties properties;
    
    private String uri;
    
    private String baseDN;

    private String adminDN;
    
    private String adminBindPW;
    
    private SearchScope searchScope;
    
    private AuthType authtype;
    
    private String uniqueid;
    
    private String displayname;
    
    private String givenname;

    private String surname;
    
    private String password;

    private String email;
    
    
    private final static String PROPFILE = "contacts-ldap.properties";
    
    private static PropertyHandler singleton = new PropertyHandler();
    
    public static PropertyHandler getInstance() {
        return singleton;
    }

    public void loadProperties() throws LdapConfigurationException {
        final ConfigurationService configuration = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        this.properties = configuration.getFile(PROPFILE);
        
        // Here we iterate over all properties...
        this.uri = checkStringProperty(Parameters.uri.getName());
        
        this.baseDN = checkStringProperty(Parameters.baseDN.getName());
        
        this.adminDN = checkStringProperty(Parameters.AdminDN.getName());
        
        this.adminBindPW = checkStringProperty(Parameters.AdminBindPW.getName());
        
        final String searchScopeString = checkStringProperty(Parameters.searchScope.getName());
        try {
            this.searchScope = SearchScope.valueOf(searchScopeString);
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SEARCH_SCOPE_WRONG, searchScopeString);
        }
        
        final String authstring = checkStringProperty(Parameters.authtype.getName());
        try {
            this.authtype  = AuthType.valueOf(authstring);
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.AUTH_TYPE_WRONG, authstring);
        }
        
        this.uniqueid = checkStringProperty(Parameters.uniqueid.getName());
        
        this.displayname = checkStringProperty(Parameters.displayname.getName());
        
        this.givenname = checkStringProperty(Parameters.givenname.getName());
        
        this.surname = checkStringProperty(Parameters.surname.getName());

        this.password = checkStringProperty(Parameters.password.getName());

        this.email = checkStringProperty(Parameters.email.getName());
        
//        configuration.getPropertiesInFolder(folderName)
        this.loaded.set(true);
    }
    
    public void reloadProperties() {
        
    }
    
    public String getBaseDN() {
        return this.baseDN;
    }

    
    public final String getUri() {
        return uri;
    }

    
    public final String getAdminDN() {
        return adminDN;
    }

    
    public final SearchScope getSearchScope() {
        return searchScope;
    }

    
    public final AuthType getAuthtype() {
        return authtype;
    }

    
    public final String getAdminBindPW() {
        return adminBindPW;
    }
    
    public static final String getBundlename() {
        return bundlename;
    }

    
    public final String getUniqueid() {
        return uniqueid;
    }

    
    public final String getDisplayname() {
        return displayname;
    }

    
    public final String getGivenname() {
        return givenname;
    }

    
    public final String getSurname() {
        return surname;
    }

    
    public final String getPassword() {
        return password;
    }

    
    public final String getEmail() {
        return email;
    }

    
    public static final String getPROPFILE() {
        return PROPFILE;
    }

    
    public static final PropertyHandler getSingleton() {
        return singleton;
    }

    private String checkStringProperty(final String name) throws LdapConfigurationException {
        final String property = this.properties.getProperty(name);
        if (null == property) {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, name);
        } else {
            return property;
        }
        
    }
}
