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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.publish.actions.AbstractPublicationResponse;
import com.openexchange.ajax.publish.actions.AllPublicationsRequest;
import com.openexchange.ajax.publish.actions.AllPublicationsResponse;
import com.openexchange.ajax.publish.actions.DeletePublicationRequest;
import com.openexchange.ajax.publish.actions.DeletePublicationResponse;
import com.openexchange.ajax.publish.actions.GetPublicationRequest;
import com.openexchange.ajax.publish.actions.GetPublicationResponse;
import com.openexchange.ajax.publish.actions.ListPublicationsRequest;
import com.openexchange.ajax.publish.actions.ListPublicationsResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.ajax.publish.actions.UpdatePublicationRequest;
import com.openexchange.ajax.publish.actions.UpdatePublicationResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTargetDiscoveryService;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PublicationTestManager {

        private AbstractPublicationResponse lastResponse;

        private final Set<Integer> createdItems;

        private boolean failOnError = true;

        private AJAXClient client;

        private DynamicFormDescription formDescription;

        private PublicationTargetDiscoveryService publicationTargetDiscoveryService;

        public AbstractPublicationResponse getLastResponse() {
            return lastResponse;
        }

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        public boolean getFailOnError() {
            return failOnError;
        }

        public void setClient(AJAXClient client) {
            this.client = client;
        }

        public AJAXClient getClient() {
            return client;
        }

        public void setFormDescription(DynamicFormDescription formDescription) {
            this.formDescription = formDescription;
        }

        public DynamicFormDescription getFormDescription() {
            return formDescription;
        }

        public void setPublicationTargetDiscoveryService(PublicationTargetDiscoveryService service) {
            this.publicationTargetDiscoveryService = service;
        }

        public PublicationTargetDiscoveryService getPublicationTargetDiscoveryService() {
            return this.publicationTargetDiscoveryService;
        }

        public PublicationTestManager() {
            createdItems = new HashSet<Integer>();
        }

        public PublicationTestManager(AJAXClient client) {
            this();
            setClient(client);
        }

        public Publication newAction(Publication publication) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
            NewPublicationRequest newReq = new NewPublicationRequest(publication);
            newReq.setFailOnError(getFailOnError());
            NewPublicationResponse newResp = getClient().execute(newReq);
            createdItems.add(I(newResp.getId()));
            Publication updatedPublication = getAction(newResp.getId());
            publication.setConfiguration(updatedPublication.getConfiguration());
            publication.setId(updatedPublication.getId());
            lastResponse = newResp;
            return publication;
        }

        public Publication getAction(int id) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
            GetPublicationRequest getReq = new GetPublicationRequest(id);
            getReq.setFailOnError(getFailOnError());
            GetPublicationResponse getResp = getClient().execute(getReq);
            lastResponse = getResp;
            return getResp.getPublication(getPublicationTargetDiscoveryService());
        }

        public void deleteAction(Publication publication) throws OXException, IOException, SAXException, JSONException {
            int id = publication.getId();
            DeletePublicationRequest delReq = new DeletePublicationRequest(id);
            delReq.setFailOnError(getFailOnError());
            DeletePublicationResponse delResp = getClient().execute(delReq);
            createdItems.remove(I(id));
            lastResponse = delResp;
        }

        public void deleteAction(Collection<Integer> ids) throws OXException, IOException, SAXException, JSONException {
            DeletePublicationRequest delReq = new DeletePublicationRequest(ids);
            delReq.setFailOnError(getFailOnError());
            DeletePublicationResponse delResp = getClient().execute(delReq);
            createdItems.removeAll(ids);
            lastResponse = delResp;
        }

        public List<JSONArray> listAction(List<Integer> ids, List<String> columns) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
            ListPublicationsRequest listReq = new ListPublicationsRequest(ids,columns);
            listReq.setFailOnError(getFailOnError());
            ListPublicationsResponse listResp = getClient().execute(listReq);
            lastResponse = listResp;
            return listResp.getList();
        }

        public List<JSONArray> listAction(List<Integer> ids, List<String> columns, Map<String,List<String>> dynamicColumns) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
            ListPublicationsRequest listReq = new ListPublicationsRequest(ids,columns,dynamicColumns);
            listReq.setFailOnError(getFailOnError());
            ListPublicationsResponse listResp = getClient().execute(listReq);
            lastResponse = listResp;
            return listResp.getList();
        }

        public List<JSONArray> allAction(String folder, int id, String entityModule, List<String> columns) throws OXException, IOException, SAXException, JSONException, OXException, OXException{
            AllPublicationsRequest allReq = new AllPublicationsRequest(folder, id, entityModule, columns);
            allReq.setFailOnError(getFailOnError());
            AllPublicationsResponse allResp = getClient().execute(allReq);
            lastResponse = allResp;
            return allResp.getAll();
        }

        public List<JSONArray> allAction(String entityModule, int id, List<String> columns) throws OXException, IOException, SAXException, JSONException, OXException, OXException{
            AllPublicationsRequest allReq = new AllPublicationsRequest(id, entityModule, columns);
            allReq.setFailOnError(getFailOnError());
            AllPublicationsResponse allResp = getClient().execute(allReq);
            lastResponse = allResp;
            return allResp.getAll();
        }

        public List<JSONArray> allAction(List<String> columns) throws OXException, IOException, SAXException, JSONException, OXException, OXException{
            AllPublicationsRequest allReq = new AllPublicationsRequest(columns);
            allReq.setFailOnError(getFailOnError());
            AllPublicationsResponse allResp = getClient().execute(allReq);
            lastResponse = allResp;
            return allResp.getAll();
        }


        public List<JSONArray> allAction(String folder, int id, String entityModule, List<String> columns, Map<String,List<String>> dynamicColumns) throws OXException, IOException, SAXException, JSONException, OXException, OXException{
            AllPublicationsRequest allReq = new AllPublicationsRequest(folder, id, entityModule, columns, dynamicColumns);
            allReq.setFailOnError(getFailOnError());
            AllPublicationsResponse allResp = getClient().execute(allReq);
            lastResponse = allResp;
            return allResp.getAll();
        }


        public void updateAction(Publication publication) throws OXException, IOException, SAXException, JSONException {
            UpdatePublicationRequest updReq = new UpdatePublicationRequest(publication);
            updReq.setFailOnError(getFailOnError());
            UpdatePublicationResponse updResp = getClient().execute(updReq);
            lastResponse = updResp;
        }


        public void cleanUp() throws OXException, IOException, SAXException, JSONException {
            boolean failOnError2 = getFailOnError();
            setFailOnError(false);
            if(createdItems.size() > 0) {
                deleteAction(createdItems);
            }
            setFailOnError(failOnError2);
        }

}
