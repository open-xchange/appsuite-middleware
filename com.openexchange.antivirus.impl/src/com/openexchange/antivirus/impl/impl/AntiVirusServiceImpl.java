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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.antivirus.impl.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.antivirus.AntiVirusProperty;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.icap.ICAPClient;
import com.openexchange.icap.ICAPClientFactoryService;
import com.openexchange.icap.ICAPMethod;
import com.openexchange.icap.ICAPOptions;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.OperationMode;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.java.Strings;
import com.openexchange.lock.LockService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AntiVirusServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class AntiVirusServiceImpl implements AntiVirusService {

    private static final Logger LOG = LoggerFactory.getLogger(AntiVirusServiceImpl.class);

    private final ServiceLookup services;
    private final ICAPResponseParser parser;
    private final Cache<String, AntiVirusResult> cachedResults;
    private final MetricHandler metricHandler;

    /**
     * Initialises a new {@link AntiVirusServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public AntiVirusServiceImpl(ServiceLookup services) {
        super();
        this.cachedResults = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(365, TimeUnit.DAYS).build(); //Yup, never expire, we invalidate manually
        this.services = services;
        this.parser = new ICAPResponseParser();
        this.metricHandler = new MetricHandler(services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#scan(com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure, java.lang.String, long)
     */
    @Override
    public AntiVirusResult scan(InputStreamClosure stream, String uniqueId, long contentLength) throws OXException {
        return performScan(stream, uniqueId, contentLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#scan(com.openexchange.ajax.fileholder.IFileHolder)
     */
    @Override
    public AntiVirusResult scan(IFileHolder fileHolder, String uniqueId) throws OXException {
        return performScan(() -> fileHolder.getStream(), uniqueId, fileHolder.getLength());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#scan(java.io.File)
     */
    @Override
    public AntiVirusResult scan(File file, String uniqueId, long fileSize) throws OXException {
        return performScan(() -> new FileInputStream(file), uniqueId, fileSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#scan(com.openexchange.antivirus.ManagedFile)
     */
    @Override
    public AntiVirusResult scan(ManagedFile managedFile, String uniqueId) throws OXException {
        return performScan(() -> managedFile.getInputStream(), uniqueId, managedFile.getSize());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#canStream()
     */
    @Override
    public boolean canStream() {
        LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
        return OperationMode.parse(leanConfigurationService.getProperty(AntiVirusProperty.mode)).equals(OperationMode.STREAMING);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.antivirus.AntiVirusService#isEnabled(com.openexchange.session.Session)
     */
    @Override
    public boolean isEnabled(Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (serverSession.isAnonymous()) {
            return false;
        }
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        if (capabilityService == null) {
            LOG.debug("The CapabilityService is absent.");
            return false;
        }
        CapabilitySet capabilitySet = capabilityService.getCapabilities(session);
        if (capabilitySet == null) {
            throw AntiVirusServiceExceptionCodes.CAPABILITY_DISABLED.create(session.getUserId(), session.getContextId());
        }
        if (false == capabilitySet.contains("antivirus")) {
            throw AntiVirusServiceExceptionCodes.CAPABILITY_DISABLED.create(session.getUserId(), session.getContextId());
        }
        LeanConfigurationService leanConfigService = services.getService(LeanConfigurationService.class);
        if (leanConfigService == null) {
            LOG.debug("The LeanConfigurationService is absent.");
            return false;
        }
        if (false == leanConfigService.getBooleanProperty(session.getUserId(), session.getContextId(), AntiVirusProperty.enabled)) {
            throw AntiVirusServiceExceptionCodes.ANTI_VIRUS_SERVICE_DISABLED.create(session.getUserId(), session.getContextId());
        }
        return true;
    }
    /////////////////////////////////////// HELPERS ////////////////////////////////////////

    /**
     * Performs the Anti-Virus scan for the specified InputStream by executing an {@link ICAPRequest}
     * via the {@link ICAPClient}.
     * 
     * @param stream The {@link InputStream} to scan
     * @param uniqueId The uniqueId that uniquely identifies the specified {@link InputStream}
     * @param contentLength The {@link InputStream}'s content length (or -1 if unknown)
     * @return The {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */

    private AntiVirusResult performScan(InputStreamClosure stream, String uniqueId, long contentLength) throws OXException {
        LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
        int maxFileSize = leanConfigurationService.getIntProperty(AntiVirusProperty.maxFileSize);
        long max = (long) (maxFileSize * Math.pow(1024, 2));
        if (contentLength > max) {
            throw AntiVirusServiceExceptionCodes.FILE_TOO_BIG.create(Strings.humanReadableByteCount(max, true));
        }
        String server = leanConfigurationService.getProperty(AntiVirusProperty.server);
        int port = leanConfigurationService.getIntProperty(AntiVirusProperty.port);
        String service = leanConfigurationService.getProperty(AntiVirusProperty.service);
        OperationMode mode = OperationMode.parse(leanConfigurationService.getProperty(AntiVirusProperty.mode));

        ICAPClient client = services.getService(ICAPClientFactoryService.class).getOrCreate();
        ICAPOptions options = client.getOptions(server, port, service);

        // No unique id? No caching, just scan and return
        if (Strings.isEmpty(uniqueId)) {
            return scan(stream, contentLength, server, port, service, mode, client, options);
        }

        AntiVirusResult result = cachedResults.getIfPresent(uniqueId);
        if (result != null && result.getISTag().equals(options.getIsTag())) {
            metricHandler.incrementCacheHits();
            result.setStreamScanned(false);
            return result;
        }
        metricHandler.incrementCacheMisses();

        LockService lockService = services.getService(LockService.class);
        Lock lock = lockService == null ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(uniqueId);
        lock.lock();
        try {
            // Check again to ensure nothing was changed in the meanwhile
            if (result != null && result.getISTag().equals(options.getIsTag())) {
                metricHandler.incrementCacheHits();
                return result;
            }
            if (result == null) {
                metricHandler.incrementCacheMisses();
            } else {
                // The ISTag is different, we scan again
                LOG.debug("The ISTag '{}' of the cached result of the file with uniqueId '{}' differs from the server's ISTag '{}'. Scanning again.", result.getISTag(), uniqueId, options.getIsTag());
                cachedResults.invalidate(uniqueId);
                metricHandler.incrementCacheInvalidations();
            }

            result = scan(stream, contentLength, server, port, service, mode, client, options);
            if (Strings.isNotEmpty(result.getISTag())) {
                cachedResults.put(uniqueId, result);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Performs the actual scan and (optionally) logs the trace times
     * 
     * @param stream The {@link InputStream}
     * @param contentLength The {@link InputStream}'s content length
     * @param server The ICAP server
     * @param port The ICAP server's port
     * @param service The AntiVirus service
     * @param mode The {@link OperationMode}
     * @param client The {@link ICAPClient}
     * @param options The {@link ICAPOptions}
     * @return The parsed {@link AntiVirusResult} of the scan
     */
    private AntiVirusResult scan(InputStreamClosure stream, long contentLength, String server, int port, String service, OperationMode mode, ICAPClient client, ICAPOptions options) {
        try (InputStream inputStream = stream.newStream()) {
            long start = System.currentTimeMillis();
            ICAPResponse response = client.execute(createBuilder(options, server, port, service, mode, inputStream, contentLength).build());
            logTraceInformation(start, System.currentTimeMillis(), contentLength);
            return parser.parse(response);
        } catch (IOException e) {
            LOG.error("", e);
            return new AntiVirusResultImpl.Builder().withError(AntiVirusServiceExceptionCodes.IO_ERROR.create(e, e.getMessage())).build();
        } catch (OXException e) {
            LOG.error("", e);
            return new AntiVirusResultImpl.Builder().withError(e).build();
        }
    }

    /**
     * Creates an {@link com.openexchange.icap.ICAPRequest.Builder} with the specified settings:
     * 
     * <ul>
     * <li>Server: from the configuration</li>
     * <li>Port: from the configuration</li>
     * <li>Service: from the configuration</li>
     * <li>Method: RESPMOD</li>
     * <li>Preview: if available</li>
     * <li>Allow: if available</li>
     * <li>Content-Length: if available</li>
     * </ul>
     * 
     * @return The {@link com.openexchange.icap.ICAPRequest.Builder}
     * @throws OXException if the ICAP server's options cannot be retrieved.
     */
    private ICAPRequest.Builder createBuilder(ICAPOptions options, String server, int port, String service, OperationMode mode, InputStream inputStream, long contentLength) throws OXException {
        // Base request
        ICAPRequest.Builder builder = new ICAPRequest.Builder();
        builder.withServer(server);
        builder.withPort(port);
        builder.withService(service);
        builder.withMethod(ICAPMethod.RESPMOD);
        // Headers
        long previewSize = options.getPreviewSize();
        if (mode.equals(OperationMode.DOUBLE_FETCH)) {
            if (previewSize > 0) {
                builder.withHeader(ICAPRequestHeader.PREVIEW, Long.toString(options.getPreviewSize()));
            }
            if (options.isAllow()) {
                builder.withHeader(ICAPRequestHeader.ALLOW, "204");
            }
        }
        // Body
        if (contentLength > 0 && contentLength > previewSize) {
            builder.withBodyHeader(ICAPRequestHeader.CONTENT_LENGTH, Long.toString(contentLength));
            // Maybe add 'Last-Modified' and 'Date' body headers?
        }
        builder.withBody(inputStream);
        return builder;
    }

    /**
     * Logs the runtime information
     * 
     * @param start The start time
     * @param end The end time
     * @param contentLength The content length
     */
    private void logTraceInformation(long start, long end, long contentLength) {
        if (false == LOG.isTraceEnabled()) {
            return;
        }
        Duration duration = new Duration(start, end);
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix("m ").appendSeconds().appendSuffix("s ").appendMillis().appendSuffix("ms ").toFormatter();
        String formatted = formatter.print(duration.toPeriod());
        long transferRate = (long) (contentLength / (duration.getMillis() / 1000.0));

        MDC.clear();
        LOG.trace("Completed scanning of {} in {}-- average rate {}/sec.", Strings.humanReadableByteCount(contentLength, true), formatted, Strings.humanReadableByteCount(transferRate, true));

        metricHandler.updateScansPerSecond();
        metricHandler.updateScanningTime(duration.getMillis());
        metricHandler.updateTransferRate(contentLength);
    }
}
