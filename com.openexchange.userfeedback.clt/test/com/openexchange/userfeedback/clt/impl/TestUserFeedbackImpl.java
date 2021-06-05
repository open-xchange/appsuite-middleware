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

package com.openexchange.userfeedback.clt.impl;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.userfeedback.clt.AbstractUserFeedback;


/**
 * {@link TestUserFeedbackImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class TestUserFeedbackImpl extends AbstractUserFeedback {

    @Override
    protected void checkArguments(CommandLine cmd) {
    }

    @Override
    protected void addOptions(Options options) {
    }

    @Override
    protected WebTarget getEndpoint(CommandLine cmd) {
        return null;
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, Builder context) throws Exception {
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {

    }

    @Override
    protected String getFooter() {
        return null;
    }

    @Override
    protected String getName() {
        return null;
    }

}
