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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mdns.internal;

import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.exception.OXException;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceEntry;

/**
 * {@link MDNSCommandProvider} - The {@link CommandProvider command provider} to output MDNS status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSCommandProvider implements CommandProvider {

    private final MDNSService mdnsService;

    /**
     * Initializes a new {@link MDNSCommandProvider}.
     *
     * @param registry
     */
    public MDNSCommandProvider(final MDNSService mdnsService) {
        super();
        this.mdnsService = mdnsService;
    }

    public Object _mdnsServices(final CommandInterpreter intp) {
        /*
         * Check service identifier
         */
        String serviceId = intp.nextArgument();
        if (null == serviceId) {
            serviceId = "openexchange.service.messaging";
        }
        final StringBuilder sb = new StringBuilder(256);
        final List<MDNSServiceEntry> services;
        try {
            services = mdnsService.listByService(serviceId);
        } catch (final OXException e) {
            intp.print(sb.append("Error: ").append(e.getMessage()).toString());
            return null;
        }
        sb.setLength(0);
        intp.print(sb.append("---Tracked services of \"").append(serviceId).append(
            "\" ---\n").toString());
        final String delim = "\n\t";
        for (final MDNSServiceEntry mdnsServiceEntry : services) {
            sb.setLength(0);
            sb.append(delim).append("UUID: ").append(mdnsServiceEntry.getId());
            sb.append(delim).append("Address: ").append(mdnsServiceEntry.getAddress());
            sb.append(delim).append("Port: ").append(mdnsServiceEntry.getPort());
            sb.append('\n');
            intp.print(sb.toString());
        }
        /*
         * Return
         */
        return null;
    }

    @Override
    public String getHelp() {
        final StringBuilder builder = new StringBuilder(256).append("---Output tracked hosts of specified service---\n\t");
        builder.append("mdnsServices <service-id> - Output tracked hosts. Specify the service identifier; by default \"openexchange.service.messaging\".\n");
        return builder.toString();
    }

}
