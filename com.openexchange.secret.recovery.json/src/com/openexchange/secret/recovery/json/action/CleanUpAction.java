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

package com.openexchange.secret.recovery.json.action;

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.json.SecretRecoveryAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CleanUpAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CleanUpAction extends AbstractSecretRecoveryAction {

    private final Set<EncryptedItemCleanUpService> cleanUpServices;

    /**
     * Initializes a new {@link CleanUpAction}.
     *
     * @param services
     * @param cleanUpServices 
     */
    public CleanUpAction(final ServiceLookup services, Set<EncryptedItemCleanUpService> cleanUpServices) {
        super(services);
        this.cleanUpServices = cleanUpServices;
    }

    @Override
    protected AJAXRequestResult perform(final SecretRecoveryAJAXRequest req) throws OXException, JSONException {
        final SecretService secretService = getService(SecretService.class);
        if (null == secretService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SecretService.class.getName());
        }
        // Get the secret string
        final String secret = secretService.getSecret(req.getSession());
        // Do the clean-up
        for(EncryptedItemCleanUpService cleanUp: cleanUpServices) {
            cleanUp.cleanUpEncryptedItems(secret, req.getSession());
        }
        // Prepare response
        final JSONObject object = new JSONObject(1);
        object.put("clean_up", true);
        return new AJAXRequestResult(object, "json");
    }

}
