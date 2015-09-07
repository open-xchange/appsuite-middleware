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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.capabilities.rest;

import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONException;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.internal.CapabilityServiceImpl;
import com.openexchange.capabilities.json.CapabilitiesJsonWriter;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.GET;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.server.ServiceExceptionCode;

/**
 * The {@link CapabilitiesRESTService} allows clients to retrieve capabilities for arbitrary users.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@ROOT("/capabilities/v1")
public class CapabilitiesRESTService extends OXRESTService<Void> {
    
    private static final AtomicReference<CapabilityServiceImpl> CAP_SERVICE_REF = new AtomicReference<CapabilityServiceImpl>();
    
    /**
     * Sets the given capabilities service
     *
     * @param capService The service to set
     */
    public static void setCapabilityService(CapabilityServiceImpl capService) {
        CAP_SERVICE_REF.set(capService);
    }
    
    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CapabilitiesRESTService}.
     */
    public CapabilitiesRESTService() {
        super();
    }
    
    /**
     * GET /rest/capabilities/v1/all/[contextId]/[userId]
     */
    @GET("/all/:context/:user")
    public Object getAll(int context, int user) throws OXException {
        CapabilityServiceImpl capService = CAP_SERVICE_REF.get();
        if (null == capService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        
        try {
            CapabilitySet capabilities = capService.getCapabilities(user, context);
            return CapabilitiesJsonWriter.toJson(capabilities.asSet());
        } catch (JSONException e) {
            throw CapabilityExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
