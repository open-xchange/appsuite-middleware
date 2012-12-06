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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.snippet.json.converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SnippetJSONResultConverter} - The result converter for snippet module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SnippetJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link JSONResultConverter}.
     */
    public SnippetJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "snippet";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        try {
            final Object resultObject = result.getResultObject();
            if (resultObject instanceof Snippet) {
                final Snippet snippet = (Snippet) resultObject;
                result.setResultObject(convertSnippet(snippet), "json");
                return;
            }
            /*
             * Collection of snippets
             */
            @SuppressWarnings("unchecked") final Collection<Snippet> snippets = (Collection<Snippet>) resultObject;
            final JSONArray jArray = new JSONArray();
            for (final Snippet snippet : snippets) {
                jArray.put(convertSnippet(snippet));
            }
            result.setResultObject(jArray, "json");
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertSnippet(final Snippet snippet) throws JSONException {
        final JSONObject json = new JSONObject();
        int itg = snippet.getAccountId();
        if (itg >= 0) {
            json.put(Property.ACCOUNT_ID.getPropName(), itg);
        }
        String tmp = snippet.getContent();
        if (null != tmp) {
            json.put("content", tmp);
        }
        itg = snippet.getCreatedBy();
        if (itg >= 0) {
            json.put(Property.CREATED_BY.getPropName(), itg);
        }
        tmp = snippet.getDisplayName();
        if (null != tmp) {
            json.put(Property.DISPLAY_NAME.getPropName(), tmp);
        }
        tmp = snippet.getId();
        if (null != tmp) {
            json.put(Property.ID.getPropName(), tmp);
        }
        final Object misc = snippet.getMisc();
        if (null != misc) {
            json.put(Property.MISC.getPropName(), misc);
        }
        tmp = snippet.getModule();
        if (null != tmp) {
            json.put(Property.MODULE.getPropName(), tmp);
        }
        tmp = snippet.getType();
        if (null != tmp) {
            json.put(Property.TYPE.getPropName(), tmp);
        }
        json.put(Property.SHARED.getPropName(), snippet.isShared());
        final Map<String, Object> unnamedProperties = snippet.getUnnamedProperties();
        if (null != unnamedProperties && !unnamedProperties.isEmpty()) {
            json.put("props", new JSONObject(unnamedProperties));
        }
        final List<Attachment> attachments = snippet.getAttachments();
        if (null != attachments && !attachments.isEmpty()) {
            final JSONArray jArray = new JSONArray();
            for (final Attachment attachment : attachments) {
                final JSONObject jsonAttachment = new JSONObject();
                tmp = extractFilename(attachment);
                if (null != tmp) {
                    jsonAttachment.put("filename", tmp);
                }
                tmp = attachment.getContentType();
                if (null != tmp) {
                    jsonAttachment.put("mimetype", tmp);
                }
                tmp = attachment.getId();
                if (null != tmp) {
                    jsonAttachment.put("id", tmp);
                }
                final long size = attachment.getSize();
                if (size > 0) {
                    jsonAttachment.put("size", size);
                }
                jArray.put(jsonAttachment);
            }
            json.put("files", jArray);
        }
        return json;
    }

    private static String extractFilename(final Attachment attachment) {
        if (null == attachment) {
            return null;
        }
        try {
            final String sContentDisposition = attachment.getContentDisposition();
            String fn = null == sContentDisposition ? null : new ContentDisposition(sContentDisposition).getFilenameParameter();
            if (fn == null) {
                final String sContentType = attachment.getContentType();
                fn = null == sContentType ? null : new ContentType(sContentType).getNameParameter();
            }
            return fn;
        } catch (final Exception e) {
            return null;
        }
    }

}
