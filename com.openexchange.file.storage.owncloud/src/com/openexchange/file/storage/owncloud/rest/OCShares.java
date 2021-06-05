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

package com.openexchange.file.storage.owncloud.rest;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;

/**
 * {@link OCShares}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OCShares extends AbstractOCJSONResponse {

    private static final Logger LOG = LoggerFactory.getLogger(OCShares.class);

    JSONArray shares;

    /**
     * Initializes a new {@link OCShares}.
     *
     * @param json
     * @throws OXException
     */
    public OCShares(JSONObject json) throws OXException {
        super(json);
    }

    /**
     * Parses the {@link JSONObject}
     *
     * @param json
     * @throws OXException
     */
    public static OCShares parse(JSONObject json) throws OXException {
        OCShares result = new OCShares(json);
        result.shares = (JSONArray) result.getData();
        return result;
    }

    /**
     * Gets the shares
     *
     * @return The shares
     */
    public List<OCShare> getShares(){
        List<OCShare> result = new ArrayList<>();
        shares.forEach((share) -> {
            JSONObject s = (JSONObject) share;
            try {
                result.add(new OCShare( s.getString("id"),
                                        s.getInt("share_type"),
                                        s.getString("uid_owner"),
                                        s.getInt("permissions"),
                                        s.getLong("stime"),
                                        s.getString("share_with"),
                                        s.getInt("mail_send")));
            } catch (JSONException e) {
              // TODO Handle error
                LOG.error(e.getMessage(), e);
            }
        });
        return result;
    }

    /**
     * {@link OCShare}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.4
     */
    public static class OCShare {

        final String id;
        final int share_type;
        final String owner;
        final int permission;
        final long stime;
        // expiration
        final String share_with;
        // share_with_additional_info
        final int mail_send;

        /**
         * Initializes a new {@link OCShare}.
         * @param id
         * @param share_type
         * @param owner
         * @param permission
         * @param stime
         * @param share_with
         * @param mail_send
         */
        public OCShare(String id, int share_type, String owner, int permission, long stime, String share_with, int mail_send) {
            super();
            this.id = id;
            this.share_type = share_type;
            this.owner = owner;
            this.permission = permission;
            this.stime = stime;
            this.share_with = share_with;
            this.mail_send = mail_send;
        }

        /**
         * Gets the id
         *
         * @return The id
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the share_type
         *
         * @return The share_type
         */
        public int getShare_type() {
            return share_type;
        }

        /**
         * Gets the owner
         *
         * @return The owner
         */
        public String getOwner() {
            return owner;
        }

        /**
         * Gets the permission
         *
         * @return The permission
         */
        public int getPermission() {
            return permission;
        }

        /**
         * Gets the stime
         *
         * @return The stime
         */
        public long getStime() {
            return stime;
        }

        /**
         * Gets the share_with
         *
         * @return The share_with
         */
        public String getShare_with() {
            return share_with;
        }

        /**
         * Gets the mail_send
         *
         * @return The mail_send
         */
        public int getMail_send() {
            return mail_send;
        }
    }

}
