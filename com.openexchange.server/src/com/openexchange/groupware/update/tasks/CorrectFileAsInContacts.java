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

package com.openexchange.groupware.update.tasks;

import com.openexchange.groupware.update.SimpleStatementsUpdateTask;

/**
 * Corrects field90 aka fileAs in contacts to have proper contact names in card view of Outlook OXtender 2.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CorrectFileAsInContacts extends SimpleStatementsUpdateTask {

    public CorrectFileAsInContacts() {
        super();
    }

    @Override
    protected void statements() {
        add("UPDATE prg_contacts SET field90=field01 WHERE field90!=field01 OR field90 IS NULL AND field01 IS NOT NULL OR field01 IS NULL AND field90 IS NOT NULL");
    }
}
