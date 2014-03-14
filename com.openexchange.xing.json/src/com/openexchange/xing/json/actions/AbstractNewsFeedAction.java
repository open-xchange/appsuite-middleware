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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.xing.json.actions;

import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.xing.json.XingRequest;


/**
 * {@link AbstractNewsFeedAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractNewsFeedAction extends AbstractXingAction {

    /**
     * Initializes a new {@link AbstractNewsFeedAction}.
     * @param services
     */
    protected AbstractNewsFeedAction(ServiceLookup services) {
        super(services);
    }
    
    /**
     * Get the value of the specified mandatory parameter from the specified {@link XingRequest}.
     * @param request
     * @param param the parameter's name
     * @return the value of the parameter
     * @throws OXException if the parameter is missing from the request.
     */
    protected String getMandatoryStringParameter(XingRequest request, String param) throws OXException {
        String pv = request.getParameter(param);
        if (pv == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(param);
        }
        return pv;
    }
    
    /**
     * Get the specified integer parameter from the specified request.
     * 
     * @param request
     * @param param the parameter's name
     * @return the parameter as integer, or -1 if not present.
     * @throws OXException if the parameter value cannot be parsed to an integer
     */
    protected int getOptIntParameter(XingRequest request, String param) throws OXException {
        String p = request.getParameter(param);
        if (p != null) {
            try {
                return Integer.parseInt(p);
            } catch (NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(param, p);
            }
        } else {
            return -1;
        }
    }

}
