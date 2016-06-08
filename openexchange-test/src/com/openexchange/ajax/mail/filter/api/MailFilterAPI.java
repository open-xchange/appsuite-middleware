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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.filter.api.dao.MailFilterConfiguration;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.request.ConfigRequest;
import com.openexchange.ajax.mail.filter.api.request.InsertRequest;
import com.openexchange.ajax.mail.filter.api.response.ConfigResponse;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;

/**
 * {@link MailFilterAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterAPI {

    private final AJAXClient client;

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
        InsertRequest request = new InsertRequest(rule);
        InsertResponse response = client.execute(request);
        return Integer.parseInt(response.getId());
    }
}
