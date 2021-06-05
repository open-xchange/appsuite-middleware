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

package com.openexchange.jolokia.restrictor;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jolokia.restrictor.policy.NetworkChecker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;

/**
 *
 * {@link OXRestrictorLocalhost} - Access from localhost only. All operations allowed
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.8.3
 */
public class OXRestrictorLocalhost extends OXRestrictor {

    private NetworkChecker networkChecker;

    public OXRestrictorLocalhost() throws OXException {
        super();
        initiateNetworkChecker();
    }

    /**
     * This method will create a networkChecker that only allows local access
     * @throws OXException in case a parsing error occured
     */
    private void initiateNetworkChecker() throws OXException {
        try {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element host = document.createElement("host");
        host.setTextContent("localhost");
        Element hostByIp = document.createElement("host");
        hostByIp.setTextContent("127.0.0.0/8");
        Element hostByIpV6 = document.createElement("host");
        hostByIpV6.setTextContent("0:0:0:0:0:0:0:1");
        Element remote = document.createElement("remote");
        remote.appendChild(host);
        remote.appendChild(hostByIp);
        remote.appendChild(hostByIpV6);
        Element restrict = document.createElement("restrict");
        restrict.appendChild(remote);
        document.appendChild(restrict);
        networkChecker = new NetworkChecker(document);
        } catch (ParserConfigurationException e) {
            throw OXException.general("Exception while creating the NetworkChecker for Jolokia", e);
        }
    }

    @Override
    public boolean isRemoteAccessAllowed(String... pHostOrAddress) {
        return networkChecker.check(pHostOrAddress);
    }

}
