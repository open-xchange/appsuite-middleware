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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.contact.storage.ldap.config;

import java.util.Properties;
import javax.naming.directory.SearchControls;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link LdapConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapConfig {

    public static final String CONFIG_PREFIX = "com.openexchange.contact.storage.ldap.";

    public enum AuthType {
        ADMINDN,
        ANONYMOUS,
        USER;
    }

    public enum IDMapping {
        STATIC,
        DYNAMIC,
        PERSISTENT;
    }

    public enum LoginSource {
        /**
         * Login is taken from user.imapLogin kept in storage; e.g. <code>test</code>
         */
        LOGIN,
        /**
         * Login is taken from user.mail kept in storage; e.g. <code>test@foo.bar</code>
         */
        MAIL,
        /**
         * Login is user's name; e.g. <code>test</code>
         */
        NAME
    }

    public enum SearchScope {
        BASE(SearchControls.OBJECT_SCOPE),
        ONE(SearchControls.ONELEVEL_SCOPE),
        SUB(SearchControls.SUBTREE_SCOPE);

        public int getValue() {
            return value;
        }

        private final int value;

        private SearchScope(int value) {
            this.value = value;
        }
    }

    public enum Sorting {
        GROUPWARE,
        SERVER;
    }

    public enum ReferralTypes {
        FOLLOW("follow"),
        IGNORE("ignore"),
        STANDARD(null);

        public String getValue() {
            return value;
        }

        private final String value;

        private ReferralTypes(String value) {
            this.value = value;
        }
    }

    public enum DerefAliases {
        ALWAYS("always"),
        NEVER("never"),
        FINDING("finding"),
        SEARCHING("searching");

        public String getValue() {
            return value;
        }

        private final String value;

        private DerefAliases(String value) {
            this.value = value;
        }
    }

    private int contextID;
    private String folderID;
    private String folderName;
    private int storagePriority;
    private IDMapping idMapping;
    private String mappingFile;
    private String cacheConfigFile;
    private int refreshInterval;
    private String uri;
    private String baseDN;
    private String adminDN;
    private String adminBindPW;
    private SearchScope searchScope;
    private AuthType authtype;
    private Sorting sorting;
    private LoginSource userLoginSource;
    private String userSearchFilter;
    private SearchScope userSearchScope;
    private String userSearchAttribute;
    private String userSearchBaseDN;
    private AuthType userAuthType;
    private int pagesize;
    private String searchfilter;
    private boolean adsDeletionSupport;
    private ReferralTypes referrals;
    private int pooltimeout;
    private DerefAliases derefAliases;
    private boolean connectionPooling;
    private boolean trustAllCerts;
    private boolean excludeEmptyLists;

    /**
     * Initializes a new {@link LdapConfig}.
     */
    public LdapConfig(Properties properties) throws OXException {
        super();
        this.init(properties);
    }

    private void init(Properties properties) throws OXException {
        folderID = getProperty(properties, "folderID", null);
        if (null == folderID) {
            folderName = getProperty(properties, "foldername");
        }
        storagePriority = Integer.parseInt(getProperty(properties, "storagePriority"));
        contextID = Integer.parseInt(getProperty(properties, "contextID"));
        idMapping = IDMapping.valueOf(getProperty(properties, "idMapping").toUpperCase());
        uri = getProperty(properties, "uri");
        baseDN = getProperty(properties, "baseDN", null);
        adminDN = getProperty(properties, "AdminDN", null);
        adminBindPW = getProperty(properties, "AdminBindPW", null);
        searchScope = SearchScope.valueOf(getProperty(properties, "searchScope", "sub").toUpperCase());
        authtype = AuthType.valueOf(getProperty(properties, "authtype", "AdminDN").toUpperCase());
        if (AuthType.ADMINDN.equals(authtype) && (null == adminDN || null == adminBindPW)) {
            throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("authtype/adminDN/adminBindPW");
        }
        if (AuthType.USER.equals(authtype)) {
            userLoginSource = LoginSource.valueOf(getProperty(properties, "userLoginSource").toUpperCase());
            userSearchAttribute = getProperty(properties, "userSearchAttribute");
            userAuthType= AuthType.valueOf(getProperty(properties, "userAuthType").toUpperCase());
            if (AuthType.USER.equals(userAuthType)) {
                throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("userAuthType");
            } else if (AuthType.ADMINDN.equals(userAuthType) && (null == adminDN || null == adminBindPW)) {
                throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("userAuthType/adminDN/adminBindPW");
            }
            String userSearchScopeValue = getProperty(properties, "userSearchScope", null);
            userSearchScope = null != userSearchScopeValue ? SearchScope.valueOf(userSearchScopeValue.toUpperCase()) : null;
            userSearchFilter = getProperty(properties, "userSearchFilter", null);
            userSearchBaseDN = getProperty(properties, "userSearchBaseDN", null);
        }
        sorting = Sorting.valueOf(getProperty(properties, "sorting", "groupware").toUpperCase());
        pagesize = Integer.parseInt(getProperty(properties, "pagesize", "500"));
        mappingFile = getProperty(properties, "mappingFile");
        searchfilter = getProperty(properties, "searchfilter");
        adsDeletionSupport = Boolean.parseBoolean(getProperty(properties, "ADS_deletion_support"));
        String referralsValue = getProperty(properties, "referrals", null);
        referrals = null != referralsValue ? ReferralTypes.valueOf(referralsValue.toUpperCase()) : null;
        refreshInterval = Integer.parseInt(getProperty(properties, "refreshinterval", "0"));
        if (0 < refreshInterval) {
            if (IDMapping.DYNAMIC.equals(idMapping)) {
                throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("refreshinterval/idMapping");
            } else if (AuthType.USER.equals(authtype)) {
                throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("refreshinterval/authtype");
            }
            cacheConfigFile = getProperty(properties, "cacheConfigFile");
        }
        connectionPooling = Boolean.parseBoolean(getProperty(properties, "connectionPooling", "false"));
        if (connectionPooling) {
            pooltimeout = Integer.parseInt(getProperty(properties, "pooltimeout", "0"));
        }
        String derefAliasesValue = getProperty(properties, "derefAliases", "always");
        derefAliases = null != derefAliasesValue ? DerefAliases.valueOf(derefAliasesValue.toUpperCase()) : null;
        trustAllCerts = Boolean.parseBoolean(getProperty(properties, "trustAllCerts", "false"));
        excludeEmptyLists = Boolean.parseBoolean(getProperty(properties, "trustAllCerts", "true"));
    }

    private static String getProperty(Properties properties, String propertyName, String defaultValue) {
        String value = properties.getProperty(CONFIG_PREFIX + propertyName);
        return null != value && 0 < value.length() ? value : defaultValue;
    }

    private static String getProperty(Properties properties, String propertyName) throws OXException {
        String value = properties.getProperty(CONFIG_PREFIX + propertyName);
        if (null == value || 0 == value.length()) {
            throw LdapExceptionCodes.MISSING_CONFIG_VALUE.create(CONFIG_PREFIX + propertyName);
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append('\t').append("Folder name: ").append(folderName).append('\n')
            .append('\t').append("Folder ID: ").append(folderID).append('\n')
            .append('\t').append("Context ID: ").append(contextID).append('\n')
            .append('\t').append("Storage priority: ").append(storagePriority).append('\n')
            .append('\t').append("ID mapping: ").append(idMapping).append('\n')
            .append('\t').append("Mapping configuration file: ").append(mappingFile).append('\n')
            .append('\t').append("Cache refresh interval: ").append(refreshInterval).append('\n')
            .append('\t').append("Cache configuration file: ").append(cacheConfigFile).append('\n')
            .append('\t').append("LDAP server URI: ").append(uri).append('\n')
            .append('\t').append("Base DN: ").append(baseDN).append('\n')
            .append('\t').append("Search filter: ").append(searchfilter).append('\n')
            .append('\t').append("Authentication type: ").append(authtype).append('\n')
            .append('\t').append("Admin DN: ").append(adminDN).append('\n')
            .append('\t').append("Admin bind password: ").append(null != adminBindPW ? "***" : "").append('\n')
            .append('\t').append("User login source: ").append(userLoginSource).append('\n')
            .append('\t').append("User search attribute: ").append(userSearchAttribute).append('\n')
            .append('\t').append("User search authentication type: ").append(userAuthType).append('\n')
            .append('\t').append("User search scope: ").append(userSearchScope).append('\n')
            .append('\t').append("User search base DN: ").append(userSearchBaseDN).append('\n')
            .append('\t').append("User search filter: ").append(userSearchFilter).append('\n')
            .append('\t').append("Search scope: ").append(searchScope).append('\n')
            .append('\t').append("Referrals: ").append(referrals).append('\n')
            .append('\t').append("Connection pooling: ").append(connectionPooling).append('\n')
            .append('\t').append("Connection pool timeout: ").append(pooltimeout).append('\n')
            .append('\t').append("Dereference aliases: ").append(derefAliases).append('\n')
            .append('\t').append("Page results size: ").append(pagesize).append('\n')
            .append('\t').append("Result sorting: ").append(sorting).append('\n')
            .append('\t').append("ADS deletion support: ").append(adsDeletionSupport).append('\n')
            .append('\t').append("Exclude empty distribution lists: ").append(excludeEmptyLists).append('\n')
        ;
        return stringBuilder.toString();
    }

    /**
     * Gets the uri
     *
     * @return The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the uri
     *
     * @param uri The uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the baseDN
     *
     * @return The baseDN
     */
    public String getBaseDN() {
        return baseDN;
    }

    /**
     * Sets the baseDNUsers
     *
     * @param baseDN The baseDN to set
     */
    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    /**
     * Gets the adminDN
     *
     * @return The adminDN
     */
    public String getAdminDN() {
        return adminDN;
    }

    /**
     * Sets the adminDN
     *
     * @param adminDN The adminDN to set
     */
    public void setAdminDN(String adminDN) {
        this.adminDN = adminDN;
    }

    /**
     * Gets the adminBindPW
     *
     * @return The adminBindPW
     */
    public String getAdminBindPW() {
        return adminBindPW;
    }

    /**
     * Sets the adminBindPW
     *
     * @param adminBindPW The adminBindPW to set
     */
    public void setAdminBindPW(String adminBindPW) {
        this.adminBindPW = adminBindPW;
    }

    /**
     * Gets the searchScope
     *
     * @return The searchScope
     */
    public SearchScope getSearchScope() {
        return searchScope;
    }

    /**
     * Sets the searchScope
     *
     * @param searchScope The searchScope to set
     */
    public void setSearchScope(SearchScope searchScope) {
        this.searchScope = searchScope;
    }

    /**
     * Gets the authtype
     *
     * @return The authtype
     */
    public AuthType getAuthtype() {
        return authtype;
    }

    /**
     * Sets the authtype
     *
     * @param authtype The authtype to set
     */
    public void setAuthtype(AuthType authtype) {
        this.authtype = authtype;
    }

    /**
     * Gets the sorting
     *
     * @return The sorting
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * Sets the sorting
     *
     * @param sorting The sorting to set
     */
    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
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
     * Sets the userLoginSource
     *
     * @param userLoginSource The userLoginSource to set
     */
    public void setUserLoginSource(LoginSource userLoginSource) {
        this.userLoginSource = userLoginSource;
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
     * Sets the userSearchFilter
     *
     * @param userSearchFilter The userSearchFilter to set
     */
    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    /**
     * Gets the userSearchScope
     *
     * @return The userSearchScope
     */
    public SearchScope getUserSearchScope() {
        return userSearchScope;
    }

    /**
     * Sets the userSearchScope
     *
     * @param userSearchScope The userSearchScope to set
     */
    public void setUserSearchScope(SearchScope userSearchScope) {
        this.userSearchScope = userSearchScope;
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
     * Sets the userSearchAttribute
     *
     * @param userSearchAttribute The userSearchAttribute to set
     */
    public void setUserSearchAttribute(String userSearchAttribute) {
        this.userSearchAttribute = userSearchAttribute;
    }

    /**
     * Gets the userSearchBaseDN
     *
     * @return The userSearchBaseDN
     */
    public String getUserSearchBaseDN() {
        return userSearchBaseDN;
    }

    /**
     * Sets the userSearchBaseDN
     *
     * @param userSearchBaseDN The userSearchBaseDN to set
     */
    public void setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = userSearchBaseDN;
    }

    /**
     * Gets the userAuthType
     *
     * @return The userAuthType
     */
    public AuthType getUserAuthType() {
        return userAuthType;
    }

    /**
     * Sets the userAuthType
     *
     * @param userAuthType The userAuthType to set
     */
    public void setUserAuthType(AuthType userAuthType) {
        this.userAuthType = userAuthType;
    }

    /**
     * Gets the pagesize
     *
     * @return The pagesize
     */
    public int getPagesize() {
        return pagesize;
    }

    /**
     * Sets the pagesize
     *
     * @param pagesize The pagesize to set
     */
    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    /**
     * Gets the mappingfile
     *
     * @return The mappingfile
     */
    public String getContactMappingFile() {
        return mappingFile;
    }

    /**
     * Sets the mappingfile
     *
     * @param mappingfile The mappingfile to set
     */
    public void setContactMappingFile(String mappingfile) {
        this.mappingFile = mappingfile;
    }

    /**
     * Gets the foldername
     *
     * @return The foldername
     */
    public String getFoldername() {
        return folderName;
    }

    /**
     * Sets the foldername
     *
     * @param foldername The foldername to set
     */
    public void setFoldername(String foldername) {
        this.folderName = foldername;
    }


    /**
     * Gets the searchfilter
     *
     * @return The searchfilter
     */
    public String getSearchfilter() {
        return searchfilter;
    }

    /**
     * Sets the searchfilter
     *
     * @param searchfilter The searchfilter to set
     */
    public void setSearchfilter(String searchfilter) {
        this.searchfilter = searchfilter;
    }

    /**
     * Gets the adsDeletionSupport
     *
     * @return The adsDeletionSupport
     */
    public boolean isAdsDeletionSupport() {
        return adsDeletionSupport;
    }

    /**
     * Sets the adsDeletionSupport
     *
     * @param adsDeletionSupport The adsDeletionSupport to set
     */
    public void setAdsDeletionSupport(boolean adsDeletionSupport) {
        this.adsDeletionSupport = adsDeletionSupport;
    }

    /**
     * Gets the referrals
     *
     * @return The referrals
     */
    public ReferralTypes getReferrals() {
        return referrals;
    }

    /**
     * Sets the referrals
     *
     * @param referrals The referrals to set
     */
    public void setReferrals(ReferralTypes referrals) {
        this.referrals = referrals;
    }

    /**
     * Gets the refreshinterval
     *
     * @return The refreshinterval
     */
    public int getRefreshinterval() {
        return refreshInterval;
    }

    /**
     * Sets the refreshinterval
     *
     * @param refreshinterval The refreshinterval to set
     */
    public void setRefreshinterval(int refreshinterval) {
        this.refreshInterval = refreshinterval;
    }

    /**
     * Gets the pooltimeout
     *
     * @return The pooltimeout
     */
    public int getPooltimeout() {
        return pooltimeout;
    }

    /**
     * Sets the pooltimeout
     *
     * @param pooltimeout The pooltimeout to set
     */
    public void setPooltimeout(int pooltimeout) {
        this.pooltimeout = pooltimeout;
    }

    /**
     * Gets the derefAliases
     *
     * @return The derefAliases
     */
    public DerefAliases getDerefAliases() {
        return derefAliases;
    }

    /**
     * Sets the derefAliases
     *
     * @param derefAliases The derefAliases to set
     */
    public void setDerefAliases(DerefAliases derefAliases) {
        this.derefAliases = derefAliases;
    }

    /**
     * Gets the storagePriority
     *
     * @return The storagePriority
     */
    public int getStoragePriority() {
        return storagePriority;
    }

    /**
     * Sets the storagePriority
     *
     * @param storagePriority The storagePriority to set
     */
    public void setStoragePriority(int storagePriority) {
        this.storagePriority = storagePriority;
    }

    /**
     * Gets the contextID
     *
     * @return The contextID
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Sets the contextID
     *
     * @param contextID The contextID to set
     */
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    /**
     * Gets the folderID
     *
     * @return The folderID
     */
    public String getFolderID() {
        return folderID;
    }

    /**
     * Sets the folderID
     *
     * @param folderID The folderID to set
     */
    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    /**
     * Gets the idMapping
     *
     * @return The idMapping
     */
    public IDMapping getIDMapping() {
        return idMapping;
    }

    /**
     * Sets the idMapping
     *
     * @param idMapping The idMapping to set
     */
    public void setIDMapping(IDMapping idMapping) {
        this.idMapping = idMapping;
    }

    /**
     * Gets the cacheConfigFile
     *
     * @return The cacheConfigFile
     */
    public String getCacheConfigFile() {
        return cacheConfigFile;
    }

    /**
     * Sets the cacheConfigFile
     *
     * @param cacheConfigFile The cacheConfigFile to set
     */
    public void setCacheConfigFile(String cacheConfigFile) {
        this.cacheConfigFile = cacheConfigFile;
    }

    /**
     * Gets the connectionPooling
     *
     * @return The connectionPooling
     */
    public boolean isConnectionPooling() {
        return connectionPooling;
    }

    /**
     * Sets the connectionPooling
     *
     * @param connectionPooling The connectionPooling to set
     */
    public void setConnectionPooling(boolean connectionPooling) {
        this.connectionPooling = connectionPooling;
    }

    /**
     * Gets the trustAllCerts
     *
     * @return The trustAllCerts
     */
    public boolean isTrustAllCerts() {
        return trustAllCerts;
    }

    /**
     * Sets the trustAllCerts
     *
     * @param trustAllCerts The trustAllCerts to set
     */
    public void setTrustAllCerts(boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    /**
     * Gets the excludeEmptyLists
     *
     * @return The excludeEmptyLists
     */
    public boolean isExcludeEmptyLists() {
        return excludeEmptyLists;
    }

    /**
     * Sets the excludeEmptyLists
     *
     * @param excludeEmptyLists The excludeEmptyLists to set
     */
    public void setExcludeEmptyLists(boolean excludeEmptyLists) {
        this.excludeEmptyLists = excludeEmptyLists;
    }

}
