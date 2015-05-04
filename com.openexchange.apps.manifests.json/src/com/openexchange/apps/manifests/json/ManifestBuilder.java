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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.apps.manifests.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.apps.manifests.ManifestContributor;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ManifestBuilder} - Build manifests based on manifest files and registered {@link ManifestContributor}s.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ManifestBuilder {

    final private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManifestBuilder.class);
    
    private JSONArray manifests;
    private NearRegistryServiceTracker<ManifestContributor> manifestContributorTracker;

    public ManifestBuilder(JSONArray manifests, NearRegistryServiceTracker<ManifestContributor> manifestContributorTracker) {
        this.manifests = manifests;
        this.manifestContributorTracker = manifestContributorTracker;
    }
    
    /**
     * Compute the manifests from files and {@link ManifestContributor}s.
     * 
     * @param session The {@link ServerSession}
     * @throws OXException 
     */
    public JSONArray buildManifests(ServerSession session) throws OXException {
        manifests = new JSONArray(manifests);

        JSONArray result = new JSONArray();
        for(ManifestContributor contributor: manifestContributorTracker.getServiceList()) {
            try {
                JSONArray additionalManifests = contributor.getAdditionalManifests(session);
                if (additionalManifests != null) {
                    for(int i = 0, size = additionalManifests.length(); i < size; i++) {
                        manifests.put(additionalManifests.get(i));
                    }
                }
            } catch (OXException|JSONException ex) {
                LOG.error("Error while trying to get additional manifests from contributor {} ", contributor, ex);
            }
        }

        try {
                for (int i = 0, size = manifests.length(); i < size; i++) {
                    JSONObject definition = manifests.getJSONObject(i);
                    result.put(new JSONObject(definition));
                }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage(), x);
        }
        return result;
    }
    
}
