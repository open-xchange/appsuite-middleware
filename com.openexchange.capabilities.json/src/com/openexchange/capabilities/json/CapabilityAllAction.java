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

package com.openexchange.capabilities.json;

import java.util.Set;
import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.json.osgi.TrackerAvailabilityChecker;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CapabilityAllAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(noSession = true)
public class CapabilityAllAction implements AJAXActionService {

    private final ServiceLookup services;
    private final Capability editPasswordCapability;
    private final AvailabilityChecker editPasswordChecker;

    /**
     * Initializes a new {@link CapabilityAllAction}.
     *
     * @param services The service look-up
     * @param context The bundle context
     */
    public CapabilityAllAction(final ServiceLookup services, final BundleContext context) {
        super();
        this.services = services;
        editPasswordCapability = new Capability(Permission.EDIT_PASSWORD.name().toLowerCase());
        editPasswordChecker = TrackerAvailabilityChecker.getAvailabilityCheckerFor(PasswordChangeService.class, true, context);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        final CapabilityService capabilityService = services.getService(CapabilityService.class);
        // Get capabilities
        final Set<Capability> capabilities = capabilityService.getCapabilities(session);
        // Check them
        if (!editPasswordChecker.isAvailable()) {
            capabilities.remove(editPasswordCapability);
        }
        return new AJAXRequestResult(capabilities, "capability");
    }

    public void close() {
        editPasswordChecker.close();
    }

}
