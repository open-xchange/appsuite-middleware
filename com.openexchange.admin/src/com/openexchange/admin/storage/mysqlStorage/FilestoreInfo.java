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

package com.openexchange.admin.storage.mysqlStorage;

class FilestoreInfo {

    public final int contextID;
    public final int userID;
    public final int filestoreID;
    public final int writeDBPoolID;
    public final String dbSchema;

    public long usage;

    public FilestoreInfo(int contextID, int writeDBPoolID, String dbSchema, int filestoreID) {
        this(contextID, -1, writeDBPoolID, dbSchema, filestoreID);
    }

    public FilestoreInfo(int contextID, int userID, int writeDBPoolID, String dbSchema, int filestoreID) {
        super();
        this.contextID = contextID;
        this.userID = userID;
        this.writeDBPoolID = writeDBPoolID;
        this.dbSchema = dbSchema;
        this.filestoreID = filestoreID;
    }

    @Override
    public String toString(){
        return "cid: " + contextID + ", user: " + userID + ", fid: " + filestoreID + ", db: " + dbSchema + ", writepoolID: " + writeDBPoolID + ", usage: " + usage;
    }
}
