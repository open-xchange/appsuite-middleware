#!/usr/bin/perl
package Listresource;
use strict;
use Data::Dumper;
use base qw(BasicCommandlineOptions);


#Use the SOAP::Lite Perl module
#use SOAP::Lite +trace => 'debug';
use SOAP::Lite;

my $test = new Listresource();
$test->doRequest();

sub new {
	my ($inPkg) = @_;
	my $self = BasicCommandlineOptions->new();
	
	bless $self, $inPkg;
    return $self;
}

sub doRequest {
   	my $inSelf = shift;
    my $soap = SOAP::Lite->ns( $inSelf->{'serviceNs'} )->proxy( $inSelf->{'basisUrl'}."OXUserService" );
    
    my $pattern = SOAP::Data->value("*");
    
    my $som_entry = 
      $soap->list($inSelf->{'Context'},$pattern,$inSelf->{'creds'});
    
    if ( $som_entry->fault() ) {
        $inSelf->fault_output($som_entry);
    } else {
        my $fields = [ "Display_name", "PrimaryEmail", "PasswordMech2String", "Mailenabled", 
            "Sur_name", "Given_name", "Birthday", "Anniversary", "Branches", "Business_category", 
            "Postal_code_business", "State_business", "Street_business", "Telephone_callback", 
            "City_home", "Commercial_register", "Country_home", "Company", "Department", "Email2", 
            "Email3", "EmployeeType", "Fax_business", "Fax_home", "Fax_other", "ImapPort", "ImapServer", 
            "ImapSchema", "ImapLogin", "SmtpServer", "SmtpSchema", "SmtpPort", "Instant_messenger1", 
            "Instant_messenger2", "Telephone_ip", "Telephone_isdn", "Mail_folder_drafts_name", 
            "Mail_folder_sent_name", "Mail_folder_spam_name", "Mail_folder_trash_name", "Manager_name", 
            "Marital_status", "Cellular_telephone1", "Cellular_telephone2", "Nickname", 
            "Number_of_children", "Note", "Number_of_employee", "Telephone_pager", "Password_expired", 
            "Telephone_assistant", "Telephone_business1", "Telephone_business2", "Telephone_car", 
            "Telephone_company", "Telephone_home1", "Telephone_home2", "Telephone_other", 
            "Postal_code_home", "Profession", "Telephone_radio", "Room_number", "Sales_volume", 
            "City_other", "Country_other", "Middle_name", "Postal_code_other", "State_other", 
            "Street_other", "Spouse_name", "State_home", "Street_home", "Suffix", "Tax_id", 
            "Telephone_telex", "Timezone", "Telephone_ttytdd", "Url", "Userfield01", "Userfield02", 
            "Userfield03", "Userfield04", "Userfield05", "Userfield06", "Userfield07", "Userfield08", 
            "Userfield09", "Userfield10", "Userfield11", "Userfield12", "Userfield13", "Userfield14", 
            "Userfield15", "Userfield16", "Userfield17", "Userfield18", "Userfield19", "Userfield20", 
            "City_business", "Country_business", "Assistant_name", "Telephone_primary", "Email1", 
            "PasswordMech", "Mail_folder_confirmed_ham_name", "Mail_folder_confirmed_spam_name", 
            "GUI_Spam_filter_capabilities_enabled", "Spam_filter_enabled", "DefaultSenderAddress", 
            "Name", "Language", "Id", "Info", "Title", "Position", "Password", "Aliases", "Categories", 
            "access-calendar", "access-contacts", "access-delegate-tasks", "access-edit-public-folder", 
            "access-forum", "access-ical", "access-infostore", "access-pinboard-write", 
            "access-projects", "access-read-create-shared-Folders", "access-rss-bookmarks", 
            "access-rss-portal", "access-syncml", "access-tasks", "access-vcard", "access-webdav", 
            "access-webdav-xml", "access-webmail" ]; 
        my $fieldnames;
        
        for my $field (@$fields) {
            push (@$fieldnames, lcfirst($field));
        }
        
        my @results = $som_entry->paramsall;

        my $som_entry2 = 
          $soap->getData($inSelf->{'Context'},@results,$inSelf->{'creds'});
        
        my @data = $inSelf->SUPER::fetch_results($fieldnames, \@results);
        
        $inSelf->doCSVOutput($fields, \@data);
    }

}

exit;

