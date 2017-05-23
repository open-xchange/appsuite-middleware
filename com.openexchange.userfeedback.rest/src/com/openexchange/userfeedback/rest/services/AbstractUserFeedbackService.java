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

package com.openexchange.userfeedback.rest.services;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;

/**
 * {@link AbstractUserFeedbackService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public abstract class AbstractUserFeedbackService extends JAXRSService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractUserFeedbackService.class);
    protected static final String DEFAULT_CONFIG_ERROR_MESSAGE = "A configuration error occurred.";

    protected AbstractUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    protected void validateParams(long start, long end) throws OXException {
        Set<String> badParams = new HashSet<>(2);
        if (start < 0L) {
            badParams.add("start");
        }
        if (end < 0L) {
            badParams.add("end");
        }
        if ((end != 0L) && (start != 0L) && (end < start)) {
            badParams.add("start");
            badParams.add("end");
        }
        if (!badParams.isEmpty()) {
            throw FeedbackExceptionCodes.INVALID_PARAMETER_VALUE.create(Strings.concat(",", badParams));
        }
    }

    protected JSONObject generateError(OXException ex) {
        JSONObject main = new JSONObject();
        try {
            ResponseWriter.addException(main, ex);
        } catch (JSONException e) {
            LOG.error("Error while generating error for client.", e);
        }
        return main;
    }
}
