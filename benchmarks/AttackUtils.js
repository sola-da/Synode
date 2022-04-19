/**
 * Copyright 2016 Software Lab, TU Darmstadt, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Created by Cristian-Alexandru Staicu on 03.03.16.
 */
var callStrings = [];
var fs = require("fs");
const TIMEOUT_CHECK = 3000;
const DUMMY_FILE = "tmp-success-23-42" + Math.random();
const PROP_FLAG = "my-awesome-prop-23-42";
var path = require("path");
const SINK_VALUES_PATH = path.resolve(__dirname, "./resources/sink-values.txt");
var oldCons = console.log;
var filesContainingSinks = {};
var currentPayloads;

// wrap， print callsite messages
function getWrapper(fct, type) {
  //console.log("in wrapper");
  var f = function () {
    var file = _getCallerFile(); //stack position or undefined
    if (file) {
      //callStrings []
      callStrings.push("[" + type + ":" + file + "]" + arguments[0]);
      //callStrings: ["[eval:module.js:456]console.log('1')"];
      //callstack & args of eval
      console.log("callStrings", callStrings);
      filesContainingSinks[file.replace(/:.*/, "")] = 23;
      //filesContainingSinks { 'module.js': 23 }
    } else callStrings.push("[" + type + "/?]" + arguments[0]);
    return fct.apply(this, arguments);
  };
  f.isWrapped = true;
  return f;
}

function print(str) {
  fs.appendFileSync(SINK_VALUES_PATH, str + "\n*****\n");
}

function checkPostTest(payload) {
  var result = 0;
  if (console[PROP_FLAG]) {
    result = 1;
    delete console[PROP_FLAG];
    console[PROP_FLAG] = undefined;
  }
  if (fs.existsSync(DUMMY_FILE)) {
    result = 1;
    fs.unlinkSync(DUMMY_FILE);
  }
  if (result === 1) {
    for (var i = 0; i < callStrings.length; i++) {
      if (
        callStrings[i].indexOf(payload) !== -1 ||
        containsPayload(callStrings[i])
      )
        //why we can use this to get mal/beg info
        print("[malicious]" + callStrings[i]);
      else print("[benign]" + callStrings[i]);
    }
  }

  return result;
}

function containsPayload(str) {
  for (var i = 0; i < currentPayloads.length; i++) {
    if (str.indexOf(currentPayloads[i]) !== -1) {
      return true;
    }
  }
  return false;
}

function clearPreTest() {
  callStrings = [];
  oldCons = console.log;
  if (fs.existsSync(DUMMY_FILE)) {
    fs.unlinkSync(DUMMY_FILE);
  }
}

//deliverPayload({ succeded: 0, count: 0 }, payloads, cb, tcb);
function deliverPayload(result, payloads, cb, tcb) {
  clearPreTest();
  try {
    //cb(attackUtils.payloadsEval[result.count]);
    //cb('console[\'my-awesome-prop-23-42\'] = 23;') ???
    cb(payloads[result.count]);
  } catch (e) {
    //console.log(e);
  }
  setTimeout(function () {
    result.succeded += checkPostTest(payloads[result.count]);
    result.count++;
    if (result.count < payloads.length)
      deliverPayload(result, payloads, cb, tcb);
    else if (tcb) {
      clearPreTest();
      tcb(
        result.succeded + "/" + result.count,
        Object.keys(filesContainingSinks)
      );
    }
  }, TIMEOUT_CHECK);
}

// get current path
function _getCallerFile() {
  try {
    var err = new Error();
    var callerfile;
    var currentfile;

    var oldE = Error.prepareStackTrace;
    Error.prepareStackTrace = function (err, stack) {
      return stack;
    };
    //customization why?

    // /home/pc12/ming/Synode/benchmarks/AttackUtils.js
    currentfile = err.stack.shift().getFileName();
    // why use this method to get current path?

    while (err.stack.length) {
      var entry = err.stack.shift();
      callerfile = entry.getFileName();

      if (currentfile !== callerfile) {
        // a loop until we get module.js why module.js?
        Error.prepareStackTrace = oldE;
        console.log(callerfile + ":" + entry.getLineNumber());
        //module.js:456
        return callerfile + ":" + entry.getLineNumber();
      }
    }
  } catch (err) {}
  Error.prepareStackTrace = oldE;
  return undefined;
}

//_getCallerFile();
//why when node ./TestsNode5.7.1.js we can run below function?
//eval = getWrapper(eval, "eval");
//console.log(eval("console.log('1')"), "in attack js");

module.exports = {
  payloadsExec: [
    'a;" touch ' + DUMMY_FILE + " #",
    "; touch " + DUMMY_FILE + " #",
    "a'; touch " + DUMMY_FILE + " #",
    " | touch " + DUMMY_FILE + " #",
    "`touch " + DUMMY_FILE + "`",
    "$(touch " + DUMMY_FILE + ")",
    '"$(touch ' + DUMMY_FILE + ')"',
  ],
  payloadsEval: [
    "console['" + PROP_FLAG + "'] = 23;",
    "Object.defineProperty(console, '" + PROP_FLAG + "',{value:23});",
  ],
  setup: function (m) {
    filesContainingSinks = {};
    if (!require("child_process").exec.isWrapped)
      require("child_process").exec = getWrapper(
        require("child_process").exec,
        "exec"
      );
    if (!eval.isWrapped) eval = getWrapper(eval, "eval");
  },
  deliverPayloads: function (payloads, cb, tcb) {
    currentPayloads = payloads;
    deliverPayload({ succeded: 0, count: 0 }, payloads, cb, tcb);
  },
  deleteFolderRecursive: function (path) {
    var fs = require("fs");
    if (fs.existsSync(path)) {
      fs.readdirSync(path).forEach(function (file, index) {
        var curPath = path + "/" + file;
        if (fs.lstatSync(curPath).isDirectory()) {
          // recurse
          deleteFolderRecursive(curPath);
        } else {
          // delete file
          fs.unlinkSync(curPath);
        }
      });
      fs.rmdirSync(path);
    }
  },
  observedString: function (str) {
    for (var i = 0; i < callStrings.length; i++)
      if (callStrings[i].indexOf(str) != -1) return true;
    return false;
  },
  getCallStrings: function () {
    return callStrings;
  },
  printCallStrings: function () {
    for (var i = 0; i < callStrings.length; i++) {
      print("[benign]" + callStrings[i]);
    }
  },
  DUMMY_FILE: DUMMY_FILE,
};
