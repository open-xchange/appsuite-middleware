package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;

public class FolderProperties {

    
    public interface SetterFallbackClosure {
        public void set(String property);
        
        public String getFallback();
    }

    public enum AuthType {
        AdminDN,
        anonymous,
        user;
    }
    
    public enum LoginSource {
        /**
         * Login is taken from user.imapLogin kept in storage; e.g. <code>test</code>
         */
        login,
        /**
         * Login is taken from user.mail kept in storage; e.g. <code>test@foo.bar</code>
         */
        mail,
        /**
         * Login is user's name; e.g. <code>test</code>
         */
        name
    }
    
    public enum SearchScope {
        base,
        one,
        sub;
    }
    
    public enum Sorting {
        groupware,
        server;
    }

    public enum UserAuthType {
        AdminDN,
        anonymous;
    }
    
    public enum ContactTypes {
        users,
        distributionlists,
        both;
    }

    
    private enum Parameters {
        AdminBindPW("AdminBindPW"),
        AdminDN("AdminDN"),
        authtype("authtype"),
        baseDN("baseDN_users"),
        foldername("foldername"),
        mappingfile("mappingfile"),
        memorymapping("memorymapping"),
        pagesize("pagesize"),
        searchfilter("searchfilter"),
        searchScope("searchScope"),
        sorting("sorting"),
        uri("uri"),
        userAdminBindPW("userAdminBindPW"),
        userAdminDN("userAdminDN"),
        userAuthType("userAuthType"),
        userLoginSource("userLoginSource"),
        userSearchAttribute("userSearchAttribute"),
        userSearchBaseDN("userSearchBaseDN"),
        userSearchFilter("userSearchFilter"),
        userSearchScope("userSearchScope"),
        contactTypes("contactTypes"),
        searchfilter_distributionlist("searchfilter_distributionlist"),
        searchScope_distributionlist("searchScope_distributionlist"),
        baseDN_distributionlist("baseDN_distributionlist"),
        outlook_support("outlook_support"),
        ADS_deletion_support("ADS_deletion_support");
        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        public final String getName() {
            return name;
        }    
    }
    
    private interface SetterEnumClosure<T> {
        public void set(final T enumeration);
        
        public T valueOf(final String string) throws IllegalArgumentException;
    }

    private String adminBindPW;
    
    private String adminDN;

    private AuthType authtype;

    private String baseDN;

    private String foldername;

    private Mappings mappings;

    private boolean memorymapping;

    private int pagesize;

    private String searchfilter;

    private SearchScope searchScope;

    private Sorting sorting;

    private String uri;

    private String userAdminBindPW;

    private String userAdminDN;

    private UserAuthType userAuthType;

    private LoginSource userLoginSource;

    private String userSearchAttribute;

    private String userSearchBaseDN;

    private String userSearchFilter;

    private SearchScope userSearchScope;
    
    private ContactTypes contacttypes;
    
    private String searchfilterDistributionlist;
    
    private SearchScope searchScopeDistributionlist;
    
    private String baseDNDistributionlist;
    
    private boolean outlook_support;
    
    private boolean ads_deletion_support;
    
