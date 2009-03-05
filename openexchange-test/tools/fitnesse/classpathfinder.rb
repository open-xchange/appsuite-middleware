def abs_path(myDir, myFile)
  File.expand_path( File.join( myDir.path, File.basename( myFile ) ) )
end

myDir = Dir.new("../../../openexchange-test-gui/lib")
excludes = ["open-xchange-iface-tests.jar"].map{ |elem| abs_path(myDir, elem) }
myJARs = []

myDir.each do |entry|
  if entry =~ /\.jar$/
     myJARs.push( abs_path(myDir, entry) )
  end
end

print myJARs.reject{|jar| excludes.include?(jar) }.join(":")