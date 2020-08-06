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
 *    trademarks of the OX Software GmbH. group of companies.
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
