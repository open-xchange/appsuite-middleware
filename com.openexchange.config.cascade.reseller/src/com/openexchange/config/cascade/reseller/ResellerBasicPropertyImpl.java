/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.server.ServiceLookup;

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
    private final ServiceLookup services;
    private ResellerConfigProperty resellerProperty;
    private boolean loaded;

    /**
     * Initializes a new {@link ResellerBasicPropertyImpl}.
     */
    public ResellerBasicPropertyImpl(String property, int contextId, ServiceLookup services) {
        super();
        this.property = property;
        this.contextId = contextId;
        this.services = services;
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
        ResellerService resellerService = services.getServiceSafe(ResellerService.class);
        resellerProperty = resellerService.getConfigPropertyByContext(contextId, property);
        loaded = true;
    }
}
