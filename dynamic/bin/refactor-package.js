#!/usr/bin/env node
/**
 * Copyright 2017 Software Lab, TU Darmstadt, Germany
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
 * Created by Cristian-Alexandru Staicu on 12.12.17.
 */
var fs = require("fs");
var path = require("path");
var spawn = require("child_process").spawn;
var pParser = require(path.resolve(__dirname, "../lib/templatesPartialParser.js"));
var refactorer = require(path.resolve(__dirname, "../lib/sinkRefactorer.js"));

if (process.argv.length < 3) {
    console.log("You need to provide the package name");
    process.exit(1);
} else {
    console.log("Synode is running on package " + process.argv[2]);
    var pathModule = path.resolve(process.argv[2]);
    var res = getFilesToInstr(pathModule);
    var synodeDir = pathModule + "/.synode";
    if (fs.existsSync(synodeDir))
        console.log("Synode was already run on this package. Will not instrument.")
        // deleteFolderRecursive(synodeDir);
    else {
        fs.mkdirSync(synodeDir);
        var jarPath = path.resolve(__dirname, "../depd/static-analysis.jar");
        console.log("Looking for sinks in " + res.length + " files");
        mineTemplates(0);
    }
    function mineTemplates(index) {
        var fileName = res[index];
        console.log("Analyzing " + fileName);
        var output = fs.readFileSync(fileName).toString();
        if (output.indexOf("eval") != -1 || output.indexOf("exec") != -1) {
            var outFile = path.resolve(synodeDir, index + ".txt");
            var sa = spawn("java", ["-jar", jarPath, fileName, outFile]);
            sa.stdout.on('data', function (data) {
                console.log(data.toString());
            });
            sa.on('close', function (code) {
                console.log("Spawn exited with code " + code);
                if (fs.existsSync(outFile)) {
                    var temps = JSON.parse(fs.readFileSync(outFile).toString());
                    console.log("Partially parsing " + temps.length + " templates");
                    var seen = [];
                    var locsToInst = [];
                    var needsInstrum = false;
                    for (var i = 0; i < temps.length; i++) {
                        var isSeen = false;
                        for (var j = 0; j < seen.length; j++) {
                            if (temps[i].template === seen[j].template)
                                isSeen = true;
                        }
                        if (!isSeen) {
                            locsToInst.push({lineNo: temps[i].lineNo, columnNo: temps[i].columnNo});
                            if (temps[i].template.indexOf("¦V-HOLE¦") !== -1 || temps[i].template.indexOf("¦F-HOLE¦") !== -1)
                                needsInstrum = true;
                            console.log(i + "/" + temps.length + ":" + temps[i].type);
                            console.log(temps[i].template);
                            temps[i].template = JSON.stringify(pParser(temps[i].template, temps[i].type));
                            console.log(temps[i].template);
                            seen.push(temps[i]);
                        }
                    }
                    if (needsInstrum) {
                        var newOut = refactorer(output, locsToInst, outFile);
                        console.log(newOut);
                        fs.writeFileSync(fileName, newOut)
                    }
                    fs.writeFileSync(outFile, JSON.stringify(temps));
                }
                if (res && index < res.length - 1) {
                    mineTemplates(index + 1);
                }
            });
        } else {
            mineTemplates(index + 1);
        }
    }
}

function getFilesToInstr(path) {
    var res = [];
    if (fs.existsSync(path)) {
        var list = fs.readdirSync(path);
        list.forEach(function (file, index) {
            var curPath = path + "/" + file;
            //synode will not run on these directories
            if (fs.lstatSync(curPath).isDirectory() && file != "node_modules"
                && file != "public" && file != "test" && file != "assets"
                && file != "bin"
                && file != "min" && file != "locale") { // recurse
                res = res.concat(getFilesToInstr(curPath));
            } else {
                if (file.toString().match(/.*\.js$/)) {
                    res.push(curPath);
                }
            }
        });
    }
    return res;
}

function deleteFolderRecursive(path) {
    if (fs.existsSync(path)) {
        var list = fs.readdirSync(path);
        list.forEach(function (file, index) {
            var curPath = path + "/" + file;
            if (fs.lstatSync(curPath).isDirectory()) { // recurse
                deleteFolderRecursive(curPath);
            } else { // delete file
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(path);
    }
}