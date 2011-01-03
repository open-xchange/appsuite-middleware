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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.voipnow.json.actions;

import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import com._4psa.common_xsd._2_0_4.PositiveInteger;
import com._4psa.headerdata_xsd._2_0_4.UserCredentials;
import com._4psa.pbxdata_xsd._2_0_4.TimeIntervalBlock;
import com._4psa.pbxmessages_xsd._2_0_4.GetTimeIntervalBlocksRequest;
import com._4psa.pbxmessages_xsd._2_0_4.GetTimeIntervalBlocksRequestChoice_type0;
import com._4psa.pbxmessages_xsd._2_0_4.GetTimeIntervalBlocksResponse;
import com._4psa.pbxmessagesinfo_xsd._2_0_4.GetTimeIntervalBlocksResponseType;
import com._4psa.voipnowservice._2_0_4.PBXPortStub;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;


/**
 * {@link GetTimeIntervalAction} - Gets the time interval.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetTimeIntervalAction extends AbstractVoipNowSOAPAction<PBXPortStub> {

    /**
     * The <tt>timeinterval</tt> action string.
     */
    public static final String ACTION = "timeinterval";

    /**
     * Initializes a new {@link GetTimeIntervalAction}.
     */
    public GetTimeIntervalAction() {
        super();
    }

    @Override
    protected String getSOAPPath() {
        return "/soap2/pbx_agent.php";
    }

    @Override
    protected int getSOAPTimeout() {
        return 60000;
    }

    @Override
    protected PBXPortStub newSOAPStub() throws AxisFault {
        return new PBXPortStub();
    }

    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws AbstractOXException {
        try {
            /*
             * Parse parameters
             */
            final String userId = String.valueOf(getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId()));
            final VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);
            /*
             * Stub
             */
            final PBXPortStub stub = configureStub(setting);
            /*
             * Create appropriate request
             */
            final GetTimeIntervalBlocksRequest getTimeIntervalBlocksRequest = new GetTimeIntervalBlocksRequest();
            
            final GetTimeIntervalBlocksRequestChoice_type0 ct = new GetTimeIntervalBlocksRequestChoice_type0();
            final PositiveInteger pi = new PositiveInteger();
            pi.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(userId));
            ct.setUserID(pi);
            
            getTimeIntervalBlocksRequest.setGetTimeIntervalBlocksRequestChoice_type0(ct);
            
            /*
             * Create user credentials
             */
            final UserCredentials userCredentials = getUserCredentials(setting);
            /*
             * Execute request
             */
            final GetTimeIntervalBlocksResponse getTimeIntervalBlocksResponse = stub.getTimeIntervalBlocks(getTimeIntervalBlocksRequest, userCredentials);
            final GetTimeIntervalBlocksResponseType getTimeIntervalBlocksResponseType = getTimeIntervalBlocksResponse.getGetTimeIntervalBlocksResponse();
            
            final TimeIntervalBlock timeIntervalBlock = getTimeIntervalBlocksResponseType.getTimeIntervalBlock()[0];
            
            final PositiveInteger id = timeIntervalBlock.getID();
            
            return new AJAXRequestResult(id.getPositiveInteger());
        } catch (final AxisFault e) {
            throw VoipNowExceptionCodes.SOAP_FAULT.create(e, e.getMessage());
        } catch (final RemoteException e) {
            throw VoipNowExceptionCodes.REMOTE_ERROR.create(e, e.getMessage());
        }
    }

}
