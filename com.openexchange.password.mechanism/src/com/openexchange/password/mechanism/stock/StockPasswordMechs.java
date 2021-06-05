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

package com.openexchange.password.mechanism.stock;

import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.impl.algorithm.SHACrypt;
import com.openexchange.password.mechanism.impl.mech.BCryptMech;
import com.openexchange.password.mechanism.impl.mech.CryptMech;
import com.openexchange.password.mechanism.impl.mech.SHAMech;

/**
 * {@link StockPasswordMechs}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
@SuppressWarnings("deprecation")
public enum StockPasswordMechs {

    SHA1(new SHAMech(SHACrypt.SHA1)),

    SHA256(new SHAMech(SHACrypt.SHA256)),

    SHA512(new SHAMech(SHACrypt.SHA512)),

    BCRYPT(new BCryptMech()),

    CRYPT(new CryptMech()),
    ;

    private PasswordMech passwordMech;

    private StockPasswordMechs(PasswordMech passwordMech) {
        this.passwordMech = passwordMech;
    }

    public String getIdentifier() {
        return this.passwordMech.getIdentifier();
    }

    public PasswordMech getPasswordMech() {
        return this.passwordMech;
    }
}
