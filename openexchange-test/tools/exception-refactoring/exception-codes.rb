require 'find'

files_changed = []
Find.find(ARGV[0]) do |filename|
	next unless File.exists?(filename) && File.file?(filename) && filename.end_with?(".java")
	
	content = File.read(filename)
	#example: throw new ConfigurationException(Code.PROPERTY_MISSING, Property.CONTEXTNAME.getPropertyName());
	pattern = /throw new (.+?)Exception\s*\(Code\.(\S+?),\s*(.*?)\);/
	next unless content =~ pattern

	File.open(filename,"w") do |fh|
		fh.puts(content.gsub(pattern, "throw \\1ExceptionCodes.\\2.create(\\3);"))
	end
	files_changed << filename
end

puts "Changed the following files:\n"+files_changed.join("\n")
puts "#{files_changed.size} changed."

