# Synode
This is a repository hosting the prototype implementation of Synode, a tool for automatically preventing code injection
attacks on Node.js. Synode was first described in the research paper *Synode: Understanding and Automatically
Preventing Injection Attacks on Node.js*.

# Directory structure
The current project contains the following folders:

1. **benchmarks** - the injections suite containing the 24 vulnerable modules together with malicious and benign
inputs.
2. **dynamic*** - the dynamic mechanism responsible for instrumenting the vulnerable modules and for enforcing the
templates at runtime.
3. **static** - the static analysis built on top of Google Closure, responsible for mining the templates.

# Installing dependencies and Synode hello world
In order to run the injection benchmarks, the following prerequisites are needed:

1. Ubuntu operating system,
2. mongodb,
3. npm.

Once all this dependencies are met, the vulnerable packages together with other required libraries can be installed
by running ```./install_dependencies.sh``` from the "benchmarks" folder. In order to install Synode globally on your
computer run ```npm install -g``` from the "dynamic" folder.

Once Synode is globally installed, we can run it on third-party npm modules, 
by executing the following command:

```synode path-to-package```

See some more examples in the "benchmarks/run_tests.sh" script.

# Rerun the evaluation: Synode defends against injection attacks
After installing the dependencies, the evaluation can be reproduced by running the script
```./run_tests.sh``` from the benchmarks directory. After the script completes, the results can be inspected by opening
the following files: "out.txt" and "out-without-synode.txt". For each of these files, the first column is the test name. The
second column represents the number of malicious inputs that successfully reached the sink. For example 2/3 means that
out of three malicious input tried, two reached the sink. The last column shows whether the benign input reached the
sink or not. Note that in the evaluation we only use one benign input for each module and thus a true/false flag
suffices for conveying this information.
