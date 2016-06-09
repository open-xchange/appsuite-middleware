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

package com.openexchange.ajax.mail.filter.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.filter.api.dao.MailFilterConfiguration;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.request.AllRequest;
import com.openexchange.ajax.mail.filter.api.request.ConfigRequest;
import com.openexchange.ajax.mail.filter.api.request.DeleteRequest;
import com.openexchange.ajax.mail.filter.api.request.DeleteScriptRequest;
import com.openexchange.ajax.mail.filter.api.request.InsertRequest;
import com.openexchange.ajax.mail.filter.api.request.UpdateRequest;
import com.openexchange.ajax.mail.filter.api.response.AllResponse;
import com.openexchange.ajax.mail.filter.api.response.ConfigResponse;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;

/**
 * {@link MailFilterAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterAPI {

    private final AJAXClient client;
    private boolean failOnError = true;

    /**
     * Initialises a new {@link MailFilterAPI}.
     * 
     * @param client The {@link AJAXClient}
     */
    public MailFilterAPI(AJAXClient client) {
        super();
        this.client = client;
    }

    /**
     * Gets the failOnError
     *
     * @return The failOnError
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Sets the failOnError
     *
     * @param failOnError The failOnError to set
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Returns the configuration of the mail filter backend
     * 
     * @return the {@link MailFilterConfiguration} of the mail filter backend
     * @throws Exception if the operation fails
     */
    public MailFilterConfiguration getConfiguration() throws Exception {
        ConfigRequest request = new ConfigRequest();
        ConfigResponse response = client.execute(request);
        return response.getMailFilterConfiguration();
    }

    /**
     * Creates the specified mail filter {@link Rule}
     * 
     * @param rule The mail filter {@link Rule} to create
     * @return The identifier of the created rule
     * @throws Exception if the operation fails
     */
    public int createRule(Rule rule) throws Exception {
        InsertRequest request = new InsertRequest(rule, failOnError);
        InsertResponse response = client.execute(request);
        if (response.hasError()) {
            throw response.getException();
        }
        return response.getId();
    }

    /**
     * Updates the specified mail filter {@link Rule}
     * 
     * @param rule The mail filter {@link Rule} to update
     * @throws Exception if the operation fails
     */
    public void updateRule(Rule rule) throws Exception {
        UpdateRequest request = new UpdateRequest(rule);
        client.execute(request);
    }

    /**
     * Get all rules for the user
     * 
     * @return an unmodifiable list with all the rules for the user
     * @throws Exception
     */
    public List<Rule> listRules() throws Exception {
        return listRules(new AllRequest());
    }

    /**
     * Gets all rules for the specified user
     * 
     * @param username The user for which to get the rules
     * @return The list of all rules
     * @throws Exception if the operation fails
     */
    public List<Rule> listRules(String username) throws Exception {
        return listRules(new AllRequest(username));
    }

    /**
     * Deletes the rule with the specified identifier
     * 
     * @param id The rule's identifier
     * @throws Exception if the operation fails
     */
    public void deleteRule(int id) throws Exception {
        DeleteRequest request = new DeleteRequest(id);
        client.execute(request);
    }

    /**
     * Purges all mail filters for the specified user
     * 
     * @throws Exception if the operation fails
     */
    public void purge() throws Exception {
        List<Rule> rules = listRules();
        for (Rule r : rules) {
            deleteRule(r.getId());
        }
    }

    /**
     * Deletes the entire script of the user
     * 
     * @throws Exception if the operation fails
     */
    public void deleteScript() throws Exception {
        DeleteScriptRequest request = new DeleteScriptRequest();
        client.execute(request);
    }

    ///////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Executes the specified {@link AllRequest} and returns the list with rules
     * 
     * @param request The {@link AllRequest} to execute
     * @return An unmodifiable list with all rules
     * @throws Exception if execution fails
     */
    private List<Rule> listRules(AllRequest request) throws Exception {
        AllResponse response = client.execute(request);

        Rule[] ruleArray = response.getRules();
        List<Rule> rules = new ArrayList<>(ruleArray.length);
        Collections.addAll(rules, ruleArray);

        return Collections.unmodifiableList(rules);
    }
}
