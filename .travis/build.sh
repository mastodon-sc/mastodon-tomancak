#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_cd8525cd96c6_key $encrypted_cd8525cd96c6_iv
