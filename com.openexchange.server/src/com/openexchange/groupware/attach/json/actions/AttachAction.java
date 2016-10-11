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

package com.openexchange.groupware.attach.json.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentUtility;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AttachAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AttachAction extends AbstractAttachmentAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachAction.class);

    private static final String DATASOURCE = "datasource";

    private static final String IDENTIFIER = "identifier";

    /**
     * Initializes a new {@link AttachAction}.
     */
    public AttachAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    public static transient final AttachmentField[] REQUIRED = Attachment.REQUIRED;

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            User user = session.getUser();
            UserConfiguration userConfiguration = session.getUserConfiguration();
            long maxUploadSize = AttachmentConfig.getMaxUploadSize();
            if (requestData.hasUploads(-1, maxUploadSize > 0 ? maxUploadSize : -1L)) {
                final UploadEvent upload = requestData.getUploadEvent();
                final List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
                final List<UploadFile> uploadFiles = new ArrayList<UploadFile>();

                long sum = 0;
                final JSONObject json = new JSONObject();
                final List<UploadFile> l = upload.getUploadFiles();
                final int size = l.size();
                final Iterator<UploadFile> iter = l.iterator();
                for (int a = 0; a < size; a++) {
                    final UploadFile uploadFile = iter.next();
                    final String fileField = uploadFile.getFieldName();
                    final int index = Integer.parseInt(fileField.substring(5));
                    final String obj = upload.getFormField("json_" + index);
                    if (obj == null || obj.length() == 0) {
                        continue;
                    }
                    json.reset();
                    json.parseJSONString(obj);
                    for (final AttachmentField required : REQUIRED) {
                        if (!json.has(required.getName())) {
                            throw AjaxExceptionCodes.MISSING_PARAMETER.create(required.getName());
                        }
                    }

                    final AttachmentMetadata attachment = PARSER.getAttachmentMetadata(json);
                    assureSize(index, attachments, uploadFiles);

                    attachments.set(index, attachment);
                    uploadFiles.set(index, uploadFile);
                    sum += uploadFile.getSize();

                    AttachmentUtility.checkSize(sum, requestData);
                }

                return attach(attachments, uploadFiles, session, session.getContext(), user, userConfiguration);
            }
            final JSONObject object = (JSONObject) requestData.getData();
            if (object == null) {
            	return new AJAXRequestResult(Integer.valueOf(0), new Date(System.currentTimeMillis()), "int");
            }
            for (final AttachmentField required : Attachment.REQUIRED) {
                if (!object.has(required.getName())) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create(required.getName());
                }
            }
            if (!object.has(DATASOURCE)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(DATASOURCE);
            }

            final AttachmentMetadata attachment = PARSER.getAttachmentMetadata(object);
            final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);

            if (conversionService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConversionService.class.getName());
            }

            final JSONObject datasourceDef = object.getJSONObject(DATASOURCE);
            final String datasourceIdentifier = datasourceDef.getString(IDENTIFIER);

            final DataSource source = conversionService.getDataSource(datasourceIdentifier);
            if (source == null) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("datasource", datasourceIdentifier);
            }

            final List<Class<?>> types = Arrays.asList(source.getTypes());

            final Map<String, String> arguments = new HashMap<String, String>();

            for (final String key : datasourceDef.keySet()) {
                arguments.put(key, datasourceDef.getString(key));
            }

            InputStream is;
            if (types.contains(InputStream.class)) {
                final Data<InputStream> data = source.getData(InputStream.class, new DataArguments(arguments), session);
                final String sizeS = data.getDataProperties().get(DataProperties.PROPERTY_SIZE);
                final String contentTypeS = data.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);

                if (sizeS != null) {
                    attachment.setFilesize(Long.parseLong(sizeS));
                }

                if (contentTypeS != null) {
                    attachment.setFileMIMEType(contentTypeS);
                }

                final String name = data.getDataProperties().get(DataProperties.PROPERTY_NAME);
                if (name != null && null == attachment.getFilename()) {
                    attachment.setFilename(name);
                }

                is = data.getData();

            } else if (types.contains(byte[].class)) {
                final Data<byte[]> data = source.getData(byte[].class, new DataArguments(arguments), session);
                final byte[] bytes = data.getData();
                is = new ByteArrayInputStream(bytes);
                attachment.setFilesize(bytes.length);

                final String contentTypeS = data.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
                if (contentTypeS != null) {
                    attachment.setFileMIMEType(contentTypeS);
                }

                final String name = data.getDataProperties().get(DataProperties.PROPERTY_NAME);
                if (name != null && null == attachment.getFilename()) {
                    attachment.setFilename(name);
                }

            } else {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("datasource", datasourceIdentifier);
            }

            if (attachment.getFilename() == null) {
                attachment.setFilename("unknown" + System.currentTimeMillis());
            }

            attachment.setId(AttachmentBase.NEW);

            ATTACHMENT_BASE.startTransaction();
            long ts;
            try {
                ts =
                    ATTACHMENT_BASE.attachToObject(
                        attachment,
                        is,
                        session,
                        session.getContext(),
                        user,
                        userConfiguration);
                ATTACHMENT_BASE.commit();
            } catch (final OXException x) {
                ATTACHMENT_BASE.rollback();
                throw x;
            } finally {
                ATTACHMENT_BASE.finish();
            }

            return new AJAXRequestResult(Integer.valueOf(attachment.getId()), new Date(ts), "int");
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult attach(final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles, final ServerSession session, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        initAttachments(attachments, uploadFiles);
        boolean rollback = false;
        try {
            ATTACHMENT_BASE.startTransaction();
            rollback = true;

            final Iterator<UploadFile> ufIter = uploadFiles.iterator();
            final JSONArray arr = new JSONArray();
            long timestamp = 0;

            for (final AttachmentMetadata attachment : attachments) {
                final UploadFile uploadFile = ufIter.next();

                attachment.setId(AttachmentBase.NEW);

                BufferedInputStream data = new BufferedInputStream(new FileInputStream(uploadFile.getTmpFile()), 65536);
                final long modified = ATTACHMENT_BASE.attachToObject(attachment, data, session, ctx, user, userConfig);
                if (modified > timestamp) {
                    timestamp = modified;
                }
                arr.put(attachment.getId());

            }

            ATTACHMENT_BASE.commit();
            rollback = false;
            return new AJAXRequestResult(arr, new Date(timestamp), "json");
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                AttachmentUtility.rollback();
            }
            AttachmentUtility.finish();
        }
    }

    private void initAttachments(final List<AttachmentMetadata> attachments, final List<UploadFile> uploads) {
        final List<AttachmentMetadata> attList = new ArrayList<AttachmentMetadata>(attachments);
        // final Iterator<AttachmentMetadata> attIter = new ArrayList<AttachmentMetadata>(attachments).iterator();
        final Iterator<UploadFile> ufIter = new ArrayList<UploadFile>(uploads).iterator();

        int index = 0;
        for (final AttachmentMetadata attachment : attList) {
            // while(attIter.hasNext()) {
            // final AttachmentMetadata attachment = attIter.next();
            if (attachment == null) {
                attachments.remove(index);
                ufIter.next();
                uploads.remove(index);
                continue;
            }
            final UploadFile upload = ufIter.next();
            if (upload == null) {
                attachments.remove(index);
                uploads.remove(index);
                continue;
            }
            if (attachment.getFilename() == null || "".equals(attachment.getFilename())) {
                attachment.setFilename(upload.getPreparedFileName());
            }
            if (attachment.getFilesize() <= 0) {
                attachment.setFilesize(upload.getSize());
            }
            if (attachment.getFileMIMEType() == null || "".equals(attachment.getFileMIMEType())) {
                attachment.setFileMIMEType(upload.getContentType());
            }
            index++;
        }
    }

    private void assureSize(final int index, final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles) {
        int enlarge = index - (attachments.size() - 1);
        for (int i = 0; i < enlarge; i++) {
            attachments.add(null);
        }

        enlarge = index - (uploadFiles.size() - 1);
        for (int i = 0; i < enlarge; i++) {
            uploadFiles.add(null);
        }

    }

}
