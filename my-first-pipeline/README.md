# A simple jenkins pipeline to verify if the docker slave configuration is working as expected.#

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

