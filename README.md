Casanova
========

> A charming general game player.


Getting started
---------------
### Becoming a collaborator
In order to participate you must [create a GitHub user](http://github.com/signup/free) and send you username to steinar@steinar.is (email/MSN/gTalk). Once I've added you as a collaborator you've get write access to the repository


### Git repository
Before you start, it's a good idea to check out [the git introduction video](http://learn.github.com/p/intro.html). To be able to survive you must know and understand these terms: commit, pull, push, branch and merge.


First you must [install Git](http://help.github.com/linux-git-installation/) and [generate a ssh key](http://help.github.com/linux-key-setup/). Then add the key you generated to you [SSH Public keys](https://github.com/account#ssh_bucket).

It's good to configure git setting your name and email. It will appear in the Git log.

    $ git config --global user.name "Steinar Hugi"
    $ git config --global user.email "steinar@steinar.is"

This is where the fun stuff starts.  Let's create a local clone of the repository.

    $ git clone git@github.com:ru/casanova.git

We have already created a branch for each group. Before you start doing your stuff, switch to that branch. 

    $ cd casanova
    $ git branch

This will show a list of all the branches. If your group's name is 'lonerangers', run

    $ git checkout -t origin/lonerangers

If you now type

    $ git status
    
The first line should say "On branch lonerangers".

#### Further reading

For further instructions, refer to these sites

+ [GitHub's learning site](http://learn.github.com/)
+ [GitReady](http://www.gitready.com/) (My favorite)
+ [git-scm](http://git-scm.com/)
+ [Git man pages](http://www.kernel.org/pub/software/scm/git/docs/)



### Setting up the project

After doing so, you can either chose to work with eclipse or IntelliJ. With eclipse you can use palamedes which gives
you general game playing perspective over your project, allows you to debug GDL, etc. etc. In both cases, you will get
all dependencies with sources attached to you project.

#### Install maven

We recommend the maven build system and have created a maven build descriptor for this project. Follow the directions
at the [maven website](http://maven.apache.org) get up and running with maven.

#### If you want to use eclipse

First, you need to install [m2eclipse](http://m2eclipse.sonatype.org). (Eclipse update site: http://m2eclipse.sonatype.org/sites/m2e)

You can use maven to generate an eclipse project for you by running

> mvn eclipse:m2eclipse

You can now import the generated project into your eclipse workspace by doing

> File -> Import.. -> General -> Existing Projects into Workspace

and point to the directory which you checked out earlier.

#### If you want to use IntelliJ

You can create a new project from a maven project in IntelliJ by doing

> File -> New Project -> Import project from external model -> Maven

and point to the directory which you checked out earlier.

Running a test match
--------------------
In order to run a test match with your player you must run a gamecontroller server. We've added the JAR, two example GDL's and an example shell script to a "test" folder under the repository root.

This is the command where  gdl/connect4.gdl is the game description in a match between a random player and a player running on localhost at port 4001

>  java -jar gamecontroller-cli-r360.jar Test gdl/connect4.gdl 10 10 -remote 1 YB localhost 4001
