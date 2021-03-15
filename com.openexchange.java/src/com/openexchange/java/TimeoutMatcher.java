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

package com.openexchange.java;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link TimeoutMatcher} - Wraps a {@link Matcher matcher} and applies given timeout whenever matching the input sequence or finding a
 * certain sub-sequence is requested to avoid possibly long-running matcher invocations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class TimeoutMatcher {

    /**
     * Creates a new builder instance.
     *
     * @return The newly created builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>TimeoutMatcher</code> */
    public static class Builder {

        private Pattern pattern;
        private CharSequence input;
        private long timeout;
        private TimeUnit unit;
        private ExecutorService executor;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the pattern.
         *
         * @param pattern The pattern to set
         * @return This builder
         */
        public Builder withPattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        /**
         * Sets the character sequence to be matched.
         *
         * @param input The character sequence to be matched
         * @return This builder
         */
        public Builder withInput(CharSequence input) {
            this.input = input;
            return this;
        }

        /**
         * Sets the maximum time to wait.
         *
         * @param timeout The maximum time to wait
         * @param unit The time unit of the timeout argument to set
         * @return This builder
         */
        public Builder withTimeout(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
            return this;
        }

        /**
         * Sets the executor.
         *
         * @param executor The executor to set
         * @return This builder
         */
        public Builder withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Creates the resulting instance of <code>TimeoutMatcher</code> from this builder's arguments.
         *
         * @return The resulting instance of <code>TimeoutMatcher</code>
         */
        public TimeoutMatcher build() {
            return new TimeoutMatcher(pattern, input, timeout, unit, executor);
        }
    } // End of class Builder

    /**
     * Creates a new instance of <code>TimeoutMatcher</code> simply passing through w/o any timeout.
     *
     * @param pattern The pattern to generate matcher from
     * @param input The input character sequence
     * @return The resulting instance of <code>TimeoutMatcher</code> simply passing through w/o any timeout
     */
    public static TimeoutMatcher newPassThroughInstance(Pattern pattern, CharSequence input) {
        return new TimeoutMatcher(pattern, input, 0, null, null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Matcher matcher;
    private final long timeout;
    private final TimeUnit unit;
    private final ExecutorService executor;

    /**
     * Initializes a new {@link TimeoutMatcher}.
     *
     * @param pattern The pattern to generate matcher from
     * @param input The input character sequence
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout argument
     * @param executor The executor service to use
     */
    TimeoutMatcher(Pattern pattern, CharSequence input, long timeout, TimeUnit unit, ExecutorService executor) {
        super();
        long tmout = timeout <= 0 || executor == null || unit == null ? 0 : timeout;
        this.matcher = tmout <= 0 ? pattern.matcher(input) : pattern.matcher(InterruptibleCharSequence.valueOf(input));
        this.timeout = tmout;
        this.unit = unit;
        this.executor = executor;
    }

    /**
     * Submits given task for execution and retrieves its result with respect to timeout.
     *
     * @param <R> The type of the result
     * @param task The task that should be executed
     * @param defaultValue The default value to return in case a timeout occurs
     * @return The result
     * @throws IllegalStateException If thread gets interrupted
     * @throws RuntimeException If execution fails
     */
    private <R> R executeAndGet(Callable<R> task, R defaultValue) throws Error {
        // Submit for execution (or run with calling thread)
        Future<R> future;
        try {
            future = executor.submit(task);
        } catch (RejectedExecutionException e) {
            // Run with calling thread
            FutureTask<R> ft = new FutureTask<R>(task);
            ft.run();
            future = ft;
        }

        // Retrieve result with respect to timeout
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Failed attempt to find a sequence or match the input sequence", cause == null ? e : cause);
        } catch (TimeoutException e) {
            future.cancel(true);
        }
        return defaultValue;
    }

    /**
     * Gets the pattern that is interpreted by this matcher.
     *
     * @return The pattern for which this matcher was created
     */
    public Pattern pattern() {
        return matcher.pattern();
    }

    /**
     * Gets the match state of this matcher as a {@link MatchResult}.
     * The result is unaffected by subsequent operations performed upon this
     * matcher.
     *
     * @return A <code>MatchResult</code> with the state of this matcher
     */
    public MatchResult toMatchResult() {
        return matcher.toMatchResult();
    }

    /**
     * Changes the <tt>Pattern</tt> that this <tt>Matcher</tt> uses to find matches with.
     *
     * <p> This method causes this matcher to lose information
     * about the groups of the last match that occurred. The
     * matcher's position in the input is maintained and its
     * last append position is unaffected.</p>
     *
     * @param newPattern The new pattern used by this matcher
     * @return This matcher
     * @throws IllegalArgumentException If newPattern is <tt>null</tt>
     */
    public Matcher usePattern(Pattern newPattern) {
        return matcher.usePattern(newPattern);
    }

    /**
     * Resets this matcher.
     * <p>
     * Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to the
     * default region, which is its entire character sequence. The anchoring
     * and transparency of this matcher's region boundaries are unaffected.
     *
     * @return This matcher
     */
    public Matcher reset() {
        return matcher.reset();
    }

    /**
     * Resets this matcher with a new input sequence.
     * <p>
     * Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to
     * the default region, which is its entire character sequence. The
     * anchoring and transparency of this matcher's region boundaries are
     * unaffected.
     *
     * @param input The new input character sequence
     * @return This matcher
     */
    public Matcher reset(CharSequence input) {
        return matcher.reset(input);
    }

    /**
     * Gets the start index of the previous match.
     *
     * @return The index of the first character matched
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     */
    public int start() {
        return matcher.start();
    }

    /**
     * Gets the start index of the subsequence captured by the given group
     * during the previous match operation.
     * <p>
     * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one. Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>start(0)</tt> is equivalent to
     * <i>m.</i><tt>start()</tt>. </p>
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The index of the first character captured by the group, or <tt>-1</tt> if the match was successful but the group itself
     *         did not match anything
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given index
     */
    public int start(int group) {
        return matcher.start(group);
    }

    /**
     * Gets the start index of the subsequence captured by the given
     * <a href="Pattern.html#groupname">named-capturing group</a> during the
     * previous match operation.
     *
     * @param name The name of a named-capturing group in this matcher's pattern
     * @return The index of the first character captured by the group, or {@code -1} if the match was successful but the group itself
     *         did not match anything
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IllegalArgumentException If there is no capturing group in the pattern with the given name
     */
    public int end() {
        return matcher.end();
    }

    /**
     * Gets the offset after the last character of the subsequence
     * captured by the given group during the previous match operation.
     * <p>
     * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one. Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>end(0)</tt> is equivalent to
     * <i>m.</i><tt>end()</tt>. </p>
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The offset after the last character captured by the group, or <tt>-1</tt> if the match was successful but the group itself
     *         did not match anything
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given index
     */
    public int end(int group) {
        return matcher.end(group);
    }

    /**
     * Gets the input subsequence matched by the previous match.
     * <p>
     * For a matcher <i>m</i> with input sequence <i>s</i>,
     * the expressions <i>m.</i><tt>group()</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * are equivalent. </p>
     * <p>
     * Note that some patterns, for example <tt>a*</tt>, match the empty
     * string. This method will return the empty string when the pattern
     * successfully matches the empty string in the input. </p>
     *
     * @return The (possibly empty) subsequence matched by the previous match, in string form
     * @throws IllegalStateException If no match has yet been attempted,
     */
    public String group() {
        return matcher.group();
    }

    /**
     * Gets the input subsequence captured by the given group during the
     * previous match operation.
     * <p>
     * For a matcher <i>m</i>, input sequence <i>s</i>, and group index
     * <i>g</i>, the expressions <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * are equivalent. </p>
     * <p>
     * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one. Group zero denotes the entire pattern, so
     * the expression <tt>m.group(0)</tt> is equivalent to <tt>m.group()</tt>.
     * </p>
     * <p>
     * If the match was successful but the group specified failed to match
     * any part of the input sequence, then <tt>null</tt> is returned. Note
     * that some groups, for example <tt>(a*)</tt>, match the empty string.
     * This method will return the empty string when such a group successfully
     * matches the empty string in the input. </p>
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The (possibly empty) subsequence captured by the group during the previous match, or <tt>null</tt> if the group failed
     *         to match part of the input
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given index
     */
    public String group(int group) {
        return matcher.group(group);
    }

    /**
     * Gets the number of capturing groups in this matcher's pattern.
     * <p>
     * Group zero denotes the entire pattern by convention. It is not
     * included in this count.
     * <p>
     * Any non-negative integer smaller than or equal to the value
     * returned by this method is guaranteed to be a valid group index for
     * this matcher. </p>
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    public int groupCount() {
        return matcher.groupCount();
    }

    /**
     * Attempts to match the entire region against the pattern.
     * <p>
     * If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods. </p>
     *
     * @return <tt>true</tt> if, and only if, the entire region sequence matches this matcher's pattern
     */
    public boolean matches() {
        if (timeout <= 0) {
            return matcher.matches();
        }

        Callable<Boolean> matchesCallable = new MatcherAcceptingCallable<Boolean>(matcher) {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(matcher.matches());
            }
        };

        return executeAndGet(matchesCallable, Boolean.FALSE).booleanValue();
    }

    /**
     * Attempts to find the next subsequence of the input sequence that matches the pattern.
     * <p>
     * This method starts at the beginning of this matcher's region, or, if
     * a previous invocation of the method was successful and the matcher has
     * not since been reset, at the first character not matched by the previous
     * match.
     * <p>
     * If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods. </p>
     *
     * @return <tt>true</tt> if, and only if, a subsequence of the input sequence matches this matcher's pattern
     */
    public boolean find() {
        if (timeout <= 0) {
            return matcher.find();
        }

        Callable<Boolean> findCallable = new MatcherAcceptingCallable<Boolean>(matcher) {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(matcher.find());
            }
        };

        return executeAndGet(findCallable, Boolean.FALSE).booleanValue();
    }

    /**
     * Resets this matcher and then attempts to find the next subsequence of
     * the input sequence that matches the pattern, starting at the specified
     * index.
     * <p>
     * If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods, and subsequent
     * invocations of the {@link #find()} method will start at the first
     * character not matched by this match. </p>
     *
     * @param start The index to start searching for a match
     * @throws IndexOutOfBoundsException If start is less than zero or if start is greater than the length of the input sequence.
     * @return <tt>true</tt> if, and only if, a subsequence of the input sequence starting at the given index matches this matcher's
     *         pattern
     */
    public boolean find(final int start) {
        if (timeout <= 0) {
            return matcher.find(start);
        }

        Callable<Boolean> findCallable = new MatcherAcceptingCallable<Boolean>(matcher) {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(matcher.find(start));
            }
        };

        return executeAndGet(findCallable, Boolean.FALSE).booleanValue();
    }

    /**
     * Attempts to match the input sequence, starting at the beginning of the
     * region, against the pattern.
     * <p>
     * Like the {@link #matches matches} method, this method always starts
     * at the beginning of the region; unlike that method, it does not
     * require that the entire region be matched.
     * <p>
     * If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods. </p>
     *
     * @return <tt>true</tt> if, and only if, a prefix of the input
     *         sequence matches this matcher's pattern
     */
    public boolean lookingAt() {
        if (timeout <= 0) {
            return matcher.lookingAt();
        }

        Callable<Boolean> lookingAtCallable = new MatcherAcceptingCallable<Boolean>(matcher) {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(matcher.lookingAt());
            }
        };

        return executeAndGet(lookingAtCallable, Boolean.FALSE).booleanValue();
    }

    /**
     * Implements a non-terminal append-and-replace step.
     * <p>
     * This method performs the following actions:
     * </p>
     *
     * <ol>
     *
     * <li><p> It reads characters from the input sequence, starting at the
     * append position, and appends them to the given string buffer. It
     * stops after reading the last character preceding the previous match,
     * that is, the character at index {@link
     * #start()}&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>. </p></li>
     *
     * <li><p> It appends the given replacement string to the string buffer.
     * </p></li>
     *
     * <li><p> It sets the append position of this matcher to the index of
     * the last character matched, plus one, that is, to {@link #end()}.
     * </p></li>
     *
     * </ol>
     *
     * <p> The replacement string may contain references to subsequences
     * captured during the previous match: Each occurrence of
     * <tt>${</tt><i>name</i><tt>}</tt> or <tt>$</tt><i>g</i>
     * will be replaced by the result of evaluating the corresponding
     * {@link #group(String) group(name)} or {@link #group(int) group(g)}
     * respectively. For <tt>$</tt><i>g</i>,
     * the first number after the <tt>$</tt> is always treated as part of
     * the group reference. Subsequent numbers are incorporated into g if
     * they would form a legal group reference. Only the numerals '0'
     * through '9' are considered as potential components of the group
     * reference. If the second group matched the string <tt>"foo"</tt>, for
     * example, then passing the replacement string <tt>"$2bar"</tt> would
     * cause <tt>"foobar"</tt> to be appended to the string buffer. A dollar
     * sign (<tt>$</tt>) may be included as a literal in the replacement
     * string by preceding it with a backslash (<tt>\$</tt>).
     *
     * <p> Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> This method is intended to be used in a loop together with the
     * {@link #appendTail appendTail} and {@link #find find} methods. The
     * following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream: </p>
     *
     * <blockquote><pre>
     * Pattern p = Pattern.compile("cat");
     * Matcher m = p.matcher("one cat two cats in the yard");
     * StringBuffer sb = new StringBuffer();
     * while (m.find()) {
     * m.appendReplacement(sb, "dog");
     * }
     * m.appendTail(sb);
     * System.out.println(sb.toString());</pre></blockquote>
     *
     * @param sb The target string buffer
     * @param replacement The replacement string
     * @return This matcher
     * @throws IllegalStateException If no match has yet been attempted, or if the previous match operation failed
     * @throws IllegalArgumentException If the replacement string refers to a named-capturing group that does not exist in the pattern
     * @throws IndexOutOfBoundsException If the replacement string refers to a capturing group that does not exist in the pattern
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {
        return matcher.appendReplacement(sb, replacement);
    }

    /**
     * Implements a terminal append-and-replace step.
     * <p>
     * This method reads characters from the input sequence, starting at
     * the append position, and appends them to the given string buffer. It is
     * intended to be invoked after one or more invocations of the {@link
     * #appendReplacement appendReplacement} method in order to copy the
     * remainder of the input sequence. </p>
     *
     * @param sb The target string buffer
     * @return The target string buffer
     */
    public StringBuffer appendTail(StringBuffer sb) {
        return matcher.appendTail(sb);
    }

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     * <p>
     * This method first resets this matcher. It then scans the input
     * sequence looking for matches of the pattern. Characters that are not
     * part of any match are appended directly to the result string; each match
     * is replaced in the result by the replacement string. The replacement
     * string may contain references to captured subsequences as in the {@link
     * #appendReplacement appendReplacement} method.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     * <p>
     * Given the regular expression <tt>a*b</tt>, the input
     * <tt>"aabfooaabfooabfoob"</tt>, and the replacement string
     * <tt>"-"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"-foo-foo-foo-"</tt>.
     * <p>
     * Invoking this method changes this matcher's state. If the matcher
     * is to be used in further matching operations then it should first be
     * reset. </p>
     *
     * @param replacement The replacement string
     *
     * @return The string constructed by replacing each matching subsequence by the replacement string, substituting captured
     *         subsequences as needed
     */
    public String replaceAll(String replacement) {
        return matcher.replaceAll(replacement);
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * <p> This method first resets this matcher. It then scans the input
     * sequence looking for a match of the pattern. Characters that are not
     * part of the match are appended directly to the result string; the match
     * is replaced in the result by the replacement string. The replacement
     * string may contain references to captured subsequences as in the {@link
     * #appendReplacement appendReplacement} method.
     *
     * <p>Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> Given the regular expression <tt>dog</tt>, the input
     * <tt>"zzzdogzzzdogzzz"</tt>, and the replacement string
     * <tt>"cat"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"zzzcatzzzdogzzz"</tt>. </p>
     *
     * <p> Invoking this method changes this matcher's state. If the matcher
     * is to be used in further matching operations then it should first be
     * reset. </p>
     *
     * @param replacement The replacement string
     * @return The string constructed by replacing the first matching
     *         subsequence by the replacement string, substituting captured
     *         subsequences as needed
     */
    public String replaceFirst(String replacement) {
        return matcher.replaceFirst(replacement);
    }

    /**
     * Sets the limits of this matcher's region. The region is the part of the
     * input sequence that will be searched to find a match. Invoking this
     * method resets the matcher, and then sets the region to start at the
     * index specified by the <code>start</code> parameter and end at the
     * index specified by the <code>end</code> parameter.
     *
     * <p>Depending on the transparency and anchoring being used (see
     * {@link #useTransparentBounds useTransparentBounds} and
     * {@link #useAnchoringBounds useAnchoringBounds}), certain constructs such
     * as anchors may behave differently at or around the boundaries of the
     * region.
     *
     * @param start The index to start searching at (inclusive)
     * @param end The index to end searching at (exclusive)
     * @throws IndexOutOfBoundsException If start or end is less than zero, if
     *             start is greater than the length of the input sequence, if
     *             end is greater than the length of the input sequence, or if
     *             start is greater than end.
     * @return This matcher
     */
    public Matcher region(int start, int end) {
        return matcher.region(start, end);
    }

    /**
     * Reports the start index of this matcher's region. The
     * searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return The starting point of this matcher's region
     */
    public int regionStart() {
        return matcher.regionStart();
    }

    /**
     * Reports the end index (exclusive) of this matcher's region.
     * The searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return The ending point of this matcher's region
     */
    public int regionEnd() {
        return matcher.regionEnd();
    }

    /**
     * Queries the transparency of region bounds for this matcher.
     * <p>
     * This method returns <tt>true</tt> if this matcher uses
     * <i>transparent</i> bounds, <tt>false</tt> if it uses <i>opaque</i>
     * bounds.
     * <p>
     * See {@link #useTransparentBounds useTransparentBounds} for a
     * description of transparent and opaque bounds.
     * <p>
     * By default, a matcher uses opaque region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using transparent bounds, <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     */
    public boolean hasTransparentBounds() {
        return matcher.hasTransparentBounds();
    }

    /**
     * Sets the transparency of region bounds for this matcher.
     *
     * <p> Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use <i>transparent</i> bounds. If the boolean
     * argument is <tt>false</tt>, then <i>opaque</i> bounds will be used.
     *
     * <p> Using transparent bounds, the boundaries of this
     * matcher's region are transparent to lookahead, lookbehind,
     * and boundary matching constructs. Those constructs can see beyond the
     * boundaries of the region to see if a match is appropriate.
     *
     * <p> Using opaque bounds, the boundaries of this matcher's
     * region are opaque to lookahead, lookbehind, and boundary matching
     * constructs that may try to see beyond them. Those constructs cannot
     * look past the boundaries so they will fail to match anything outside
     * of the region.
     *
     * <p> By default, a matcher uses opaque bounds.
     *
     * @param b A boolean indicating whether to use opaque or transparent
     *            regions
     * @return this matcher
     * @see java.util.regex.Matcher#hasTransparentBounds
     */
    public Matcher useTransparentBounds(boolean b) {
        return matcher.useTransparentBounds(b);
    }

    /**
     * Queries the anchoring of region bounds for this matcher.
     *
     * <p> This method returns <tt>true</tt> if this matcher uses
     * <i>anchoring</i> bounds, <tt>false</tt> otherwise.
     *
     * <p> See {@link #useAnchoringBounds useAnchoringBounds} for a
     * description of anchoring bounds.
     *
     * <p> By default, a matcher uses anchoring region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using anchoring bounds,
     *         <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     */
    public boolean hasAnchoringBounds() {
        return matcher.hasAnchoringBounds();
    }

    /**
     * Sets the anchoring of region bounds for this matcher.
     *
     * <p> Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use <i>anchoring</i> bounds. If the boolean
     * argument is <tt>false</tt>, then <i>non-anchoring</i> bounds will be
     * used.
     *
     * <p> Using anchoring bounds, the boundaries of this
     * matcher's region match anchors such as ^ and $.
     *
     * <p> Without anchoring bounds, the boundaries of this
     * matcher's region will not match anchors such as ^ and $.
     *
     * <p> By default, a matcher uses anchoring region boundaries.
     *
     * @param b A boolean indicating whether or not to use anchoring bounds.
     * @return this matcher
     * @see java.util.regex.Matcher#hasAnchoringBounds
     */
    public Matcher useAnchoringBounds(boolean b) {
        return matcher.useAnchoringBounds(b);
    }

    @Override
    public String toString() {
        return matcher.toString();
    }

    /**
     * Gets <code>true</code> if the end of input was hit by the search engine in
     * the last match operation performed by this matcher.
     * <p>
     * When this method returns true, then it is possible that more input
     * would have changed the result of the last search.
     *
     * @return <code>true</code> if the end of input was hit in the last match; <code>false</code> otherwise
     */
    public boolean hitEnd() {
        return matcher.hitEnd();
    }

    /**
     * Gets true if more input could change a positive match into a
     * negative one.
     * <p>
     * If this method returns true, and a match was found, then more
     * input could cause the match to be lost. If this method returns false
     * and a match was found, then more input might change the match but the
     * match won't be lost. If a match was not found, then requireEnd has no
     * meaning.
     *
     * @return <code>true</code> if more input could change a positive match into a
     *         negative one.
     */
    public boolean requireEnd() {
        return matcher.requireEnd();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static abstract class MatcherAcceptingCallable<R> implements Callable<R> {

        /** The matcher instance */
        protected final Matcher matcher;

        /**
         * Initializes a new {@link MatcherAcceptingCallable}.
         *
         * @param matcher The matcher instance
         */
        protected MatcherAcceptingCallable(Matcher matcher) {
            super();
            this.matcher = matcher;
        }
    }

}
