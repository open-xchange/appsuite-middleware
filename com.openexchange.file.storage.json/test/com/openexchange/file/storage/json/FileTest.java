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

package com.openexchange.file.storage.json;

import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.actions.files.TestFriendlyInfostoreRequest;
import com.openexchange.sim.SimBuilder;

/**
 * {@link FileTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileTest {

    protected SimBuilder fileAccessBuilder;

    protected TestFriendlyInfostoreRequest request;

    public TestFriendlyInfostoreRequest request() {
        return request = new TestFriendlyInfostoreRequest() {

            private IDBasedFileAccess files = null;

            @Override
            public IDBasedFileAccess getFileAccess() {
                if (files != null) {
                    return files;
                }
                if (fileAccessBuilder != null) {
                    files = fileAccessBuilder.getSim(IDBasedFileAccess.class);
                } else {
                    files = null;
                }
                return files;
            }

        };
    }

    public SimBuilder fileAccess() {
        if (fileAccessBuilder != null) {
            return fileAccessBuilder;
        }
        return fileAccessBuilder = new SimBuilder();
    }
}
