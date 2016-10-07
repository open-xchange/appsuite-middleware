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
