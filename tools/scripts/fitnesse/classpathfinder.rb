def abs_path(myDir, myFile)
  File.expand_path( File.join( myDir.path, File.basename( myFile ) ) )
end

directories = ["../../../openexchange-test-gui/lib", "../../../com.openexchange.common/lib"]
excludes = ["open-xchange-iface-tests.jar"]
jars = []

directories.each do |myDir|
  myDir = Dir.new(myDir)
  myDir.each do |entry|
    if entry =~ /\.jar$/
       jars.push( abs_path( myDir, entry ) )
    end
  end
end

print jars.reject{|jar| excludes.any?{|elem| jar =~ /#{elem}/} }.join(":")