package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;


public class FolderProperties {

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

    
    private enum Parameters {
        AdminBindPW("AdminBindPW"),
        AdminDN("AdminDN"),
        authtype("authtype"),
        baseDN("baseDN"),
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
        userSearchScope("userSearchScope");

        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        public final String getName() {
            return name;
        }    
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

    public static FolderProperties getFolderPropertiesFromProperties(final ConfigurationService configuration, final String name, final String folder, final String contextnr, final StringBuilder logBuilder) throws LdapConfigurationException {
        final String prefix = PropertyHandler.bundlename + "context" + contextnr + "." + folder + ".";
        
        final Properties conf = configuration.getFile(name);
        final FolderProperties retval = new FolderProperties();
        
        
        final String folderparameter = prefix + Parameters.foldername.getName();
        final String searchparameter = prefix + Parameters.searchfilter.getName();
        final String foldername = conf.getProperty(folderparameter);
        final String searchfilter = conf.getProperty(searchparameter);
        if (null != foldername && foldername.length() != 0) {
            retval.setFoldername(foldername);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, folderparameter, name);
        }

        logBuilder.append("-------------------------------------------------------------------------------").append('\n');
        logBuilder.append("Properties for Context: ").append(contextnr).append(" Propertyfile: ").append(name).append(':').append(" Foldername: ").append(retval.getFoldername()).append('\n');
        logBuilder.append("-------------------------------------------------------------------------------").append('\n');

        if (null != searchfilter && searchfilter.length() != 0) {
            retval.setSearchfilter(searchfilter);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, searchfilter, name);
        }

        // Here we iterate over all properties...
        retval.setUri(PropertyHandler.checkStringProperty(conf, prefix + Parameters.uri.getName(), name));
        logBuilder.append("\tUri: ").append(retval.getUri()).append('\n');
        
        retval.setBaseDN(PropertyHandler.checkStringProperty(conf, prefix + Parameters.baseDN.getName(), name));
        logBuilder.append("\tBaseDN: ").append(retval.getBaseDN()).append('\n');
        
        retval.setAdminDN(PropertyHandler.checkStringProperty(conf, prefix + Parameters.AdminDN.getName(), name));
        logBuilder.append("\tAdminDN: ").append(retval.getAdminDN()).append('\n');
        
        retval.setAdminBindPW(PropertyHandler.checkStringProperty(conf, prefix + Parameters.AdminBindPW.getName(), name));
        
