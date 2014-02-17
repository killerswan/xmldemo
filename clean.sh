#!/bin/bash -x

DIR="$(cd "$(dirname "$0")"; pwd)"

rm -f  "$DIR/www/app.js"
rm -rf "$DIR/www/app/"
