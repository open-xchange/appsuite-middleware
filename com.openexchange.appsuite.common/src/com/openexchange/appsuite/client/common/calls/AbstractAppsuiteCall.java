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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.appsuite.client.common.calls;

import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.ACTION;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.appsuite.client.AppsuiteApiCall;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link AbstractAppsuiteCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The class of the response
 * @since v7.10.5
 */
public abstract class AbstractAppsuiteCall<T> implements AppsuiteApiCall<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractAppsuiteCall.class);

    /**
     * Initializes a new {@link AbstractAppsuiteCall}.
     */
    public AbstractAppsuiteCall() {
        super();
    }

    @Override
    @NonNull
    public Map<String, String> getPathParameters() {
        Map<String, String> parameters = new HashMap<>(10);
        if (Strings.isNotEmpty(getAction())) {
            parameters.put(ACTION, getAction());
        }
        fillParameters(parameters);
        return parameters;
    }

    protected abstract void fillParameters(Map<String, String> parameters);

    /**
     * Get the action that should be appended to the path parameters
     *
     * @return The action
     */
    protected abstract String getAction();

    /*
     * ------------------------- HELPERS -------------------------
     */

    /**
     * Checks that the given objects are not <code>null</code>
     *
     * @param objects The objects to check
     * @throws OXException {@link AppsuiteClientExceptions#MISSING_PARAMETER} in case a object is missing
     */
    protected void checkParameters(Object... objects) throws OXException {
        for (Object object : objects) {
            if (null == object) {
                throw AppsuiteClientExceptions.MISSING_PARAMETER.create("Missing parameter for login request");
            }
        }
    }

    /**
     * Transforms a {@link JSONObject} into an {@link HttpEntity}
     *
     * @param json The JSON to transform
     * @return The {@link HttpEntity}
     */
    protected HttpEntity toHttpEntity(JSONObject json) {
        return new InputStreamEntity(new JSONInputStream(json, Charsets.UTF_8_NAME), -1L, ContentType.APPLICATION_JSON);
    }

}
