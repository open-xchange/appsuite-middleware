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

package com.openexchange.security.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.security.manager.OXSecurityManager;
import com.openexchange.security.manager.configurationReader.ConfigurationReader;
import com.openexchange.security.manager.exceptions.SecurityManagerExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OXSecurityManagerImpl} Adds and loads permissions to the Security Manager
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class OXSecurityManagerImpl implements OXSecurityManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXSecurityManagerImpl.class);

    private final ConditionalPermissionAdmin condPermAdminService;
    private final ServiceLookup serviceLookup;
    private static String DEFAULT_POLICY_FILE = "/opt/open-xchange/etc/security/policies.policy";  // Default file if not specified in POLICY_FILE_JAVA_PARAM
    private static String POLICY_FILE_JAVA_PARAM = "openexchange.security.policy";
    private static Pattern VAR_PATTERN = Pattern.compile("\\$\\{[^}]+\\}");

    /**
     * Initializes a new {@link OXSecurityManagerImpl}.
     *
     * @param serviceLookup
     * @throws OXException
     */
    public OXSecurityManagerImpl (ServiceLookup serviceLookup) throws OXException {
        this.condPermAdminService = serviceLookup.getServiceSafe(ConditionalPermissionAdmin.class);
        this.serviceLookup = serviceLookup;
    }

    /**
     * Get a new conditionalPermissionUpdate
     *
     * @return A new ConditionalPermissionUpdate
     * @throws OXException
     */
    private ConditionalPermissionUpdate getPermissionUpdate () {
        return condPermAdminService.newConditionalPermissionUpdate();
    }

    /**
     * Replace variables in the policy file with java system parameters
     *
     * @param encoded the String from the configuration file with parameters
     * @return String with variables populated with the system values
     */
    private static String replaceVariables (String encoded) {
        if (encoded == null) {
            return null;
        }
        encoded = encoded.replaceAll("\\$\\{/\\}", File.separator);
        Matcher matcher = VAR_PATTERN.matcher(encoded);
        while (matcher.find()) {
            String param = matcher.group().trim();
            String variable = System.getProperty(param.substring(2, param.length() -1));
            if (variable == null || variable.isEmpty()) {
                LOG.debug("Wiping rule {} due to missing parameter", encoded);
                return null;   /// Wiping the rule if we can't find all parameters
            }
            encoded = encoded.replace(param, variable);
        }
        return encoded;
    }

    /**
     * Get a ConditionalPermissionInfo from a FolderPermission
     *
     * @param folderPermission  The foldePermission
     * @param recursive  If should be recursive
     * @return  ConditionalPermissionInfo based on the FolderPermission
     */
    private ConditionalPermissionInfo getInfoFromFolderPerm (FolderPermission folderPermission, boolean recursive) {
        return condPermAdminService.newConditionalPermissionInfo(
            recursive ? folderPermission.getRecursiveName() : folderPermission.getName(),
            new ConditionInfo[0],
            new PermissionInfo[] {
                recursive ? folderPermission.getRecursivePermissionInfo() : folderPermission.getPermissionInfo()},
            folderPermission.getDecision());
    }

    /**
     * Get a list of ConditionalPermissionInfo for a Folderpermission.  Will include recursive if applicable
     *
     * @param folderPermission
     * @return List of COnfitionalPermissionInfo based on the FolderPermission
     */
    private List<ConditionalPermissionInfo> getInfoListFromFolderPerm (FolderPermission folderPermission) {
        ArrayList<ConditionalPermissionInfo> newList = new ArrayList<ConditionalPermissionInfo>(2);
        if (folderPermission.getType() == FolderPermission.Type.RECURSIVE) {
            newList.add(getInfoFromFolderPerm(folderPermission, true));
        }
        newList.add(getInfoFromFolderPerm(folderPermission,false));
        return newList;

    }

    @Override
    public void insertFolderPolicy(List<FolderPermission> folderPermissions) throws OXException {
        ConditionalPermissionUpdate permissionUpdate = getPermissionUpdate();
        List<ConditionalPermissionInfo> list = permissionUpdate.getConditionalPermissionInfos();
        for (FolderPermission folderPermission: folderPermissions) {
            list.addAll(0, getInfoListFromFolderPerm(folderPermission));
        }
        final boolean commited = permissionUpdate.commit();
        if (!commited) {
            LOG.error("Cannot apply security policies because \"Conditional Permission Admin\" was modified concurrently");
        }

    }

    @Override
    public void insertPolicy(ConditionInfo[] conditions, PermissionInfo[] permissions, String name, String access) {
        ConditionalPermissionUpdate permissionUpdate = getPermissionUpdate();
        List<ConditionalPermissionInfo> list = permissionUpdate.getConditionalPermissionInfos();
        ConditionalPermissionInfo newInfo = condPermAdminService.newConditionalPermissionInfo(name, conditions, permissions, access);
        list.add(0, newInfo);
        final boolean commited = permissionUpdate.commit();
        if (!commited) {
            LOG.error("Cannot apply security policies because \"Conditional Permission Admin\" was modified concurrently");
        }
    }

    @Override
    public void appendFolderPolicy(FolderPermission folderPermission) throws OXException {
        ConditionalPermissionUpdate permissionUpdate = getPermissionUpdate();
        final List<ConditionalPermissionInfo> list = permissionUpdate.getConditionalPermissionInfos();
        list.addAll(getInfoListFromFolderPerm(folderPermission));
        final boolean commited = permissionUpdate.commit();
        if (!commited) {
            LOG.error("Cannot apply security policies because \"Conditional Permission Admin\" was modified concurrently");
        }

    }

    @Override
    public void loadFromPolicyFile() throws OXException {
        String policyFileName = System.getProperty(POLICY_FILE_JAVA_PARAM);
        final File policyFile = new File(policyFileName != null ? policyFileName : DEFAULT_POLICY_FILE);
        if (policyFile.exists()) {
            ConditionalPermissionUpdate permissionUpdate = getPermissionUpdate();
            final List<ConditionalPermissionInfo> list = permissionUpdate.getConditionalPermissionInfos();
            PolicyFileParser policyParser = new PolicyFileParser(policyFile);
            try {
                List<String> encodedPolicies = policyParser.readPolicies();
                for (String encodedPolicy : encodedPolicies) {
                    try {
                        String policy = replaceVariables(encodedPolicy);
                        if (policy != null) {
                            ConditionalPermissionInfo conditionalPermissionInfo =
                                condPermAdminService.newConditionalPermissionInfo(policy);
                            list.add(conditionalPermissionInfo);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while parsing the following policy: {}. The policy will be ignored.", encodedPolicy, e.getMessage());
                    }
                }
                final boolean commited = permissionUpdate.commit();
                if (!commited) {
                    LOG.error("Cannot apply security policies because \"Conditional Permission Admin\" was modified concurrently");
                }
            } catch (IOException ex) {
                throw SecurityManagerExceptionCodes.PROBLEM_POLICY_FILE.create(ex.getCause(), ex);
            } catch (IllegalStateException e) {
                throw SecurityManagerExceptionCodes.PROBLEM_POLICY_FILE.create(e.getCause(), e);
            }
        } else {
            LOG.error("Security Policy file not found {}", policyFileName);
            LOG.error("Security Manager will not function properly");
        }
    }

    @Override
    public void updateFromConfiguration() throws OXException {
        ConfigurationReader secConfig = serviceLookup.getService(ConfigurationReader.class);
        try {
            boolean changed = false;
            List<FolderPermission> folderPermissions = secConfig.readConfigFolders();
            ConditionalPermissionUpdate permissionUpdate = getPermissionUpdate();
            List<ConditionalPermissionInfo> currentList = permissionUpdate.getConditionalPermissionInfos();
            List<FolderPermission> updatedPermissions = new ArrayList<FolderPermission>();
            List<ConditionalPermissionInfo> toRemove = new ArrayList<ConditionalPermissionInfo>();
            // Loop through permissions and compare with existing.  Check for updates/changes
            for (FolderPermission perm : folderPermissions) {
                boolean found = false;
                for (ConditionalPermissionInfo info : currentList) {
                    if (info.getName().equals(perm.getName())) {
                        ConditionalPermissionInfo newInfo = getInfoFromFolderPerm(perm, false);
                        found = true;
                        if (!newInfo.getEncoded().equals(info.getEncoded())) {        // Check if changed at all
                            toRemove.addAll(getInfoListFromFolderPerm(perm));         // Add for removal
                            updatedPermissions.add(perm);                             // Add to list for inserting
                            LOG.info("Found updated security configuration {}", newInfo.getName());
                        }
                    }
                }
                if (!found) {  // Apparently new
                    updatedPermissions.add(perm);
                    LOG.info("Found new security configuration {}", perm.getName());
                }
            }
            // Remove updated
            for (ConditionalPermissionInfo toRem : toRemove) {
                currentList.removeIf(p -> (toRem.getName().equals(p.getName())));
            }
            // Add new values
            for (FolderPermission perm: updatedPermissions) {
                changed=true;
                currentList.addAll(0, getInfoListFromFolderPerm(perm));
            }

            if (changed) {
                final boolean commited = permissionUpdate.commit();
                if (!commited) {
                    LOG.error("Cannot apply security policies because \"Conditional Permission Admin\" was modified concurrently");
                }
            }
        } catch (OXException e) {
            throw SecurityManagerExceptionCodes.PROBLEM_UPDATING_SECURITY_POLICIES.create(e.getCause(), e);
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            updateFromConfiguration();
        } catch (OXException e) {
            LOG.error("Error reloading security configuration ", e);
        }

    }

    @Override
    public Interests getInterests() {
        ConfigurationReader configReader = serviceLookup.getService(ConfigurationReader.class);
        if (configReader == null) {
            return Reloadables.getInterestsForAll();
        }
        return Reloadables.interestsForProperties(configReader.getConfigurationPaths());
    }


}
