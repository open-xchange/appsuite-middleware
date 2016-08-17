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

package com.openexchange.file.storage.json.actions.files;

import static com.openexchange.groupware.infostore.utils.UploadSizeValidation.checkSize;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.crypto.CryptographicAwareIDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.crypto.CryptographyMode;
import com.openexchange.file.storage.json.FileMetadataParser;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction.Param;
import com.openexchange.file.storage.json.osgi.FileFieldCollector;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.java.FileKnowingInputStream;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXInfostoreRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AJAXInfostoreRequest implements InfostoreRequest {

    private static final String PARAM_TIMEZONE = Param.TIMEZONE.getName();
    private static final String PARAM_TIMESTAMP = Param.TIMESTAMP.getName();
    private static final String PARAM_ORDER = Param.ORDER.getName();
    private static final String PARAM_SORT = Param.SORT.getName();
    private static final String PARAM_COLUMNS = Param.COLUMNS.getName();
    private static final String PARAM_VERSION = Param.VERSION.getName();
    private static final String PARAM_ID = Param.ID.getName();
    private static final String PARAM_FOLDER_ID = Param.FOLDER_ID.getName();

    private static final String JSON = "json";

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final ServerSession session;
    private List<Field> fieldsToLoad;
    private int[] requestedColumns;
    private byte[] contentData;
    private List<File.Field> fields;
    private File file;
    private IDBasedFileAccess fileAccess;
    private IDBasedFolderAccess folderAccess;
    private Map<String, String> folderMapping;
    private Map<String, Set<String>> versionMapping;
    private List<String> folders;
    private List<String> ids;
    private Field sortingField;
    private String[] versions;
    private boolean notifyPermissionEntities;
    private Transport notificationTransport;
    private String notificationMessage;
	protected AJAXRequestData data;

    public AJAXInfostoreRequest(final AJAXRequestData requestData, final ServerSession session) {
        super();
        this.data = requestData;
        this.session = session;
    }

    @Override
    public AJAXRequestData getRequestData() {
        return data;
    }

    @Override
    public boolean extendedResponse() throws OXException {
        return data.isSet("extendedResponse") && data.getParameter("extendedResponse", Boolean.class).booleanValue();
    }

    @Override
    public int getAttachedId() {
        return getInt(Param.ATTACHED_ID);
    }

    @Override
    public int getAttachment() {
        return getInt(Param.ATTACHMENT);
    }


    @Override
    public AttachmentBase getAttachmentBase() {
        return Services.getAttachmentBase();
    }

    /**
     * Gets the boolean value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The boolean value mapped to given parameter name or <code>false</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    @Override
    public boolean getBoolParameter(final String name) {
        return AJAXRequestDataTools.parseBoolParameter(name, data);
    }

    @Override
    public List<Field> getFieldsToLoad() throws OXException {
        if (null == fieldsToLoad) {
            parseColumns();
        }
        return fieldsToLoad;
    }

    @Override
    public int[] getRequestedColumns() throws OXException {
        if (null == requestedColumns) {
            parseColumns();
        }
        return requestedColumns;
    }

    @Override
    public long getDiff() {
        final String parameter = data.getParameter(Param.DIFF.getName());
        if (parameter == null) {
            return -1;
        }
        return Long.parseLong(parameter);
    }

    @Override
    public int getEnd() throws OXException {
        String parameter = data.getParameter("end");
        if (parameter == null) {
            parameter = data.getParameter("limit");
            if (parameter == null) {
                return FileStorageFileAccess.NOT_SET;
            }
            try {
                return Integer.parseInt(parameter) - 1;
            } catch (NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "limit", parameter);
            }
        }
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "end", parameter);
        }
    }

    @Override
    public File getFile() throws OXException {
        parseFile();
        return file;
    }

    @Override
    public IDBasedFileAccess getFileAccess() throws OXException {
        if (fileAccess != null) {
            return fileAccess;
        }

        String cryptoAction = data != null ? data.getParameter("cryptoAction") : null;
        if(cryptoAction != null && !cryptoAction.isEmpty()) {

            //Parsing authentication from the request. Might be null since not all crypto-actions require authentication.
            String authentication = null;
            CryptographicServiceAuthenticationFactory encryptionAuthenticationFactory = Services.getCryptographicServiceAuthenticationFactory();
            if(encryptionAuthenticationFactory != null) {
                if(data.optHttpServletRequest() != null) {
                    authentication = encryptionAuthenticationFactory.createAuthenticationFrom(data.optHttpServletRequest());
                }
            }

            //Creating file access with crypto functionalities
            CryptographicAwareIDBasedFileAccessFactory encryptionAwareFileAccessFactory = Services.getCryptographicFileAccessFactory();
            if (encryptionAwareFileAccessFactory != null) {
                EnumSet<CryptographyMode> cryptMode = CryptographyMode.createSet(cryptoAction);
                if (cryptMode.size() > 0) {
                    return fileAccess = encryptionAwareFileAccessFactory.createAccess(Services.getFileAccessFactory().createAccess(session), cryptMode, session, authentication);
                } else {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("cryptoAction",cryptoAction);
                }
            } else {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CryptographicAwareIDBasedFileAccessFactory.class.getSimpleName());
            }
        }

        return fileAccess = Services.getFileAccessFactory().createAccess(session);
    }

    @Override
    public IDBasedFileAccess optFileAccess() {
        return fileAccess;
    }

    @Override
    public IDBasedFolderAccess getFolderAccess() throws OXException {
        if (folderAccess != null) {
            return folderAccess;
        }
        return folderAccess = Services.getFolderAccessFactory().createAccess(session);
    }

    @Override
    public IDBasedFolderAccess optFolderAccess() {
        return folderAccess;
    }

    @Override
    public String getFolderAt(final int index) {
        return index < 0 || index >= folders.size() ? null : folders.get(index);
    }

    @Override
    public String getFolderForID(final String id) throws OXException {
        parseIDList(true);
        return folderMapping.get(id);
    }

    @Override
    public String getFolderId() throws OXException {
    	final String parameter = data.getParameter(PARAM_FOLDER_ID);
        if (parameter == null || parameter.equals("null") || parameter.equals("undefined")) {
            return FileStorageFileAccess.ALL_FOLDERS;
        }
        return parameter;
    }

    @Override
    public List<String> getFolders() {
        return folders;
    }

    @Override
    public String getId() {
        return data.getParameter(PARAM_ID);
    }

    @Override
    public List<String> getIds() throws OXException {
        parseIDList(true);
        return ids;
    }

    @Override
    public List<IdVersionPair> getIdVersionPairs() throws OXException {
        parseIDList(true);
        return generateIdVersionPairs();
    }

    @Override
    public List<IdVersionPair> optIdVersionPairs() throws OXException {
        return parseIDList(false) ? generateIdVersionPairs() : null;
    }

    /**
     * Generates the pairs of identifier and version from available request body data.
     *
     * @return The generated pairs of identifier and version
     */
    private List<IdVersionPair> generateIdVersionPairs() {
        int size = ids.size();
        List<IdVersionPair> retval = new ArrayList<IdVersionPair>(size);

        for (int i = size, pos = 0; i-- > 0; pos++) {
            String id = ids.get(pos);
            if (null == id) {
                retval.add(new IdVersionPair(null, null, folders.get(pos)));
            } else {
                Set<String> versions = versionMapping.get(id);
                if (null == versions) {
                    retval.add(new IdVersionPair(id, FileStorageFileAccess.CURRENT_VERSION, folderMapping.get(id)));
                } else {
                    for (String version : versions) {
                        retval.add(new IdVersionPair(id, version, folderMapping.get(id)));
                    }
                }
            }

        }

        return retval;
    }

    @Override
    public Set<String> getIgnore() {
        String parameter = data.getParameter(Param.IGNORE.getName());
        if (parameter == null) {
            return Collections.emptySet();
        }

        return new HashSet<String>(Arrays.asList(Strings.splitByComma(parameter)));
    }

    @Override
    public int getModule() {
        return getInt(Param.MODULE);
    }

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    @Override
    public String getParameter(final String name) {
        return data.getParameter(name);
    }

    @Override
    public String getSearchFolderId() throws OXException {
        return getFolderId();
    }

    @Override
    public String getSearchQuery() throws OXException {
        Object data2 = data.getData();
        if(data2 == null) {
            return "";
        }
        final JSONObject queryObject = (JSONObject) data2;

        try {
            return queryObject.getString("pattern");
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create( x.getMessage());
        }
    }

    @Override
    public List<Field> getSentColumns() throws OXException {
        parseFile();
        return fields;
    }

    @Override
    public ServerSession getSession() {
        return session;
    }

    @Override
    public Field getSortingField() throws OXException {
        if (sortingField != null) {
            return sortingField;
        }

        String sort = data.getParameter(PARAM_SORT);
        if (sort == null) {
            return null;
        }

        Field field = sortingField = Field.get(sort);
        if (field == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( PARAM_SORT, sort);
        }

        return field;
    }

    @Override
    public SortDirection getSortingOrder() throws OXException {
        SortDirection sortDirection = SortDirection.get(data.getParameter(PARAM_ORDER));
        if (sortDirection == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( PARAM_ORDER, sortDirection);
        }
        return sortDirection;
    }

    @Override
    public int getStart() throws OXException {
        String parameter = data.getParameter("start");
        if (parameter == null) {
            if (data.getParameter("limit") != null) {
                return 0;
            }
            return FileStorageFileAccess.NOT_SET;
        }
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "start", parameter);
        }
    }

    @Override
    public long getTimestamp() {
        final String parameter = data.getParameter(PARAM_TIMESTAMP);
        if (parameter == null) {
            return FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER;
        }

        return Long.parseLong(parameter);
    }

    @Override
    public TimeZone getTimezone() {
        String parameter = data.getParameter(PARAM_TIMEZONE);
        if (parameter == null) {
            parameter = getSession().getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(parameter);
    }

    @Override
    public InputStream getUploadedFileData() throws OXException {
        long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
        if (data.hasUploads(-1, maxSize > 0 ? maxSize : -1L)) {
            try {
                final UploadFile uploadFile = data.getFiles(-1, maxSize > 0 ? maxSize : -1L).get(0);
                checkSize(uploadFile.getSize());
                java.io.File tmpFile = uploadFile.getTmpFile();
                return new FileKnowingInputStream(new FileInputStream(tmpFile), tmpFile);
            } catch (final FileNotFoundException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        if (contentData != null) {
            return new UnsynchronizedByteArrayInputStream(contentData);
        }
        return null;
    }

    @Override
    public InputStream getUploadStream() throws OXException {
        try {
            return data.getUploadStream();
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        final String parameter = data.getParameter(PARAM_VERSION);
        if (parameter == null) {
            return FileStorageFileAccess.CURRENT_VERSION;
        }
        return "null".equalsIgnoreCase(parameter) ? FileStorageFileAccess.CURRENT_VERSION : parameter;
    }

    @Override
    public String[] getVersions() throws OXException {
        if (versions != null) {
            return versions;
        }
        final JSONArray body = getBodyAsJsonArray();

        try {
            versions = new String[body.length()];
            for (int i = 0; i < versions.length; i++) {
                versions[i] = body.getString(i);
            }
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create( x.getMessage());
        }
        return versions;
    }

    public boolean has(final String paramName) {
        return data.getParameter(paramName) != null;
    }

    @Override
    public boolean hasUploads() throws OXException {
        long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
        return data.hasUploads(-1, maxSize > 0 ? maxSize : -1L) || contentData != null;
    }

    @Override
    public boolean isForSpecificVersion() {
    	return getVersion() != FileStorageFileAccess.CURRENT_VERSION;
    }

    @Override
    public InfostoreRequest require(final Param... params) throws OXException {
        final String[] names = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            names[i] = params[i].getName();
        }
        final List<String> missingParameters = data.getMissingParameters(names);
        if (!missingParameters.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( missingParameters.toString());
        }
        return this;
    }

    @Override
    public InfostoreRequest requireBody() throws OXException {
        if (data.getData() == null) {
            long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
            if (!data.hasUploads(-1, maxSize > 0 ? maxSize : -1L) && data.getParameter("json") == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( "data");
            }
        }
        return this;
    }

    @Override
    public InfostoreRequest requireFileMetadata() throws OXException {
        return requireBody();
    }

    @Override
    public Document getCachedDocument() {
        return this.data.getProperty(DocumentAction.DOCUMENT);
    }

    @Override
    public boolean notifyPermissionEntities() throws OXException {
        parseNotification();
        return notifyPermissionEntities;
    }

    @Override
    public Transport getNotificationTransport() throws OXException {
        parseNotification();
        return notificationTransport;
    }

    @Override
    public String getNotifiactionMessage() throws OXException {
        parseNotification();
        return notificationMessage;
    }

    @Override
    public List<Integer> getEntities() throws OXException {
        JSONObject data = getBodyAsJSONObject();
        if (false == data.hasAndNotNull("entities")) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("entities");
        }
        try {
            JSONArray jsonArray = data.getJSONArray("entities");
            List<Integer> entityIDs = new ArrayList<Integer>(jsonArray.length());
            entityIDs = new ArrayList<Integer>();
            for (int i = 0; i < jsonArray.length(); i++) {
                entityIDs.add(Integer.valueOf(jsonArray.getInt(i)));
            }
            if (0 >= entityIDs.size()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("entities");
            }
            return entityIDs;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private int getInt(final Param param) {
        return Integer.parseInt(data.getParameter(param.getName()));
    }

    /**
     * Parses ID, folder and version information from request's JSON array
     *
     * @param required Whether the JSON array is required or not
     * @return <code>true</code> if ID, folder and version information are successfully parsed; otherwise <code>false</code> if absent
     * @throws OXException If parsing fails
     */
    private boolean parseIDList(boolean required) throws OXException {
        try {
            if (ids != null) {
                return true;
            }

            // Check request body
            JSONArray array;
            if (required) {
                // Require request body as a JSON array
                array = getBodyAsJsonArray();
            } else {
                // Optional request body as a JSON array
                array = optBodyAsJsonArray();
                if (null == array) {
                    return false;
                }
            }
            int length = array.length();

            // Initialize
            List<String> ids = new ArrayList<String>(length);
            List<String> folders = new ArrayList<String>(length);
            Map<String, String> folderMapping = new HashMap<String, String>(length);
            Map<String, Set<String>> versionMapping = new HashMap<String, Set<String>>(length);

            // Iterate JSON array
            for (int i = length, pos = 0; i-- > 0; pos++) {
                /*-
                 * A JSON identifier tuple; either
                 *
                 * - For a folder:  {"folder": <folder-id>}
                 * - For a file:    {"folder": <optional-folder-id>, "id": <document-id>, "version": <optional-version-number>}
                 */
                JSONObject tuple = array.getJSONObject(pos);

                // Check folder identifier
                String folderId = tuple.optString(PARAM_FOLDER_ID, null);
                folders.add(folderId);

                // Check file identifier
                String id = tuple.optString(PARAM_ID, null);
                if (null == id) {
                    ids.add(id);
                } else {
                    // Ensure folder identifier is "encoded" in the file identifier
                    FileID fileID = new FileID(id);
                    if (fileID.getFolderId() == null) {
                        fileID.setFolderId(folderId);
                        ids.add(fileID.toUniqueID());
                    } else {
                        ids.add(id);
                    }

                    // Add to id-to-folder mapping
                    folderMapping.put(id, folderId);

                    // Add to id-to-versions mapping
                    final String version = tuple.optString(PARAM_VERSION, FileStorageFileAccess.CURRENT_VERSION);
                    Set<String> list = versionMapping.get(id);
                    if (null == list) {
                        list = new LinkedHashSet<String>(2);
                        versionMapping.put(id, list);
                    }
                    list.add(version);
                }
            }

            // Assign to members
            this.ids = ids;
            this.folders = folders;
            this.folderMapping = folderMapping;
            this.versionMapping = versionMapping;
            return true;
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create( x.getMessage());
        }

    }

    private JSONArray getBodyAsJsonArray() throws OXException {
        Object obj = data.requireData();
        if (!(obj instanceof JSONArray)) {
            try {
                return new JSONArray(obj.toString());
            } catch (Exception e) {
                throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONArray.class.getSimpleName(), obj.getClass().getSimpleName());
            }
        }
        return (JSONArray) obj;
    }

    private JSONArray optBodyAsJsonArray() throws OXException {
        Object obj = data.getData();
        if (null == obj) {
            return null;
        }

        if (!(obj instanceof JSONArray)) {
            try {
                return new JSONArray(obj.toString());
            } catch (Exception e) {
                return null;
            }
        }
        return (JSONArray) obj;
    }

    protected void parseNotification() throws OXException {
        if (null == notificationTransport) {
            JSONObject object = getBodyAsJSONObject();
            JSONObject jNotification = object.optJSONObject("notification");
            if (jNotification != null) {
                Transport transport = Transport.MAIL;
                if (jNotification.hasAndNotNull("transport")) {
                    try {
                        transport = Transport.forID(jNotification.getString("transport"));
                        if (transport == null) {
                            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
                        }
                    } catch (JSONException e) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
                    }
                }
                notificationTransport = transport;
                notifyPermissionEntities = true;
                String message = jNotification.optString("message", null);
                if (Strings.isNotEmpty(message)) {
                    notificationMessage = message;
                }
            }
        }
    }

    private JSONObject getBodyAsJSONObject() throws OXException {
        JSONObject object = (JSONObject) data.getData();
        if (object == null) {
            try {
                object = new JSONObject(data.getParameter(JSON));
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
            }
        }
        return object;
    }

    protected void parseFile() throws OXException {
        if (file != null) {
            return;
        }
        requireFileMetadata();

        JSONObject jFile;
        JSONObject object = getBodyAsJSONObject();

        if (object.hasAndNotNull("file")) {
            try {
                jFile = object.getJSONObject("file");
            } catch (JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
            }
        } else {
            jFile = object;
        }

        UploadFile uploadFile = null;
        {
            long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
            if (data.hasUploads(-1, maxSize > 0 ? maxSize : -1L)) {
                uploadFile = data.getFiles(-1, maxSize > 0 ? maxSize : -1L).get(0);
            }
        }

        if (data.getUploadEvent() != null) {
            final List<UploadFile> list = data.getUploadEvent().getUploadFilesByFieldName("file");
            if (list != null && !list.isEmpty()) {
                uploadFile = list.get(0);
            }
        }


        FileMetadataParser parser = FileMetadataParser.getInstance();
        file = parser.parse(jFile);
        fields = parser.getFields(jFile);
        if (uploadFile != null) {
            if (!fields.contains(File.Field.FILENAME) || file.getFileName() == null || file.getFileName().trim().length() == 0) {
                file.setFileName(uploadFile.getPreparedFileName());
                fields.add(File.Field.FILENAME);
            }

            if (!fields.contains(File.Field.FILE_MIMETYPE)) {
                file.setFileMIMEType(uploadFile.getContentType());
                fields.add(File.Field.FILE_MIMETYPE);
            }

            file.setFileSize(uploadFile.getSize());
            fields.add(File.Field.FILE_SIZE);
            // TODO: Guess Content-Type
        }

        final String fileDisplay = data.getParameter("filedisplay");
        if (fileDisplay != null && fileDisplay.trim().length() > 0 && (file.getFileName() == null || file.getFileName().trim().length() == 0)) {
            file.setFileName(fileDisplay);
            fields.add(File.Field.FILENAME);
        }

        if (has("id") && !fields.contains(File.Field.ID)) {
            file.setId(getId());
            fields.add(File.Field.ID);
        }

        if (jFile.has("content")) {
            try {
                contentData = jFile.opt("content").toString().getBytes("UTF-8");

                file.setFileSize(contentData.length);
                fields.add(File.Field.FILE_SIZE);
            } catch (UnsupportedEncodingException e) {
                // IGNORE;
            }
        }
    }

    /**
     * Parses the requests <code>columns</code> parameter and sets the {@link #fieldsToLoad} and {@link #requestedColumns} members for
     * this infostore request.
     */
    private void parseColumns() throws OXException {
        String columnsParameter = data.getParameter(PARAM_COLUMNS);
        if (Strings.isEmpty(columnsParameter)) {
            /*
             * use all known file fields
             */
            fieldsToLoad = Arrays.asList(Field.values());
            requestedColumns = new int[fieldsToLoad.size()];
            for (int i = 0; i < requestedColumns.length; i++) {
                requestedColumns[i] = fieldsToLoad.get(i).getNumber();
            }
        } else {
            /*
             * use requested file fields only
             */
            List<String> unknownColumns = new ArrayList<String>(0);
            String[] columns = Strings.splitByComma(columnsParameter);
            fieldsToLoad = new ArrayList<Field>(columns.length);
            requestedColumns = new int[columns.length];
            for (int i = 0; i < columns.length; i++) {
                /*
                 * try regular file field first
                 */
                Field field = Field.get(columns[i]);
                if (null != field) {
                    fieldsToLoad.add(field);
                    requestedColumns[i] = field.getNumber();
                    continue;
                }

                /*
                 * check additionally registered file fields
                 */
                FileFieldCollector fieldCollector = Services.getFieldCollector();
                if (null != fieldCollector) {
                    AdditionalFileField additionalField = fieldCollector.getField(columns[i]);
                    if (null != additionalField) {
                        Field[] requiredFields = additionalField.getRequiredFields();
                        if (null != requiredFields && 0 < requiredFields.length) {
                            fieldsToLoad.addAll(Arrays.asList(requiredFields));
                        }
                        requestedColumns[i] = additionalField.getColumnID();
                        continue;
                    }
                }
                /*
                 * unknown column
                 */
                unknownColumns.add(columns[i]);
            }
            if (0 < unknownColumns.size()) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAM_COLUMNS, unknownColumns.toString());
            }
        }
    }

}
