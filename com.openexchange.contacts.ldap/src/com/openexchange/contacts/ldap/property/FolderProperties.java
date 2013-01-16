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

package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationExceptionCode;
import com.openexchange.contacts.ldap.exceptions.LdapExceptionCode;
import com.openexchange.exception.OXException;

/**
 * Holds all folder specific properties
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class FolderProperties {


    private static final int DEFAULT_REFRESH_INTERVAL = 10000;

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

    public enum ReferralTypes {
        follow,
        ignore,
        standard
    }

    public enum DerefAliases {
        always,
        never,
        finding,
        searching
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
        ADS_deletion_support("ADS_deletion_support"),
        referrals("referrals"),
        refreshinterval("refreshinterval"),
        pooltimeout("pooltimeout"),
        derefAliases("derefAliases"),
        storagePriority("storagePriority");

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

    private ReferralTypes referrals;

    private int refreshinterval;

    private int pooltimeout;

    private DerefAliases derefAliases;

    private int storagePriority;

    public static FolderProperties getFolderPropertiesFromProperties(final ConfigurationService configuration, final String name, final String folder, final String contextnr, final StringBuilder logBuilder) throws OXException {
        final String prefix = PropertyHandler.bundlename + "context" + contextnr + "." + folder + ".";

        final Properties conf = configuration.getFile(name);
        final FolderProperties retval = new FolderProperties();

        final CheckStringPropertyEnumParameter parameterObject = new CheckStringPropertyEnumParameter(conf, logBuilder, prefix, name);

        checkStringPropertyNonOptional(parameterObject, Parameters.foldername, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setFoldername(string);
            }
        });

        logBuilder.append("-------------------------------------------------------------------------------").append('\n');
        logBuilder.append("Properties for Context: ").append(contextnr).append(" Propertyfile: ").append(name).append(':').append(" Foldername: ").append(retval.getFoldername()).append('\n');
        logBuilder.append("-------------------------------------------------------------------------------").append('\n');

        checkStringPropertyEnum(parameterObject, Parameters.contactTypes, LdapConfigurationExceptionCode.CONTACT_TYPES_WRONG, new SetterEnumClosure<ContactTypes>() {
            @Override
            public void set(final ContactTypes enumeration) {
                retval.setContacttypes(enumeration);
            }
            @Override
            public ContactTypes valueOf(final String string) throws IllegalArgumentException {
                return ContactTypes.valueOf(string);
            }
        });

        checkStringPropertyNonOptional(parameterObject, Parameters.searchfilter, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setSearchfilter(string);
            }
        });
        logBuilder.append("\tSearchfilter: ").append(retval.getSearchfilter()).append('\n');

        // Here we iterate over all properties...
        checkStringPropertyNonOptional(parameterObject, Parameters.uri, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setUri(string);
            }
        });
        logBuilder.append("\tUri: ").append(retval.getUri()).append('\n');

        checkStringPropertyNonOptional(parameterObject, Parameters.baseDN, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setBaseDN(string);
            }
        });
        logBuilder.append("\tBaseDN: ").append(retval.getBaseDN()).append('\n');

        checkStringProperty(parameterObject, Parameters.AdminDN, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setAdminDN(string);
            }
        });
        logBuilder.append("\tAdminDN: ").append(retval.getAdminDN()).append('\n');

        checkStringProperty(parameterObject, Parameters.AdminBindPW, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setAdminBindPW(string);
            }
        });

        checkStringPropertyEnum(parameterObject, Parameters.searchScope, LdapConfigurationExceptionCode.SEARCH_SCOPE_WRONG, new SetterEnumClosure<SearchScope>() {
            @Override
            public void set(final SearchScope enumeration) {
                retval.setSearchScope(enumeration);
            }
            @Override
            public SearchScope valueOf(final String string) throws IllegalArgumentException {
                return SearchScope.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject , Parameters.authtype, LdapConfigurationExceptionCode.AUTH_TYPE_WRONG, new SetterEnumClosure<AuthType>() {
            @Override
            public void set(final AuthType enumeration) {
                retval.setAuthtype(enumeration);
            }
            @Override
            public AuthType valueOf(final String string) throws IllegalArgumentException {
                return AuthType.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject, Parameters.sorting, LdapConfigurationExceptionCode.SORTING_WRONG, new SetterEnumClosure<Sorting>() {
            @Override
            public void set(final Sorting enumeration) {
                retval.setSorting(enumeration);
            }
            @Override
            public Sorting valueOf(final String string) throws IllegalArgumentException {
                return Sorting.valueOf(string);
            }
        });

        checkStringPropertyEnum(parameterObject, Parameters.userLoginSource, LdapConfigurationExceptionCode.USER_LOGIN_SOURCE_WRONG, new SetterEnumClosure<LoginSource>() {
            @Override
            public void set(final LoginSource enumeration) {
                retval.setUserLoginSource(enumeration);
            }
            @Override
            public LoginSource valueOf(final String string) throws IllegalArgumentException {
                return LoginSource.valueOf(string);
            }
        });

        checkStringProperty(parameterObject, Parameters.userSearchFilter, new SetterClosure() {
            @Override
            public void set(final String string) {
                retval.setUserSearchFilter(string);
            }
        });
        logBuilder.append("\tuserSearchFilter: ").append(retval.getUserSearchFilter()).append('\n');

        final String userSearchScopeString = checkStringProperty(parameterObject, Parameters.userSearchScope);
        if (null != userSearchScopeString && 0 != userSearchScopeString.length()) {
            try {
                retval.setUserSearchScope(SearchScope.valueOf(userSearchScopeString));
            } catch (final IllegalArgumentException e) {
                throw LdapConfigurationExceptionCode.USER_SEARCH_SCOPE_WRONG.create(userSearchScopeString);
            }
        } else {
            retval.setUserSearchScope(retval.getSearchScope());
        }
        logBuilder.append("\tuserSearchScope: ").append(retval.getUserSearchScope()).append('\n');

        checkStringProperty(parameterObject, Parameters.userSearchAttribute, new SetterClosure() {
            @Override
            public void set(final String string) {
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
                throw LdapConfigurationExceptionCode.USER_AUTH_TYPE_WRONG.create();
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
            @Override
            public void set(final String string) {
                retval.setSearchfilterDistributionlist(string);
            }

            @Override
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
                throw LdapConfigurationExceptionCode.SEARCH_SCOPE_DISTRI_WRONG.create(searchScopeString);
            }
        } else {
            retval.setSearchScopeDistributionlist(retval.getSearchScope());
        }
        logBuilder.append("\tsearchScope_distributionlist: ").append(retval.getSearchScopeDistributionlist()).append('\n');

        checkStringProperty(parameterObject, Parameters.baseDN_distributionlist, new SetterFallbackClosure() {
            @Override
            public void set(final String string) {
                retval.setBaseDNDistributionlist(string);
            }

            @Override
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

        checkStringPropertyEnum(parameterObject, Parameters.referrals, LdapConfigurationExceptionCode.REFERRALS_WRONG, new SetterEnumClosure<ReferralTypes>() {
            @Override
            public void set(final ReferralTypes enumeration) {
                retval.setReferrals(enumeration);
            }
            @Override
            public ReferralTypes valueOf(final String string) throws IllegalArgumentException {
                return ReferralTypes.valueOf(string);
            }
        });

        final String refreshintervalstring = checkStringProperty(parameterObject, Parameters.refreshinterval);
        try {
            if (null != refreshintervalstring) {
                retval.setRefreshinterval(Integer.parseInt(refreshintervalstring));
            } else {
                retval.setRefreshinterval(DEFAULT_REFRESH_INTERVAL);
            }
            logBuilder.append("\trefreshinterval: ").append(retval.getRefreshinterval()).append('\n');
        } catch (final NumberFormatException e) {
            throw LdapConfigurationExceptionCode.INVALID_REFRESHINTERVAL.create(refreshintervalstring);
        }

        final String pooltimeoutString = checkStringProperty(parameterObject, Parameters.pooltimeout);
        try {
            if (null != pooltimeoutString) {
                retval.setPooltimeout(Integer.parseInt(pooltimeoutString));
            } else {
                retval.setPooltimeout(-1);
            }
            logBuilder.append("\tpooltimeout (-1 for not set): ").append(retval.getPooltimeout()).append('\n');
        } catch (final NumberFormatException e) {
            throw LdapConfigurationExceptionCode.INVALID_POOLTIMEOUT.create(pooltimeoutString, parameterObject.getFilename());
        }

        final String derefAliasesString = checkStringProperty(parameterObject, Parameters.derefAliases);
        if (null != derefAliasesString) {
            try {
                retval.setDerefAliases(DerefAliases.valueOf(derefAliasesString));
            } catch (final IllegalArgumentException e) {
                throw LdapConfigurationExceptionCode.DEREF_ALIASES_WRONG.create(derefAliasesString, parameterObject.getFilename());
            }
        }
        logBuilder.append("\tderefAliases (null for not set): ").append(retval.getDerefAliases()).append('\n');

        final String memoryMappingString = checkStringProperty(parameterObject, Parameters.memorymapping);

        // TODO: Throws no error, so use an error checking method
        retval.setMemorymapping(Boolean.parseBoolean(memoryMappingString));
        logBuilder.append("\tmemorymapping: ").append(retval.isMemorymapping()).append('\n');

        final String pagesizestring = checkStringProperty(parameterObject, Parameters.pagesize);
        try {
            retval.setPagesize(Integer.parseInt(pagesizestring));
            logBuilder.append("\tpagesize: ").append(retval.getPagesize()).append('\n');
        } catch (final NumberFormatException e) {
            throw LdapConfigurationExceptionCode.INVALID_PAGESIZE.create(pagesizestring);
        }

        final String mappingfile = checkStringProperty(parameterObject, Parameters.mappingfile);
        if (null != mappingfile) {
            final Properties mapprops = configuration.getFile(mappingfile);
            if (mapprops.isEmpty()) {
                throw LdapConfigurationExceptionCode.INVALID_MAPPING_FILE.create(mappingfile);
            } else {
                retval.setMappings(Mappings.getMappingsFromProperties(mapprops, PropertyHandler.bundlename + mappingfile.replace(
                    ".properties",
                    ""), mappingfile));
            }
        } else {
            throw LdapConfigurationExceptionCode.PARAMETER_NOT_SET.create(parameterObject.getPrefix() + Parameters.mappingfile.getName(), parameterObject.getFilename());
        }

        String storagePriorityString = checkStringProperty(parameterObject, Parameters.storagePriority);
        try {
            if (null != storagePriorityString) {
                retval.setStoragePriority(Integer.parseInt(storagePriorityString));
            } else {
                retval.setStoragePriority(17);
            }
            logBuilder.append("\tstoragePriority: ").append(retval.getStoragePriority()).append('\n');
        } catch (NumberFormatException e) {
            throw LdapConfigurationExceptionCode.INVALID_STORAGE_PRIORITY.create(storagePriorityString, parameterObject.getFilename());
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


    public DerefAliases getDerefAliases() {
        return derefAliases;
    }


    public String getFoldername() {
        return foldername;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public ReferralTypes getReferrals() {
        return referrals;
    }

    public int getRefreshinterval() {
        return refreshinterval;
    }



    public int getPagesize() {
        return pagesize;
    }

    public int getPooltimeout() {
        return pooltimeout;
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

    /**
     * Gets the storage priority.
     *
     * @return the priority
     */
    public int getStoragePriority() {
        return this.storagePriority;
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

    private void setAds_deletion_support(final boolean ads_deletion_support) {
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

    private void setDerefAliases(final DerefAliases derefAliases) {
        this.derefAliases = derefAliases;
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

    private final void setOutlook_support(final boolean outlook_support) {
        this.outlook_support = outlook_support;
    }

    private void setPagesize(final int pagesize) {
        this.pagesize = pagesize;
    }

    private void setPooltimeout(final int pooltimeout) {
        this.pooltimeout = pooltimeout;
    }

    private void setReferrals(final ReferralTypes referrals) {
        this.referrals = referrals;
    }

    private void setRefreshinterval(final int refreshinterval) {
        this.refreshinterval = refreshinterval;
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
    private void setUserAdminDN(final String userAdminDN) {
        this.userAdminDN = userAdminDN;
    }

    /**
     * @param userAuthType
     */
    private void setUserAuthType(final UserAuthType userAuthType) {
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

    private void setStoragePriority(int priority) {
        this.storagePriority = priority;
    }

    public static class CheckStringPropertyEnumParameter {

        private final Properties m_props;

        private final StringBuilder m_log;

        private final String m_prefix;

        private final String m_filename;

        public CheckStringPropertyEnumParameter(final Properties props, final StringBuilder log, final String prefix, final String filename) {
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

    private static <T> void checkStringPropertyEnum(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final LdapConfigurationExceptionCode code, final SetterEnumClosure<T> setter) throws OXException {
        final String paramname = param.getName();
        final String property = parameterObject.getProps().getProperty(parameterObject.getPrefix() + paramname);
        if (null != property && 0 != property.length()) {
            try {
                final T valueOf = setter.valueOf(property);
                setter.set(valueOf);
                parameterObject.getLog().append('\t').append(paramname).append(": ").append(valueOf).append('\n');
            } catch (final IllegalArgumentException e) {
                throw LdapExceptionCode.MISSING_ATTRIBUTE.create(property);
            }
        } else {
            throw LdapConfigurationExceptionCode.PARAMETER_NOT_SET.create(parameterObject.getPrefix() + paramname, parameterObject.getFilename());
        }
    }

    private static void checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterClosure setter) throws OXException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        }
    }

    private static void checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterFallbackClosure setter) throws OXException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        } else {
            setter.set(setter.getFallback());
        }
    }

    private static void checkStringPropertyNonOptional(final CheckStringPropertyEnumParameter parameterObject, final Parameters param, final SetterClosure setter) throws OXException {
        final String property = checkStringProperty(parameterObject, param);
        if (null != property) {
            setter.set(property);
        } else {
            throw LdapConfigurationExceptionCode.PARAMETER_NOT_SET.create(parameterObject.getPrefix() + param.getName(), parameterObject.getFilename());
        }
    }

    private static String checkStringProperty(final CheckStringPropertyEnumParameter parameterObject, final Parameters param) throws OXException {
        return PropertyHandler.checkStringProperty(parameterObject.getProps(), parameterObject.getPrefix() + param.getName());
    }

    private void setSearchfilterDistributionlist(final String searchfilterDistriutionlist) {
        this.searchfilterDistributionlist = searchfilterDistriutionlist;
    }

    public String getSearchfilterDistributionlist() {
        return searchfilterDistributionlist;
    }

    private void setBaseDNDistributionlist(final String baseDNDistriutionlist) {
        this.baseDNDistributionlist = baseDNDistriutionlist;
    }

    public String getBaseDNDistributionlist() {
        return baseDNDistributionlist;
    }

    private void setSearchScopeDistributionlist(final SearchScope searchScoprDistributionlist) {
        this.searchScopeDistributionlist = searchScoprDistributionlist;
    }

    public SearchScope getSearchScopeDistributionlist() {
        return searchScopeDistributionlist;
    }
}
