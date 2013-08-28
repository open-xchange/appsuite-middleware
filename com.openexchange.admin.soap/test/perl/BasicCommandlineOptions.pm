package BasicCommandlineOptions;
use Data::Dumper;
use strict;

our @ENV_OPTIONS = ( "RMI_HOSTNAME", "COMMANDLINE_TIMEZONE", "COMMANDLINE_DATEFORMAT");

sub new {
	my ($inPkg) = @_;
	my $self = {};
	my $shost = $ENV{"SOAPHOST"} || "127.0.0.1";
	my $mpw = $ENV{"MASTERPW"} || "secret";
	$self->{'basisUrl'} = "http://$shost/webservices/";
	$self->{'serviceNs'} = "http://soap.admin.openexchange.com";
	$self->{'Context'} = SOAP::Data->type("Context")->value(
			     \SOAP::Data->value(
                              SOAP::Data->name("id" => "424242"),
			      SOAP::Data->name("name" => "testcontext"),
			      SOAP::Data->name("maxQuota" => 5555)));
	$self->{'creds'} = SOAP::Data->type("Credentials")->value(
			     \SOAP::Data->value(
                              SOAP::Data->name("login" => "oxadmin"),
                              SOAP::Data->name("password" => "$mpw")));
	$self->{'mastercreds'} = SOAP::Data->type("Credentials")->value(
			     \SOAP::Data->value(
                              SOAP::Data->name("login" => "oxadminmaster"),
                              SOAP::Data->name("password" => "$mpw")));
	
	foreach my $opt(@ENV_OPTIONS) {
		# Call setEnvConfigOption(opt); here
	}
        
	bless $self, $inPkg;
    return $self;
}

sub doCSVOutput {
	my $inSelf = shift;
	my $columns = shift;
	my $data = shift;
	
	#print Dumper($columns);
	#print Dumper($data);
	#my ($inSelf, @columns, @data) = @_;
    # first prepare the columns line
    # StringBuilder sb = new StringBuilder();
    my $sb = "";
    foreach my $column_entry(@$columns) {
    	$sb .= $column_entry;
    	$sb .= ",";
    }
    if (length $sb > 0) {
		#remove last ","
		chop($sb);
    } 
        
    # print the columns line
    print $sb."\n";
    
    if (defined($columns) && scalar(@$columns) != 0 && defined($data) && scalar(@$data) != 0) {
    	# Check if each row in data is != 0
    	foreach my $ref (@$data) {
    		if (scalar(@$ref) == 0) {
    			throw InvalidDataException("One of the data rows is null");
    		}
    	}
    	if (scalar(@$columns) != scalar(@{@$data[0]})) {
  			throw InvalidDataException("Number of columnnames and number of columns in data object must be the same");
    	}
        # now prepare all data lines
        foreach my $data_list (@$data) {
        	my $sb = "";
        	foreach my $data_column (@$data_list) {
        		if (defined($data_column)) {
			  if( ref($data_column) eq "ARRAY" ) {
			    $sb .= "\"";
			    foreach my $p ( @$data_column ) {
			      $sb .= "$p,";
			    }
			    $sb = substr($sb,0,length($sb)-1);
			    $sb .= "\"";
			  } elsif( ref($data_column) eq "HASH" ) {
			    $sb .= "\"";
			    foreach my $p ( keys %$data_column ) {
			      $sb .= "$p=$data_column->{$p},";
			    }
			    $sb = substr($sb,0,length($sb)-1);
			    $sb .= "\"";
			  } else {
			    $sb .= "\"".$data_column."\"";
			  }
			}
			$sb .= ",";
		      }
        	if (length $sb > 0) {
        		# remove trailing ","
        		chop($sb);
        	}
        	print $sb."\n";
        }
    }
}

sub fetch_results {
    my $self = shift;
    my $fields = shift;
    my $results = shift;
    
    my @data;
    
    for my $result (@$results) {
        my @row;
        foreach my $field (@$fields) {
             push (@row, $result->{$field});
        }
        
    #    for my $key ( keys %$result ) {
    #        print $key."\n";
    #    }
        push (@data, \@row);
    } 
    
    return @data;
}

sub fault_output {
    my $self = shift;
    my $s = shift;
    printf("Code: %s\nString: %s\nDetail: %s\nActor: %s\n",
        $s->faultcode(), $s->faultstring(), Dumper($s->faultdetail()), $s->faultactor() );
}
