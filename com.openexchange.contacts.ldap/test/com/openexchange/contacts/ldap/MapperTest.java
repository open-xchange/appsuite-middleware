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
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;


public class MapperTest extends Mapper {

    private Mappings mappingsFromProperties;
    
    @Before
    public void setUp() throws LdapConfigurationException, FileNotFoundException, IOException {
        final Properties props = new Properties();
        final String mappingfile = "etc/contacts-ldap/mapping.ads.properties.example";
        props.load(new FileInputStream(mappingfile));
        this.mappingsFromProperties = Mappings.getMappingsFromProperties(props, PropertyHandler.bundlename + "mapping.ads", mappingfile);
    }
    
    @Test
    public void testCommonPartsBothDates() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsCreationDateOnly() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsModifiedDateOnly() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsNoDates() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsLastModifiedOnlyColOnly() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsLastModifiedMissingColOnly() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsCreationOnlyColOnly() throws LdapException, LdapConfigurationException {
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
    public void testCommonPartsCreationMissingColOnly() throws LdapException, LdapConfigurationException {
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
            
            public String getObjectFullName() throws LdapException {
                return "Test";
            }
            
            public List<String> getMultiValueAttribute(String attributename) throws LdapException {
                return null;
            }
            
            public LdapGetter getLdapGetterForDN(String dn, String[] attributes) throws LdapException {
                return null;
            }
            
            public int getIntAttribute(String attributename) throws LdapException {
                return 0;
            }
            
            public Date getDateAttribute(String attributename) throws LdapException {
                if (mappingsFromProperties.getCreationdate().equals(attributename)) {
                    return creationdate;
                }
                if (mappingsFromProperties.getLastmodified().equals(attributename)) {
                    return modifieddate;
                }
                return null;
            }
            
            public String getAttribute(String attributename) throws LdapException {
                return null;
            }
        };
    }
}
