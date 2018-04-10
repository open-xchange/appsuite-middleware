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

package com.openexchange.mail.authenticity.impl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CustomRuleChecker}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class CustomRuleChecker implements Reloadable{

    private static final Property YML_FILE_NAME_PROP = DefaultProperty.valueOf("com.openexchange.mail.authenticity.custom.rules.filename", null);

    private final ConcurrentMap<String, Future<List<Rule>>> rulesMap;
    private final LeanConfigurationService leanConfigurationService;

    /**
     * Initializes a new {@link CustomRuleChecker}.
     */
    public CustomRuleChecker(LeanConfigurationService leanService) {
        super();
        this.leanConfigurationService = leanService;
        rulesMap = new ConcurrentHashMap<>(32, 0.9F, 1);
    }

    /**
     * Loads the rule from the given YAML file
     *
     * @param yml The name of the YAML file
     * @return The loaded rules
     * @throws OXException In case an error occurs during the initialization. E.g. because of a syntax error in the YAML file.
     */
    @SuppressWarnings("unchecked")
    protected List<Rule> load(String yml) throws OXException {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        Object yaml = configService.getYaml(yml);
        if (null == yaml) {
            return Collections.emptyList();
        }

        Map<String, Object> map = get(yaml, Map.class, yml);
        map = get(map.get("custom_rules"), Map.class, yml);
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        for (Object o : map.values()) {
            Map<String, String> ruleMap = get(o, Map.class, yml);
            rules.add(new Rule( toList(ruleMap.get("spf_status")),
                                ruleMap.get("spf_domain"),
                                toList(ruleMap.get("dkim_status")),
                                ruleMap.get("dkim_domain"),
                                ruleMap.get("from_domain"),
                                ruleMap.get("result")));
        }
        return rules.build();
    }

    private static final String ANY_VALUE = "any";
    private static final List<String> ALL_STATI;

    static {
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("fail");
        tmp.add("neutral");
        tmp.add("none");
        tmp.add("pass");
        ALL_STATI = Collections.unmodifiableList(tmp);
    }

    private List<String> toList(String input) {
        if (Strings.isEmpty(input)) {
            return null;
        }
        input = input.toLowerCase();
        if (ANY_VALUE.equals(input)) {
            return ALL_STATI;
        }
        String[] splitByComma = Strings.splitByComma(input);
        List<String> result = new ArrayList<>();
        for(String str: splitByComma) {
            result.add(str);
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private <T> T get(Object o, Class<T> clazz, String ymlName) throws OXException {
        if (false == clazz.isInstance(o)) {
            throw MailAuthenticityExceptionCodes.UNEXPECTED_ERROR.create("The YAML structure of file \"" + ymlName + "\" is invalid.");
        }

        return (T) o;
    }

    /**
     * Checks and applies custom rules
     *
     * @param session The user session
     * @param result The {@link MailAuthenticityResult}
     * @throws OXException
     */
    public void check(Session session, MailAuthenticityResult result) throws OXException {
        final String ymlName = getYmlFile(session);
        if (ymlName == null) {
            return;
        }

        Future<List<Rule>> f = rulesMap.get(ymlName);
        if (f == null) {
            FutureTask<List<Rule>> ft = new FutureTask<List<Rule>>(new Callable<List<Rule>>() {

                @Override
                public List<Rule> call() throws Exception {
                    return load(ymlName);
                }
            });
            f = rulesMap.putIfAbsent(ymlName, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }

        List<Rule> rules = ThreadPools.getFrom(f);
        for (Rule r : rules) {
            if (r.match(result)) {
                result.setStatus(MailAuthenticityStatus.valueOf(r.getResult().toUpperCase()));
                return;
            }
        }
    }

    /**
     * Gets the name of the YAML file for session-associated user.
     *
     * @param session The session providing user data
     * @return The YAML file name
     */
    private String getYmlFile(Session session) {
        return leanConfigurationService.getProperty(session.getUserId(), session.getContextId(), YML_FILE_NAME_PROP);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        rulesMap.clear();
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     *
     * {@link Rule} defines a custom mail authenticity rule
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    private static class Rule {

        private final List<String> spfStati;
        private final String spfDomain;
        private final List<String> dkimStati;
        private final String dkimDomain;
        private final String result;
        private final String fromDomain;

        /**
         * Initializes a new {@link Rule}.
         *
         * @param spfStati The optional SPF status
         * @param spfDomain The SPF domain in case spfStatus is set
         * @param dkimStatus The optional DKIM status
         * @param dkimDomain The DKIM domain in case dkimStatus is set
         * @param fromDomain The optional from domain
         * @param result The new result in case of match or null to fall-back to 'PASS'
         * @throws OXException
         */
        Rule(List<String> spfStati, String spfDomain, List<String> dkimStati, String dkimDomain, String fromDomain, String result) throws OXException {
            super();
            this.spfStati = spfStati;
            this.spfDomain = spfDomain;
            this.dkimStati = dkimStati;
            this.dkimDomain = dkimDomain;
            this.fromDomain = fromDomain;
            this.result = result;

            if ((spfStati != null && spfDomain == null) || (dkimStati != null && dkimDomain == null)) {
                throw MailAuthenticityExceptionCodes.UNEXPECTED_ERROR.create("The mail authenticity custom rule is invalid.");
            }

            if (spfStati == null && dkimStati == null) {
                throw MailAuthenticityExceptionCodes.UNEXPECTED_ERROR.create("The mail authenticity custom rule is invalid. Missing either a spf value or a dkim value.");
            }
        }

        public String getResult() {
            return result == null ? "PASS" : result;
        }

        @SuppressWarnings("unchecked")
        public boolean match(MailAuthenticityResult result) {
            List<MailAuthenticityMechanismResult> results = result.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
            if (null == results || results.isEmpty()) {
                return false;
            }

            if (spfStati != null) {
                boolean spfFound = false;
                for (MailAuthenticityMechanismResult mechResult : results) {

                    if (DefaultMailAuthenticityMechanism.SPF.equals(mechResult.getMechanism())) {
                        if (spfStati != null && !spfStati.contains(mechResult.getResult().getDisplayName().toLowerCase())) {
                            return false;
                        }
                        if (spfDomain != null && !mechResult.getDomain().equalsIgnoreCase(spfDomain)) {
                            return false;
                        }
                        spfFound = true;
                        break;
                    }
                }
                if (!spfFound) {
                    return false;
                }
            }

            if (dkimStati != null) {
                boolean skimFound = false;
                for (MailAuthenticityMechanismResult mechResult : results) {
                    if (DefaultMailAuthenticityMechanism.DKIM.equals(mechResult.getMechanism())) {
                        if (dkimStati != null && !dkimStati.contains(mechResult.getResult().getDisplayName().toLowerCase())) {
                            return false;
                        }
                        if (dkimDomain != null && !mechResult.getDomain().equalsIgnoreCase(dkimDomain)) {
                            return false;
                        }
                        skimFound = true;
                        break;
                    }
                }
                if (!skimFound) {
                    return false;
                }
            }
            Object attribute = result.getAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN);
            if (attribute != null && fromDomain != null && !attribute.toString().equalsIgnoreCase(fromDomain)) {
                return false;
            }
            return true;

        }
    } // End of class Rule

}

