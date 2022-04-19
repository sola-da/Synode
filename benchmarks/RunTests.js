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
 *
 * For changing the node version:
 * sudo ./node_modules/n/bin/n
 */
//var tests = [{ name: "mixin-pro", test: "./TestMixinPro" }];
//require("./RunTests.js")(tests, "./resources/out.txt", true);
module.exports = function (tests, file, appendHeader) {
  const fs = require("fs");
  const path = require("path");
  const process = require("process");
  //'/home/pc12/ming/Synode/benchmarks/resources/out.txt'
  var fileRes = path.resolve(file);
  var filesWithsinks = {};
  //prepare();
  setTimeout(start, 3000);
  //'/home/pc12/ming/Synode/benchmarks/resources/sink-values.txt'
  const OUT_FILE = path.resolve("./resources/sink-values.txt");

  function start() {
    var oldPE = process.exit;
    process.exit = function () {}; // why change exit function
    //why 0 (maybe we can drop) just append header
    fs.appendFile(OUT_FILE, "test:" + tests[0].name + "\n");
    fs.appendFile(OUT_FILE, "*****\n");
    console.log("Running " + tests[0].test);

    // we should see the call method of TestMixinPro, kind of weird
    //maybe we can construct a better function, in this the args must be 3
    //we can log all the message
    require(tests[0].test)(aggregateResults);
    //we can log these var
    var results = {};
    var inputs = {};
    var count = 0;
    var sprintf = require("sprintf-js").sprintf;

    function aggregateResults(file, result, sinksFiles) {
      results[file] = result;
      for (var i = 0; i < sinksFiles.length; i++)
        filesWithsinks[sinksFiles[i]] = 23;
      count++;
      if (count === tests.length) {
        console.log("");
        if (appendHeader)
          //why use this can get result
          fs.appendFileSync(fileRes, sprintf("%70s", "=== Test results ===\n"));
        var tsts = Object.keys(results);
        for (var t in tsts) {
          fs.appendFileSync(
            fileRes,
            sprintf(
              "%50s %20s\n",
              tsts[t].replace(/.*\//, ""),
              results[tsts[t]]
            )
          );
        }
        console.log("");
        console.log("Files containing sinks");
        var filesSinksKeys = Object.keys(filesWithsinks);
        for (var i = 0; i < filesSinksKeys.length; i++)
          console.log(filesSinksKeys[i]);
        oldPE(0);
      } else {
        fs.appendFile(OUT_FILE, "test:" + tests[count].name + "\n");
        fs.appendFile(OUT_FILE, "*****\n");
        console.log("Running " + tests[count].test);
        require(tests[count].test)(aggregateResults);
      }
    }
  }

  function prepare(cb) {
    var exec = require("child_process").exec;
    exec("mongod", cb);
    exec(
      "mosquitto -p 1884",
      // function (a, b) {
      // //exec("mosquitto -p 1880", function (a, b) {
      //     console.log(`${a}`);// error message, get object.toString,rather than whole object.
      //     console.log(b); //null in my situation
      // }
      (error, stdout, stderr) => {
        if (error) {
          console.error(`exec error: ${error}`);
          return;
        }
        console.log(`stdout: ${stdout}`);
        console.error(`stderr: ${stderr}`);
      }
    );
  }

  process.on("uncaughtException", function (a) {
    console.log(a);
  });
};
