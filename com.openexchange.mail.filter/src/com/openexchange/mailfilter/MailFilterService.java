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

package com.openexchange.mailfilter;

import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link MailFilterService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SingletonService
public interface MailFilterService {

    public enum FilterType {
        antispam("antispam"), autoforward("autoforward"), vacation("vacation"), all(""), custom("custom");

        private final String flag;

        private FilterType(final String flag) {
            this.flag = flag;
        }

        public String getFlag() {
            return flag;
        }
    }

    public enum DayOfWeek {
        sunday, monday, tuesday, wednesday, thursday, friday, saturday
    }

    /**
     * Create a new mail filter rule and return it's UID.
     *
     * @param credentials user's credentials
     * @param rule the new mail filter rule
     * @return the UID of the new mail filter
     * @throws OXException
     */
    public int createFilterRule(final Credentials credentials, final Rule rule) throws OXException;

    /**
     * Updates an already existing mail filter rule.
     *
     * @param credentials user's credentials
     * @param rule the mail filter rule
     * @param uid the rule's UID
     * @throws OXException
     */
    public void updateFilterRule(final Credentials credentials, final Rule rule, int uid) throws OXException;

    /**
     * Delete the specified rule
     *
     * @param credentials user's credentials
     * @param uid UID of the mail filter rule to delete
     * @throws OXException
     */
    public void deleteFilterRule(final Credentials credentials, final int uid) throws OXException;

    /**
     * Delete the specified rules
     *
     * @param credentials user's credentials
     * @param uids UIDs of the mail filter rules to delete
     * @throws OXException
     */
    public void deleteFilterRules(final Credentials credentials, final int... uids) throws OXException;

    /**
     * Delete all filters for the specified user
     *
     * @param credentials the user's credentials
     * @throws OXException
     */
    public void purgeFilters(final Credentials credentials) throws OXException;

    /**
     * Get the filter rule uniquely identified by the specified UID
     *
     * @param credentials user's credentials
     * @param uid rule's UID
     * @return the rule or null if none found
     * @throws OXException
     */
    public Rule getFilterRule(final Credentials credentials, final int uid) throws OXException;

    /**
     * Return a list with all mail filter rules
     *
     * @param credentials the user's credentials
     * @param flag instructs the method to only rules matching the specified flag (optional, can be null)
     * @return a list with all mail filter rules
     * @throws OXException
     */
    public List<Rule> listRules(final Credentials credentials, final FilterType flag) throws OXException;

    /**
     * Return a list with all mail filter rules
     *
     * @param credentials the user's credentials
     * @param flag instructs the method to only rules matching the specified flag (optional, can be null)
     * @return a list with all mail filter rules
     * @throws OXException
     */
    public List<Rule> listRules(final Credentials credentials, final String flag) throws OXException;

    /**
     * Return a list with all mail filters except those specified in the exclusion list
     *
     * @param credentials the user's credentials
     * @param exclusionFlags a list with exclusion flags
     * @return a list with all mail filter rules except those in the exclusion list
     * @throws OXException
     */
    public List<Rule> listRules(final Credentials credentials, final List<FilterType> exclusionFlags) throws OXException;

    /**
     * Reorder the rules
     *
     * @param credentials user's credentials
     * @param uids An array of UIDs which represents how the rules should be reordered
     * @throws OXException
     */
    public void reorderRules(final Credentials credentials, final int[] uids) throws OXException;

    /**
     * Fetch the entire active script for the specified user
     *
     * @param credentials user's credentials
     * @return the active mail filter script as a string
     * @throws OXException
     */
    public String getActiveScript(final Credentials credentials) throws OXException;

    /**
     * Get a set with capabilities
     *
     * @param credentials the user's credentials
     * @return a Set with capabilities
     * @throws OXException
     */
    public Set<String> getCapabilities(final Credentials credentials) throws OXException;

    /**
     * Get a set with static capabilities
     *
     * @param credentials the user's credentials (but only used to determine host and port)
     * @return a Set with static capabilities
     * @throws OXException
     */
    public Set<String> getStaticCapabilities(Credentials credentials) throws OXException;

}
