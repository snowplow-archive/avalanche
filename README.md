# Avalanche

[ ![Build Status] [travis-image] ] [travis] [ ![Release] [release-image] ] [releases] [ ![License] [license-image] ] [license]

Avalanche is designed to mimic the behaviour of sudden increases and decreases in requests to the Snowplow Pipeline and the ability for the pipeline to scale up/down to match these changes.  This should assert that under any amount of load the pipeline will scale to compensate while not losing any data.

## Quick Start

Assuming git, **[Vagrant] [vagrant-install]** and **[VirtualBox] [virtualbox-install]** installed:

```bash
 host$ git clone https://github.com/snowplow/avalanche.git
 host$ cd avalanche
 host$ vagrant up && vagrant ssh
guest$ cd /vagrant
```

### To run a simulation from your local machine:

* Set your collector URL as an environment variable:

```bash
guest$ export SP_COLLECTOR_URL={{ the collector URL }}
guest$ export SP_SIM_TIME={{ the time to run the simulation for in minutes }}
guest$ export SP_BASELINE_USERS={{ the amount of users as a baseline }}
guest$ export SP_PEAK_USERS={{ the peak amount of users }}
```

__NOTES__:

* The baseline users will send events for the entirety of the simulation.
  - Each user will emit ~ 1 event per second.
* The peak users is the count of users at a maximal point.

* List all of the simulations available:

```bash
guest$ /vagrant/dist/gatling-charts-highcharts-bundle-2.2.0-SNAPSHOT/bin/gatling.sh -sf /vagrant/src/ -rf /vagrant/dist/results/ -m
```

* Select the simulation you wish to run.
* Wait for simulation to complete

__NOTE__: It is advised to run heavy simulations from a server in the cloud rather than from your local machine:

1. The connection of an EC2 instance will no doubt be faster than your host machine; yielding better results.
2. You will quickly saturate your home network and be unable to use your network.

The Vagrant environment should be used solely for testing purposes or for very light simulations.

### To run a simulation from an EC2 instance:

#### 1. Create a Security Group

In the EC2 Console UI select `Security Groups` from the panel on the left.

Select the `Create Security Group` button and fill in the name, description and what VPC you want to attach it to.

You will then need to add the following InBound rules with either `0.0.0./0` as the source or something more restrictive:

* SSH: `SSH | Port Range (22)`

For OutBound you can leave the default to allow everything out.

#### 2. Launch the instance

In the EC2 Console UI select the `Launch Instance` button then select the `Community AMIs` button.

##### 2.1 Choose AMI

In the search bar enter `snowplow-avalanche-0.1.0` to find the needed AMI then select it.

##### 2.2 Choose an Instance Type

For load testing using Gatling we will need a large instance with high network performance.  We recommend any of the `m4.large` up to `m4.4xlarge` depending on the intensity of the simulation you wish to run.

##### 2.3 Configure Instance

If you created your Security Group in a different VPC than the default you will need to select the same VPC in the Network field.

##### 2.4 Add Storage

For basic testing and debugging 8gb should suffice.

We also recommend changing the `Volume Type` to GP2 from Magnetic for a faster experience.

##### 2.5 Tag Instance

Add any tags you like here.

##### 2.6 Configure Security Group

Select the Security Group you created in Step 1.

##### 2.7 Review

Press the `Launch` button and select an existing, or create a new, key-pair if you want to be able to SSH onto the box.

#### 3. Run the simulations

To run the simulations you will first need to SSH onto the instance like so:

```bash
host$ chmod 400 {{ key-pair file path }}
host$ ssh -i {{ key-pair file path }} ubuntu@{{ public DNS of instance }}
```

Set your collector URL as an environment variable:

```bash
ubuntu$ export SP_COLLECTOR_URL={{ the collector URL }}
```

Set your simulation variables:

```bash
ubuntu$ export SP_SIM_TIME={{ the time to run the simulation for in minutes }}
ubuntu$ export SP_BASELINE_USERS={{ the amount of users as a baseline }}
ubuntu$ export SP_PEAK_USERS={{ the peak amount of users }}
```

You will then need to run the following command:

```bash
ubuntu$ ./snowplow/scripts/2_run.sh
```

This will launch Gatling and allow you to select the simulation you wish to run.  There are currently two options available:

* [ExponentialPeak](https://github.com/snowplow/avalanche/blob/master/src/main/com/snowplowanalytics/avalanche/ExponentialPeak.scala#L20-L44) : Will increase load on the Collector exponentially up until the maximum users.
* [LinearPeak](https://github.com/snowplow/avalanche/blob/master/src/main/com/snowplowanalytics/avalanche/LinearPeak.scala#L20-L39) : Will increase load on a linear scale up until the maximum users.

Once the simulation is finished the results will be saved to `/home/ubuntu/snowplow/results`, to then view these results you can:

* Launch a Python server in the root of the results directory:

```bash
ubuntu$ cd /home/ubuntu/snowplow/results
ubuntu$ python -m SimpleHTTPServer 3000
```

Access the results by going to your browser and entering: `http://{{ public DNS of instance }}:3000` and then navigating to your results.

* Copy the results folder from EC2 back to your local machine using `scp`:

```bash
host$ scp -i {{ key-pair file path }} -r ubuntu@{{ public DNS of instance }}:/home/ubuntu/snowplow/results {{ local directory }}
```

Access the results by opening the `index.html` file in the results directories.

## Copyright and license

Avalanche is copyright 2016 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0] [license]** (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[vagrant-install]: http://docs.vagrantup.com/v2/installation/index.html
[virtualbox-install]: https://www.virtualbox.org/wiki/Downloads
[license]: http://www.apache.org/licenses/LICENSE-2.0

[travis]: https://travis-ci.org/snowplow/avalanche
[travis-image]: https://travis-ci.org/snowplow/avalanche.svg?branch=master

[release-image]: http://img.shields.io/badge/release-0.1.0-blue.svg?style=flat
[releases]: https://github.com/snowplow/avalanche/releases

[license-image]: http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
