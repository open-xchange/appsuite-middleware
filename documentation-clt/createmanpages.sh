#!/bin/bash
set -Eeuo pipefail

output_dir=output/manpages
md2mangz() {
  target_dir="${1}"
  md_file="${2}"
  name=$(basename -s .md "${md_file}")
  echo ${md_file}
  pandoc "${md_file}" -s -t man -o - | gzip -c > ${target_dir}/${name}.1.gz
}
export -f md2mangz
export output_dir

echo "Make output directory..."
mkdir -p ${output_dir}

echo "Creating manpages with pandoc..."
find -L . -maxdepth 3 -mindepth 3 -name "*.md" -exec bash -c "md2mangz ${output_dir} {}" \;
