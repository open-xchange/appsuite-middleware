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

package com.openexchange.dav.principals;

import java.util.Arrays;
import java.util.List;
import org.jdom2.Namespace;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.principals.reports.PrincipalMatchReport;
import com.openexchange.dav.principals.reports.PrinicpalSearchPropertySetReport;
import com.openexchange.dav.reports.ExpandPropertyReport;
import com.openexchange.dav.reports.PrinicpalPropertySearchReport;
import com.openexchange.webdav.action.WebdavAction;

/**
 * {@link PrincipalProtocol}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrincipalProtocol extends DAVProtocol {

    private static final List<Namespace> ADDITIONAL_NAMESPACES = Arrays.asList(CAL_NS, APPLE_NS, CALENDARSERVER_NS);

    /**
     * Initializes a new {@link PrincipalProtocol}.
     */
    public PrincipalProtocol() {
        super();
    }

    @Override
    public WebdavAction getReportAction(String ns, String name) {
        if (PrincipalMatchReport.NAMESPACE.equals(ns) && PrincipalMatchReport.NAME.equals(name)) {
            return new PrincipalMatchReport(this);
        }
        if (PrinicpalSearchPropertySetReport.NAMESPACE.equals(ns) && PrinicpalSearchPropertySetReport.NAME.equals(name)) {
            return new PrinicpalSearchPropertySetReport(this);
        }
        if (PrinicpalPropertySearchReport.NAMESPACE.equals(ns) && PrinicpalPropertySearchReport.NAME.equals(name)) {
            return new PrinicpalPropertySearchReport(this);
        }
        if (ExpandPropertyReport.NAMESPACE.equals(ns) && ExpandPropertyReport.NAME.equals(name)) {
            return new ExpandPropertyReport(this);
        }
        return null;
    }

    @Override
    public List<Namespace> getAdditionalNamespaces() {
        return ADDITIONAL_NAMESPACES;
    }

}
