#!/bin/bash -e

main_dir=/home/ubuntu/snowplow
gatling_dir=$main_dir/gatling
results_dir=$main_dir/results
src_dir=$main_dir/src

$gatling_dir/gatling-charts-highcharts-bundle-2.2.1-SNAPSHOT/bin/gatling.sh -sf $src_dir -rf $results_dir -m
