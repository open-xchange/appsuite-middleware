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

package com.openexchange.user.copy.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.osgi.CompositeBundleActivator;
import com.openexchange.user.copy.internal.additional.osgi.AdditionalCopyActivator;
import com.openexchange.user.copy.internal.attachment.osgi.AttachmentCopyActivator;
import com.openexchange.user.copy.internal.chronos.osgi.ChronosCopyActivator;
import com.openexchange.user.copy.internal.connection.osgi.ConnectionFetcherActivator;
import com.openexchange.user.copy.internal.contact.osgi.ContactCopyActivator;
import com.openexchange.user.copy.internal.context.osgi.ContextLoadActivator;
import com.openexchange.user.copy.internal.folder.osgi.FolderCopyActivator;
import com.openexchange.user.copy.internal.infostore.osgi.InfostoreCopyActivator;
import com.openexchange.user.copy.internal.mailaccount.osgi.MailAccountCopyActivator;
import com.openexchange.user.copy.internal.messaging.osgi.MessagingCopyActivator;
import com.openexchange.user.copy.internal.oauth.osgi.OAuthCopyActivator;
import com.openexchange.user.copy.internal.subscription.osgi.SubscriptionCopyActivator;
import com.openexchange.user.copy.internal.tasks.osgi.TaskCopyActivator;
import com.openexchange.user.copy.internal.usecount.osgi.UseCountCopyActivator;
import com.openexchange.user.copy.internal.user.osgi.UserCopyActivator;
import com.openexchange.user.copy.internal.usersettings.osgi.UserSettingsActivator;

public class Activator extends CompositeBundleActivator {

    public Activator() {
	    super();
	}

    @Override
    protected BundleActivator[] getActivators() {
        return new BundleActivator[] {
            new UserCopyServiceActivator(),
            new ContextLoadActivator(),
            new UserSettingsActivator(),
            new UserCopyActivator(),
            new FolderCopyActivator(),
            new ConnectionFetcherActivator(),
            new ChronosCopyActivator(),
            new ContactCopyActivator(),
            new AttachmentCopyActivator(),
            new TaskCopyActivator(),
            new SubscriptionCopyActivator(),
            new MessagingCopyActivator(),
            new OAuthCopyActivator(),
            new InfostoreCopyActivator(),
            new AdditionalCopyActivator(),
            new CommandActivator(),
            new MailAccountCopyActivator(),
            new UseCountCopyActivator()
        };
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Activator.class);
        super.start(context);
        log.info("Bundle started: com.openexchange.user.copy");
    }
}
