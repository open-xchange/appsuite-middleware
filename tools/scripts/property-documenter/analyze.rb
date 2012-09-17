require 'pathname'

excludes = [/\/open-xchange-development\//, /\/tmp\//, /\/adminTmp\//, /\/build.properties$/, /\/buildservice.properties$/, /\/open[-e]xchange-test\//]
class Prop
  attr_accessor :key, :value, :comment, :file, :line

  def initialize(init)
     init.each_pair do |key, val|
       instance_variable_set('@' + key.to_s, val)
     end
   end
end

input = ARGF.read
properties = {}


input.each_line do |filepath|
  next if excludes.any?{|exclude| filepath =~ exclude}
  filepath.chomp!
  filename = Pathname.new(filepath).basename
  props = []
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
        comment += line.chomp
      when :EMPTY_LINE
        comment = "" #assume that an empty line between comment and k/v pair means this comment is unrelated to the pair
      when :KV_WITH_COLON
        line =~ /(.+?):(.*)/
        props.push Prop.new(:key => $1, :value => $2, :file => filename, :comment => "", :line => line_num)
        comment = ""
      when :KV_WITH_EQUAL
        line =~ /(.+?)=(.*)/
        props.push Prop.new(:key => $1, :value => $2, :file => filename, :comment => "", :line => line_num)
        comment = ""
      end
      
      state = nil
    end #lines
  end #file
  puts "<!-- Duplicate file #{filename} in #{filepath} -->" if(properties.key?(filename))
  properties[filename] = props
end #files



preface = <<END
<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.5/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.5/ http://www.mediawiki.org/xml/export-0.5.xsd" version="0.5" xml:lang="en">
  <siteinfo>
    <sitename>Open-Xchange</sitename>
    <base>http://oxpedia.org/wiki/index.php?title=Main_Page</base>
    <generator>MediaWiki 1.18.1</generator>
    <case>first-letter</case>
    <namespaces>
      <namespace key="-2" case="first-letter">Media</namespace>
      <namespace key="-1" case="first-letter">Special</namespace>
      <namespace key="0" case="first-letter" />
      <namespace key="1" case="first-letter">Talk</namespace>
      <namespace key="2" case="first-letter">User</namespace>
      <namespace key="3" case="first-letter">User talk</namespace>
      <namespace key="4" case="first-letter">Open-Xchange</namespace>
      <namespace key="5" case="first-letter">Open-Xchange talk</namespace>
      <namespace key="6" case="first-letter">File</namespace>
      <namespace key="7" case="first-letter">File talk</namespace>
      <namespace key="8" case="first-letter">MediaWiki</namespace>
      <namespace key="9" case="first-letter">MediaWiki talk</namespace>
      <namespace key="10" case="first-letter">Template</namespace>
      <namespace key="11" case="first-letter">Template talk</namespace>
      <namespace key="12" case="first-letter">Help</namespace>
      <namespace key="13" case="first-letter">Help talk</namespace>
      <namespace key="14" case="first-letter">Category</namespace>
      <namespace key="15" case="first-letter">Category talk</namespace>
      <namespace key="200" case="first-letter">Development</namespace>
    </namespaces>
  </siteinfo>
END
footer = <<END
</mediawiki>
END

middle = ""
now = Time.now
timestamp = now.strftime("%FT%TZ")
rev_id = now.to_i

properties.each do |filename, props|
  pagecontent = "\n{|\n!Key\n!Default value\n!Comment\n"
  props.each {|prop| pagecontent += "|-\n|#{prop.key}\n|#{prop.value}\n|#{prop.comment}\n"}
  pagecontent += "|}\n"
  title = filename
  
  template = <<END
  <page>
    <title>#{title}</title>
    <revision>
      <timestamp>#{timestamp}</timestamp>
      <contributor>
        <username>Tierlieb</username>
        <id>5</id>
      </contributor>
      <comment>Automatic update</comment>
      <text xml:space="preserve">{{Generated}}{{Properties}}
      #{pagecontent}
      [[Category:Config]] [[Category:Generated]]</text>
    </revision>
  </page>  
END
  middle += template
end

puts preface + middle + footer