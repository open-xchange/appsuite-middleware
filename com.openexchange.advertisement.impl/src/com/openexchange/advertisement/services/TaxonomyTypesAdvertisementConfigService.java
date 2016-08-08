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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.advertisement.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.osgi.Services;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.session.Session;

/**
 * {@link TaxonomyTypesAdvertisementConfigService} is an implementation of the {@link AdvertisementConfigService} based on taxonomy/types.
 *
 * It compares the taxonomy/types of the given context with a configured list of packages (com.openexchange.advertisement.taxonomy.types) and returns the first matching item.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class TaxonomyTypesAdvertisementConfigService extends AbstractAdvertisementConfigService {

    private static final String TAXONOMY_TYPES = "taxonomy/types";
    private static final String TAXONOMY_TYPE_CONFIGURATION = "com.openexchange.advertisement.taxonomy.types";

    /**
     * Gets the instance of {@code TaxonomyTypesAdvertisementConfigService}.
     *
     * @return The instance
     */
    public static TaxonomyTypesAdvertisementConfigService getInstance() {
        return new TaxonomyTypesAdvertisementConfigService();
    }

    // ------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TaxonomyTypesAdvertisementConfigService}.
     */
    private TaxonomyTypesAdvertisementConfigService() {
        super();
    }

    @Override
    protected String getReseller(Session session) throws OXException {
        ResellerService resellerService = Services.getService(ResellerService.class);
        ResellerAdmin resellerAdmin = resellerService.getReseller(session.getContextId());
        return resellerAdmin.getName();
    }

    @Override
    protected String getPackage(Session session) throws OXException {
        ContextService ctxService = Services.getService(ContextService.class);
        Context ctx = ctxService.getContext(session.getContextId());
        Map<String, List<String>> attributes = ctx.getAttributes();
        List<String> packs = new ArrayList<>();
        if (attributes.containsKey(TAXONOMY_TYPES)) {
            List<String> taxonomyTypes = attributes.get(TAXONOMY_TYPES);
            for (String types : taxonomyTypes) {
                packs.addAll(Arrays.asList(Strings.splitByComma(types)));
            }
        }

        ConfigViewFactory configurationService = Services.getService(ConfigViewFactory.class);
        ConfigView view = configurationService.getView();
        String types = view.get(TAXONOMY_TYPE_CONFIGURATION, String.class);
        if (Strings.isEmpty(types)) {
            return PACKAGE_ALL;
        }
        String[] typesArray = Strings.splitByComma(types);

        for (String type : typesArray) {
            if (packs.contains(type)) {
                return type;
            }
        }

        return PACKAGE_ALL;
    }

    @Override
    public String getSchemeId() {
        return "TaxonomyTypes";
    }

}
