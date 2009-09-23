package com.openexchange.contacts.ldap.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
import com.openexchange.contacts.ldap.contacts.LdapContactInterface.Order;
import com.openexchange.contacts.ldap.contacts.LdapContactInterface.SortInfo;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.contacts.ldap.property.FolderProperties.Sorting;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;


public class LdapJNDIImpl implements LdapInterface {

    /**
     * This class is used to be able to view deleted object in an Active Directory
     *
     * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
     *
     */
    private class DeletedControl implements Control {
    
        /**
         * For serialization
         */
        private static final long serialVersionUID = -3548239536056697658L;
    
        public byte[] getEncodedValue() {
            return new byte[] {};
        }
    
        public String getID() {
            return "1.2.840.113556.1.4.417";
        }
    
        public boolean isCritical() {
            return true;
        }
    }

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapJNDIImpl.class);
    
    private final FolderProperties folderprop;
    
    private final LdapContext context;
    
    private final boolean deleted;
    
    public LdapJNDIImpl(final String login, final String password, final FolderProperties folderprop, final boolean deleted, final boolean distributionlist, final SortInfo sortField) throws LdapException {
        this.folderprop = folderprop;
        this.deleted = deleted;
        try {
            this.context = LdapUtility.createContext(login, password, folderprop);
        } catch (final NamingException e1) {
            LOG.error(e1.getMessage(), e1);
            throw new LdapException(Code.INITIAL_LDAP_ERROR, e1.getMessage());
        }
        // TODO Implement right check if server supports pagedResults
//      final boolean pagedResultControlSupported = isPagedResultControlSupported(context);
//      if (!pagedResultControlSupported) {
//          System.out.println("Paged results are not supported");
//      }

        try {
            this.context.setRequestControls(getControls(sortField, distributionlist, folderprop.getPagesize()));
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
        
    }
    
    public void search(final String baseDN, final String filter, final boolean distributionslist, final Set<Integer> columns, final FillClosure closure) throws LdapException {
        final String defaultNamingContext;
        if (deleted) {
            defaultNamingContext = getDefaultNamingContext(context);
        } else {
            defaultNamingContext = null;
        }

        final SearchControls searchControls;
        if (distributionslist) {
            searchControls = getSearchControlDistri(columns, deleted);
        } else {
            searchControls = getSearchControl(columns);
        }

        final String ownBaseDN;
        final String ownFilter;
        if (deleted) {
            ownBaseDN = defaultNamingContext;
            ownFilter = "(&" + filter + "(isDeleted=TRUE))";
        } else {
            ownBaseDN = baseDN;
            ownFilter = filter;
        }
        
        try {
            byte[] cookie = null;
            do {
                final NamingEnumeration<SearchResult> search = context.search(ownBaseDN, ownFilter, searchControls);
                while (null != search && search.hasMoreElements()) {
                    final SearchResult next = search.next();
                    final Attributes attributes = next.getAttributes();
                    final LdapGetter ldapGetter = getLdapGetter(attributes, context, next.getNameInNamespace());
                    closure.execute(ldapGetter);
                }
                // Examine the paged results control response 
                final Control[] controls = context.getResponseControls();
                if (controls != null) {
                    for (int i = 0; i < controls.length; i++) {
                        if (controls[i] instanceof PagedResultsResponseControl) {
                            final PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                            cookie = prrc.getCookie();
                        }
                    }
                }
                // Re-activate paged results
                context.setRequestControls(new Control[] { new PagedResultsControl(this.folderprop.getPagesize(), cookie, Control.CRITICAL) });
            } while (null != cookie);
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }

    }
    
    private Control[] getControls(final SortInfo sortField, final boolean distributionlist, final int pagesize) throws IOException, NamingException {
        if (this.deleted) {
            return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL), new DeletedControl() };
        } else {
            if (null != sortField && folderprop.getSorting().equals(Sorting.server)) {
                final SortKey sortKey = new SortKey(getFieldFromColumn(sortField.getField(), distributionlist), sortField.getSort().equals(Order.asc), null);
                final SortKey[] sortKeyArray = new SortKey[] { sortKey };
                return new Control[] { new SortControl(sortKeyArray, Control.CRITICAL), new PagedResultsControl(pagesize, Control.CRITICAL) };
            } else {
                return new Control[] { new PagedResultsControl(pagesize, Control.CRITICAL) };
            }
        }
    }

    private SearchControls getSearchControl(final Set<Integer> columns) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(LdapUtility.getSearchControl(folderprop.getSearchScope()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, false);
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

    private SearchControls getSearchControlDistri(final Set<Integer> columns, boolean deleted) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(LdapUtility.getSearchControl(folderprop.getSearchScopeDistributionlist()));
        searchControls.setCountLimit(0);
        final List<String> array = getAttributes(columns, true);
        if (!deleted) {
            array.add("member");
        }
        searchControls.setReturningAttributes(array.toArray(new String[array.size()]));
        return searchControls;
    }

    private LdapGetter getLdapGetter(final Attributes attributes, final LdapContext context, final String objectfullname) {
        return new LdapGetterJNDIImpl(attributes, context, objectfullname);
    }

    private List<String> getAttributes(final Set<Integer> columns, boolean distributionlist) {
        final List<String> retval = new ArrayList<String>();
        for (final Integer col : columns) {
            final String fieldFromColumn = getFieldFromColumn(col, distributionlist);
            if (null != fieldFromColumn && fieldFromColumn.length() != 0) {
                retval.add(fieldFromColumn);
            }
        }
        return retval;
    }

    /**
     * @param col
     * @param distributionlist TODO
     * @param instance
     * @return a String value or null if no corresponding entry was found
     */
    private String getFieldFromColumn(final Integer col, final boolean distributionlist) {
        final Mappings mappings = folderprop.getMappings();
        switch (col) {
        case Contact.ANNIVERSARY:
            return mappings.getAnniversary();
        case Contact.ASSISTANT_NAME:
            return mappings.getAssistant_name();
        case Contact.BIRTHDAY:
            return mappings.getBirthday();
        case Contact.BRANCHES:
            return mappings.getBranches();
        case Contact.BUSINESS_CATEGORY:
            return mappings.getBusiness_category();
        case Contact.CELLULAR_TELEPHONE1:
            return mappings.getCellular_telephone1();
        case Contact.CELLULAR_TELEPHONE2:
            return mappings.getCellular_telephone2();
        case Contact.CITY_BUSINESS:
            return mappings.getCity_business();
        case Contact.CITY_HOME:
            return mappings.getCity_home();
        case Contact.CITY_OTHER:
            return mappings.getCity_other();
        case Contact.COMMERCIAL_REGISTER:
            return mappings.getCommercial_register();
        case Contact.COMPANY:
            return mappings.getCompany();
        case Contact.COUNTRY_BUSINESS:
            return mappings.getCountry_business();
        case Contact.COUNTRY_HOME:
            return mappings.getCountry_home();
        case Contact.COUNTRY_OTHER:
            return mappings.getCountry_other();
        case Contact.DEFAULT_ADDRESS:
            return mappings.getDefaultaddress();
        case Contact.DEPARTMENT:
            return mappings.getDepartment();
        case Contact.DISPLAY_NAME:
            if (distributionlist) {
                return mappings.getDistributionlistname();
            } else {
                return mappings.getDisplayname();
            }
        case Contact.EMAIL1:
            return mappings.getEmail1();
        case Contact.EMAIL2:
            return mappings.getEmail2();
        case Contact.EMAIL3:
            return mappings.getEmail3();
        case Contact.EMPLOYEE_TYPE:
            return mappings.getEmployeetype();
        case Contact.FAX_BUSINESS:
            return mappings.getFax_business();
        case Contact.FAX_HOME:
            return mappings.getFax_home();
        case Contact.FAX_OTHER:
            return mappings.getFax_other();
        case Contact.GIVEN_NAME:
            return mappings.getGivenname();
        case Contact.INFO:
            return mappings.getInfo();
        case Contact.INSTANT_MESSENGER1:
            return mappings.getInstant_messenger1();
        case Contact.INSTANT_MESSENGER2:
            return mappings.getInstant_messenger2();
        case Contact.MANAGER_NAME:
            return mappings.getManager_name();
        case Contact.MARITAL_STATUS:
            return mappings.getMarital_status();
        case Contact.MIDDLE_NAME:
            return mappings.getMiddle_name();
        case Contact.NICKNAME:
            return mappings.getNickname();
        case Contact.NOTE:
            return mappings.getNote();
        case Contact.NUMBER_OF_CHILDREN:
            return mappings.getNumber_of_children();
        case Contact.NUMBER_OF_EMPLOYEE:
            return mappings.getNumber_of_employee();
        case DataObject.OBJECT_ID:
            if (distributionlist) {
                return mappings.getDistributionuid();
            } else {
                return mappings.getUniqueid();
            }
        case Contact.POSITION:
            return mappings.getPosition();
        case Contact.POSTAL_CODE_BUSINESS:
            return mappings.getPostal_code_business();
        case Contact.POSTAL_CODE_HOME:
            return mappings.getPostal_code_home();
        case Contact.POSTAL_CODE_OTHER:
            return mappings.getPostal_code_other();
        case Contact.PROFESSION:
            return mappings.getProfession();
        case Contact.ROOM_NUMBER:
            return mappings.getRoom_number();
        case Contact.SALES_VOLUME:
            return mappings.getSales_volume();
        case Contact.SPOUSE_NAME:
            return mappings.getSpouse_name();
        case Contact.STATE_BUSINESS:
            return mappings.getState_business();
        case Contact.STATE_HOME:
            return mappings.getState_home();
        case Contact.STATE_OTHER:
            return mappings.getState_other();
        case Contact.STREET_BUSINESS:
            return mappings.getStreet_business();
        case Contact.STREET_HOME:
            return mappings.getStreet_home();
        case Contact.STREET_OTHER:
            return mappings.getStreet_other();
        case Contact.SUFFIX:
            return mappings.getSuffix();
        case Contact.SUR_NAME:
            if (distributionlist) {
                return mappings.getDistributionlistname();
            } else {
                return mappings.getSurname();
            }
        case Contact.TAX_ID:
            return mappings.getTax_id();
        case Contact.TELEPHONE_ASSISTANT:
            return mappings.getTelephone_assistant();
        case Contact.TELEPHONE_BUSINESS1:
            return mappings.getTelephone_business1();
        case Contact.TELEPHONE_BUSINESS2:
            return mappings.getTelephone_business2();
        case Contact.TELEPHONE_CALLBACK:
            return mappings.getTelephone_callback();
        case Contact.TELEPHONE_CAR:
            return mappings.getTelephone_car();
        case Contact.TELEPHONE_COMPANY:
            return mappings.getTelephone_company();
        case Contact.TELEPHONE_HOME1:
            return mappings.getTelephone_home1();
        case Contact.TELEPHONE_HOME2:
            return mappings.getTelephone_home2();
        case Contact.TELEPHONE_IP:
            return mappings.getTelephone_ip();
        case Contact.TELEPHONE_ISDN:
            return mappings.getTelephone_isdn();
        case Contact.TELEPHONE_OTHER:
            return mappings.getTelephone_other();
        case Contact.TELEPHONE_PAGER:
            return mappings.getTelephone_pager();
        case Contact.TELEPHONE_PRIMARY:
            return mappings.getTelephone_primary();
        case Contact.TELEPHONE_RADIO:
            return mappings.getTelephone_radio();
        case Contact.TELEPHONE_TELEX:
            return mappings.getTelephone_telex();
        case Contact.TELEPHONE_TTYTDD:
            return mappings.getTelephone_ttytdd();
        case Contact.TITLE:
            return mappings.getTitle();
        case Contact.URL:
            return mappings.getUrl();
        case Contact.USERFIELD01:
            return mappings.getUserfield01();
        case Contact.USERFIELD02:
            return mappings.getUserfield02();
        case Contact.USERFIELD03:
            return mappings.getUserfield03();
        case Contact.USERFIELD04:
            return mappings.getUserfield04();
        case Contact.USERFIELD05:
            return mappings.getUserfield05();
        case Contact.USERFIELD06:
            return mappings.getUserfield06();
        case Contact.USERFIELD07:
            return mappings.getUserfield07();
        case Contact.USERFIELD08:
            return mappings.getUserfield08();
        case Contact.USERFIELD09:
            return mappings.getUserfield09();
        case Contact.USERFIELD10:
            return mappings.getUserfield10();
        case Contact.USERFIELD11:
            return mappings.getUserfield11();
        case Contact.USERFIELD12:
            return mappings.getUserfield12();
        case Contact.USERFIELD13:
            return mappings.getUserfield13();
        case Contact.USERFIELD14:
            return mappings.getUserfield14();
        case Contact.USERFIELD15:
            return mappings.getUserfield15();
        case Contact.USERFIELD16:
            return mappings.getUserfield16();
        case Contact.USERFIELD17:
            return mappings.getUserfield17();
        case Contact.USERFIELD18:
            return mappings.getUserfield18();
        case Contact.USERFIELD19:
            return mappings.getUserfield19();
        case Contact.USERFIELD20:
            return mappings.getUserfield20();
        case DataObject.LAST_MODIFIED:
            return mappings.getLastmodified();
        case DataObject.CREATION_DATE:
            return mappings.getCreationdate();
        default:
            return null;
        }
    }

    private static String getDefaultNamingContext(final LdapContext ctx) throws LdapException {
        try {
            SearchControls searchCtls2 = new SearchControls();
            searchCtls2.setSearchScope(SearchControls.OBJECT_SCOPE);
            searchCtls2.setReturningAttributes(new String[]{"defaultnamingcontext"});
            final NamingEnumeration<SearchResult> search = ctx.search("", "(objectclass=*)", searchCtls2);
            while (search.hasMoreElements()) {
                SearchResult sr = (SearchResult)search.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    for (NamingEnumeration<?> ae = attrs.getAll();ae.hasMoreElements();) {
                        Attribute attr = (Attribute)ae.next();
                        if (null != attr) {
                            for (NamingEnumeration<?> e = attr.getAll(); e.hasMore();) {
                                final Object next = e.next();
                                if (null != next) {
                                    return next.toString();
                                }
                            }
                        }
                    }
                } 
            }
            throw new LdapException(Code.ERROR_GETTING_DEFAULT_NAMING_CONTEXT);
        } catch (final NamingException e) {
            throw new LdapException(Code.ERROR_GETTING_DEFAULT_NAMING_CONTEXT, e);
        }
    }


    public void close() throws LdapException {
        try {
            this.context.close();
        } catch (final NamingException e) {
            LOG.error(e.getMessage(), e);
            throw new LdapException(Code.ERROR_GETTING_ATTRIBUTE, e.getMessage());
        }
    }

}
