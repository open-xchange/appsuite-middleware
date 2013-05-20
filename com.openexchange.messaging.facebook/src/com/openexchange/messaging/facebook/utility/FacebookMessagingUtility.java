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

package com.openexchange.messaging.facebook.utility;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.scribe.model.Response;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.openexchange.exception.OXException;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.facebook.FacebookConstants;
import com.openexchange.messaging.facebook.FacebookMessagingExceptionCodes;
import com.openexchange.messaging.facebook.FacebookMessagingMessageAccess;
import com.openexchange.messaging.facebook.session.FacebookOAuthAccess;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeStringMessagingHeader;
import com.openexchange.oauth.OAuthExceptionCodes;

/**
 * {@link FacebookMessagingUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FacebookMessagingUtility {

    /**
     * Initializes a new {@link FacebookMessagingUtility}.
     */
    private FacebookMessagingUtility() {
        super();
    }

    /**
     * Gets pretty-printed string representation of specified node.
     *
     * @param node The node
     * @return The pretty-printed string representation of specified node
     */
    public static String toString(Node node) {
        try {
            final AllocatingStringWriter writer = new AllocatingStringWriter(1024);
            printDocument(node, writer);
            return writer.toString();
        } catch (final TransformerException e) {
            return e.getMessage();
        }
    }

    /**
     * Pretty prints specified node.
     *
     * @param node The node
     * @param writer The writer
     * @throws TransformerException If a transformation error occurs
     */
    public static void printDocument(Node node, Writer writer) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(node), new StreamResult(writer));
    }

    public interface StaticFiller {

        void fill(FacebookMessagingMessage message) throws OXException;
    }

    private static final class AccountNameFiller implements StaticFiller {

        private static final String NAME = MessagingHeader.KnownHeader.ACCOUNT_NAME.toString();

        private final String accountName;

        public AccountNameFiller(final String accountName) {
            super();
            this.accountName = accountName;
        }

        @Override
        public void fill(final FacebookMessagingMessage message) throws OXException {
            message.addHeader(new MimeStringMessagingHeader(NAME, accountName));
        }

    }

    private static final class ToFiller implements StaticFiller {

        private static final String TO = MessagingHeader.KnownHeader.TO.toString();

        private final String userId;

        private final String userName;

        public ToFiller(final String userId, final String userName) {
            super();
            this.userId = userId;
            this.userName = userName;
        }

        @Override
        public void fill(final FacebookMessagingMessage message) throws OXException {
            /*
             * Add "To"
             */
            message.setHeader(MimeAddressMessagingHeader.valueOfPlain(TO, userName, userId));
        }
    }

    /**
     * {@link FolderFiller} - The folder filler
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since Open-Xchange v6.16
     */
    public static final class FolderFiller implements StaticFiller {

        private final String folder;

        public FolderFiller(final String folder) {
            this.folder = folder;
        }

        @Override
        public void fill(final FacebookMessagingMessage message) throws OXException {
            message.setFolder(folder);
        }

    }

    private interface QueryAdder {

        void add2Query(Set<String> fieldNames);

        String getOrderBy();

    }

    private static final Map<MessagingField, QueryAdder> ADDERS_STREAM;

    private static final Map<MessagingField, QueryAdder> ADDERS_USER;

    private static final Map<MessagingField, QueryAdder> ADDERS_GROUP;

    private static final Map<MessagingField, QueryAdder> ADDERS_PAGE;

    static {
        {
            EnumMap<MessagingField, QueryAdder> m = new EnumMap<MessagingField, QueryAdder>(MessagingField.class);

            m.put(MessagingField.BODY, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("message");
                    fieldNames.add("attachment");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.CONTENT_TYPE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    /*
                     * We need attachment info to determine Content-Type
                     */
                    fieldNames.add("attachment");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.FROM, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("actor_id");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.FULL, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("post_id");
                    fieldNames.add("actor_id");
                    fieldNames.add("message");
                    fieldNames.add("updated_time");
                    fieldNames.add("created_time");
                    fieldNames.add("filter_key");
                    fieldNames.add("attachment");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.HEADERS, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("post_id");
                    fieldNames.add("actor_id");
                    fieldNames.add("updated_time");
                    fieldNames.add("created_time");
                    fieldNames.add("filter_key");
                    /*
                     * We need attachment info to determine Content-Type
                     */
                    fieldNames.add("attachment");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.ID, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("post_id");
                }

                @Override
                public String getOrderBy() {
                    return "post_id";
                }
            });

            m.put(MessagingField.RECEIVED_DATE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("updated_time");
                    fieldNames.add("created_time");
                }

                @Override
                public String getOrderBy() {
                    return "created_time";
                }
            });

            m.put(MessagingField.SENT_DATE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("updated_time");
                    fieldNames.add("created_time");
                }

                @Override
                public String getOrderBy() {
                    return "created_time";
                }
            });

            m.put(MessagingField.SUBJECT, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("message");
                }

                @Override
                public String getOrderBy() {
                    return "message";
                }
            });

            m.put(MessagingField.SIZE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("message");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            ADDERS_STREAM = Collections.unmodifiableMap(m);

            /*
             * ----------------------------------------------
             */

            m = new EnumMap<MessagingField, QueryAdder>(MessagingField.class);

            m.put(MessagingField.FROM, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("uid");
                    fieldNames.add("name");
                }

                @Override
                public String getOrderBy() {
                    return "name";
                }
            });

            m.put(MessagingField.PICTURE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.FULL, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("uid");
                    fieldNames.add("name");
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            ADDERS_USER = Collections.unmodifiableMap(m);

            /*
             * ----------------------------------------------
             */

            m = new EnumMap<MessagingField, QueryAdder>(MessagingField.class);

            m.put(MessagingField.FROM, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("gid");
                    fieldNames.add("name");
                }

                @Override
                public String getOrderBy() {
                    return "name";
                }
            });

            m.put(MessagingField.PICTURE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.FULL, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("gid");
                    fieldNames.add("name");
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            ADDERS_GROUP = Collections.unmodifiableMap(m);

            m = new EnumMap<MessagingField, QueryAdder>(MessagingField.class);

            m.put(MessagingField.FROM, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("page_id");
                    fieldNames.add("name");
                }

                @Override
                public String getOrderBy() {
                    return "name";
                }
            });

            m.put(MessagingField.PICTURE, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            m.put(MessagingField.FULL, new QueryAdder() {

                @Override
                public void add2Query(final Set<String> fieldNames) {
                    fieldNames.add("page_id");
                    fieldNames.add("name");
                    fieldNames.add("pic_small");
                }

                @Override
                public String getOrderBy() {
                    return null;
                }
            });

            ADDERS_PAGE = Collections.unmodifiableMap(m);
        }
    }

    /**
     * Gets the stream query-able fields.
     *
     * @return The stream query-able fields
     */
    public static EnumSet<MessagingField> getStreamQueryableFields() {
        return EnumSet.copyOf(ADDERS_STREAM.keySet());
    }

    /**
     * Gets the user/group query-able fields.
     *
     * @return The user/group query-able fields
     */
    public static EnumSet<MessagingField> getEntityQueryableFields() {
        final EnumSet<MessagingField> ret = EnumSet.copyOf(ADDERS_USER.keySet());
        ret.addAll(ADDERS_GROUP.keySet());
        return ret;
    }

    /**
     * Gets the static fillers for given fields.
     *
     * @param fields The fields
     * @param access The messaging access
     * @return The fillers
     */
    public static List<StaticFiller> getStreamStaticFillers(final Collection<MessagingField> fields, final FacebookMessagingMessageAccess access) {
        return getStreamStaticFillers(EnumSet.copyOf(fields), access);
    }

    /**
     * Gets the static fillers for given fields.
     *
     * @param fields The fields
     * @param access The messaging access
     * @return The fillers
     */
    public static List<StaticFiller> getStreamStaticFillers(final EnumSet<MessagingField> fieldSet, final FacebookMessagingMessageAccess access) {
        final List<StaticFiller> ret = new ArrayList<StaticFiller>(fieldSet.size());
        if (fieldSet.contains(MessagingField.ACCOUNT_NAME) || fieldSet.contains(MessagingField.FULL)) {
            ret.add(new AccountNameFiller(access.getMessagingAccount().getDisplayName()));
        }
        if (fieldSet.contains(MessagingField.TO) || fieldSet.contains(MessagingField.FULL)) {
            ret.add(new ToFiller(access.getFacebookUserId(), access.getFacebookUserName()));
        }
        return ret;
    }

    /**
     * Checks specified message's content to be of given type.
     *
     * @param message The message
     * @return The typed content
     * @throws OXException If message's content is of given type
     */
    public static <C extends MessagingContent> C checkContent(final Class<C> clazz, final MessagingMessage message) throws OXException {
        final MessagingContent content = message.getContent();
        if (!(clazz.isInstance(content))) {
            throw MessagingExceptionCodes.UNKNOWN_MESSAGING_CONTENT.create(content.toString());
        }
        return clazz.cast(content);
    }

    private static final long DEFAULT = -1L;

    private static final int RADIX = 10;

    /**
     * Parses as an unsigned <code>long</code>.
     *
     * @param s The string to parse
     * @return An unsigned <code>long</code> or <code>-1</code>.
     */
    public static long parseUnsignedLong(final String s) {
        if (s == null) {
            return DEFAULT;
        }
        final int max = s.length();
        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        long result = 0;
        int i = 0;

        final long limit = -Long.MAX_VALUE;
        final long multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return DEFAULT;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return DEFAULT;
            }
            if (result < multmin) {
                return DEFAULT;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return DEFAULT;
            }
            result -= digit;
        }
        return -result;
    }

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.UTF_8);

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String encodeUrl(final String s) {
        try {
            return isEmpty(s) ? s : URL_CODEC.encode(s);
        } catch (final EncoderException e) {
            return s;
        }
    }

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String decodeUrl(final String s, final String charset) {
        try {
            return isEmpty(s) ? s : (isEmpty(charset) ? URL_CODEC.decode(s) : URL_CODEC.decode(s, charset));
        } catch (final DecoderException e) {
            return s;
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static boolean startsWith(final char startingChar, final String toCheck, final boolean ignoreHeadingWhitespaces) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        if (!ignoreHeadingWhitespaces) {
            return startingChar == toCheck.charAt(0);
        }
        int i = 0;
        if (Strings.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Strings.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    /**
     * Extracts JSON from given response.
     *
     * @param response The response
     * @return The extracted JSON
     * @throws OXException If operation fails
     */
    public static JSONObject extractJson(final Response response) throws OXException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getStream(), Charsets.UTF_8);
            final JSONValue value = JSONObject.parse(reader);
            if (value.isObject()) {
                return value.toObject();
            }
            throw OAuthExceptionCodes.JSON_ERROR.create("Not a JSON object, but " + value.getClass().getName());
        } catch (final JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * Performs specified FQL query and returns its result as a JSON object.
     *
     * @param fqlQuery The FQL query
     * @return The queried JSON object
     * @throws OXException If FQL query fails
     */
    public static List<JSONObject> performFQLQuery(final String fqlQuery, final FacebookOAuthAccess facebookOAuthAccess) throws OXException {
        return fireFQLJsonQuery(fqlQuery, facebookOAuthAccess);
    }

    private static final String FQL_XML_START = "https://api.facebook.com/method/fql.query?format=XML&query=";
    private static final int FQL_XML_START_LEN = FQL_XML_START.length();

    /**
     * Fires given FQL query using specified Facebook REST client.
     *
     * @param fqlQuery The FQL query to fire
     * @param facebookOAuthAccess The Facebook OAuth access
     * @return The FQL query's results
     * @throws OXException If query cannot be fired
     * @deprecated Use {@link #fireFQLJsonQuery(CharSequence, FacebookOAuthAccess)} instead
     */
    @Deprecated
    public static List<Element> fireFQLQuery(final CharSequence fqlQuery, final FacebookOAuthAccess facebookOAuthAccess) throws OXException {
        try {
            final String encodedQuery = encodeUrl(fqlQuery.toString());
            return FacebookDOMParser.parseXMLResponse(facebookOAuthAccess.executeGETRequest(new StringBuilder(
                FQL_XML_START_LEN + encodedQuery.length()).append(FQL_XML_START).append(encodedQuery).toString()));
        } catch (final RuntimeException e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final String FQL_JSON_START = "https://api.facebook.com/method/fql.query?format=JSON&query=";
    private static final int FQL_JSON_START_LEN = FQL_JSON_START.length();

    /**
     * Fires given FQL query using specified Facebook REST client.
     *
     * @param fqlQuery The FQL query to fire
     * @param facebookOAuthAccess The Facebook OAuth access
     * @return The FQL query's results
     * @throws OXException If query cannot be fired
     */
    public static List<JSONObject> fireFQLJsonQuery(final CharSequence fqlQuery, final FacebookOAuthAccess facebookOAuthAccess) throws OXException {
        try {
            final String encodedQuery = encodeUrl(fqlQuery.toString());
            final JSONValue body = facebookOAuthAccess.executeGETJsonRequest(new StringBuilder(
                FQL_JSON_START_LEN + encodedQuery.length()).append(FQL_JSON_START).append(encodedQuery));
            if (body.isArray()) {
                final JSONArray array = body.toArray();
                final int length = array.length();
                if (length <= 0) {
                    return Collections.emptyList();
                }
                final List<JSONObject> ret = new ArrayList<JSONObject>(length);
                for (int i = 0; i < length; i++) {
                    ret.add(array.getJSONObject(i));
                }
                return ret;
            }
            if (body.isObject()) {
                /*
                 * Expect the body to be a JSON object
                 */
                final JSONObject result = body.toObject();
                if (result.has("error")) {
                    final JSONObject error = result.getJSONObject("error");
                    final String type = error.optString("type");
                    final String message = error.optString("message");
                    if ("OAuthException".equals(type)) {
                        throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == message ? "" : message);
                    }
                    throw FacebookMessagingExceptionCodes.FQL_ERROR.create(
                        null == type ? "<unknown>" : type,
                        null == message ? "" : message);
                }
                return Collections.singletonList(result);
            }
            throw FacebookMessagingExceptionCodes.INVALID_RESPONSE_BODY.create(body);
        } catch (final JSONException e) {
            throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * A FQL query.
     */
    public static final class FQLQuery {

        private final CharSequence charSequence;

        private final boolean orderBy;

        /**
         * Initializes a new {@link FQLQuery}.
         *
         * @param query The query character sequence
         * @param orderBy <code>true</code> if query contains <code>ORDER BY</code> clause; otherwise <code>false</code>
         */
        public FQLQuery(final CharSequence charSequence, final boolean orderBy) {
            super();
            this.charSequence = charSequence;
            this.orderBy = orderBy;
        }

        /**
         * Gets the query character sequence
         *
         * @return The query character sequence
         */
        public CharSequence getCharSequence() {
            return charSequence;
        }

        /**
         * Checks if query contains <code>ORDER BY</code> clause
         *
         * @return <code>true</code> if query contains <code>ORDER BY</code> clause; otherwise <code>false</code>
         */
        public boolean containsOrderBy() {
            return orderBy;
        }

        @Override
        public String toString() {
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(256).append("( charSequence=");
            if (null == charSequence) {
                sb.append("<not-available>");
            } else {
                sb.append('"').append(charSequence).append('"');
            }
            sb.append(", orderBy=").append(orderBy).append(" )");
            return sb.toString();
        }

    }

    /**
     * The Facebook query type.
     */
    public static enum FQLQueryType {

        /**
         * Retrieve a user's News Feed.
         */
        NEWS_FEED("news_feed"),
        /**
         * Retrieve a user's wall posts (stories on their profile).
         */
        WALL("wall");

        private final String name;

        private FQLQueryType(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * Gets the Facebook query type for specified folder identifier.
         *
         * @param folderId The folder identifier
         * @return The query type or <code>null</code> if no query is associated with specified folder identifier
         */
        public static FQLQueryType queryTypeFor(final String folderId) {
            if (MessagingFolder.ROOT_FULLNAME.equals(folderId)) {
                return NEWS_FEED;
            }
            if (FacebookConstants.FOLDER_WALL.equals(folderId)) {
                return WALL;
            }
            return null;
        }
    }

    /**
     * Composes the FQL stream query for given post identifier.
     *
     * @param fields The fields
     * @param postId The post identifier
     * @param facebookUserId The Facebook user identifier
     * @return The FQL stream query or <code>null</code> if fields require no query
     * @throws OXException If composing query fails
     */
    public static FQLQuery composeFQLStreamQueryFor(final Collection<MessagingField> fields, final String postId) throws OXException {
        return composeFQLStreamQueryFor0(null, fields, null, null, new String[] { postId }, null);
    }

    /**
     * Composes the FQL stream query for given post identifiers.
     *
     * @param fields The fields
     * @param postIds The post identifiers
     * @param facebookUserId The Facebook user identifier
     * @return The FQL stream query or <code>null</code> if fields require no query
     * @throws OXException If composing query fails
     */
    public static FQLQuery composeFQLStreamQueryFor(final Collection<MessagingField> fields, final String[] postIds) throws OXException {
        return composeFQLStreamQueryFor0(null, fields, null, null, postIds, null);
    }

    /**
     * Composes the FQL stream query for given fields.
     *
     * @param queryType The query type constant
     * @param fields The fields
     * @param facebookUserId The Facebook user identifier
     * @return The FQL stream query or <code>null</code> if fields require no query
     * @throws OXException If composing query fails
     */
    public static FQLQuery composeFQLStreamQueryFor(final FQLQueryType queryType, final Collection<MessagingField> fields, final String facebookUserId) throws OXException {
        return composeFQLStreamQueryFor0(queryType, fields, null, null, null, facebookUserId);
    }

    /**
     * Composes the FQL stream query for given fields.
     *
     * @param queryType The query type constant
     * @param fields The fields
     * @param sortField The sort field; may be <code>null</code>
     * @param order The order direction
     * @param facebookUserId The Facebook user identifier
     * @return The FQL stream query or <code>null</code> if fields require no query
     * @throws OXException If composing query fails
     */
    public static FQLQuery composeFQLStreamQueryFor(final FQLQueryType queryType, final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final String facebookUserId) throws OXException {
        return composeFQLStreamQueryFor0(queryType, fields, sortField, order, null, facebookUserId);
    }

    /**
     * The default limit for a FQL query.
     */
    private static final int DEFAULT_LIMIT = 1000;

    private static FQLQuery composeFQLStreamQueryFor0(final FQLQueryType queryType, final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final String[] postIds, final String facebookUserId) throws OXException {
        final StringBuilder query = startFQLStreamQuery(queryType, fields, facebookUserId);
        if (null == query) {
            /*
             * No fields queried
             */
            return null;
        }
        return finishFQLStreamQuery(sortField, order, postIds, DEFAULT_LIMIT, query, (null != queryType));
    }

    /**
     * Composes the FQL stream query for given fields considering posts created <b>before</b> given time stamp.
     *
     * @param timeStamp The time stamp
     * @param queryType The query type constant
     * @param fields The fields
     * @param sortField The sort field; may be <code>null</code>
     * @param order The order direction
     * @param facebookUserId The Facebook user identifier
     * @return The FQL stream query or <code>null</code> if fields require no query
     * @throws OXException If composing query fails
     */
    public static FQLQuery composeFQLStreamQueryBefore(final long timeStamp, final FQLQueryType queryType, final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final String facebookUserId) throws OXException {
        return composeFQLStreamQueryBefore(timeStamp, queryType, fields, sortField, order, null, facebookUserId);
    }

    private static FQLQuery composeFQLStreamQueryBefore(final long timeStamp, final FQLQueryType queryType, final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final String[] postIds, final String facebookUserId) throws OXException {
        final StringBuilder query = startFQLStreamQuery(queryType, fields, facebookUserId);
        if (null == query) {
            /*
             * No fields queried
             */
            return null;
        }
        /*
         * Add since
         */
        query.append(" AND created_time < ").append(timeStamp);
        return finishFQLStreamQuery(sortField, order, postIds, DEFAULT_LIMIT, query, (null != queryType));
    }

    private static StringBuilder startFQLStreamQuery(final FQLQueryType queryType, final Collection<MessagingField> fields, final String facebookUserId) throws OXException {
        /*
         * Resolve fields to known FQL fields
         */
        final Set<String> fieldNames = new HashSet<String>(fields.size());
        if (fields.contains(MessagingField.FULL)) {
            final QueryAdder queryAdder = ADDERS_STREAM.get(MessagingField.FULL);
            queryAdder.add2Query(fieldNames);
        } else {
            for (final MessagingField mf : fields) {
                final QueryAdder queryAdder = ADDERS_STREAM.get(mf);
                if (null != queryAdder) {
                    queryAdder.add2Query(fieldNames);
                }
            }
        }
        if (fieldNames.isEmpty()) {
            /*
             * No field could be mapped to a known FQL field
             */
            return null;
        }
        final int size = fieldNames.size();
        final StringBuilder query = new StringBuilder(size << 5).append("SELECT ");
        {
            final Iterator<String> iter = fieldNames.iterator();
            query.append(iter.next());
            for (int i = 1; i < size; i++) {
                query.append(", ").append(iter.next());
            }
        }
        /*
         * Compose query type
         */
        if (null == queryType) {
            query.append(" FROM stream WHERE");
        } else {
            switch (queryType) {
            case NEWS_FEED:
                // Retrieving the user's News Feed.
                query.append(" FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=").append(facebookUserId).append(
                    " AND type='newsfeed') AND is_hidden = 0");
                break;
            case WALL:
                // Retrieve a user's wall posts (stories on their profile).
                query.append(" FROM stream WHERE source_id = ").append(facebookUserId);
                break;
            default:
                throw FacebookMessagingExceptionCodes.UNSUPPORTED_QUERY_TYPE.create(queryType.toString());
            }
        }
        return query;
    }

    private static FQLQuery finishFQLStreamQuery(final MessagingField sortField, final OrderDirection order, final String[] postIds, final int limit, final StringBuilder query, final boolean prependAnd) {
        /*
         * Check for identifiers
         */
        if (null != postIds && 0 < postIds.length) {
            if (prependAnd) {
                query.append(" AND");
            }
            if (1 == postIds.length) {
                query.append(" post_id = '").append(postIds[0]).append('\'');
            } else {
                query.append(" post_id IN ");
                query.append('(');
                {
                    query.append('\'').append(postIds[0]).append('\'');
                    for (int i = 1; i < postIds.length; i++) {
                        query.append(',').append('\'').append(postIds[i]).append('\'');
                    }
                }
                query.append(')');
            }
        }
        /*
         * Check sort field
         */
        if (null == sortField) {
            return new FQLQuery(query.append(" LIMIT ").append(limit), false);
        }
        boolean containsOrderBy = false;
        final QueryAdder adder = ADDERS_STREAM.get(sortField);
        if (null != adder) {
            final String orderBy = adder.getOrderBy();
            if (null != orderBy) {
                query.append(" ORDER BY ").append(orderBy).append(OrderDirection.DESC.equals(order) ? " DESC" : " ASC");
                containsOrderBy = true;
            }
        }
        return new FQLQuery(query.append(" LIMIT ").append(limit), containsOrderBy);
    }

    /**
     * Composes the FQL user query for given fields.
     *
     * @param fields The fields
     * @param userId The user identifier
     * @return The FQL user query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLUserQueryFor(final Collection<MessagingField> fields, final long userId) {
        return composeFQLUserQueryFor0(fields, null, null, new long[] { userId });
    }

    /**
     * Composes the FQL user query for given fields.
     *
     * @param fields The fields
     * @param userIds The user identifiers
     * @return The FQL user query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLUserQueryFor(final Collection<MessagingField> fields, final long[] userIds) {
        return composeFQLUserQueryFor0(fields, null, null, userIds);
    }

    /**
     * Composes the FQL user query for given fields.
     *
     * @param fields The fields
     * @param sortField The sort field; may be <code>null</code>
     * @param order The order direction
     * @param userIds The user identifiers
     * @return The FQL user query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLUserQueryFor(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] userIds) {
        return composeFQLUserQueryFor0(fields, sortField, order, userIds);
    }

    private static FQLQuery composeFQLUserQueryFor0(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] userIds) {
        final Set<String> fieldNames = new HashSet<String>(fields.size());
        if (fields.contains(MessagingField.FULL)) {
            final QueryAdder queryAdder = ADDERS_USER.get(MessagingField.FULL);
            queryAdder.add2Query(fieldNames);
        } else {
            for (final MessagingField mf : fields) {
                final QueryAdder queryAdder = ADDERS_USER.get(mf);
                if (null != queryAdder) {
                    queryAdder.add2Query(fieldNames);
                }
            }
        }
        if (fieldNames.isEmpty()) {
            return null;
        }
        final int size = fieldNames.size();
        final StringBuilder query = new StringBuilder(size << 5).append("SELECT ");
        {
            final Iterator<String> iter = fieldNames.iterator();
            query.append(iter.next());
            for (int i = 1; i < size; i++) {
                query.append(", ").append(iter.next());
            }
        }
        query.append(" FROM user WHERE");
        if (null != userIds && 0 < userIds.length) {
            if (1 == userIds.length) {
                query.append(" uid = '").append(userIds[0]).append('\'');
            } else {
                query.append(" uid IN ");
                query.append('(');
                {
                    query.append('\'').append(userIds[0]).append('\'');
                    for (int i = 1; i < userIds.length; i++) {
                        query.append(',').append('\'').append(userIds[i]).append('\'');
                    }
                }
                query.append(')');
            }
        }
        /*
         * Check sort field
         */
        if (null == sortField) {
            return new FQLQuery(query, false);
        }
        boolean containsOrderBy = false;
        final QueryAdder adder = ADDERS_USER.get(sortField);
        if (null != adder) {
            final String orderBy = adder.getOrderBy();
            if (null != orderBy) {
                query.append(" ORDER BY ").append(orderBy).append(OrderDirection.DESC.equals(order) ? " DESC" : " ASC");
                containsOrderBy = true;
            }
        }
        return new FQLQuery(query, containsOrderBy);
    }

    /**
     * Composes the FQL group query for given fields.
     *
     * @param fields The fields
     * @param groupId The group identifier
     * @return The FQL group query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLGroupQueryFor(final Collection<MessagingField> fields, final long groupId) {
        return composeFQLGroupQueryFor0(fields, null, null, new long[] { groupId });
    }

    /**
     * Composes the FQL group query for given fields.
     *
     * @param fields The fields
     * @param groupIds The group identifiers
     * @return The FQL group query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLGroupQueryFor(final Collection<MessagingField> fields, final long[] groupIds) {
        return composeFQLGroupQueryFor0(fields, null, null, groupIds);
    }

    /**
     * Composes the FQL group query for given fields.
     *
     * @param fields The fields
     * @param sortField The sort field; may be <code>null</code>
     * @param order The order direction
     * @param groupIds The group identifiers
     * @return The FQL group query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLGroupQueryFor(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] groupIds) {
        return composeFQLGroupQueryFor0(fields, sortField, order, groupIds);
    }

    private static FQLQuery composeFQLGroupQueryFor0(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] groupIds) {
        final Set<String> fieldNames = new HashSet<String>(fields.size());
        if (fields.contains(MessagingField.FULL)) {
            final QueryAdder queryAdder = ADDERS_GROUP.get(MessagingField.FULL);
            queryAdder.add2Query(fieldNames);
        } else {
            for (final MessagingField mf : fields) {
                final QueryAdder queryAdder = ADDERS_GROUP.get(mf);
                if (null != queryAdder) {
                    queryAdder.add2Query(fieldNames);
                }
            }
        }
        if (fieldNames.isEmpty()) {
            return null;
        }
        final int size = fieldNames.size();
        final StringBuilder query = new StringBuilder(size << 5).append("SELECT ");
        {
            final Iterator<String> iter = fieldNames.iterator();
            query.append(iter.next());
            for (int i = 1; i < size; i++) {
                query.append(", ").append(iter.next());
            }
        }
        query.append(" FROM group WHERE");
        if (null != groupIds && 0 < groupIds.length) {
            if (1 == groupIds.length) {
                query.append(" gid = '").append(groupIds[0]).append('\'');
            } else {
                query.append(" gid IN ");
                query.append('(');
                {
                    query.append('\'').append(groupIds[0]).append('\'');
                    for (int i = 1; i < groupIds.length; i++) {
                        query.append(',').append('\'').append(groupIds[i]).append('\'');
                    }
                }
                query.append(')');
            }
        }
        /*
         * Check sort field
         */
        if (null == sortField) {
            return new FQLQuery(query, false);
        }
        boolean containsOrderBy = false;
        final QueryAdder adder = ADDERS_GROUP.get(sortField);
        if (null != adder) {
            final String orderBy = adder.getOrderBy();
            if (null != orderBy) {
                query.append(" ORDER BY ").append(orderBy).append(OrderDirection.DESC.equals(order) ? " DESC" : " ASC");
                containsOrderBy = true;
            }
        }
        return new FQLQuery(query, containsOrderBy);
    }

    /**
     * Composes the FQL page query for given fields.
     *
     * @param fields The fields
     * @param groupId The page identifier
     * @return The FQL page query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLPageQueryFor(final Collection<MessagingField> fields, final long groupId) {
        return composeFQLPageQueryFor0(fields, null, null, new long[] { groupId });
    }

    /**
     * Composes the FQL page query for given fields.
     *
     * @param fields The fields
     * @param groupIds The page identifiers
     * @return The FQL page query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLPageQueryFor(final Collection<MessagingField> fields, final long[] groupIds) {
        return composeFQLPageQueryFor0(fields, null, null, groupIds);
    }

    /**
     * Composes the FQL page query for given fields.
     *
     * @param fields The fields
     * @param sortField The sort field; may be <code>null</code>
     * @param order The order direction
     * @param groupIds The page identifiers
     * @return The FQL page query or <code>null</code> if fields require no query
     */
    public static FQLQuery composeFQLPageQueryFor(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] groupIds) {
        return composeFQLPageQueryFor0(fields, sortField, order, groupIds);
    }

    private static FQLQuery composeFQLPageQueryFor0(final Collection<MessagingField> fields, final MessagingField sortField, final OrderDirection order, final long[] groupIds) {
        final Set<String> fieldNames = new HashSet<String>(fields.size());
        if (fields.contains(MessagingField.FULL)) {
            final QueryAdder queryAdder = ADDERS_PAGE.get(MessagingField.FULL);
            queryAdder.add2Query(fieldNames);
        } else {
            for (final MessagingField mf : fields) {
                final QueryAdder queryAdder = ADDERS_PAGE.get(mf);
                if (null != queryAdder) {
                    queryAdder.add2Query(fieldNames);
                }
            }
        }
        if (fieldNames.isEmpty()) {
            return null;
        }
        final int size = fieldNames.size();
        final StringBuilder query = new StringBuilder(size << 5).append("SELECT ");
        {
            final Iterator<String> iter = fieldNames.iterator();
            query.append(iter.next());
            for (int i = 1; i < size; i++) {
                query.append(", ").append(iter.next());
            }
        }
        query.append(" FROM page WHERE");
        if (null != groupIds && 0 < groupIds.length) {
            if (1 == groupIds.length) {
                query.append(" page_id = '").append(groupIds[0]).append('\'');
            } else {
                query.append(" page_id IN ");
                query.append('(');
                {
                    query.append('\'').append(groupIds[0]).append('\'');
                    for (int i = 1; i < groupIds.length; i++) {
                        query.append(',').append('\'').append(groupIds[i]).append('\'');
                    }
                }
                query.append(')');
            }
        }
        /*
         * Check sort field
         */
        if (null == sortField) {
            return new FQLQuery(query, false);
        }
        boolean containsOrderBy = false;
        final QueryAdder adder = ADDERS_PAGE.get(sortField);
        if (null != adder) {
            final String orderBy = adder.getOrderBy();
            if (null != orderBy) {
                query.append(" ORDER BY ").append(orderBy).append(OrderDirection.DESC.equals(order) ? " DESC" : " ASC");
                containsOrderBy = true;
            }
        }
        return new FQLQuery(query, containsOrderBy);
    }

    /**
     * Abbreviates a text using ellipses. This will turn "Now is the time for all good men" into "Now is the time for..."
     *
     * @param text The text to check, may be null
     * @param maxWidth The maximum length of result String, must be at least 4
     * @return The abbreviated text or <code>null</code>
     * @throws IllegalArgumentException if the width is too small
     */
    public static String abbreviate(final String text, final int maxWidth) {
        return abbreviate(text, 0, maxWidth);
    }

    /**
     * Abbreviates a text using ellipses. This will turn "Now is the time for all good men" into "...is the time for..."
     *
     * @param text The text to check, may be null
     * @param offset The left edge of source String
     * @param maxWidth The maximum length of result String, must be at least 4
     * @return The abbreviated text or <code>null</code>
     * @throws IllegalArgumentException if the width is too small
     */
    public static String abbreviate(final String text, final int offset, final int maxWidth) {
        if (text == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (text.length() <= maxWidth) {
            return text;
        }
        int off = offset;
        if (off > text.length()) {
            off = text.length();
        }
        if ((text.length() - off) < (maxWidth - 3)) {
            off = text.length() - (maxWidth - 3);
        }
        if (off <= 4) {
            return new StringBuilder(text.substring(0, maxWidth - 3)).append("...").toString();
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if ((off + (maxWidth - 3)) < text.length()) {
            return new StringBuilder("...").append(abbreviate(text.substring(off), maxWidth - 3)).toString();
        }
        return new StringBuilder("...").append(text.substring(text.length() - (maxWidth - 3))).toString();
    }

    /**
     * Checks specified message's content to be of given type.
     *
     * @param part The part
     * @return The typed content
     * @throws OXException If message's content is of given type
     */
    public static <C extends MessagingContent> C checkContent(final Class<C> clazz, final MessagingPart part) throws OXException {
        final MessagingContent content = part.getContent();
        if (!(clazz.isInstance(content))) {
            throw MessagingExceptionCodes.UNKNOWN_MESSAGING_CONTENT.create(content.toString());
        }
        return clazz.cast(content);
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link java.util.regex.Pattern pattern}
     */
    public static String wildcardToRegex(final String wildcard) {
        final com.openexchange.java.StringAllocator s = new com.openexchange.java.StringAllocator(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

    /**
     * Gets the IN expression for specified <code>long</code>s ready for being used in a FQL query.<br>
     * E.g.: <code>&quot;(12,13,14,15)&quot;</code> would be returned for providing array <code>[12,13,14,15]</code>.
     *
     * @param arr The <code>long</code>s
     * @return The IN expression or <code>null</code> in case of a <code>null</code> dereference or an empty array
     */
    public static String getINString(final long arr[]) {
        if (arr == null || arr.length <= 0) {
            return null;
        }
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(arr.length * 8);
        sb.append('(');
        sb.append(arr[0]);
        for (int a = 1; a < arr.length; a++) {
            sb.append(',');
            sb.append(arr[a]);
        }
        sb.append(')');
        return sb.toString();
    }

}
