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
 * @author Cristian-Alexandru Staicu on 13.10.16.
 */
var fs = require("fs");
var esprima = require("esprima");
var shellParse = require("shell-parse");

var policy = {
    eval: ["Literal", "Identifier", "Property", "ArrayExpression", "ObjectExpression", "MemberExpression", "ExpressionStatement"],
    exec: ["literal"]
};

function matchPolicy(ast, type) {
    return matchWithWhiteListEff(ast, type);

}

function checkTypesF(ast, f) {

    if (ast && ast instanceof Object) {
        if (ast.type) {
            if (!f(ast.type))
                return false;
        }
        var keys = Object.keys(ast);
        for (var i = 0; i < keys.length; i++) {
            if (ast[keys[i]] != ast)
                if (!checkTypesF(ast[keys[i]], f))
                    return false;
        }
    }
    return true;
}

function matchWithWhiteListEff(ast, type) {
    var res;
    if (type === "eval") {
        res = checkTypesF(ast, function (nType) {
            if (policy.eval.indexOf(nType) === -1) {
                return false;
            }
            return true;
        })
    } else {
        res = checkTypesF(ast, function (nType) {
            if (policy.exec.indexOf(nType) === -1)
                return false;
            return true;
        })
    }
    return res;
}

function matchesTrees(astA, astB, type) {
    if (astB === "HOLE") {
        return matchPolicy(astA, type);
    }
    if (astA && !astB)
        return false;
    if (astA instanceof Object) {
        var result = true;
        var keys = Object.keys(astA);
        var keysB = Object.keys(astB);
        if (keys.length != keysB.length)
            return false;
        var result = true;
        for (var i = 0; i < keys.length; i++) {
            if (astB[keys[i]] === "HOLE") {
                result = result && matchPolicy(astA ? astA[keys[i]] : undefined, type);
            } else if (!astB || !astB.hasOwnProperty(keys[i])) {
                return false;
            }
        }
        for (var i = 0; i < keys.length; i++) {
            if (!matchesTrees(astA[keys[i]], astB[keys[i]], type))
                return false;
        }
        return true;
    }
    return astA === astB;
}

function checkInput(type, input, currTemplates) {
    var currAST;
    try {
        if (type === "eval") {
            currAST = esprima.parse(input.toString());
        }
        else {
            currAST = shellParse(input.replace("\"\"","''"));
        }
        for (var j = 0; j < currTemplates.length; j++) {
            if (matchesTrees(currAST, currTemplates[j], type)) {
                return true;
            }
        }
    } catch(e) {
    }
    return false;
}

module.exports = checkInput;