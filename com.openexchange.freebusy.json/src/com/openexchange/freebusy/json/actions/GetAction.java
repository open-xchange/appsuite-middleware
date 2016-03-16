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

package com.openexchange.freebusy.json.actions;

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.json.FreeBusyRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get free/busy information.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "participant", description = "The participant to get the free/busy data for. May be either an internal user-, group- or resource-ID, or an e-mail address for external participants."),
    @Parameter(name = "from", description = "The lower (inclusive) limit of the requested time-range."),
    @Parameter(name = "until", description = "The upper (exclusive) limit of the requested time-range."),
    @Parameter(name = "merged", type = Type.BOOLEAN, optional = true, description = "Whether to pre-process the free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.")
}, responseDescription = "Response: An array of free/busy intervals.")
public class GetAction extends FreeBusyAction {

    public GetAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(FreeBusyRequest request) throws OXException {
        FreeBusyData freeBusyData;
        if (request.isMerged()) {
            freeBusyData = getFreeBusyService().getMergedFreeBusy(
                request.getSession(), request.getParticipant(), request.getFrom(), request.getUntil());
        } else {
            freeBusyData = getFreeBusyService().getFreeBusy(
                request.getSession(), request.getParticipant(), request.getFrom(), request.getUntil());
        }
        return new AJAXRequestResult(null != freeBusyData ?
            serialize(freeBusyData.getIntervals(), request.getTimeZone()) : JSONObject.NULL, "json");
    }

}
