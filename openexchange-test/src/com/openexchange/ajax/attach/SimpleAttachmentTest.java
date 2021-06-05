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

package com.openexchange.ajax.attach;

import org.junit.Test;

public class SimpleAttachmentTest extends AbstractAttachmentTest {

    @Override
    public int createExclusiveWritableAttachable(final int folderId) throws Exception {
        return 22;
    }

    @Override
    public int getExclusiveWritableFolder() throws Exception {
        return 22;
    }

    @Override
    public int getModule() throws Exception {
        return 22;
    }

    @Test
    public void testMultiple() throws Exception {
        doMultiple();
    }

    @Test
    public void testDetach() throws Exception {
        doDetach();
    }

    @Test
    public void testUpdates() throws Exception {
        doUpdates();
    }

    @Test
    public void testAll() throws Exception {
        doAll();
    }

    @Test
    public void testGet() throws Exception {
        doGet();
    }

    @Test
    public void testDocument() throws Exception {
        doDocument();
    }

    @Test
    public void testList() throws Exception {
        doList();
    }

    @Test
    public void testQuota() throws Exception {
        doQuota();
    }
}
