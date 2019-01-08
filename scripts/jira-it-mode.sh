#!/usr/bin/env bash
atlas-debug \
    --product jira \
    -Dcst.test.mode=true \
    -DskipTests | tee log.log
