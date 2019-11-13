#!/usr/bin/env bash

set -e

environments=(
  stg
  ppd
)

SCRIPT_DIR=$(cd `dirname $0` && pwd)

cd $SCRIPT_DIR/..
mvn clean package
cd $SCRIPT_DIR

for i in "${environments[@]}"; do
  docker build --build-arg ENV=${i} -t dr:443/web-sdk/tpa-${i}-test:latest .
done
