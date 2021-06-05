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

package com.openexchange.mail.attachment.impl.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.attachment.impl.AttachmentTokenRegistry;
import com.openexchange.mail.attachment.impl.portable.PortableAttachmentTokenFactory;
import com.openexchange.mail.attachment.impl.portable.PortableCheckTokenExistenceFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;


/**
 * {@link AttachmentActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class AttachmentActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link AttachmentActivator}.
     */
    public AttachmentActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TimerService.class, HazelcastInstance.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AttachmentTokenRegistry service = AttachmentTokenRegistry.initInstance(this);

        registerService(CustomPortableFactory.class, new PortableCheckTokenExistenceFactory());
        registerService(CustomPortableFactory.class, new PortableAttachmentTokenFactory());

        registerService(AttachmentTokenService.class, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        AttachmentTokenRegistry.releaseInstance();
        super.stopBundle();
    }

}
