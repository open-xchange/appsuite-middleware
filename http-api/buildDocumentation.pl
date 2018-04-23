#!/usr/bin/perl
use Getopt::Long;
use warnings;
use strict;

my $api = "http_api";
my $target = "/Users/mah/Desktop/whatever";
my $deps;

GetOptions ("api=s" => \$api,
            "target=s" => \$target,
            "dependencies" => \$deps);

if (! -e "resolve.js") {
    die "Please navigate to the documentation directory where the 'resolve.js' file is present.\n";
}

if (! -d $api) {
    die "The API directory '$api' does not exist.\n";
}

printf("Generating swagger.json...\n");
system("node", "resolve.js", $api, "--debug");

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
