# IOVAR Web Platform and Shell

A shell for the web. IOVAR is a general-purpose collection of Java-based utilities.

As a web shell, a distinguishing characteristic is the addition of the $PWU environment variable as
a superset to the standard $PWD (working directory). $PWU (parent working universe) represents the
working uri space at which to root the relative working directories.

This web application is currently known to run on Tomcat on Linux.

Executables generally live in /bin (standard utilities), /sbin (system administration), /usr/bin,
/usr/local/bin, /opt/bin, etc.

SECURITY WARNING: The IOVAR Web Shell is currently in alpha single-user status and runs with the
privileges of the Tomcat user. It may not be safe to use.

Read the INSTALL file for installation instructions.

Volunteers are needed to help this project out and create a useful shell framework for the web
context. Please contact lannocc for information or to offer help.

Property of Virgo Venture, Inc. and others. Licensed under MIT license (see LICENSE file).

