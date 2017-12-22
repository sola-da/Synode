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
var tests = [
    {"name": "gm", "test": "./TestGm"},
    {"name": "libnotify", "test": "./TestLibnotify"},
    {"name": "printer", "test": "./TestPrinter"},
    {"name": "mixin-pro", "test": "./TestMixinPro"},
    {"name": "modulify", "test": "./TestModulify"},
    {"name": "mol-proto", "test": "./TestMolProto"},
    {"name": "mongoosify", "test": "./TestMongosify"},
    {"name": "mobile-icon-resizer", "test": "./TestMobileIconResizer"},
    {"name": "m-log", "test": "./TestMLog"},
    {"name": "mongo-parse", "test": "./TestMongoParse"},
    {"name": "mongoosemask", "test": "./TestMongooseMask"},
    {"name": "mongui", "test": "./TestMongui"},
    {"name": "mongo-edit", "test": "./TestMongoEdit"},
    {"name": "mock2easy", "test": "./TestMock2Easy"},
    {"name": "growl", "test": "./TestGrowl"},
    {"name": "mqtt-growl", "test": "./TestMqtt"},
    {"name": "chook-growl-reporter", "test": "./TestChookGrowlReporter"},
    {"name": "bungle", "test": "./TestBungle"},
    {"name": "fish", "test": "./TestFish"},
    {"name": "git2json", "test": "./TestGit2Json"},
    {"name": "kerb_request", "test": "./TestKerbRequest"},
    {"name": "keepass-dmenu", "test": "./TestKeepassDmenu"}
];
require("./RunTests.js")(tests, "./resources/out.txt", true);