        final String searchScopeString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.searchScope.getName(), name);
        try {
            retval.setSearchScope(SearchScope.valueOf(searchScopeString));
            logBuilder.append("\tsearchScope: ").append(retval.getSearchScope()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SEARCH_SCOPE_WRONG, searchScopeString);
        }
        
        final String authstring = PropertyHandler.checkStringProperty(conf, prefix + Parameters.authtype.getName(), name);
        try {
            retval.setAuthtype(AuthType.valueOf(authstring));
            logBuilder.append("\tauthtype: ").append(retval.getAuthtype()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.AUTH_TYPE_WRONG, authstring);
        }
        
        final String sortingString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.sorting.getName(), name);
        try {
            retval.setSorting(Sorting.valueOf(sortingString));
            logBuilder.append("\tsorting: ").append(retval.getSorting()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SORTING_WRONG, authstring);
        }
        
        final String userLoginSourceString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userLoginSource.getName(), name);
        try {
            retval.setUserLoginSource(LoginSource.valueOf(userLoginSourceString));
            logBuilder.append("\tuserLoginSource: ").append(retval.getUserLoginSource()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.USER_LOGIN_SOURCE_WRONG, userLoginSourceString);
        }
        
        retval.setUserSearchFilter(PropertyHandler.checkStringProperty(conf, prefix + Parameters.userSearchFilter.getName(), name));
        logBuilder.append("\tuserSearchFilter: ").append(retval.getUserSearchFilter()).append('\n');
        
        final String userSearchScopeString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userSearchScope.getName(), name);
        if (0 != userSearchScopeString.length()) {
            try {
                retval.setUserSearchScope(SearchScope.valueOf(userSearchScopeString));
            } catch (final IllegalArgumentException e) {
                throw new LdapConfigurationException(Code.USER_SEARCH_SCOPE_WRONG, authstring);
            }
        } else {
            retval.setUserSearchScope(retval.getSearchScope());
        }
        logBuilder.append("\tuserSearchScope: ").append(retval.getUserSearchScope()).append('\n');
        
        retval.setUserSearchAttribute(PropertyHandler.checkStringProperty(conf, prefix + Parameters.userSearchAttribute.getName(), name));
        logBuilder.append("\tuserSearchAttribute: ").append(retval.getUserSearchAttribute()).append('\n');
        
        final String userSearchBaseDNString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userSearchBaseDN.getName(), name);
        if (0 != userSearchBaseDNString.length()) {
            retval.setUserSearchBaseDN(userSearchBaseDNString);
        } else {
            retval.setUserSearchBaseDN(retval.getBaseDN());
        }        
        logBuilder.append("\tuserSearchBaseDN: ").append(retval.getUserSearchBaseDN()).append('\n');
        
        final String userAuthTypeString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userAuthType.getName(), name);
        try {
            retval.setUserAuthType(UserAuthType.valueOf(userAuthTypeString));
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.USER_AUTH_TYPE_WRONG);
        }
        logBuilder.append("\tuserAuthType: ").append(retval.getUserAuthType()).append('\n');
        
        final String userAdminDNString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userAdminDN.getName(), name);
        if (0 != userAdminDNString.length()) {
            retval.setUserAdminDN(userAdminDNString);
        } else {
            retval.setUserAdminDN(retval.getAdminDN());
        }
        logBuilder.append("\tuserAdminDN: ").append(retval.getUserAdminDN()).append('\n');
        
        final String userAdminBindPWString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.userAdminBindPW.getName(), name);
        if (0 != userAdminBindPWString.length()) {
            retval.setUserAdminBindPW(userAdminBindPWString);
        } else {
            retval.setUserAdminBindPW(retval.getAdminBindPW());
        }
        
        final String memoryMappingString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.memorymapping.getName(), name);
        
        // TODO: Throws no error, so use an error checking method
        retval.setMemorymapping(Boolean.parseBoolean(memoryMappingString));
        logBuilder.append("\tmemorymapping: ").append(retval.isMemorymapping()).append('\n');

        final String pagesizestring = PropertyHandler.checkStringProperty(conf, prefix + Parameters.pagesize.getName(), name);
        try {
            retval.setPagesize(Integer.parseInt(pagesizestring));
            logBuilder.append("\tpagesize: ").append(retval.getPagesize()).append('\n');
        } catch (final NumberFormatException e) {
            throw new LdapConfigurationException(Code.INVALID_PAGESIZE, pagesizestring);
        }

        final String mappingfile = PropertyHandler.checkStringProperty(conf, prefix + Parameters.mappingfile.getName(), name);
        final Properties mapprops = configuration.getFile(mappingfile);
        if (mapprops.isEmpty()) {
            throw new LdapConfigurationException(Code.INVALID_MAPPING_FILE, mappingfile);
        } else {
            retval.setMappings(Mappings.getMappingsFromProperties(mapprops, PropertyHandler.bundlename +  mappingfile.replace(".properties", ""), mappingfile));
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

    public boolean isMemorymapping() {
        return memorymapping;
    }

    private void setAdminBindPW(final String adminBindPW) {
        this.adminBindPW = adminBindPW;
    }

    private void setAdminDN(final String adminDN) {
        this.adminDN = adminDN;
    }

    private void setAuthtype(final AuthType authtype) {
        this.authtype = authtype;
    }

    private void setBaseDN(final String baseDN) {
        this.baseDN = baseDN;
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

}