    public static FolderProperties getFolderPropertiesFromProperties(final ConfigurationService configuration, final String name, final String folder, final String contextnr, final StringBuilder logBuilder) throws LdapConfigurationException {
        final String prefix = PropertyHandler.bundlename + "context" + contextnr + "." + folder + ".";
        
        final Properties conf = configuration.getFile(name);
        final FolderProperties retval = new FolderProperties();
        
        final CheckStringPropertyEnumParameter parameterObject = new CheckStringPropertyEnumParameter(conf, logBuilder, prefix, name);

        checkStringPropertyNonOptional(parameterObject, Parameters.foldername, new SetterClosure() {
            public void set(String string) {
                retval.setFoldername(string);
            }
        });

        logBuilder.append("-------------------------------------------------------------------------------").append('\n');
        logBuilder.append("Properties for Context: ").append(contextnr).append(" Propertyfile: ").append(name).append(':').append(" Foldername: ").append(retval.getFoldername()).append('\n');
        logBuilder.append("-------------------------------------------------------------------------------").append('\n');

        checkStringPropertyEnum(parameterObject, Parameters.contactTypes, Code.CONTACT_TYPES_WRONG, new SetterEnumClosure<ContactTypes>() {
            public void set(ContactTypes enumeration) {
                retval.setContacttypes(enumeration);
            }
            public ContactTypes valueOf(String string) throws IllegalArgumentException {
                return ContactTypes.valueOf(string);
            }
        });

        checkStringPropertyNonOptional(parameterObject, Parameters.searchfilter, new SetterClosure() {
            public void set(String string) {
                retval.setSearchfilter(string);
            }
        }); 
        logBuilder.append("\tSearchfilter: ").append(retval.getSearchfilter()).append('\n');

        // Here we iterate over all properties...
        checkStringPropertyNonOptional(parameterObject, Parameters.uri, new SetterClosure() {
            public void set(String string) {
                retval.setUri(string);
            }
        });
        logBuilder.append("\tUri: ").append(retval.getUri()).append('\n');
        
        checkStringPropertyNonOptional(parameterObject, Parameters.baseDN, new SetterClosure() {
            public void set(String string) {
                retval.setBaseDN(string);
            }
        });
        logBuilder.append("\tBaseDN: ").append(retval.getBaseDN()).append('\n');
        
        checkStringProperty(parameterObject, Parameters.AdminDN, new SetterClosure() {
            public void set(final String string) {
                retval.setAdminDN(string);
            }
        });
        logBuilder.append("\tAdminDN: ").append(retval.getAdminDN()).append('\n');
        
        checkStringProperty(parameterObject, Parameters.AdminBindPW, new SetterClosure() {
            public void set(final String string) {
                retval.setAdminBindPW(string);
            }
        });
        
        checkStringPropertyEnum(parameterObject, Parameters.searchScope, Code.SEARCH_SCOPE_WRONG, new SetterEnumClosure<SearchScope>() {
            public void set(SearchScope enumeration) {
                retval.setSearchScope(enumeration);
            }
            public SearchScope valueOf(String string) throws IllegalArgumentException {
                return SearchScope.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject , Parameters.authtype, Code.AUTH_TYPE_WRONG, new SetterEnumClosure<AuthType>() {
            public void set(AuthType enumeration) {
                retval.setAuthtype(enumeration);
            }
            public AuthType valueOf(String string) throws IllegalArgumentException {
                return AuthType.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject, Parameters.sorting, Code.SORTING_WRONG, new SetterEnumClosure<Sorting>() {
            public void set(Sorting enumeration) {
                retval.setSorting(enumeration);
            }
            public Sorting valueOf(String string) throws IllegalArgumentException {
                return Sorting.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject, Parameters.userLoginSource, Code.USER_LOGIN_SOURCE_WRONG, new SetterEnumClosure<LoginSource>() {
            public void set(LoginSource enumeration) {
                retval.setUserLoginSource(enumeration);
            }
            public LoginSource valueOf(String string) throws IllegalArgumentException {
                return LoginSource.valueOf(string);
            }
        });
        
        checkStringProperty(parameterObject, Parameters.userSearchFilter, new SetterClosure() {
            public void set(String string) {
                retval.setUserSearchFilter(string);
            }
        });
        logBuilder.append("\tuserSearchFilter: ").append(retval.getUserSearchFilter()).append('\n');

        final String userSearchScopeString = checkStringProperty(parameterObject, Parameters.userSearchScope);
        if (0 != userSearchScopeString.length()) {
            try {
                retval.setUserSearchScope(SearchScope.valueOf(userSearchScopeString));
            } catch (final IllegalArgumentException e) {
                throw new LdapConfigurationException(Code.USER_SEARCH_SCOPE_WRONG, userSearchScopeString);
            }
        } else {
            retval.setUserSearchScope(retval.getSearchScope());
        }
        logBuilder.append("\tuserSearchScope: ").append(retval.getUserSearchScope()).append('\n');
        
        checkStringProperty(parameterObject, Parameters.userSearchAttribute, new SetterClosure() {
            public void set(String string) {
                retval.setUserSearchAttribute(string);
            }
        });
        logBuilder.append("\tuserSearchAttribute: ").append(retval.getUserSearchAttribute()).append('\n');
        
        final String userSearchBaseDNString = checkStringProperty(parameterObject, Parameters.userSearchBaseDN);
        if (null != userSearchBaseDNString) {
            retval.setUserSearchBaseDN(userSearchBaseDNString);
        } else {
            retval.setUserSearchBaseDN(retval.getBaseDN());
        }        
        logBuilder.append("\tuserSearchBaseDN: ").append(retval.getUserSearchBaseDN()).append('\n');
        
        final String userAuthTypeString = checkStringProperty(parameterObject, Parameters.userAuthType);
        if (null != userAuthTypeString) {
            try {
                retval.setUserAuthType(UserAuthType.valueOf(userAuthTypeString));
            } catch (final IllegalArgumentException e) {
                throw new LdapConfigurationException(Code.USER_AUTH_TYPE_WRONG);
            }
        }
        logBuilder.append("\tuserAuthType: ").append(retval.getUserAuthType()).append('\n');
        
        final String userAdminDNString = checkStringProperty(parameterObject, Parameters.userAdminDN);
        if (null != userAdminDNString) {
            retval.setUserAdminDN(userAdminDNString);
        } else {
            retval.setUserAdminDN(retval.getAdminDN());
        }
        logBuilder.append("\tuserAdminDN: ").append(retval.getUserAdminDN()).append('\n');
        
        final String userAdminBindPWString = checkStringProperty(parameterObject, Parameters.userAdminBindPW);
        if (null != userAdminBindPWString) {
            retval.setUserAdminBindPW(userAdminBindPWString);
        } else {
            retval.setUserAdminBindPW(retval.getAdminBindPW());
        }
        
        checkStringProperty(parameterObject, Parameters.searchfilter_distributionlist, new SetterFallbackClosure() {
            public void set(String string) {
                retval.setSearchfilterDistributionlist(string);
            }

            public String getFallback() {
                return retval.getSearchfilter();
            }
        });
        logBuilder.append("\tsearchfilter_distributionlist: ").append(retval.getSearchfilterDistributionlist()).append('\n');
        
        final String searchScopeString = checkStringProperty(parameterObject, Parameters.searchScope_distributionlist);
        if (null != searchScopeString) {
            try {
                retval.setSearchScopeDistributionlist(SearchScope.valueOf(searchScopeString));
            } catch (final IllegalArgumentException e) {
                throw new LdapConfigurationException(Code.SEARCH_SCOPE_DISTRI_WRONG, searchScopeString);
            }
        } else {
            retval.setSearchScopeDistributionlist(retval.getSearchScope());
        }
        logBuilder.append("\tsearchScope_distributionlist: ").append(retval.getSearchScopeDistributionlist()).append('\n');
        
        checkStringProperty(parameterObject, Parameters.baseDN_distributionlist, new SetterFallbackClosure() {
            public void set(String string) {
                retval.setBaseDNDistributionlist(string);
            }

            public String getFallback() {
                return retval.getBaseDN();
            }
        });

        final String outlookSupportString = checkStringProperty(parameterObject, Parameters.outlook_support);
        retval.setOutlook_support(Boolean.parseBoolean(outlookSupportString));
        logBuilder.append("\toutlook_support: ").append(retval.isOutlook_support()).append('\n');

        final String ads_deletion_supportString = checkStringProperty(parameterObject, Parameters.ADS_deletion_support);
        retval.setAds_deletion_support(Boolean.parseBoolean(ads_deletion_supportString));
        logBuilder.append("\tADS_deletion_support: ").append(retval.isAds_deletion_support()).append('\n');
        
        final String memoryMappingString = checkStringProperty(parameterObject, Parameters.memorymapping);
        
        // TODO: Throws no error, so use an error checking method
        retval.setMemorymapping(Boolean.parseBoolean(memoryMappingString));
        logBuilder.append("\tmemorymapping: ").append(retval.isMemorymapping()).append('\n');

        final String pagesizestring = checkStringProperty(parameterObject, Parameters.pagesize);
        try {
            retval.setPagesize(Integer.parseInt(pagesizestring));
            logBuilder.append("\tpagesize: ").append(retval.getPagesize()).append('\n');
        } catch (final NumberFormatException e) {
            throw new LdapConfigurationException(Code.INVALID_PAGESIZE, pagesizestring);
        }

        final String mappingfile = checkStringProperty(parameterObject, Parameters.mappingfile);
        if (null != mappingfile) {
            final Properties mapprops = configuration.getFile(mappingfile);
            if (mapprops.isEmpty()) {
                throw new LdapConfigurationException(Code.INVALID_MAPPING_FILE, mappingfile);
            } else {
                retval.setMappings(Mappings.getMappingsFromProperties(mapprops, PropertyHandler.bundlename + mappingfile.replace(
                    ".properties",
                    ""), mappingfile));
            }
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, parameterObject.getPrefix() + Parameters.mappingfile.getName(), parameterObject.getFilename());
        }
        
        return retval;
    }

    
    
    public String getAdminBindPW() {
        return adminBindPW;
    }

    public String getAdminDN() {
        return adminDN;
    }

    public AuthType getAuthtype() {
        return authtype;
    }

    public String getBaseDN() {
        return baseDN;
    }

    /**
     * Gets the contacttypes
     *
     * @return The contacttypes
     */
    public ContactTypes getContacttypes() {
        return contacttypes;
    }


    public String getFoldername() {
        return foldername;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public int getPagesize() {
        return pagesize;
    }
    
    public String getSearchfilter() {
        return searchfilter;
    }

    public SearchScope getSearchScope() {
        return searchScope;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public final String getUri() {
        return uri;
    }

    /**
     * Gets the userAdminBindPW
     *
     * @return The userAdminBindPW
     */
    public String getUserAdminBindPW() {
        return userAdminBindPW;
    }

    /**
     * Gets the userAdminDN
     *
     * @return The userAdminDN
     */
    public String getUserAdminDN() {
        return userAdminDN;
    }

    /**
     * Gets the userAuthType
     *
     * @return The userAuthType
     */
    public UserAuthType getUserAuthType() {
        return userAuthType;
    }

    
    
    /**
     * Gets the userLoginSource
     *
     * @return The userLoginSource
     */
    public LoginSource getUserLoginSource() {
        return userLoginSource;
    }

    
    /**
     * Gets the userSearchAttribute
     *
     * @return The userSearchAttribute
     */
    public String getUserSearchAttribute() {
        return userSearchAttribute;
    }

    /**
     * @return
     */
    public String getUserSearchBaseDN() {
        return this.userSearchBaseDN;
    }

    /**
     * Gets the userSearchFilter
     *
     * @return The userSearchFilter
     */
    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    /**
     * @return
     */
    public SearchScope getUserSearchScope() {
        return this.userSearchScope;
    }

    public boolean isAds_deletion_support() {
        return ads_deletion_support;
    }



    public boolean isMemorymapping() {
        return memorymapping;
    }

    public final boolean isOutlook_support() {
        return outlook_support;
    }

    private void setAdminBindPW(final String adminBindPW) {
        this.adminBindPW = adminBindPW;
    }

    private void setAdminDN(final String adminDN) {
        this.adminDN = adminDN;
    }

    private void setAds_deletion_support(boolean ads_deletion_support) {
        this.ads_deletion_support = ads_deletion_support;
    }



    private void setAuthtype(final AuthType authtype) {
        this.authtype = authtype;
    }

    private void setBaseDN(final String baseDN) {
        this.baseDN = baseDN;
    }

    /**
     * Sets the contacttypes
     *
     * @param contacttypes The contacttypes to set
     */
    private void setContacttypes(final ContactTypes contacttypes) {
        this.contacttypes = contacttypes;
    }


    private void setFoldername(final String foldername) {
        this.foldername = foldername;
    }

    private void setMappings(final Mappings mappings) {
        this.mappings = mappings;
    }

    private void setMemorymapping(final boolean memorymapping) {
        this.memorymapping = memorymapping;
    }

    private final void setOutlook_support(boolean outlook_support) {
        this.outlook_support = outlook_support;
    }

    private void setPagesize(final int pagesize) {
        this.pagesize = pagesize;
    }
    
    private void setSearchfilter(final String searchfilter) {
        this.searchfilter = searchfilter;
    }
    
    private void setSearchScope(final SearchScope searchScope) {
        this.searchScope = searchScope;
    }
    
    private void setSorting(final Sorting sorting) {
        this.sorting = sorting;
    }
    
    private void setUri(final String uri) {
        this.uri = uri;
    }
    
    /**
     * @param userAdminBindPW
     */
    private void setUserAdminBindPW(final String userAdminBindPW) {
        this.userAdminBindPW = userAdminBindPW;
    }
    
    /**
     * @param userAdminDN
     */
    private void setUserAdminDN(String userAdminDN) {
        this.userAdminDN = userAdminDN;
    }
    
    /**
     * @param userAuthType
     */
    private void setUserAuthType(UserAuthType userAuthType) {
        this.userAuthType = userAuthType;
    }
    
    /**
     * @param userLoginSource
     */
    private void setUserLoginSource(final LoginSource userLoginSource) {
        this.userLoginSource = userLoginSource;
    }
    
    /**
     * @param userSearchAttribute
     */
    private void setUserSearchAttribute(final String userSearchAttribute) {
        this.userSearchAttribute = userSearchAttribute;
    }
    
    /**
     * @param userSearchBaseDN
     */
    private void setUserSearchBaseDN(final String userSearchBaseDN) {
        this.userSearchBaseDN = userSearchBaseDN;
    }

    /**
     * @param userSearchFilter
     */
    private void setUserSearchFilter(final String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    
    
    /**
     * @param userSearchScope
     */
    private void setUserSearchScope(final SearchScope userSearchScope) {
        this.userSearchScope = userSearchScope;
    }

    public static class CheckStringPropertyEnumParameter {

        private Properties m_props;

        private StringBuilder m_log;

        private String m_prefix;

        private String m_filename;

        public CheckStringPropertyEnumParameter(Properties props, StringBuilder log, String prefix, String filename) {
            m_props = props;
            m_log = log;
            m_prefix = prefix;
            m_filename = filename;
        }

        public Properties getProps() {
            return m_props;
        }

        public StringBuilder getLog() {
            return m_log;
        }

        public String getPrefix() {
            return m_prefix;
        }

        public String getFilename() {
            return m_filename;
        }
    }

    public interface SetterClosure {
        public void set(final String string);
    }

    private static <T> void checkStringPropertyEnum(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final Code code, final SetterEnumClosure<T> setter) throws LdapConfigurationException {
        final String paramname = param.getName();
        final String property = parameterObject.getProps().getProperty(parameterObject.getPrefix() + paramname);
        if (null != property && 0 != property.length()) {
            try {
                final T valueOf = setter.valueOf(property);
                setter.set(valueOf);
                parameterObject.getLog().append("\t").append(paramname).append(": ").append(valueOf).append('\n');
            } catch (final IllegalArgumentException e) {
                throw new LdapConfigurationException(code, property);
            }
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, parameterObject.getPrefix() + paramname, parameterObject.getFilename());
        }
    }

    private static void checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterClosure setter) throws LdapConfigurationException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        }
    }

    private static void checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterFallbackClosure setter) throws LdapConfigurationException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        } else {
            setter.set(setter.getFallback());
        }
    }

    private static void checkStringPropertyNonOptional(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterClosure setter) throws LdapConfigurationException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, parameterObject.getPrefix() + param.getName(), parameterObject.getFilename());
        }
    }
    
    private static String checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param) throws LdapConfigurationException {
        return PropertyHandler.checkStringProperty(parameterObject.getProps(), parameterObject.getPrefix() + param.getName());
    }

    private void setSearchfilterDistributionlist(String searchfilterDistriutionlist) {
        this.searchfilterDistributionlist = searchfilterDistriutionlist;
    }

    public String getSearchfilterDistributionlist() {
        return searchfilterDistributionlist;
    }

    private void setBaseDNDistributionlist(String baseDNDistriutionlist) {
        this.baseDNDistributionlist = baseDNDistriutionlist;
    }

    public String getBaseDNDistributionlist() {
        return baseDNDistributionlist;
    }

    private void setSearchScopeDistributionlist(SearchScope searchScoprDistributionlist) {
        this.searchScopeDistributionlist = searchScoprDistributionlist;
    }

    public SearchScope getSearchScopeDistributionlist() {
        return searchScopeDistributionlist;
    }
}
