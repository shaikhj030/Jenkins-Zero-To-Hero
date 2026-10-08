# Multi Stage Multi Agent

Set up a multi stage jenkins pipeline where each stage is run on a unique agent. This is a very useful approach when you have multi language application
or application that has conflicting dependencies.


 Jenkins Pipeline (Declarative)

In Jenkins, you can use the post block with the success condition to handle the cleanup.


error "Attempted to execute a step that requires a node context while ‘agent none’ was specified. Be sure to specify your own ‘node { ... }’ blocks when using ‘agent none’"

The error happens because you have agent none defined at the top level of your Jenkinsfile, and your post block is trying to run a shell command (sh) without a designated machine (node) to run it on.
When you use agent none, every post block that uses sh or bat must explicitly have its own agent or node wrapper, because Jenkins doesn't know where to execute those commands.
Here are the two ways to fix this error, depending on how your pipeline is structured:


Solution 1: Define the agent inside the post block (Recommended)

You can leave agent none at the top level, but explicitly tell Jenkins to find an available agent specifically for the success cleanup by adding agent any directly inside the success block.

post {
        success {
            // Allocate an agent specifically for this post-action
            node { 
                echo 'Tests passed! Cleaning up Docker images...'
                sh 'docker rm test-container'
                sh 'docker rmi my-image:${BUILD_NUMBER}'
                sh 'docker image prune -f'
            }
        }



Solution 2: Change global agent to agent any

If your entire pipeline (all stages) can run on the exact same machine, the simplest fix is to remove agent none from the very top of your script and change it to agent any (or your specific label). This makes an agent available to the post block automatically.

post {
        success {
            echo 'Tests passed! Cleaning up Docker images...'
            sh 'docker rm test-container'
            sh 'docker rmi my-image:${BUILD_NUMBER}'
            sh 'docker image prune -f'
        }
    }


Important Note on Docker Cleanup

If you use Solution 1, keep in mind that node might pick a different Jenkins agent than the one that ran the tests. If your Jenkins setup has multiple build nodes (slaves), the image won't exist on the second machine. If you have multiple nodes, you should use Solution 2 or restrict both the stage and the post block to the exact same node label.

Since you are running on a single Jenkins controller node, you don't have to worry about images being on different machines.
The easiest and cleanest fix is to change the top-level agent from agent none to agent any. This gives the post block immediate access to your node so it can run the cleanup commands without crashing.
Here is your exact, ready-to-use Jenkinsfile:

If you must use agent none at the top level (for instance, because you have other stages that shouldn't occupy a heavy build machine), you have to tell Jenkins exactly which node context to use inside your post blocks.
The syntax error Missing required parameter: "label" @ line 25 happened because your Jenkins configuration does not allow an empty node {} block. You must provide it with a label string, or use the alternative agent syntax allowed inside post blocks.
Here is the exact syntax to fix this while keeping agent none at the top.

Option 1: Provide a blank string label to node('') (Simplest Fix)

Passing an empty string '' tells Jenkins to just pick any available executor on your single controller node.

post {
        always {
            // Adding '' fixes the "Missing required parameter: label" error
            node('') {
                echo 'Cleaning up test containers...'
                sh 'docker rm -f test-container-${BUILD_NUMBER} || true'
            }
        }
        
        success {
            node('') {
                echo 'Tests passed! Removing the built Docker image...'
                sh 'docker rmi my-image:${BUILD_NUMBER}'
                sh 'docker image prune -f'
            }
        }

        
Option 2: Use the standard agent syntax inside the post block

If your Jenkins version is up-to-date, Declarative Pipeline allows you to use the cleaner agent { any } block directly inside the post-condition instead of nesting things in node('').

 post {
        always {
            agent { any } // Requests an execution environment for this block
            steps {
                echo 'Cleaning up test containers...'
                sh 'docker rm -f test-container-${BUILD_NUMBER} || true'
            }
        }
        
        success {
            agent { any }
            steps {
                echo 'Tests passed! Removing the built Docker image...'
                sh 'docker rmi my-image:${BUILD_NUMBER}'
                sh 'docker image prune -f'
            }
        }


