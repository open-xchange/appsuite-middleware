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

package com.openexchange.admin.reseller.console;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link Change} - changereseller command line tool
 * 
 * @author choeger
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Change extends ResellerAbstraction {

    private enum DynamicNamespace {
        taxonomy,
        remove_taxonomy,
        config,
        remove_config;

    }

    private static Set<String> SUPPORTED_TAXONOMIES;
    static {
        Set<String> s = new HashSet<>();
        s.add("types");
        SUPPORTED_TAXONOMIES = Collections.unmodifiableSet(s);
    }

    /**
     * Entry point
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new Change().start(args);
    }

    /**
     * Initializes a new {@link Change}.
     */
    private Change() {
        super();
    }

    /**
     * Start the clt
     *
     * @param args the command line arguments
     */
    private void start(String[] args) {
        AdminParser parser = new AdminParser("changeadmin");

        setOptions(parser);

        String successtext = null;
        // parse the command line
        try {
            parser.ownparse(args);

            Credentials auth = credentialsparsing(parser);
            ResellerAdmin adm = parseChangeOptions(parser);

            OXResellerInterface rsi = getResellerInterface();

            parseAndSetAdminId(parser, adm);
            parseAndSetAdminname(parser, adm);

            successtext = nameOrIdSetInt(this.adminid, this.adminname, "admin");

            // Capabilities
            adm.setCapabilitiesToAdd(parseAndSetCapabilitiesToAdd(parser));
            adm.setCapabilitiesToRemove(parseAndSetCapabilitiesToRemove(parser));
            adm.setCapabilitiesToDrop(parseAndSetCapabilitiesToDrop(parser));

            // Configuration & Taxonomies
            applyDynamicOptionsToReseller(parser, adm);

            HashSet<String> removeRes = getRestrictionsToRemove(parser, this.removeRestrictionsOption);
            HashSet<Restriction> editRes = getRestrictionsToEdit(parser, this.editRestrictionsOption);
            ResellerAdmin dbadm = rsi.getData(adm, auth);
            HashSet<Restriction> dbres = OXResellerTools.array2HashSet(dbadm.getRestrictions());
            HashSet<Restriction> retRestrictions = handleAddEditRemoveRestrictions(dbres, OXResellerTools.array2HashSet(adm.getRestrictions()), removeRes, editRes);
            if (null != retRestrictions) {
                adm.setRestrictions(retRestrictions.toArray(new Restriction[retRestrictions.size()]));
            }
            rsi.change(adm, auth);
            displayChangedMessage(successtext, null, parser);
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(1);
        }
    }

    /**
     * Applies the dynamic options of configuration and taxonomies to the specified {@link ResellerAdmin}
     * 
     * @param parser the {@link AdminParser}
     * @param adm The {@link ResellerAdmin}
     */
    private void applyDynamicOptionsToReseller(AdminParser parser, ResellerAdmin adm) {
        Map<String, Map<String, String>> dynamicArguments = parser.getDynamicArguments();
        Map<String, String> configToAdd = new HashMap<>();
        Set<String> configToRemove = new HashSet<>();

        Set<String> taxonomiesToAdd = new HashSet<>();
        Set<String> taxonomiesToRemove = new HashSet<>();

        for (Map.Entry<String, Map<String, String>> namespaced : dynamicArguments.entrySet()) {
            String namespace = namespaced.getKey();
            for (Map.Entry<String, String> pair : namespaced.getValue().entrySet()) {
                String name = pair.getKey();
                String value = pair.getValue();
                DynamicNamespace dynamicNamespace;
                try {
                    dynamicNamespace = DynamicNamespace.valueOf(namespace.replace("-", "_"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown option '" + namespace + "'");
                    sysexit(1);
                    return;
                }
                switch (dynamicNamespace) {
                    case config:
                        configToAdd.put(name, value);
                        break;
                    case remove_config:
                        configToRemove.add(name);
                        break;
                    case taxonomy:
                        if (false == SUPPORTED_TAXONOMIES.contains(name)) {
                            System.err.println("Unsupported taxonomy '" + name + "'. Supported taxonomies are: " + SUPPORTED_TAXONOMIES);
                            sysexit(1);
                        }
                        taxonomiesToAdd.add(value);
                        break;
                    case remove_taxonomy:
                        if (false == SUPPORTED_TAXONOMIES.contains(name)) {
                            System.err.println("Unsupported taxonomy '" + name + "'. Supported taxonomies are: " + SUPPORTED_TAXONOMIES);
                            sysexit(1);
                        }
                        taxonomiesToRemove.add(value);
                        break;
                    default:
                        break;
                }
            }
        }
        // Configuration
        adm.setConfigurationToAdd(configToAdd);
        adm.setConfigurationToRemove(configToRemove);

        // Taxonomies
        adm.setTaxonomiesToAdd(taxonomiesToAdd);
        adm.setTaxonomiesToRemove(taxonomiesToRemove);
    }

    /**
     * Sets the extra options for the clt
     *
     * @param parser The {@link AdminParser}
     */
    private void setOptions(AdminParser parser) {
        setChangeOptions(parser);
        parser.allowDynamicOptions();
        parser.allowFlexibleDynamicOptions();
    }
}
