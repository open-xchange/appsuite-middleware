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

package com.openexchange.mail.json.compose.share.internal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.DefaultMessageGenerator;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link MessageGeneratorRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MessageGeneratorRegistryImpl implements MessageGeneratorRegistry {

    private final ServiceListing<MessageGenerator> generators;

    /**
     * Initializes a new {@link MessageGeneratorRegistryImpl}.
     */
    public MessageGeneratorRegistryImpl(ServiceListing<MessageGenerator> generators) {
        super();
        this.generators = generators;
    }

    @Override
    public MessageGenerator getMessageGeneratorFor(ComposeRequest composeRequest) throws OXException {
        for (MessageGenerator messageGenerator : generators.getServiceList()) {
            if (messageGenerator.applicableFor(composeRequest)) {
                return messageGenerator;
            }
        }

        return DefaultMessageGenerator.getInstance();
    }

}
