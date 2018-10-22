
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.OXTestToolkit;

/**
 *
 * {@link Bug4409Test}
 *
 * @author Offspring
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - clean-up added
 *
 */
public class Bug4409Test extends AbstractContactTest {

    @Test
    public void testBug4409() throws Exception {
        /*
         * insert contact with image
         */
        Contact contact = createContactObject(UUID.randomUUID().toString());
        contact.setImage1(image);
        contact.setImageContentType(CONTENT_TYPE);
        contact.setEmail1("Bug4409Test@example.org");
        contact = cotm.newAction(contact);
        try {
            /*
             * get & check contact image via url
             */
            GetResponse getResponse = getClient().execute(new GetRequest(contact, tz));
            String imageUrl = getResponse.getImageUrl();
            assertNotNull(imageUrl);
            byte[] imageData = loadImageByURL(getClient(), imageUrl);
            OXTestToolkit.assertImageBytesEqualsAndNotNull("Image data wrong", image, imageData);
            /*
             * remove contact image
             */
            contact.setImage1(null);
            contact.setImageContentType(null);
            contact = cotm.updateAction(contact);
            /*
             * get & check contact image via url
             */
            getResponse = getClient().execute(new GetRequest(contact, tz));
            assertNull(getResponse.getImageUrl());

            Thread.sleep(2000); // give some time for the resource cache invalidation
            /*
             * try to access previous image location
             */
            try {
                imageData = loadImageByURL(getClient(), imageUrl, true);
                assertTrue("Image data still present", null == imageData || 0 == imageData.length);
            } catch (Exception e) {
                // also okay
            }
        } finally {
            cotm.deleteAction(contact);
        }
    }
}
