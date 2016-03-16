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

package com.openexchange.ajax;


public class InfostoreClient {

    private InfostoreClient() {
        super();
    }

    private static InfostoreAJAXTest test = new InfostoreAJAXTest("InfostoreClient");

//    public static Response all(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.all(webConv, hostname, sessionId, folderId, columns, sort, order);
//    }
//
//    public static Response all(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.all(webConv, hostname, sessionId, folderId, columns);
//    }
//
//    public static Response all(final WebConversation webConv, final String protocol, final String hostname, final String sessionId, final int folderId, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.all(webConv, protocol, hostname, sessionId, folderId, columns, -1, null);
//    }
//
//    public static int copy(final WebConversation webConv, final String hostname, final String sessionId, final int id, final long timestamp, final Map<String, String> modified, final File upload, final String contentType) throws JSONException, IOException {
//        return test.copy(webConv, hostname, sessionId, id, timestamp, modified, upload, contentType);
//    }
//
//    public static int copy(final WebConversation webConv, final String hostname, final String sessionId, final int id, final long timestamp, final Map<String, String> modified) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.copy(webConv, hostname, sessionId, id, timestamp, modified);
//    }
//
//    public static int createNew(final WebConversation webConv, final String hostname, final String sessionId, final Map<String, String> fields, final File upload, final String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
//        return createNew(webConv, null, hostname, sessionId, fields, upload, contentType);
//    }
//
//    public static int createNew(final WebConversation webConv, final String protocol, final String hostname, final String sessionId, final Map<String, String> fields, final File upload, final String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.createNew(webConv, protocol, hostname, sessionId, fields, upload, contentType);
//    }
//
//    public static int createNew(final WebConversation webConv, final String hostname, final String sessionId, final Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.createNew(webConv, hostname, sessionId, fields);
//    }
//
//    public static int createNew(final WebConversation webConv, final String protocol, final String hostname, final String sessionId, final Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.createNew(webConv, protocol, hostname, sessionId, fields);
//    }
//
//    public static int[] delete(final WebConversation webConv, final String hostname, final String sessionId, final long timestamp, final int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.delete(webConv, hostname, sessionId, timestamp, ids);
//    }
//
//    public static int[] delete(final WebConversation webConv, final String protocol, final String hostname, final String sessionId, final long timestamp, final int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.delete(webConv, protocol, hostname, sessionId, timestamp, ids);
//    }
//
//    public static int[] detach(final WebConversation webConv, final String hostname, final String sessionId, final long timestamp, final int objectId, final int[] versions) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.detach(webConv, hostname, sessionId, timestamp, objectId, versions);
//    }
//
//    public static InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final int id, final int version, final String contentType) throws HttpException, IOException {
//        return test.document(webConv, hostname, sessionId, id, version, contentType);
//    }
//
//    public static InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final int id, final int version) throws HttpException, IOException {
//        return test.document(webConv, hostname, sessionId, id, version);
//    }
//
//    public static InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final int id, final String contentType) throws HttpException, IOException {
//        return test.document(webConv, hostname, sessionId, id, contentType);
//    }
//
//    public static InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final int id) throws HttpException, IOException {
//        return test.document(webConv, hostname, sessionId, id);
//    }
//
//    public static GetMethodWebRequest documentRequest(final String sessionId, final String hostname, final int id, final int version, final String contentType) {
//        return test.documentRequest(sessionId, hostname, id, version, contentType);
//    }
//
//    public static Response get(final WebConversation webConv, final String hostname, final String sessionId, final int objectId, final int version) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.get(webConv, hostname, sessionId, objectId, version);
//    }
//
//    public static Response get(final WebConversation webConv, final String hostname, final String sessionId, final int objectId) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.get(webConv, hostname, sessionId, objectId);
//    }
//
//    public static Response list(final WebConversation webConv, final String hostname, final String sessionId, final int[] columns, final int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.list(webConv, hostname, sessionId, columns, ids);
//    }
//
//    public static Response lock(final WebConversation webConv, final String hostname, final String sessionId, final int id, final long timeDiff) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.lock(webConv, hostname, sessionId, id, timeDiff);
//    }
//
//    public static Response lock(final WebConversation webConv, final String hostname, final String sessionId, final int id) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.lock(webConv, hostname, sessionId, id);
//    }
//
//    public static Response revert(final WebConversation webConv, final String hostname, final String sessionId, final long timestamp, final int objectId) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.revert(webConv, hostname, sessionId, timestamp, objectId);
//    }
//
//    public static int saveAs(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int attached, final int module, final int attachment, final Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.saveAs(webConv, hostname, sessionId, folderId, attached, module, attachment, fields);
//    }
//
//    public static Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns, final int folderId, final int sort, final String order, final int start, final int end) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.search(webConv, hostname, sessionId, query, columns, folderId, sort, order, start, end);
//    }
//
//    public static Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.search(webConv, hostname, sessionId, query, columns);
//    }
//
//    public static Response unlock(final WebConversation webConv, final String hostname, final String sessionId, final int id) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.unlock(webConv, hostname, sessionId, id);
//    }
//
//    public static Response update(final WebConversation webConv, final String hostname, final String sessionId, final int id, final long timestamp, final Map<String, String> modified, final File upload, final String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.update(webConv, hostname, sessionId, id, timestamp, modified, upload, contentType);
//    }
//
//    public static Response update(final WebConversation webConv, final String hostname, final String sessionId, final int id, final long timestamp, final Map<String, String> modified) throws MalformedURLException, IOException, SAXException, JSONException {
//        return test.update(webConv, hostname, sessionId, id, timestamp, modified);
//    }
//
//    public static Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp, final int sort, final String order, final String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp, sort, order, ignore);
//    }
//
//    public static Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp, final String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp, ignore);
//    }
//
//    public static Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp);
//    }
//
//    public static Response versions(final WebConversation webConv, final String hostname, final String sessionId, final int objectId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.versions(webConv, hostname, sessionId, objectId, columns, sort, order);
//    }
//
//    public static Response versions(final WebConversation webConv, final String hostname, final String sessionId, final int objectId, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
//        return test.versions(webConv, hostname, sessionId, objectId, columns);
//    }
}
