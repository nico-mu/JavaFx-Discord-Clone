#### STP ST21 Team Guava

[![Build Status](https://www.travis-ci.com/sekassel/STPST21TeamG.svg?token=iv8L4W51ZozK2puhSbJk&branch=master)](https://www.travis-ci.com/sekassel/STPST21TeamG)
[![codecov](https://codecov.io/gh/sekassel/STPST21TeamG/branch/master/graph/badge.svg?token=HQTC4B9IYG)](https://codecov.io/gh/sekassel/STPST21TeamG)

# Accord

Accord is the client you'll need to communicate with your peers and friends via the Accord-Server.

## Getting Started

Grab the latest application state from our GitHub repository, create a new branch from the master following our naming
conventions and start coding!

You should use the buildRouteMap Gradle-Task on every pull of the master branch. You'll create the latest
RouteMapping and allow the routing system to work properly.

### Prerequisites

Things you need before installing the software.

* Java 11+ AdoptOpenJDK (HotSpot) 11.0.1 is recommended
* JavaFX Scene Builder 11.0.0
* An IDE that will make your coding much easier IntelliJ IDEA Ultimate 2021.1.1 is recommended
* Oh, and don't forget a warm cup of coffee

### Usage Infos
There are a few things you need to know when using Accord.

* Jars can be compiled by running the "jar task" by gradle and can be found afterwards in .../app/build/libs. The "build jar" task location is in the gradle menu under Tasks/build/jar.
* Easter egg can be accessed by opening private chat, writing "!play :handshake:" and receiving the same message from the chat partner within 30 seconds.
* In case there are any problems with running the same instance of Accord multiple times, try to wait until the program has started before you launch it again.
* In case there are communication problems in an audio channel, make sure you selected the right default speaker/microphone in your system settings. 
* Supported emoji aliases are ":D", ":*" and ":(". More aliases can be easily added if needed.
* Supported 3rd party integrations are GitHub and Spotify which can be enabled in the settings menu.

### Server

* [Release 1 documentation](https://seblog.cs.uni-kassel.de/wp-content/uploads/2021/04/ServerdokuR1.pdf)
* [Release 2 documentation](https://seblog.cs.uni-kassel.de/wp-content/uploads/2021/05/ServerdokuR2.pdf)
* [Release 3 documentation](https://seblog.cs.uni-kassel.de/wp-content/uploads/2021/06/ServerdokuR3.pdf)
* [Release 4 documentation](https://seblog.cs.uni-kassel.de/wp-content/uploads/2021/07/ServerdokuR4.pdf)

### Branches

* Master: The latest working version and can only be edited by the ScrumMaster
* Feature: WIP tasks, that will be merged into the master once finished by creating a pull request
* Bugfix: Any bugfix
* Other: Can contain basically anything else.

Please let your branches have a speaking name, so everyone knows what to expect. If the branch you created is
temporary, it is your responsibility to delete it at the end of each sprint or provide an explanation why it must be
present on the remote!

## Additional Documentation and Acknowledgments

* [SE Blog STP SS21](https://seblog.cs.uni-kassel.de/ss21/software-technik-praktikum/)
  Check regularly for new information and the latest server documentation
* [GitHub Repository STPST21TeamG](https://github.com/sekassel/STPST21TeamG)
* [Jira Project TG21](https://jira.uniks.de/projects/TG21/summary)
* Icons von [Google Fonts](https://fonts.google.com/icons)
