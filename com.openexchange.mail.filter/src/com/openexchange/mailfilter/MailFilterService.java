/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailfilter;

import java.util.List;
import java.util.Map;
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
        antispam("antispam"), autoforward("autoforward"), vacation("vacation"), all(""), custom("custom"), category("category"), syscategory("syscategory");

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
    public List<Rule> listRules(final Credentials credentials, final String flag) throws OXException;

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
     * Get a map with extended properties
     *
     * @param credentials the user's credentials
     * @return a map with extended properties
     * @throws OXException
     */
    public Map<String, Object> getExtendedProperties(final Credentials credentials) throws OXException;

    /**
     * Get a set with static capabilities
     *
     * @param credentials the user's credentials (but only used to determine host and port)
     * @return a Set with static capabilities
     * @throws OXException
     */
    public Set<String> getStaticCapabilities(Credentials credentials) throws OXException;

    /**
     * Converts a rule to its textual representation
     *
     * @param credentials The credentials
     * @param rule The rule to convert
     * @return The textual representation
     * @throws OXException
     */
    public String convertToString(Credentials credentials, Rule rule) throws OXException;

    /**
     * Executes given command.
     *
     * @param credentials The credentials
     * @param command The command to execute
     * @throws OXException If command execution fails
     */
    public void executeCommand(Credentials credentials, MailFilterCommand command) throws OXException;

}
