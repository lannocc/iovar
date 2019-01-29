# IOVAR Web Platform and Shell

The official website and complete documentation is at https://iovar.net.

## A Shell for the Web

IOVAR begins as a collection of web utilities on a Java foundation. It continues by providing a
web-aware shell environment and scripting language. Common patterns, protocols, and schemas are
embraced to provide familiar environments for the user and wider opportunity for automation.

As a web shell, a distinguishing characteristic is the addition of the $PWU environment variable as
a superset to the standard $PWD (working directory). $PWU (parent working universe) represents the
working uri space at which to root the relative working directories.

This web application is currently known to run on Tomcat on Linux and macOS.

Executables generally live in /bin (standard utilities), /sbin (system administration), /usr/bin,
/usr/local/bin, /opt/bin, etc.

SECURITY WARNING: The IOVAR Web Shell is currently in single-user status and runs with the
privileges of the Tomcat user. It may not be safe to use.

Volunteers are needed to help this project out and create a useful shell framework for the web
context. Please contact lannocc for information or to offer help.

## Install

Basic requirements:
- Linux or macOS (or possibly some other Posix system)
- Java 1.7 SDK or higher
- Apache Ant
- Servlet container (Apache Tomcat 6 or higher)
- Mercurial or Git (optional)

To learn about or work with IOVAR directly you can start here. If you just want to start a new
project that runs on IOVAR then you should start with the [iobaby][iobaby] project instead.

General install procedure is:
1. `hg clone https://iovar.net/hg/iovar` or `git clone https://github.com/lannocc/iovar` or
   [download a source archive][sources]
2. `cd iovar`
3. `ant project.install`
4. `ant app.install`
5. `ant`
6. Install webapp into servlet container ROOT context (the / context is assumed).
7. Start servlet container.
8. Open browser to http://localhost:8080 or http://localhost:8080/$ for the interactive shell
   interface.

Enjoy the web!

## Open Source License

IOVAR is free and open software.

Property of Virgo Venture, Inc. and others. Licensed under MIT license (see LICENSE file).

[iobaby]: https://iovar.net/hg/iobaby
[sources]: https://github.com/lannocc/iovar/releases

