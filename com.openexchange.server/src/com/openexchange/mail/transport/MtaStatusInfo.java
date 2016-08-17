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

package com.openexchange.mail.transport;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.mail.Address;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.json.io.Jsonable;

/**
 * {@link MtaStatusInfo} - A container for MTA status information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class MtaStatusInfo implements Jsonable {

    private final List<Address> sentAddresses;
    private final List<Address> unsentAddresses;
    private final List<Address> invalidAddresses;
    private int returnCode;

    /**
     * Initializes a new {@link MtaStatusInfo}.
     */
    public MtaStatusInfo() {
        super();
        sentAddresses = new LinkedList<Address>();
        unsentAddresses = new LinkedList<Address>();
        invalidAddresses = new LinkedList<Address>();
    }

    @Override
    public Object toJson() throws IOException {
        try {
            JSONObject jMtaInfo = new JSONObject(6);

            jMtaInfo.put("return_code", returnCode);

            int size = sentAddresses.size();
            if (size > 0) {
                JSONArray jAddresses = new JSONArray(size);
                for (Address address : sentAddresses) {
                    jAddresses.put(address.toString());
                }
                jMtaInfo.put("sent_addresses", jAddresses);
            }

            size = unsentAddresses.size();
            if (!unsentAddresses.isEmpty()) {
                JSONArray jAddresses = new JSONArray(size);
                for (Address address : unsentAddresses) {
                    jAddresses.put(address.toString());
                }
                jMtaInfo.put("unsent_addresses", jAddresses);
            }

            size = invalidAddresses.size();
            if (!invalidAddresses.isEmpty()) {
                JSONArray jAddresses = new JSONArray(size);
                for (Address address : invalidAddresses) {
                    jAddresses.put(address.toString());
                }
                jMtaInfo.put("invalid_addresses", jAddresses);
            }

            return jMtaInfo;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Gets the list of addresses that the message was sent to
     *
     * @return The sent addresses
     */
    public List<Address> getSentAddresses() {
        return sentAddresses;
    }

    /**
     * Gets the list of addresses that the message was <b>not</b> sent to
     *
     * @return The unsent addresses
     */
    public List<Address> getUnsentAddresses() {
        return unsentAddresses;
    }

    /**
     * Gets the list of invalid addresses
     *
     * @return The invalid addresses
     */
    public List<Address> getInvalidAddresses() {
        return invalidAddresses;
    }

    /**
     * Gets the MTA return code.
     *
     * @return The return code
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the MTA return code
     *
     * @param The MTA return code to set
     */
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

}
