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
var enforcer = require("./templatesEnforcer");
var fs = require("fs");

module.exports.exec = function (fct, arg) {
    if (fct === require("child_process").exec) {
        console.log("Will autosanitize this call.");
        try {
            console.log("in try catch");
            var templatesFile = arguments[arguments.length - 2];
            var lineNo = arguments[arguments.length - 1];
            var templates = JSON.parse(fs.readFileSync(templatesFile).toString());
            console.log("log arguments",templatesFile,lineNo,templates);
            var actTemps = [];
            for (var i = 0; i < templates.length; i++) {
                if (templates[i].lineNo == lineNo)
                    actTemps.push(JSON.parse(templates[i].template));
            }
            if (enforcer("exec", arg, actTemps)) {
                console.log("Input matches the policy");
                if (arguments.length === 4)
                    fct(arg);
                else
                    fct(arg, arguments[2]);
            } else {
                console.log("Input does not match the policy, will skip the sink call.");
                return null;
            }
        } catch (e) {
            console.log("Error during autosanitization. Blocked input");
            return null;
        }
    } else {
        if (arguments.length === 4)
            return fct(arg);
        else
            return fct(arg, arguments[2]);
    }
};

module.exports.eval = function (fct, arg, templatesFile) {
    if (fct === eval) {
        console.log("Will autosanitize this call.");
        try {
            var templatesFile = arguments[arguments.length - 2];
            var lineNo = arguments[arguments.length - 1];
            var templates = JSON.parse(fs.readFileSync(templatesFile).toString());
            var actTemps = [];
            for (var i = 0; i < templates.length; i++) {
                if (templates[i].template && templates[i].lineNo == lineNo) {
                    try {
                        actTemps.push(JSON.parse(templates[i].template));
                    } catch(e) {
                        console.log(templates[i].template);
                        console.log(e.stack);
                    }
                }
            }
            if (enforcer("eval", arg, actTemps)) {
                console.log("Input matches the policy");
                return fct(arg);
            } else {
                console.log("Input does not match the policy, will skip the sink call.");
                return null;
            }
        } catch (e) {
            console.log("Error during autosanitization. Blocked input");
            console.log(arg);
            console.log(e.stack);
            console.log("");
            return null;
        }
    } else {
        return fct(arg);
    }
};