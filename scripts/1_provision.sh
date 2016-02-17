#!/bin/bash -e

sudo apt-get update
sudo apt-get install -y unzip

#############
# Constants #
#############

main_dir=/home/ubuntu/snowplow
gatling_dir=$main_dir/gatling
results_dir=$main_dir/results
src_dir=$main_dir/src

##################
# Install Java 8 #
##################

sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo apt-get install oracle-java8-installer -y

###################
# Install Gatling #
###################

gatling_archive='gatling-charts-highcharts-bundle-2.2.1-20160421.110654-1-bundle.zip'
gatling_url='https://oss.sonatype.org/content/repositories/snapshots/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.2.1-SNAPSHOT/gatling-charts-highcharts-bundle-2.2.1-20160421.110654-1-bundle.zip'

mkdir -p $gatling_dir
mkdir -p $results_dir
mkdir -p $src_dir

sudo wget $gatling_url -P $gatling_dir
sudo unzip $gatling_dir/$gatling_archive -d $gatling_dir
