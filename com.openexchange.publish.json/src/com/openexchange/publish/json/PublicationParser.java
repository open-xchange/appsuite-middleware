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

package com.openexchange.publish.json;

import static com.openexchange.publish.json.FieldNames.ENABLED;
import static com.openexchange.publish.json.FieldNames.ENTITY;
import static com.openexchange.publish.json.FieldNames.ENTITY_MODULE;
import static com.openexchange.publish.json.FieldNames.ID;
import static com.openexchange.publish.json.FieldNames.TARGET;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.exception.OXException;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.types.EntityMap;

/**
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationParser {

    private final PublicationTargetDiscoveryService discovery;

    // private static final FormContentParser formParser = new FormContentParser();

    private final Map<String, EntityType> entityTypes = new EntityMap();

    public PublicationParser(PublicationTargetDiscoveryService discovery) {
        super();
        this.discovery = discovery;
    }


    /**
     * @param object
     * @return
     * @throws JSONException
     * @throws OXException
     * @throws OXException
     */
    public Publication parse(JSONObject object) throws JSONException, OXException, OXException {
        Publication publication = new Publication();
        if(object.has(ID)) {
            publication.setId(object.getInt(ID));
        }
        if(object.has(ENTITY_MODULE)) {
            String module = object.getString(ENTITY_MODULE);
            publication.setModule(module);
            if(object.has(ENTITY)) {
                JSONObject entityDefinition = object.getJSONObject(ENTITY);
                EntityType entityType = entityTypes.get(module);
                if(entityType == null) {
                    throw PublicationJSONErrorMessage.UNKOWN_ENTITY_MODULE.create(module);
                }
                String entityId = entityType.toEntityID(entityDefinition);
                publication.setEntityId(entityId);
            }
        }
        if(object.has(TARGET)) {
            String target = object.getString(TARGET);
            PublicationTarget pubTarget = discovery.getTarget(target);
            if (null == pubTarget){
                throw PublicationJSONErrorMessage.UNKNOWN_TARGET.create(target);
            }
            publication.setTarget(pubTarget);
            if(object.has(pubTarget.getId())) {
                publication.setConfiguration(FormContentParser.parse(object.getJSONObject(pubTarget.getId()), pubTarget.getFormDescription()));
            }
        }

        if(object.has(ENABLED)) {
            publication.setEnabled(object.getBoolean(ENABLED));
        }
        return publication;
    }

    public void registerEntityType(String type, EntityType entityType) {
        entityTypes.put(type, entityType);
    }

}
