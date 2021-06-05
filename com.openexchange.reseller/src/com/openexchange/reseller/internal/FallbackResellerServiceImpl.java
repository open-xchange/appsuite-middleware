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

package com.openexchange.reseller.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.reseller.data.ResellerCapability;
import com.openexchange.reseller.data.ResellerConfigProperty;
import com.openexchange.reseller.data.ResellerTaxonomy;

/**
 * {@link FallbackResellerServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class FallbackResellerServiceImpl implements ResellerService {

    private static ResellerAdmin DEFAULT;
    static {
        DEFAULT = ResellerAdmin.builder().name("default").build();
    }

    /**
     * Initialises a new {@link FallbackResellerServiceImpl}.
     */
    public FallbackResellerServiceImpl() {
        super();
    }

    @Override
    public ResellerAdmin getReseller(int cid) throws OXException {
        return DEFAULT;
    }

    @Override
    public ResellerAdmin getResellerByName(String resellerName) throws OXException {
        return DEFAULT;
    }

    @Override
    public ResellerAdmin getResellerById(int resellerId) throws OXException {
        return DEFAULT;
    }

    @Override
    public List<ResellerAdmin> getResellerAdminPath(int cid) throws OXException {
        return getAll();
    }

    @Override
    public List<ResellerAdmin> getSubResellers(int parentId) throws OXException {
        return getAll();
    }

    @Override
    public List<ResellerAdmin> getAll() {
        return ImmutableList.of(DEFAULT);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Set<ResellerCapability> getCapabilities(int resellerId) throws OXException {
        return ImmutableSet.of();
    }

    @Override
    public ResellerConfigProperty getConfigProperty(int resellerId, String key) {
        return null;
    }

    @Override
    public Map<String, ResellerConfigProperty> getAllConfigProperties(int resellerId) {
        return ImmutableMap.of();
    }

    @Override
    public Map<String, ResellerConfigProperty> getConfigProperties(int resellerId, Set<String> keys) {
        return ImmutableMap.of();
    }

    @Override
    public Set<ResellerTaxonomy> getTaxonomies(int resellerId) throws OXException {
        return ImmutableSet.of();
    }

    @Override
    public Set<ResellerCapability> getCapabilitiesByContext(int contextId) throws OXException {
        return ImmutableSet.of();
    }

    @Override
    public ResellerConfigProperty getConfigPropertyByContext(int contextId, String key) throws OXException {
        return null;
    }

    @Override
    public Map<String, ResellerConfigProperty> getAllConfigPropertiesByContext(int contextId) throws OXException {
        return ImmutableMap.of();
    }

    @Override
    public Map<String, ResellerConfigProperty> getConfigPropertiesByContext(int contextId, Set<String> keys) throws OXException {
        return ImmutableMap.of();
    }

    @Override
    public Set<ResellerTaxonomy> getTaxonomiesByContext(int contextId) throws OXException {
        return ImmutableSet.of();
    }
}
