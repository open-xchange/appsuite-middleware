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

package com.openexchange.ajax.mail.filter.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.mail.filter.api.dao.MailFilterConfiguration;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.request.AbstractMailFilterRequest;
import com.openexchange.ajax.mail.filter.api.request.AllRequest;
import com.openexchange.ajax.mail.filter.api.request.ConfigRequest;
import com.openexchange.ajax.mail.filter.api.request.DeleteRequest;
import com.openexchange.ajax.mail.filter.api.request.DeleteScriptRequest;
import com.openexchange.ajax.mail.filter.api.request.GetScriptRequest;
import com.openexchange.ajax.mail.filter.api.request.InsertRequest;
import com.openexchange.ajax.mail.filter.api.request.ReorderRequest;
import com.openexchange.ajax.mail.filter.api.request.UpdateRequest;
import com.openexchange.ajax.mail.filter.api.response.AllResponse;
import com.openexchange.ajax.mail.filter.api.response.ConfigResponse;
import com.openexchange.ajax.mail.filter.api.response.GetScriptResponse;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;
import com.openexchange.ajax.mail.filter.api.response.ReorderResponse;
import com.openexchange.exception.OXException;

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
        ConfigResponse response = execute(request);
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
        InsertResponse response = execute(request);
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
        execute(request);
    }

    /**
     * Reorders the mail filters with the specified identifiers
     *
     * @param ids The identifiers of the mail filters to reorder
     * @throws Exception if the operation fails
     */
    public void reorder(int[] ids) throws Exception {
        ReorderRequest request = new ReorderRequest(ids);
        request.setFailOnError(failOnError);
        ReorderResponse response = client.execute(request);
        if (response.getException() != null) {
            throw response.getException();
        }
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
     * @param failOnError Defines whether the request should fail on error or not
     * @return The list of all rules
     * @throws Exception if the operation fails
     */
    public List<Rule> listRules(String username, boolean failOnError) throws Exception {
        return listRules(new AllRequest(username, failOnError));
    }

    /**
     * Deletes the rule with the specified identifier
     *
     * @param id The rule's identifier
     * @throws Exception if the operation fails
     */
    public void deleteRule(int id) throws Exception {
        DeleteRequest request = new DeleteRequest(id);
        execute(request);
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
        execute(request);
    }

    /**
     * Gets the whole script of the user as string
     *
     * @return The script
     * @throws Exception if the operation fails
     */
    public String getScript() throws Exception {
        GetScriptRequest request = new GetScriptRequest();
        GetScriptResponse response = client.execute(request);
        return response.getScript();
    }

    ///////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Execute the specified {@link AbstractMailFilterRequest} while considering the 'failOnError' flag. If an {@link OXException} is thrown from the
     * server and the 'failOnError' flag is set to false, then that exception is also thrown from this method.
     *
     * @param request The {@link AbstractMailFilterRequest} to execute
     * @return The {@link T} response
     * @throws OXException if a server error occurs
     * @throws IOException if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    private <T extends AbstractAJAXResponse> T execute(AbstractMailFilterRequest<T> request) throws OXException, IOException, JSONException {
        request.setFailOnError(failOnError);
        T response = client.execute(request);
        OXException oxe = response.getException();
        if (oxe != null) {
            throw oxe;
        }
        return response;
    }

    /**
     * Executes the specified {@link AllRequest} and returns the list with rules
     *
     * @param request The {@link AllRequest} to execute
     * @return An unmodifiable list with all rules
     * @throws Exception if execution fails
     */
    private List<Rule> listRules(AllRequest request) throws Exception {
        AllResponse response = execute(request);
        Rule[] ruleArray = response.getRules();
        List<Rule> rules = new ArrayList<>(ruleArray.length);
        Collections.addAll(rules, ruleArray);

        return Collections.unmodifiableList(rules);
    }
}
