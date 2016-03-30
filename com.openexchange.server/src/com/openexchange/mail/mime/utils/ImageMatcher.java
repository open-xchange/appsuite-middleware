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

package com.openexchange.mail.mime.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.image.ImageActionFactory;

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
    public static ImageMatcher matcher(final CharSequence content) {
        return new ImageMatcher(content);
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
    private static volatile Pattern PATTERN_REF_IMG = null;

    /**
     * Sets the prefix service.
     *
     * @param prefixService The prefix service to set
     */
    /**
     * @param prefixService
     */
    public static void setPrefixService(final DispatcherPrefixService prefixService) {
        if (null == prefixService) {
            PATTERN_REF_IMG = null;
        } else {
            String prefix = "[a-zA-Z_0-9&-.]+/";
            final String regexImageUrl =
                "(<img[^>]*?)(src=\")(?:[^>]*?)" + prefix + ImageActionFactory.ALIAS_APPENDIX + "([^\"]+?)(?:\\?|&amp;|&)(uid=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^>]*/?>)";
            final String regexFileUrl =
                "(<img[^>]*?)(src=\")(?:[^>]*?)" + prefix + "file([^\"]+?)(?:\\?|&amp;|&)(id=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^>]*/?>)";

            PATTERN_REF_IMG =
                Pattern.compile("(?:" + regexFileUrl + ")|(?:" + regexImageUrl + ')', Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        }
    }

    private final Matcher matcher;

    /**
     * Initializes a new {@link ImageMatcher}.
     */
    private ImageMatcher(final CharSequence content) {
        super();
        final Pattern pattern = PATTERN_REF_IMG;
        if (null == pattern) {
            throw new IllegalStateException(ImageMatcher.class.getSimpleName() + " not initialized, yet.");
        }
        matcher = pattern.matcher(content);
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
    public String group(final int group) {
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
    public Matcher appendReplacement(final StringBuffer sb, final String replacement) {
        return matcher.appendReplacement(sb, replacement);
    }

    /**
     * Implements a non-terminal append-and-replace step.
     */
    public Matcher appendLiteralReplacement(final StringBuffer sb, final String replacement) {
        return matcher.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(replacement));
    }

    /**
     * Implements a non-terminal append-and-replace step.
     */
    public StringBuffer appendTail(final StringBuffer sb) {
        return matcher.appendTail(sb);
    }

}
