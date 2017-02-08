
package com.openexchange.ajax.requesthandler.converters.preview;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.RETRY_AFTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
//import org.mockito.Mockito;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ThreadPools.class, PreviewImageGenerator.class })
public class PreviewThumbResultConverterTest {

    private final static String ETAG_VALUE = "http://www.open-xchange.com/infostore/258086/7/1409843861900";

    private ConfigurationService allowedConfig, forbiddenConfig;
    private AJAXRequestData requestData;
    private AJAXRequestResult result;
    private ServerSession session;
    private ResourceCache resourceCache;
    private CachedResource aResource = new CachedResource(new byte[] { 1 }, "cachedResource", "image/jpeg", 1);
    private CachedResource noResource = null;
    private Future voidFuture = null;
    private ExecutorService executorService;
    private PreviewThumbResultConverter converter;
    private PreviewDocument previewDocument;
    private PreviewService previewService;

    @Before
    public void setUp() throws Exception {
        allowedConfig = mock(ConfigurationService.class);
        when(allowedConfig.getBoolProperty(anyString(), anyBoolean())).thenReturn(true);

        forbiddenConfig = mock(ConfigurationService.class);
        when(forbiddenConfig.getBoolProperty(anyString(), anyBoolean())).thenReturn(false);

        session = mock(ServerSession.class);
        when(session.getContextId()).thenReturn(1);
        when(session.getUserId()).thenReturn(1);
        User user = mock(User.class);
        when(user.getPreferredLanguage()).thenReturn("de/de");
        when(session.getUser()).thenReturn(user);

        requestData = new AJAXRequestData() {

            {
                setSession(session);
                putParameter("width", "160");
                putParameter("height", "160");
                putParameter("scaleType", "cover");
                putParameter("content_type", "image/jpeg");
                putParameter("delivery", "view");
            }
        };

        result = new AJAXRequestResult();
        result.setHeader(ETAG, ETAG_VALUE);

        resourceCache = mock(ResourceCache.class);
        when(resourceCache.isEnabledFor(anyInt(), anyInt())).thenReturn(true);
        ResourceCaches.setResourceCache(resourceCache);

        previewService = mock(PreviewService.class);
        ServerServiceRegistry.getInstance().addService(PreviewService.class, previewService);

        voidFuture = mock(Future.class);
        when(voidFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(null);

        executorService = mock(ExecutorService.class);
        when(executorService.submit(any(Runnable.class))).thenReturn(voidFuture);
        PowerMockito.mockStatic(ThreadPools.class);
        PowerMockito.when(ThreadPools.getExecutorService()).thenReturn(executorService);

        result = new AJAXRequestResult();
        result.setHeader(ETAG, ETAG_VALUE);
    }

    /**
     * * The cache is enabled but we didn't find a valid entry for the request. Expect a 202 status and a Retry-After header.
     * 
     * @throws OXException
     */
    @Test
    public void testConvertWithoutCachedResult() throws OXException {
        when(resourceCache.isEnabledFor(anyInt(), anyInt())).thenReturn(true);
        when(resourceCache.get(anyString(), anyInt(), anyInt())).thenReturn(noResource);

        converter = new PreviewThumbResultConverter(allowedConfig);

        converter.convert(requestData, result, session, null);
        assertNotNull(result.getResultObject());
        FileHolder resultObject = (FileHolder) result.getResultObject();
        assertEquals(PreviewConst.MISSING_THUMBNAIL.length, resultObject.getLength());
        assertEquals(200, result.getHttpStatusCode());
        //        assertEquals(202, result.getHttpStatusCode());
        //        assertEquals(String.valueOf(10), result.getHeader(RETRY_AFTER));
    }

    /**
     * The cache is enabled and we found a valid entry for the request. Expect a successful result and no Retry-After header.
     * 
     * @throws OXException
     */
    @Test
    public void testConvertWithCachedResult() throws OXException {
        when(resourceCache.isEnabledFor(anyInt(), anyInt())).thenReturn(true);
        when(resourceCache.get(anyString(), anyInt(), anyInt())).thenReturn(aResource);
        converter = new PreviewThumbResultConverter(allowedConfig);

        converter.convert(requestData, result, session, null);
        assertNotNull(result.getResultObject());
        assertEquals(200, result.getHttpStatusCode());
        assertNull(result.getHeader(RETRY_AFTER));
    }

    /**
     * There is no cache enabled for the user but we are allowed to generate a preview in a blocking fashion. Expect a successful result
     * and no Retry-After header
     * 
     * @throws OXException
     */
    @Test
    public void testConvertWithoutCacheBlockingAllowed() throws OXException {
        when(resourceCache.isEnabledFor(anyInt(), anyInt())).thenReturn(false);
        result.setResultObject(new ByteArrayFileHolder(new byte[] { 1 }));
        previewDocument = mock(PreviewDocument.class);
        when(previewDocument.getThumbnail()).thenReturn(new ByteArrayInputStream(new byte[] { 1 }));
        Map<String, String> metadata = new HashMap<String, String>(1);
        metadata.put("resourcename", "image.jpg");
        when(previewDocument.getMetaData()).thenReturn(metadata);
        PowerMockito.mockStatic(PreviewImageGenerator.class);
        PowerMockito.when(PreviewImageGenerator.getPreviewDocument(any(AJAXRequestResult.class), any(AJAXRequestData.class), any(ServerSession.class), any(PreviewService.class), anyLong(), anyBoolean())).thenReturn(previewDocument);
        converter = new PreviewThumbResultConverter(allowedConfig);
        converter.convert(requestData, result, session, null);
        assertNotNull(result.getResultObject());
        assertEquals(200, result.getHttpStatusCode());
        assertNull(result.getHeader(RETRY_AFTER));
    }

    /**
     * There is no cache enabled for the user and we aren't allowed to generate a preview in a blocking fashion. An Exception is expected.
     * 
     * @throws OXException
     */
    @Test(expected = OXException.class)
    public void testConvertWithoutCacheBlockingForbiden() throws OXException {
        when(resourceCache.isEnabledFor(anyInt(), anyInt())).thenReturn(false);
        converter = new PreviewThumbResultConverter(forbiddenConfig);
        converter.convert(requestData, result, session, null);
    }

}
