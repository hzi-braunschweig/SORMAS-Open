

# Installing a SORMAS Server for development

*If you have already installed your development server, you can find your system-specific server reference in file [dev/config/SERVER_SPECS.md](dev/config/SERVER_SPECS.md).*

## Content
* [The SORMAS development server setup](#the-sormas-development-server-setup)
* [Prerequisites](#prerequisites)
  * [Java 8](#java-8)
  * [ant](#ant)
  * [Maven](#maven)
  * [Docker and docker-compose](#docker)
* [Installing the SORMAS development server](#sormas-server)
* [Using the development server](#using-the-development-server)

## Related
* [Installing a SORMAS Server](SERVER_SETUP.md)

## The SORMAS development server setup
**Note: The SORMAS development server setup is currently targeting development machines with Linux. Interoperability with Windows WSL is not
 guaranteed!**

SORMAS comes with an installation script for setting up a local Payara environment for testing your code. In contrast to the default server
 setup described in the [server setup guide](SERVER_SETUP.md), the development server setup gives you the option to use a PostgreSQL docker
  container for your SORMAS database. A suitable container is already included in the `dev/postgres-docker` directory.
  
Some additional features of the development server setup:
* It will create the `dev/config/env.sh` sourceable shell script containing exports for environment variables required on various occasions during
 development
* Configuration of `sormas-base/build.properties` so that you can use the `deploy-serverlibs` and `deploy-artifacts` Ant tasks to deploy your
 latest SORMAS build to your development server
* You get a reference description of your development server installation under `dev/config/SERVER_SPECS.md`, giving you a quick overview over
 relevant URLs on your server and listing your chosen configuration values

## Prerequisites

### Java 8

See [Installing Java](SERVER_SETUP.md#java-8)

### Ant

Download and install Ant, it can be done from [Ant site](https://ant.apache.org/bindownload.cgi) or with packages from your Linux distribution.

### Maven

<<<<<<< HEAD
Download and install Maven, available from the [Apache Maven Project](https://maven.apache.org/download.cgi) or package repositories.
=======
See [Installing Postgresql](SERVER_SETUP.md#postgres-database)
>>>>>>> origin/development

### Docker

Download and install Docker from the [Docker homepage](https://hub.docker.com/search?q=&type=edition) or using the package manager of your Linux
 distribution. Make sure that you have `docker-compose` on your PATH.

## Installing the SORMAS development server

Run the `dev/server-setup.sh` script from within your SORMAS Project directory to start the developer setup installation wizard. You will be
 asked about various aspects of your desired installation, such as installation paths and other configuration parameters. Most configuration
  options have default values specified in brackets (`[...]`) that are automatically applied when pressing `Enter` without entering any value.

After the script terminated successfully, you can confirm Payara is running by opening the admin page of the SORMAS domain that by default is
 accessible via <http://localhost:6048>. Replace the port if you set a custom admin port.

There are still some things you need to do yourself after installation:
* **IMPORTANT**: Adjust the SORMAS configuration for your country in the `${SORMAS_DOMAIN_DIR}/sormas.properties` file
* Adjust the logging configuration in `${SORMAS_DOMAIN_DIR}/config/logback.xml` based on your needs (e.g. configure and activate email appender)
* Configure your IDE for build and deployment

## Using the development server
### At the beginning of your coding session
1. Source the `dev/config/env.sh` file to ensure that you have all potentially needed environment variables initialized
2. If using the PostgreSQL docker container, start the container by executing `docker-compose up` in `dev/postgres-docker` directory
3. Start your Payara server by executing the `start-payara-sormas.sh` script in your SORMAS domain directory (`${SORMAS_DOMAIN_DIR}`)

### During your coding session (assuming command line)
Use these Ant targets in `sormas-base` for your development process
  - **clean**: Remove leftovers from previous builds; use it after pulling changes from the Github-Repo
  - **install**: Build all SORMAS components except for the Android App
  - **install-with-app**: Build all SORMAS components including the App
  - **deploy-serverlibs**: Deploy all general server dependencies to your payara installation
  - **deploy-artifacts**: Deploy SORMAS components to your payara installation

### After your coding session
1. Stop the Payara server by executing the `stop-payara-sormas.sh` script in your SORMAS domain directory
2. If using the PostgreSQL docker container, stop the container by executing `docker-compose stop`
