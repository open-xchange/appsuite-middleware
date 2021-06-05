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

package com.openexchange.gmail.send.http;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.NanoClock;
import com.google.api.client.util.Sleeper;
import com.openexchange.gmail.send.config.GmailSendConfig;

/**
 * Implements a request initializer that adds retry handlers.
 * <p>
 * Derived from <code>org.apache.beam.sdk.util.RetryHttpRequestInitializer</code>.
 */
public class RetryingHttpRequestInitializer implements HttpRequestInitializer {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RetryingHttpRequestInitializer.class);

    private static final Set<Integer> DEFAULT_IGNORED_RESPONSE_CODES = new HashSet<>(
        Arrays.asList(
            I(307) /* Redirect, handled by the client library */,
            I(308) /* Resume Incomplete, handled by the client library */
        )
    );

    private final HttpResponseInterceptor responseInterceptor;  // response Interceptor to use
    private final NanoClock nanoClock;  // used for testing
    private final Sleeper sleeper;  // used for testing
    private final Set<Integer> ignoredResponseCodes = new HashSet<>(DEFAULT_IGNORED_RESPONSE_CODES);
    private final GmailSendConfig gmailSendConfig;
    private final HttpRequestInitializer requestInitializer;

    /**
     * Initializes a new {@link RetryingHttpRequestInitializer}.
     *
     * @param requestInitializer The request initialized to call first
     * @param gmailSendConfig The Gmail Send configuration providing read/connect timeout to set
     */
    public RetryingHttpRequestInitializer(HttpRequestInitializer requestInitializer, GmailSendConfig gmailSendConfig) {
        this(requestInitializer, gmailSendConfig, Collections.<Integer> emptyList());
    }

    /**
     * Initializes a new {@link RetryingHttpRequestInitializer}.
     *
     * @param requestInitializer The request initialized to call first
     * @param gmailSendConfig The Gmail Send configuration providing read/connect timeout to set
     * @param additionalIgnoredResponseCodes a list of HTTP status codes that should not be logged.
     */
    public RetryingHttpRequestInitializer(HttpRequestInitializer requestInitializer, GmailSendConfig gmailSendConfig, Collection<Integer> additionalIgnoredResponseCodes) {
        this(requestInitializer, gmailSendConfig, additionalIgnoredResponseCodes, null);
    }

    /**
     * Initializes a new {@link RetryingHttpRequestInitializer}.
     *
     * @param requestInitializer The request initialized to call first
     * @param gmailSendConfig The Gmail Send configuration providing read/connect timeout to set
     * @param additionalIgnoredResponseCodes a list of HTTP status codes that should not be logged.
     * @param responseInterceptor HttpResponseInterceptor to be applied on all requests. May be null.
     */
    public RetryingHttpRequestInitializer(HttpRequestInitializer requestInitializer, GmailSendConfig gmailSendConfig, Collection<Integer> additionalIgnoredResponseCodes, HttpResponseInterceptor responseInterceptor) {
        this(requestInitializer, gmailSendConfig, NanoClock.SYSTEM, Sleeper.DEFAULT, additionalIgnoredResponseCodes, responseInterceptor);
    }

    /**
     * Initializes a new {@link RetryingHttpRequestInitializer}.
     *
     * @param requestInitializer The request initialized to call first
     * @param gmailSendConfig The Gmail Send configuration providing read/connect timeout to set
     * @param nanoClock used as a timing source for knowing how much time has elapsed.
     * @param sleeper used to sleep between retries.
     * @param additionalIgnoredResponseCodes a list of HTTP status codes that should not be logged.
     */
    RetryingHttpRequestInitializer(HttpRequestInitializer requestInitializer, GmailSendConfig gmailSendConfig, NanoClock nanoClock, Sleeper sleeper, Collection<Integer> additionalIgnoredResponseCodes, HttpResponseInterceptor responseInterceptor) {
        super();
        this.requestInitializer = requestInitializer;
        this.gmailSendConfig = gmailSendConfig;
        this.nanoClock = nanoClock;
        this.sleeper = sleeper;
        this.ignoredResponseCodes.addAll(additionalIgnoredResponseCodes);
        this.responseInterceptor = responseInterceptor;
    }

    @Override
    public void initialize(HttpRequest httpRequest) throws IOException {
        // Pre-initialize
        requestInitializer.initialize(httpRequest);

        // Set read & connect timeout
        httpRequest.setConnectTimeout(gmailSendConfig.getGmailSendProperties().getConnectionTimeout());
        httpRequest.setReadTimeout(gmailSendConfig.getGmailSendProperties().getTimeout());

        RetryingHttpRequestInitializer.LoggingHttpBackOffHandler loggingHttpBackOffHandler = new LoggingHttpBackOffHandler(
            sleeper,
            // Back off on retryable http errors and IOExceptions.
            // A back-off multiplier of 2 raises the maximum request retrying time
            // to approximately 5 minutes (keeping other back-off parameters to
            // their default values).
            new ExponentialBackOff.Builder().setNanoClock(nanoClock).setMultiplier(2).build(),
            new ExponentialBackOff.Builder().setNanoClock(nanoClock).setMultiplier(2).build(),
            ignoredResponseCodes
        );

        httpRequest.setUnsuccessfulResponseHandler(loggingHttpBackOffHandler);
        httpRequest.setIOExceptionHandler(loggingHttpBackOffHandler);

        // Set response initializer
        if (responseInterceptor != null) {
            httpRequest.setResponseInterceptor(responseInterceptor);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** Handlers used to provide additional logging information on unsuccessful HTTP requests. */
    private static class LoggingHttpBackOffHandler implements HttpIOExceptionHandler, HttpUnsuccessfulResponseHandler {

        private final Sleeper sleeper;
        private final BackOff ioExceptionBackOff;
        private final BackOff unsuccessfulResponseBackOff;
        private final Set<Integer> ignoredResponseCodes;
        private int ioExceptionRetries;
        private int unsuccessfulResponseRetries;

        LoggingHttpBackOffHandler(Sleeper sleeper, BackOff ioExceptionBackOff, BackOff unsucessfulResponseBackOff, Set<Integer> ignoredResponseCodes) {
            super();
            this.sleeper = sleeper;
            this.ioExceptionBackOff = ioExceptionBackOff;
            this.unsuccessfulResponseBackOff = unsucessfulResponseBackOff;
            this.ignoredResponseCodes = ignoredResponseCodes;
        }

        @Override
        public boolean handleIOException(HttpRequest request, boolean supportsRetry) throws IOException {
            // Retry if the request supports retry or the back-off was successful.
            // Note that the order of these checks is important since
            // backOffWasSuccessful will perform a sleep.
            boolean willRetry = supportsRetry && backOffWasSuccessful(ioExceptionBackOff);
            if (willRetry) {
                ioExceptionRetries += 1;
                LOG.debug("Request failed with IOException, will retry: {}", request.getUrl());
            } else {
                String message = "Request failed with IOException, performed {} retries due to IOExceptions, performed {} retries due to unsuccessful status codes, HTTP framework says request {} be retried, (caller responsible for retrying): {}";
                LOG.warn(message, I(ioExceptionRetries), I(unsuccessfulResponseRetries), supportsRetry ? "can" : "cannot", request.getUrl());
            }
            return willRetry;
        }

        @Override
        public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {
            // Retry if the request supports retry and the status code requires a bac-koff
            // and the back-off was successful. Note that the order of these checks is important since
            // backOffWasSuccessful will perform a sleep.
            boolean willRetry = supportsRetry && retryOnStatusCode(response.getStatusCode()) && backOffWasSuccessful(unsuccessfulResponseBackOff);
            if (willRetry) {
                unsuccessfulResponseRetries += 1;
                LOG.debug("Request failed with code {}, will retry: {}", I(response.getStatusCode()), request.getUrl());
            } else {
                String message = "Request failed with code {}, performed {} retries due to IOExceptions, performed {} retries due to unsuccessful status codes, HTTP framework says request {} be retried, (caller responsible for retrying): {}";
                if (ignoredResponseCodes.contains(I(response.getStatusCode()))) {
                    // Log ignored response codes at a lower level
                    LOG.debug(message, I(response.getStatusCode()), I(ioExceptionRetries), I(unsuccessfulResponseRetries), supportsRetry ? "can" : "cannot", request.getUrl());
                } else {
                    LOG.warn(message, I(response.getStatusCode()), I(ioExceptionRetries), I(unsuccessfulResponseRetries), supportsRetry ? "can" : "cannot", request.getUrl());
                }
            }
            return willRetry;
        }

        /** Returns true if performing the back-off was successful. */
        private boolean backOffWasSuccessful(BackOff backOff) {
            try {
                return BackOffUtils.next(sleeper, backOff);
            } catch (InterruptedException | IOException e) {
                return false;
            }
        }

        /** Returns true if the status code represents an error that should be retried. */
        private boolean retryOnStatusCode(int statusCode) {
            return (statusCode == 0) // Code 0 usually means no response / network error
                || (statusCode / 100 == 5) // 5xx: server error
                || statusCode == 429; // 429: Too many requests
        }
    }

}