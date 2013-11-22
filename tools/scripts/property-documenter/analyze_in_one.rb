require 'pathname'
require 'cgi'

excludes = [
  /\/open-xchange-development\//,
  /\/tmp\//,
  /\/adminTmp\//,
  /\/open[-e]xchange-test\//,
  /build.properties$/,
  /buildservice.properties$/,
  /3rdPartyLibs\.properties$/,
  /HTMLEntities\.properties$/,
  /whitelist.properties$/,
  /ldap\.properties$/,
  /language-codes\.properties$/,
  /mail-querybuilder\.properties$/,
  /microformatWhitelist\.properties$/,
  /open-xchange\.properties$/,
  /outlook\d+\..+?\.properties/]

$fixed = ["ModuleAccessDefinitions.properties", "Group.properties", "Resource.properties",
    "RMI.properties", "Sql.properties", "AdminUser.properties", "filestorage.properties",
    "mailfilter.properties", "recaptcha.properties", "recaptcha_options.properties"]

class Prop
  attr_accessor :key, :value, :comment, :file, :line, :isFixed

  def initialize(init)
     init.each_pair do |key, val|
       instance_variable_set('@' + key.to_s, val)
     end
   end
end

#
# READING
#
startPath = ARGV.first

properties = []

Dir.glob(startPath + "/**/*.properties") do |filepath|
  next if excludes.any?{|exclude| filepath =~ exclude}
  filepath.chomp!
  filename = Pathname.new(filepath).basename
  buffer = nil
  data = {}
  key = "THIS IS WEIRD"
  continuing = false

  File.open(filepath, "r") do |infile|
    line_num = 0
    state = nil
    comment = ""
    while (line = infile.gets)
      line_num += 1
      state = :KV_WITH_COLON if line =~ /^[^:=]+?\s*:\s*/
      state = :KV_WITH_EQUAL if line =~ /^[^:=]+?\s*=\s*/
      state = :COMMENT if line =~ /^[!#]/
      state = :EMPTY_LINE if line.chomp == ""
      next unless state

      case state
      when :COMMENT
        comment += line.chomp.reverse.chop.reverse.strip + " " #remove line break from back, comment hash from front, empty whitespace from both sides, then add single whitespace for next line
      when :EMPTY_LINE
        comment = "" #assume that an empty line between comment and k/v pair means this comment is unrelated to the pair
      when :KV_WITH_COLON
        line =~ /(.+?):(.*)/
        properties.push Prop.new(:key => $1, :value => $2, :file => filename, :comment => comment, :line => line_num)
        comment = ""
      when :KV_WITH_EQUAL
        line =~ /(.+?)=(.*)/
        properties.push Prop.new(:key => $1, :value => $2, :file => filename, :comment => comment, :line => line_num)
        comment = ""
      end

      state = nil
    end #lines
  end #file
end #files

#
# SORTING
#
properties = properties.sort_by{|prop| "#{prop.file}:#{prop.line}"} #wtf is .sort_by! undefined for arrays?

#
# PRINTING
#
pagecontent = <<EOL
This is an overview of all configuration parameters for the AppSuite backend. By default, this list is sorted by the .properties files they appear in.
Yet for most parameters, it does not really matter in which file are found. This does not apply to core configuration files. For these, you cannot re-define the value in another file:
* ModuleAccessDefinitions.properties
* Group.properties
* Resource.properties
* RMI.properties
* Sql.properties
* AdminUser.properties
* filestorage.properties
* mailfilter.properties
* recaptcha.properties
* recaptcha_options.properties

The following are all parameters from all .properties within version 7.0.1

{|width="100%" style="table-layout: fixed" class='wikitable sortable' border='1'
! scope="col" width="30%" | Key
! scope="col" width="20%" | Default value
! scope="col" width="35%" | Comment
! scope="col" width="15%" | File
EOL
properties.each do |prop|
  pagecontent += "|-\n"
  pagecontent += "| style='color:red'" if prop.isFixed
  pagecontent += "| #{CGI::escapeHTML(prop.key)}\n"
  if prop.value.length > 30
    pagecontent += "| <span class=\"shortened\" alt=\"#{CGI::escapeHTML(prop.value)}\" title=\"#{CGI::escapeHTML(prop.value)}\"><nowiki>#{CGI::escapeHTML(prop.value[0..27])}...</nowiki></span>\n"
  else
    pagecontent += "| <nowiki>#{CGI::escapeHTML(prop.value)}</nowiki>\n"
  end
  pagecontent += "| <nowiki>#{CGI::escapeHTML(prop.comment)}</nowiki>\n"
  pagecontent += "| #{prop.file}:#{prop.line}\n"
end
pagecontent += "|}\n[[Category:Configuration]] [[Category:Generated]]"

print pagecontent