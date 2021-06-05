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

package com.openexchange.push.ms.mbean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.exception.OXException;
import com.openexchange.push.ms.PushMsInit;
import com.openexchange.push.ms.osgi.PushMsActivator;

/**
 * {@link PushMsMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsMBeanImpl extends StandardMBean implements PushMsMBean {

    /**
     * Initializes a new {@link PushMsMBeanImpl}.
     */
    public PushMsMBeanImpl() throws NotCompliantMBeanException {
        super(PushMsMBean.class);

    }

    @Override
    public void startListening() {
        PushMsInit init = PushMsActivator.INIT_REF.get();
        if (null != init) {
            try {
                init.init();
            } catch (OXException e) {
                final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsMBeanImpl.class);
                LOG.error("", e);
            }
        }
    }

    @Override
    public void stopListening() {
        PushMsInit init = PushMsActivator.INIT_REF.get();
        if (null != init) {
            init.close();
        }
    }

}
