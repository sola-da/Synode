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
 * Created by Cristian-Alexandru Staicu on 10.10.16.
 */
module.exports = function (cb) {

    var attackUtils = require("./AttackUtils.js");
    attackUtils.setup();
    var fs = require("fs");
    var oldDir = __dirname;
    process.chdir("./resources");
    fs.mkdir("attacks");
    var exec = require("child_process").exec;
    require('babel-polyfill');
    require('babel-register')({
        presets: ['es2015', 'stage-3'],
        comments: false,
        only: function (filename) {
            if (/bungle\/(?!node_modules)/.test(filename)) {
                return true;
            }
            if (/esx-bower\/(?!node_modules)/.test(filename)) {
                return true;
            }
            return false;
        }
    });

    attackUtils.deliverPayloads(attackUtils.payloadsExec, function (payload) {
        try {
            fs.writeFileSync("./attacks/" + payload, "");
            var Cli = require('bungle/lib/cli').Cli;
            new Cli().run();
            setTimeout(function () {
                fs.unlinkSync("./attacks/" + payload, "");
            }, 300);
        } catch (e) {
            //console.log(e);
        }
    }, function (result, filesWithSinks) {
        setTimeout(function () {
            var benignInput = "decentBenignFileName.txt";
            fs.writeFileSync("./attacks/" + benignInput, "");
            var Cli = require('bungle/lib/cli').Cli;
            new Cli().run();
            setTimeout(function () {
                attackUtils.printCallStrings();
                result += " " + attackUtils.observedString(benignInput);
                attackUtils.deleteFolderRecursive("./attacks");
                attackUtils.deleteFolderRecursive("./.bungle");
                process.chdir(oldDir);
                cb(__filename, result, filesWithSinks);
            }, 1000);
        }, 1000);
    });
};
