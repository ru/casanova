Casanova
========

> A charming general game player.


Getting started
---------------

You need to checkout the project using git.

After doing so, you can either chose to work with eclipse or IntelliJ. With eclipse you can use palamedes which gives
you general game playing perspective over your project, allows you to debug GDL, etc. etc. In both cases, you will get
all dependencies with sources attached to you project.

## Install maven

We recommend the maven build system and have created a maven build descriptor for this project. Follow the directions
at the [maven website](http://maven.apache.org) get up and running with maven.

## If you want to use eclipse

First, you need to install [m2eclipse](http://m2eclipse.sonatype.org).

You can use maven to generate an eclipse project for you by running

> mvn eclipse:m2eclipse

You can now import the generated project into your eclipse workspace by doing

> File -> Import.. -> General -> Existing Projects into Workspace

and point to the directory which you checked out earlier.

## If you want to use IntelliJ

You can create a new project from a maven project in IntelliJ by doing

> File -> New Project -> Import project from external model -> Maven

and point to the directory which you checked out earlier.
