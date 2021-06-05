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

package com.openexchange.config.cascade.reseller;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerConfigProperty;

/**
 * {@link ResellerBasicPropertyImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ResellerBasicPropertyImpl implements BasicProperty {

    private static final String SCOPE = ConfigViewScope.RESELLER.getScopeName();
    private static final String RESELLER_ID_METADATA_NAME = "resellerId";

    private final String property;
    private final int contextId;
    private final ResellerService resellerService;
    private ResellerConfigProperty resellerProperty;
    private boolean loaded;

    /**
     * Initializes a new {@link ResellerBasicPropertyImpl}.
     */
    public ResellerBasicPropertyImpl(String property, int contextId, ResellerService resellerService) {
        super();
        this.property = property;
        this.contextId = contextId;
        this.resellerService = resellerService;
    }

    @Override
    public void set(String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create(value, SCOPE);
    }

    @Override
    public String get() throws OXException {
        loadProperty();
        if (resellerProperty == null) {
            return null;
        }
        return resellerProperty.getValue();
    }

    @Override
    public void set(String metadataName, String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, SCOPE);
    }

    @Override
    public String get(String metadataName) throws OXException {
        loadProperty();
        // The only metadata name is the 'resellerId'
        if (resellerProperty != null && RESELLER_ID_METADATA_NAME.equals(metadataName)) {
            return Integer.toString(resellerProperty.getResellerId());
        }
        return null;
    }

    @Override
    public boolean isDefined() throws OXException {
        loadProperty();
        return null != resellerProperty && resellerProperty.getValue() != null;
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        loadProperty();
        return isDefined() ? ImmutableList.of(RESELLER_ID_METADATA_NAME) : ImmutableList.of();
    }

    private synchronized void loadProperty() throws OXException {
        if (loaded) {
            return;
        }
        resellerProperty = resellerService.getConfigPropertyByContext(contextId, property);
        loaded = true;
    }
}
