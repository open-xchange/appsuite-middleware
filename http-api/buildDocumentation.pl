#!/usr/bin/perl

use Getopt::Long;
use warnings;
use strict;
use v5.10;
use Pod::Usage;
use File::HomeDir;


my $api = "http_api";
my $target = File::HomeDir->my_home."/Desktop/documentation";
my $deps;
my $help;

GetOptions ("api=s" => \$api,
            "target=s" => \$target,
            "dependencies" => \$deps,
            "help|?" => \$help) or pod2usage(2);

pod2usage(1) if $help;

if (! -e "resolve.js") {
    die "Please navigate to the documentation directory where the 'resolve.js' file is present.\n";
}

if (! -d $api) {
    die "The API directory '$api' does not exist.\n";
}

printf("Generating swagger.json...\n");
system("node", "resolve.js", $api);

if ($deps) {
    printf("Installing Bootprint...\n");
    system("npm", "install", "-g", "bootprint");
    printf("Installing Bootprint-OpenAPI...\n");
    system("npm", "install", "-g", "bootprint-openapi");
    printf("Installing swagger-tools...\n");
    system("npm", "install", "swagger-tools", "--save");
    printf("Installing js-yaml...\n");
    system("npm", "install", "js-yaml", "--save");
    printf("Installing json-schema-ref-parser...\n");
    system("npm", "install", "json-schema-ref-parser", "--save");
}
printf("Generating documentation pages...\n");
system("bootprint", "openapi", $api."/swagger.json", $target);

#
## Usage
## 
__END__


=head1 NAME

buildDocumentation.pl - Builds the HTTP, Drive and REST API documentation

=head1 SYNOPSIS

buildDocumentation.pl [options] <folder>

   Options:
     --api <relative_api_folder> The relative API folder, e.g. http_api, drive_api, rest_api
     --dependencies              Installs the required dependencies
     --target <target_folder>    The target folder
     --help                      Displays the help

=head1 OPTIONS

=over 4

=item B<--api>

 Defines the relative folder containing the API documentation. At the moment there are three folders: http_api, drive_api and rest_api

=item B<--dependencies>
 
 Installs all required dependencies for the OpenAPI, namely 'bootprint', 'bootprint-api', 'swagger-tools', 'js-yaml' and 'json-schema-ref-parser'

=item B<--target>

 Defines the target folder of the generated documentation

=item B<--help>
 
 Prints a brief help message.

=head1 AUTHOR

Ioannis Chouklis, C<ioannis.chouklis@open-xchange.com>

=cut

