#!/usr/bin/env bash

mvn verify -B -e  \
    -Dproduct=jira \
    -PIntegrationTest \
    -DtestGroups=integration \
    -Dxvfb.enable=false \
    | tee it-test.log