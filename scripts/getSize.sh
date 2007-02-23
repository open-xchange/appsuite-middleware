#!/bin/bash

du -sb $1 | awk -F'\t' {'print $1'}
