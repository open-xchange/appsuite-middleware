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

package com.openexchange.mail.mime.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.java.TimeoutMatcher;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link ImageMatcher} - Looks up occurrences of
 * <ul>
 * <li>&lt;img src="/ajax/image/...uid=xyz"&gt; and</li>
 * <li>&lt;img src="/ajax/file/...id=xyz"&gt;</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageMatcher {

    /**
     * Creates a new image matcher from given content.
     *
     * @param content The content
     * @return The appropriate matcher
     */
    public static ImageMatcher matcher(CharSequence content) {
        return content == null ? null : new ImageMatcher(content);
    }

    private static final int GROUP_FILE_ID = 5;

    private static final int GROUP_IMG_ID = 13;

    /**
     * The pattern to look-up Open-Xchange image URLs inside HTML content.
     *
     * <pre>
     * final Matcher m = ImageMatcher.matcher(s);
     * if (m.find()) {
     *     System.out.println(m.group(5)); // Prints the managed file identifier or &lt;code&gt;null&lt;/code&gt;
     *     System.out.println(m.group(13)); // Prints the image identifier or &lt;code&gt;null&lt;/code&gt;
     * }
     * </pre>
     */
    private static final AtomicReference<Pattern> PATTERN_REF_IMG = new AtomicReference<>(null);

    /**
     * Sets the prefix service.
     *
     * @param prefixService The prefix service to set
     */
    /**
     * @param prefixService
     */
    public static void setPrefixService(DispatcherPrefixService prefixService) {
        if (null == prefixService) {
            PATTERN_REF_IMG.set(null);
        } else {
            String prefix = "[a-zA-Z_0-9&-.]+/";
            final String regexImageUrl =
                "(<img[^>]*?)(src=\")(?:[^\"]*?)" + prefix + ImageActionFactory.ALIAS_APPENDIX + "([^a-zA-Z][^\"]+?)(?:\\?|&amp;|&)(uid=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^>]*/?>)";
            final String regexFileUrl =
                "(<img[^>]*?)(src=\")(?:[^\"]*?)" + prefix + "file([^a-zA-Z][^\"]+?)(?:\\?|&amp;|&)(id=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^>]*/?>)";

            PATTERN_REF_IMG.set(Pattern.compile("(?:" + regexFileUrl + ")|(?:" + regexImageUrl + ')', Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final TimeoutMatcher matcher;
    private CharSequence content;
    private FindState findState;

    /**
     * Initializes a new {@link ImageMatcher}.
     */
    private ImageMatcher(CharSequence content) {
        super();
        this.content = content;
        final Pattern pattern = PATTERN_REF_IMG.get();
        if (null == pattern) {
            throw new IllegalStateException(ImageMatcher.class.getSimpleName() + " not initialized, yet.");
        }
        findState = FindState.NOT_CHECKED;

        ThreadPoolService threadPool = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
        if (threadPool == null) {
            matcher = TimeoutMatcher.newPassThroughInstance(pattern, content);
        } else {
            matcher = TimeoutMatcher.builder()
                                    .withPattern(pattern)
                                    .withInput(content)
                                    .withTimeout(10, TimeUnit.SECONDS)
                                    .withExecutor(threadPool.getExecutor())
                                    .build();
        }
    }

    /**
     * Returns the matching image tag.
     *
     * @return The matching image tag.
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     */
    public String group() {
        return matcher.group();
    }

    /**
     * Returns the input subsequence captured by the given group during the previous match operation.
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The (possibly empty) subsequence captured by the group during the previous match, or <tt>null</tt> if the group failed to
     *         match part of the input
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given index
     */
    public String group(int group) {
        return matcher.group(group);
    }

    /**
     * Returns the number of capturing groups in this matcher's pattern.
     * <p>
     * Group zero denotes the entire pattern by convention. It is not included in this count.
     * <p>
     * Any non-negative integer smaller than or equal to the value returned by this method is guaranteed to be a valid group index for this
     * matcher.
     * </p>
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    public int groupCount() {
        return matcher.groupCount();
    }

    /**
     * Attempts to match the entire region against the pattern.
     * <p>
     * If the match succeeds then more information can be obtained via the <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.
     * </p>
     *
     * @return <tt>true</tt> if, and only if, the entire region sequence matches this matcher's pattern
     */
    public boolean matches() {
        return matcher.matches();
    }

    /**
     * Attempts to find the next subsequence of the input sequence that matches the pattern.
     * <p>
     * This method starts at the beginning of this matcher's region, or, if a previous invocation of the method was successful and the
     * matcher has not since been reset, at the first character not matched by the previous match.
     * <p>
     * If the match succeeds then more information can be obtained via the <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.
     * </p>
     *
     * @return <tt>true</tt> if, and only if, a subsequence of the input sequence matches this matcher's pattern
     */
    public boolean find() {
        // Fast plausibility check
        switch (findState) {
            case NOT_CHECKED:
                String str = content.toString();
                if (str.indexOf('/' + ImageActionFactory.ALIAS_APPENDIX) < 0 && str.indexOf("/file") < 0) {
                    // Essential substring not included. Cannot find a match.
                    findState = FindState.CANNOT_FIND;
                    content = null; // Content no more needed
                    return false;
                }
                // Essential substrings contained. Check for matches.
                findState = FindState.CHECK_FIND;
                content = null; // Content no more needed
                break;
            case CANNOT_FIND:
                // Essential substring not included. Cannot find a match.
                return false;
            default:
                break;
        }

        // Invoke matcher
        return matcher.find();
    }

    /**
     * Gets the managed file identifier.
     *
     * @return The identifier
     */
    public String getManagedFileId() {
        return matcher.group(GROUP_FILE_ID);
    }

    /**
     * Gets the image identifier
     *
     * @return The identifier
     */
    public String getImageId() {
        return matcher.group(GROUP_IMG_ID);
    }

    /**
     * Gets the start index of the previous match.
     *
     * @return The start index
     */
    public int start() {
        return matcher.start();
    }

    /**
     * Gets the end index of the previous match.
     *
     * @return The end index
     */
    public int end() {
        return matcher.end();
    }

    /**
     * Implements a non-terminal append-and-replace step.
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {
        return matcher.appendReplacement(sb, replacement);
    }

    /**
     * Implements a non-terminal append-and-replace step.
     */
    public Matcher appendLiteralReplacement(StringBuffer sb, String replacement) {
        return matcher.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(replacement));
    }

    /**
     * Implements a non-terminal append-and-replace step.
     */
    public StringBuffer appendTail(StringBuffer sb) {
        return matcher.appendTail(sb);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static enum FindState {
        /** Not yet checked */
        NOT_CHECKED,
        /** Essential substring not included. Cannot find a match. */
        CANNOT_FIND,
        /** Essential substrings contained. Check for matches. */
        CHECK_FIND;
    }

}
