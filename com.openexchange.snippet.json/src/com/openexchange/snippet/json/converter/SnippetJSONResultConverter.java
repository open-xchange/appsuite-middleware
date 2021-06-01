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

package com.openexchange.snippet.json.converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.utils.SnippetImageDataSource;
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
                result.setResultObject(convertSnippet(snippet, requestData.getSession()), "json");
                return;
            }
            /*
             * Collection of snippets
             */
            @SuppressWarnings("unchecked") final Collection<Snippet> snippets = (Collection<Snippet>) resultObject;
            final JSONArray jArray = new JSONArray();
            if (snippets != null) {
                for (final Snippet snippet : snippets) {
                    jArray.put(convertSnippet(snippet, requestData.getSession()));
                }
            }
            result.setResultObject(jArray, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertSnippet(Snippet snippet, Session session) throws JSONException, OXException {
        final JSONObject json = new JSONObject();
        if (snippet.getError().isPresent()) {
            json.put("error", snippet.getError().get());
        }
        int itg = snippet.getAccountId();
        if (itg >= 0) {
            json.put(Property.ACCOUNT_ID.getPropName(), itg);
        }
        String snippetId = snippet.getId();
        if (null != snippetId) {
            json.put(Property.ID.getPropName(), snippetId);
        }

        String tmp = snippet.getContent();
        if (null != tmp) {
            tmp = processContent(tmp, snippetId, session);
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
        final Object misc = snippet.getMisc();
        if (null != misc) {
            if (misc instanceof JSONValue) {
                json.put(Property.MISC.getPropName(), misc);
            } else {
                final String sMisc = misc.toString();
                json.put(Property.MISC.getPropName(), "null".equals(sMisc) ? JSONObject.NULL : new JSONTokener(sMisc).nextValue());
            }
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
                tmp = attachment.getContentId();
                if (null != tmp) {
                    jsonAttachment.put("contentid", tmp);
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
        } catch (Exception e) {
            return null;
        }
    }

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern CID_PATTERN = Pattern.compile("(?:src=cid:([^\\s>]*))|(?:src=\"cid:([^\"]*)\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static String processContent(String content, String snippetId, Session session) throws OXException {
        if (null == snippetId) {
            return content;
        }

        Matcher imgMatcher = IMG_PATTERN.matcher(content);
        if (false == imgMatcher.find()) {
            // No <img> tag in content
            return content;
        }

        // Replace inline images with Content-ID
        StringBuffer sb = new StringBuffer(content.length());
        StringBuilder linkBuilder = new StringBuilder(256);
        do {
            String imgTag = imgMatcher.group();

            Matcher cidMatcher = CID_PATTERN.matcher(imgTag);
            if (cidMatcher.find()) {
                StringBuffer cidBuffer = new StringBuffer(imgTag.length());
                do {
                    // Extract Content-ID
                    String cid = cidMatcher.group(2);
                    if (cid == null) {
                        cid = cidMatcher.group(1);
                    }
                    linkBuilder.setLength(0);

                    // Build image location
                    ImageLocation imageLocation = new ImageLocation.Builder(cid).id(snippetId).optImageHost(HtmlProcessing.imageHost()).build();
                    SnippetImageDataSource imgSource = SnippetImageDataSource.getInstance();
                    String imageURL = imgSource.generateUrl(imageLocation, session);
                    linkBuilder.append("src=").append('"').append(imageURL).append('"').append(" id=\"").append(cid).append("\" ").append("onmousedown=\"return false;\" oncontextmenu=\"return false;\"");

                    cidMatcher.appendReplacement(cidBuffer, Matcher.quoteReplacement( 0 == linkBuilder.length() ? cidMatcher.group() : linkBuilder.toString()));
                } while (cidMatcher.find());
                cidMatcher.appendTail(cidBuffer);
                imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(cidBuffer.toString()));
            } else {
                // Append as-is
                imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(imgTag));
            }
        } while (imgMatcher.find());
        imgMatcher.appendTail(sb);
        return sb.toString();
    }

}
