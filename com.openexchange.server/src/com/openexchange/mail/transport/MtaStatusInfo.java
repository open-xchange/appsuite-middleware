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
