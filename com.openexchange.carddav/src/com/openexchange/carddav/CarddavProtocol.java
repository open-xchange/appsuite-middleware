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

package com.openexchange.carddav;

import java.util.Arrays;
import java.util.List;
import org.jdom2.Namespace;
import com.openexchange.carddav.reports.AddressbookMultigetReport;
import com.openexchange.carddav.reports.AddressbookQueryReport;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.reports.ACLPrincipalPropSet;
import com.openexchange.dav.reports.SyncCollection;
import com.openexchange.webdav.action.WebdavAction;


/**
 * {@link CarddavProtocol}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CarddavProtocol extends DAVProtocol {

    public static final Namespace CARD_NS = Namespace.getNamespace("CARD", "urn:ietf:params:xml:ns:carddav");
    public static final String ADDRESSBOOK = "<CARD:addressbook />";

    public static final Namespace ME_NS = Namespace.getNamespace("MM", "http://me.com/_namespace/");

    private static final List<Namespace> ADDITIONAL_NAMESPACES = Arrays.asList(CARD_NS, ME_NS);

    @Override
    public List<Namespace> getAdditionalNamespaces() {
        return ADDITIONAL_NAMESPACES;
    }

    @Override
    public WebdavAction getReportAction(String namespace, String name) {
        if (namespace.equals(AddressbookMultigetReport.NAMESPACE) && name.equals(AddressbookMultigetReport.NAME)) {
            return new AddressbookMultigetReport(this);
        }
        if (namespace.equals(SyncCollection.NAMESPACE) && name.equals(SyncCollection.NAME)) {
            return new SyncCollection(this);
        }
        if (namespace.equals(ACLPrincipalPropSet.NAMESPACE) && name.equals(ACLPrincipalPropSet.NAME)) {
            return new ACLPrincipalPropSet(this);
        }
        if (namespace.equals(AddressbookQueryReport.NAMESPACE) && name.equals(AddressbookQueryReport.NAME)) {
            return new AddressbookQueryReport(this);
        }
        return super.getReportAction(namespace, name);
    }

}
