#!/usr/bin/env bash
if [ ! -f target/routes-test.js ]; then
    lein cljsbuild once
fi
echo "routes.test.run()" | d8 --shell target/routes-test.js
