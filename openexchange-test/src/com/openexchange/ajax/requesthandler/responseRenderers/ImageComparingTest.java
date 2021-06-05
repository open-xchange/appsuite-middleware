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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import javax.servlet.sim.ByteArrayServletOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.helper.ImageUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DefaultDispatcherPrefixService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.internal.ManagedFileManagementImpl;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.imagetransformation.java.impl.JavaImageTransformationProvider;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.image.WrappingImageTransformationService;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link ImageComparingTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ com.openexchange.imagetransformation.java.osgi.Services.class })
@PowerMockIgnore({ "javax.imageio.*" })
public class ImageComparingTest {

    @Mock
    private SimConfigurationService simConfigurationService;

    @Mock
    private TimerService timerService;

    @Mock
    private ManagedFileManagement simManagedFileManagement;

    private final String TEST_DATA_DIR = "testconf/";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(com.openexchange.imagetransformation.java.osgi.Services.class);

        ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());

        final SimConfigurationService simConfigurationService = new SimConfigurationService();
        simConfigurationService.stringProperties.put("UPLOAD_DIRECTORY", "/tmp/");
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, simConfigurationService);
        simManagedFileManagement = new ManagedFileManagementImpl(timerService, new DefaultDispatcherPrefixService("/ajax/"));
        ServerServiceRegistry.getInstance().addService(ManagedFileManagement.class, simManagedFileManagement);
        PowerMockito.when(com.openexchange.imagetransformation.java.osgi.Services.getService(TimerService.class)).thenReturn(timerService);
    }

    @After
    public void tearDown() {
        ServerServiceRegistry.getInstance().removeService(HtmlService.class);
    }

    @Test
    public void testRotationSuccess_Bug26630() {
        try {
            final File fileInput = new File(TEST_DATA_DIR, "Rotate_90CW.jpg");

            final FileHolder fileHolder = new FileHolder(fileInput);
            {
                fileHolder.setContentType("image/jpg");
                fileHolder.setName(fileInput.getName());
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
            }
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("cache", "false");
                requestData.setAction("attachment");
                requestData.putParameter("delivery", "view");
            }

            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            requestData.setHttpServletResponse(resp);
            ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            final byte[] bytesCurrent = servletOutputStream.toByteArray();

            // Converts byte streams to buffered images
            InputStream inOutput = new ByteArrayInputStream(bytesCurrent);
            BufferedImage outputImage = ImageIO.read(inOutput);

            // check image is rotated
            boolean transformed = false;
            float averageAreaTop = 0; // should contain the black area
            float averageAreaBottom = 0; // should contain the white area
            for (int i = 0; i < outputImage.getHeight(); i++) {
                int red = new Color(outputImage.getRGB(50, i)).getRed();
                int green = new Color(outputImage.getRGB(50, i)).getGreen();
                int blue = new Color(outputImage.getRGB(50, i)).getBlue();
                if (i < 50) {
                    averageAreaTop += (red + green + blue) / 3.0f;
                } else {
                    averageAreaBottom += (red + green + blue) / 3.0f;
                }
            }
            averageAreaBottom /= 50;
            averageAreaTop /= 50;

            // some tolerance because of compression
            if (averageAreaTop < 1 && averageAreaBottom > 253) {
                transformed = true;
            }

            assertTrue("Rotation of image not succesfull", transformed);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testAlphaChannelShouldNotGetTransformed_Bug28163() {
        try {
            final File fileInput = new File(TEST_DATA_DIR, "28163.jpg");

            final FileHolder fileHolder = new FileHolder(fileInput);
            {
                fileHolder.setContentType("image/jpg");
                fileHolder.setName(fileInput.getName());
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
            }
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("cache", "false");
                requestData.putParameter("delivery", "view");
            }

            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            requestData.setHttpServletResponse(resp);
            ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            final byte[] bytesCurrent = servletOutputStream.toByteArray();

            // Converts byte streams to buffered images
            InputStream inOriginal = new FileInputStream(fileInput);
            BufferedImage originalImage = ImageIO.read(inOriginal);
            InputStream inAfter = new ByteArrayInputStream(bytesCurrent);
            BufferedImage currentImage = ImageIO.read(inAfter);

            float expectedHistogramValue = ImageComparingTools.meanHistogramRGBValue(originalImage);
            float currentHistogramValue = ImageComparingTools.meanHistogramRGBValue(currentImage);

            // histogram should not differ too much +- 0,5
            assertValueInRange(currentHistogramValue, expectedHistogramValue - 0.5f, expectedHistogramValue + 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testGIFPictureIsAnimated_Bug29072() {
        try {
            final File fileInput = new File(TEST_DATA_DIR, "29072.gif");

            final FileHolder fileHolder = new FileHolder(fileInput);
            {
                fileHolder.setContentType("image/gif");
                fileHolder.setName(fileInput.getName());
                fileHolder.setDelivery("view");
                fileHolder.setDisposition("inline");
            }
            final AJAXRequestData requestData = new AJAXRequestData();
            {
                requestData.setSession(new SimServerSession(1, 1));
                requestData.putParameter("cache", "false");
                requestData.putParameter("delivery", "view");
            }

            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            final SimHttpServletRequest req = new SimHttpServletRequest();
            final SimHttpServletResponse resp = new SimHttpServletResponse();
            requestData.setHttpServletResponse(resp);
            ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
            resp.setOutputStream(servletOutputStream);
            final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
            fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
            fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
            final byte[] bytesCurrent = servletOutputStream.toByteArray();

            // Converts byte streams to buffered images
            InputStream inAfter = new ByteArrayInputStream(bytesCurrent);
            assertTrue("Animated GIF image not animated anymore", ImageUtils.isAnimatedGif (inAfter));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testReadImageWithCMYKProfile_Bug28082() {
        final File file = new File(TEST_DATA_DIR, "28082.jpg");
        final FileHolder fileHolder = new FileHolder(file);
        {
            fileHolder.setContentType("image/jpg");
            fileHolder.setDelivery("view");
            fileHolder.setDisposition("inline");
            fileHolder.setName(file.getName());
        }
        final AJAXRequestData requestData = new AJAXRequestData();
        {
            requestData.setSession(new SimServerSession(1, 1));
            requestData.putParameter("cache", "false");
            requestData.putParameter("delivery", "view");
        }
        final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        final SimHttpServletRequest req = new SimHttpServletRequest();
        final SimHttpServletResponse resp = new SimHttpServletResponse();
        requestData.setHttpServletResponse(resp);
        ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream();
        resp.setOutputStream(servletOutputStream);
        final FileResponseRenderer fileResponseRenderer = new FileResponseRenderer();
        fileResponseRenderer.setScaler(new WrappingImageTransformationService(new JavaImageTransformationProvider()));
        fileResponseRenderer.writeFileHolder(fileHolder, requestData, result, req, resp);
        assertNull("Got exception: " + resp.getStatusMessage(), resp.getStatusMessage());
    }

    /**
     * Asserts that a value is in range between rangeMin and rangeMax
     *
     * @param value the value to be tested
     * @param rangeMin the absolute minimum range
     * @param rangeMax the absolute maximum range
     */
    private void assertValueInRange(float value, float rangeMin, float rangeMax) {
        boolean inRange = (rangeMin <= value && value <= rangeMax);
        assertTrue("Range differs too much. Expect values between: " + rangeMin + " and " + rangeMax + "--> Got: " + value, inRange);
    }
}
