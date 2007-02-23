#!/bin/bash

rsync -a --list-only $1 | awk -F' ' {'print $5 " [" $2 "]"'} 
