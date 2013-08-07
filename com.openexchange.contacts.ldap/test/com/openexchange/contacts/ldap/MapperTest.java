package com.openexchange.contacts.ldap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contacts.ldap.contacts.Mapper;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;


public class MapperTest extends Mapper {

    private Mappings mappingsFromProperties;

    @Before
    public void setUp() throws OXException, FileNotFoundException, IOException {
        final Properties props = new Properties();
        final String mappingfile = "etc/contacts-ldap/mapping.ads.properties.example";
        props.load(new FileInputStream(mappingfile));
        this.mappingsFromProperties = Mappings.getMappingsFromProperties(props, PropertyHandler.bundlename + "mapping.ads", mappingfile);
    }

    @Test
    public void testCommonPartsBothDates() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);
        cols.add(DataObject.LAST_MODIFIED);

        final Date creationdate = new Date(4000);

        final Date modifieddate = new Date(8000);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, creationdate, modifieddate);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", creationdate, contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", modifieddate, contact.getLastModified());
        LOG.info("End of test testCommonPartsBothDates");
    }

    @Test
    public void testCommonPartsCreationDateOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);
        cols.add(DataObject.LAST_MODIFIED);

        final Date creationdate = new Date(4000);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, creationdate, null);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", creationdate, contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", creationdate, contact.getLastModified());
        LOG.info("End of test testCommonPartsCreationDateOnly");
    }

    @Test
    public void testCommonPartsModifiedDateOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);
        cols.add(DataObject.LAST_MODIFIED);

        final Date modifieddate = new Date(8000);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, null, modifieddate);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", new Date(1000), contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", modifieddate, contact.getLastModified());
        LOG.info("End of test testCommonPartsModifiedDateOnly");
    }

    @Test
    public void testCommonPartsNoDates() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);
        cols.add(DataObject.LAST_MODIFIED);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, null, null);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", new Date(1000), contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", new Date(1000), contact.getLastModified());
        LOG.info("End of test testCommonPartsNoDates");
    }

    @Test
    public void testCommonPartsLastModifiedOnlyColOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.LAST_MODIFIED);

        final Date modifiedDate = new Date(4000);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, null, modifiedDate);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", null, contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", modifiedDate, contact.getLastModified());
        LOG.info("End of test testCommonPartsLastModifiedOnlyColOnly");
    }

    @Test
    public void testCommonPartsLastModifiedMissingColOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.LAST_MODIFIED);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, null, null);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", null, contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", new Date(1000), contact.getLastModified());
        LOG.info("End of test testCommonPartsLastModifiedMissingColOnly");
    }

    @Test
    public void testCommonPartsCreationOnlyColOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);

        final Date creationDate = new Date(2000);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, creationDate, null);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", creationDate, contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", null, contact.getLastModified());
        LOG.info("End of test testCommonPartsCreationOnlyColOnly");
    }

    @Test
    public void testCommonPartsCreationMissingColOnly() throws OXException, OXException {
        final Set<Integer> cols = new HashSet<Integer>();
        cols.add(DataObject.CREATION_DATE);

        final Contact contact = new Contact();

        final LdapGetter getter = getLdapGetter(mappingsFromProperties, null, null);
        commonParts(cols, -1, -1, contact, mappingsFromProperties, getter);
        Assert.assertEquals("Creationdate not the same", new Date(1000), contact.getCreationDate());
        Assert.assertEquals("Last modified date not the same", null, contact.getLastModified());
        LOG.info("End of test testCommonPartsCreationMissingColOnly");
    }

    private LdapGetter getLdapGetter(final Mappings mappingsFromProperties, final Date creationdate, final Date modifieddate) {
        return new LdapGetter() {

            @Override
            public String getObjectFullName() throws OXException {
                return "Test";
            }

            @Override
            public List<String> getMultiValueAttribute(String attributename) throws OXException {
                return null;
            }

            @Override
            public LdapGetter getLdapGetterForDN(String dn, String[] attributes) throws OXException {
                return null;
            }

            @Override
            public int getIntAttribute(String attributename) throws OXException {
                return 0;
            }

            @Override
            public Date getDateAttribute(String attributename) throws OXException {
                if (mappingsFromProperties.getCreationdate().equals(attributename)) {
                    return creationdate;
                }
                if (mappingsFromProperties.getLastmodified().equals(attributename)) {
                    return modifieddate;
                }
                return null;
            }

            @Override
            public String getAttribute(String attributename) throws OXException {
                return null;
            }
        };
    }
}
