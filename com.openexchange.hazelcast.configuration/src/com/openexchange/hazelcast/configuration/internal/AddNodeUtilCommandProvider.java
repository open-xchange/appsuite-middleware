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


package com.openexchange.hazelcast.configuration.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.hazelcast.config.TcpIpConfig;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.java.Strings;

/**
 * {@link AddNodeUtilCommandProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddNodeUtilCommandProvider implements CommandProvider {

    private final HazelcastConfigurationService configService;

    public AddNodeUtilCommandProvider(HazelcastConfigurationService configService) {
        super();
        this.configService = configService;
    }

    @Override
    public String getHelp() {
        return "    addnode - Add a hazelcast node." + Strings.getLineSeparator();
    }

    public void _addnode(CommandInterpreter commandInterpreter) {
        String ip = commandInterpreter.nextArgument();
        if (Strings.isEmpty(ip)) {
            commandInterpreter.println("Couldn't resolve IP: " + ip);
            return;
        }
        try {
            if (false == configService.isEnabled()) {
                commandInterpreter.println("Hazelcast is disabled by configuration.");
                return;
            }
            TcpIpConfig tcpIpConfig = configService.getConfig().getNetworkConfig().getJoin().getTcpIpConfig();
            if (false == tcpIpConfig.isEnabled()) {
                commandInterpreter.println("Hazelcast newtork join is not configured to use TCP/IP.");
            }
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                commandInterpreter.println("Couldn't resolve address: " + ip);
            }
            if (null != address) {
                tcpIpConfig.addMember(address.getHostAddress());
                commandInterpreter.println("Added node to cluster network configuration: " + ip);
            }
        } catch (OXException e) {
            commandInterpreter.println("Error adding node to cluster network configuration: " + e.getMessage());
            commandInterpreter.printStackTrace(e);
        }
    }

}
