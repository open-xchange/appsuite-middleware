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

package com.openexchange.ajax;


/**
 * {@link FinalContactConstants}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum FinalContactConstants {
    ACTION_ASSOCIATE("associate"),
    ACTION_DISSOCIATE("dissociate"),
    ACTION_GET_BY_UUID("getByUuid"),
    ACTION_GET_ASSOCIATED("getAssociated"),
    ACTION_GET_ASSOCIATION("getAssociationBetween"),
    PARAMETER_UUID("uuid"),
    PARAMETER_UUID1("uuid1"),
    PARAMETER_UUID2("uuid2"),
    PARAMETER_FOLDER_ID1("folder1"),
    PARAMETER_FOLDER_ID2("folder2"),
    PARAMETER_CONTACT_ID1("id1"),
    PARAMETER_CONTACT_ID2("id2");

    private String name;

    private FinalContactConstants(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }


}
