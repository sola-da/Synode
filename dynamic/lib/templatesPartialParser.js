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
 * @author Cristian-Alexandru Staicu on 22.10.16.
 */
var evalCompleters = [
    'x',
    'y', //added
    '"x"',
    "x.p",
    "{x:23}",
    23
];

var execCompleters = [
    "ls",
    "./file.txt"
];

var fs = require("fs");
var esprima = require("esprima");//TODO add dependencies in package.json
var shellParse = require("shell-parse"); //TODO add dependencies in package.json

function replaceHoles(template, comps, cb) {
    var holes = [];
    var re = /(¦V-HOLE¦|¦F-HOLE¦)/g;
    do {
        var m = re.exec(template);
        if (m) {
            holes.push(m.index);
        }
    } while (m);
    if (holes.length === 0) {
        cb(template);
        return;
    }
    var noCombinations = Math.pow(comps.length, holes.length);
    for (var i = 0 ; i < noCombinations; i++) {
        var instantiated = template;
        var no = i;
        for (var j = 0; j < holes.length; j++) {
            var current = no % comps.length;
            no = Math.floor(no / comps.length);
            instantiated = instantiated.replace(/(¦V-HOLE¦|¦F-HOLE¦)/, comps[current]);
            cb(instantiated);
        }
    }
}


function combineASTs(asts) {
    var result = asts[0];
    for (var i = 1; i < asts.length; i++) {
        result = combineTwo(result, asts[i]);
    }
    return result;
}

function combineTwo(astA, astB) {
    if (JSON.stringify(astA) === JSON.stringify(astB)) {
        return astA;
    }
    if (astA.type || astB.type) {
        if (astA.type !== astB.type) {
            return "HOLE";
        }
    }
    if (astA instanceof Array && astB instanceof  Array
        && astA.length === astB.length) {
        var result = [];
        for (var i = 0; i < astA.length; i++)
            result.push(combineTwo(astA[i], astB[i]));
        return result;
    } else if (astA instanceof Object
        && Object.keys(astA).length  === Object.keys(astB).length) {
        var result = {};
        var keys = Object.keys(astA);
        for (var i = 0; i < keys.length; i++)
            result[keys[i]] = combineTwo(astA[keys[i]], astB[keys[i]])
        return result;
    } else {
        if (astA != astB)
            return "HOLE";
        else
            return astA;
    }
}

function partiallyParse(templateBody, type) {
    var asts = [];
    if (type === "eval") {
        replaceHoles(templateBody, evalCompleters, function (str) {
            try {
                var ast = esprima.parse(str);
                asts.push(ast);
            } catch (e) {
            }
        });
    } else {
        replaceHoles(templateBody, execCompleters, function (str) {
            try {
                var ast = shellParse(str.replace("\"\"","''"));
                asts.push(ast);
            } catch (e) {
            }
        });
    }
    var tAst = combineASTs(asts);
    return tAst;
}

module.exports = partiallyParse;