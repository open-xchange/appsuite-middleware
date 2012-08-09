input = ARGF.read

input.each_line do |filename|
  filename.chomp!

  buffer = nil
  data = {}
  key = "THIS IS WEIRD"
  continuing = false

  File.open(filename, "r") do |infile|
    counter = 0
    while (line = infile.gets)  
      counter += 1
      if continuing && line =~ /^\s/
        buffer << "\n" << line.chomp
      end
      
      if continuing && line !~ /^\s/
        data[key] = buffer
        buffer = key = nil
        continuing = false
      end
      
      if line =~ /Bundle-Name\s*:\s*(.*?)\n/
        buffer = $1
        continuing = true
        key = "name"
      end

      if line =~ /Bundle-SymbolicName\s*:\s*(.*?)\n/
        buffer = $1
        continuing = true
        key = "path"
      end

      if line =~ /X-Bundle-Description\s*:\s*(.*?)\n/
        buffer = $1
        continuing = true
        key = "desc"
      end
      
    end
    
    data[key] = buffer unless buffer
  end

  puts "#{data['path']}\t#{data['name']}\t#{data['desc']}"
end