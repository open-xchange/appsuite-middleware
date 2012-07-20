input = ARGF.read

puts <<EOF
{|class="wikitable sortable" border="1"
|+ Sortable table
|-
! scope="col" | Alphabetic
! scope="col" | Alphabetic
! scope="col" class="unsortable" | Unsortable
|-
|* path *|
|* name *|
|* description *|
|-
EOF
input.each_line do |line|
  parts = line.split("\t")
  if parts.length != 3
    puts parts.length
    next
  end
  path = parts[0].chomp
  name = parts[1].chomp
  desc = parts[2].chomp 
  desc = "''No description yet''" if desc == ""
  puts <<EOF
| #{path}
| #{name}
| #{desc}
|-
EOF
end

puts "|}"