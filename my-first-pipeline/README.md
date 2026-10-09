# A simple jenkins pipeline to verify if the docker slave configuration is working as expected.#

High-Level Summary

This is a Declarative Pipeline designed to automate the lifecycle of a Python application inside a Docker container. It imports a Jenkins Shared Library (sharedlib), changes into a specific project subdirectory (my-first-pipeline), and sequentially builds, tests, and pushes a Docker image to Docker Hub using dynamic tagging based on the unique Jenkins Build ID. It also includes an automated cleanup phase to protect your server's disk space.


Stage-by-Stage Breakdown


0. Initialization & Global Setup

• @Library('sharedlib') _: Imports your remote global shared library. The underscore (_) automatically pulls in all custom Groovy steps (vars/) so they can be called directly in this script.
• environment { imgname = 'python-app' }: Defines a pipeline-wide variable named imgname. This makes it incredibly easy to rename your application in the future without changing multiple lines of code.

1. stage('Build python images')

• dir('my-first-pipeline'): Switches the execution path into your project folder where your application code and Dockerfile live.
• sh "docker build...": Builds a local Docker image. By using double quotes ("), Jenkins evaluates the variables to tag the image dynamically (e.g., python-app:12).

2. stage('Run Python App')

• Testing in Isolation: This stage acts as an integration or unit testing step. It spins up the freshly built container (docker run --rm), mounts your local Jenkins folder directly into the container (-v \$(pwd):/app), and forces the container to execute python app.py.
• Escaped Syntax: The \$(pwd) uses an escaped backslash so that Linux (not Jenkins Groovy) handles the directory path resolution dynamically.

3. stage('Build and push')

• Shared Library Delegation: Instead of writing complex, messy shell strings for authentication here, you call your custom step dockerloginfile(imgname). This passes the application name into your shared library, which handles logging into Docker Hub securely via credentials, tags the image with your Docker Hub username, and pushes it to the cloud.

4. post { always { cleandockerimages() } }

• Automated Housekeeping: No matter if your build succeeds, fails, or is aborted, the always block intercepts the final execution path and triggers another custom shared library step called cleandockerimages(). This sweeps your Jenkins runner disk and safely deletes (docker rmi) the cached local images so your server never runs out of storage space.

1. The Foundation: Shared Library Setup

• What you asked: You wanted to know how to set up a Jenkins Shared Library.
• What we did: We established the core foundation. We broke down the specific Git repository structure required by Jenkins (src/, vars/, resources/), explained how to configure it globally or at the folder level within the Jenkins UI, and demonstrated how to import it into a pipeline using the @Library('my-library') _ syntax.


2. The Variable Pass: Removing the $

• What you asked: You showed a code snippet where you tried to call a custom step: dockerloginfile($imgname).
• What happened: In Jenkins Declarative Pipelines (which run on Groovy), passing variables as functional arguments directly does not use the shell-style $ prefix.
• The solution: We fixed the syntax to dockerloginfile(imgname) and created the foundational script template inside vars/dockerloginfile.groovy to safely accept parameters using a standard call() method.

3. The Rules of Tagging & Pushing

• What you asked: You wanted to know how to properly tag and push an image to a registry.
• What we did: We went over Docker naming conventions (registry/repository:tag). We updated the library to use a robust dynamic setup, leveraging a Groovy Map configuration. This allowed you to dynamically pass unique tags (like the built-in ${BUILD_NUMBER}) directly from your Jenkinsfile.

4. The First Crash: invalid reference format

• The Error: invalid argument "python-app:${BUILD_ID}" for "-t, --tag" flag: invalid reference format
• Why it happened: The pipeline executed the command as a literal string. Instead of converting ${BUILD_ID} into a real number (like 42), Jenkins sent the literal text ${BUILD_ID} down to Docker. Because symbols like $ and {} are illegal characters in Docker tags, Docker rejected the command.
• The solution: We introduced the concept of String Interpolation. We explained that wrapping commands in double quotes ("...") forces Jenkins to evaluate and replace variables before sending them to the shell, whereas single quotes ('...') treat everything as absolute text.


5. The Second Crash: Bad substitution

• The Error: script.sh.copy: 1: Bad substitution sh 'docker build -t ${imgname}:${env.BUILD_ID} .'
• Why it happened: You attempted to mix a Jenkins environment variable (env.BUILD_ID) inside a single-quoted (') shell block. Because of the single quotes, Jenkins bypassed the text and handed ${env.BUILD_ID} directly to the underlying Linux shell (/bin/sh). Linux doesn't understand dot-notation variables (like env.) inside curly braces, resulting in a shell-level crash.
• The solution: We reinforced the double-quotes rule ("..."), ensuring Jenkins resolves the dot-notation variable into a clean string before Linux ever sees it.

6. The Third Crash: illegal string body character

• The Error: illegal string body character after dollar sign... sh "docker run --rm -v $(pwd):/app..."
• Why it happened: Inside double quotes, Jenkins watches for any $ symbol to evaluate it as a Groovy variable. When it saw $(pwd), it assumed you were trying to use a Groovy variable, but the opening parenthesis ( broke Groovy's compilation rules.
• The solution: We looked at escaping syntax. By placing a backslash before the dollar sign (\$(pwd)), we told Jenkins: "Ignore this specific dollar sign; pass it safely to the Linux shell to run the local directory command." We also suggested using Jenkins' native ${WORKSPACE} variable as a cleaner alternative.

