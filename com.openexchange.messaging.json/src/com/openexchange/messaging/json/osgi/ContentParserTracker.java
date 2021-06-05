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

package com.openexchange.messaging.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.messaging.json.MessagingContentParser;
import com.openexchange.messaging.json.MessagingMessageParser;

/**
 * {@link ContentParserTracker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContentParserTracker extends ServiceTracker<MessagingContentParser, MessagingContentParser> {

    private final MessagingMessageParser parser;

    public ContentParserTracker(final BundleContext context, final MessagingMessageParser parser) {
        super(context, MessagingContentParser.class.getName(), null);
        this.parser = parser;
    }

    @Override
    public MessagingContentParser addingService(ServiceReference<MessagingContentParser> reference) {
        MessagingContentParser parser = super.addingService(reference);
        this.parser.addContentParser(parser);
        return parser;
    }

    @Override
    public void removedService(ServiceReference<MessagingContentParser> reference, MessagingContentParser service) {
        parser.removeContentParser(service);
        super.removedService(reference, service);
    }
}
