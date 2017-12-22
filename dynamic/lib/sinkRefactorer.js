/**
 * Created by Cristian-Alexandru Staicu on 14.12.17.
 */
var esprima = require("esprima");
var escodegen = require("escodegen");

function refactor(content, locs, outFile) {
    if (content.indexOf("/* Instrumented by Synode */") !== -1)
        return content;
    try {
        var ast = esprima.parse(content, {loc:true, range:true, comment:true, tokens:true});
    } catch (e) {
        console.log("\nPreprocessor: Error when parsing " + process.argv[2] + ". Will ignore this file.\n" + e);
        return content;
    }
    ast = escodegen.attachComments(ast, ast.comments, ast.tokens);
    traverse(ast, null, null, null, preVisitor, postVisitor, locs, outFile);
    return "/* Instrumented by Synode */\n" + escodegen.generate(ast, {comment:true});
}

function traverse(object, parent, grandpa, key, preVisitor, postVisitor, locs, outFile) {
    var key, child;

    if (preVisitor.call(null, object, parent, grandpa, key, locs, outFile) === false) {
        return;
    }
    for (key in object) {
        if (object.hasOwnProperty(key)) {
            child = object[key];
            if (typeof child === 'object' && child !== null) {
                traverse(child, object, parent, key, preVisitor, postVisitor, locs, outFile);
            }
        }
    }
    postVisitor.call(null, object, parent);
}

function preVisitor(node, parent, grandpa, key, locs, outFile) {
    if (node.type === "CallExpression") {
        var lineNo = node.loc.start.line;
        var columnNo = node.loc.start.column;
        var type;
        if (node.callee && node.callee.name === "eval")
            type = "eval";
        else
            type = "exec";
        if (contains(locs, lineNo, columnNo)) {
            var oldNode = node;
            var tempAST = esprima.parse("f('" + outFile + "', " + lineNo + ");").body[0].expression;
            if (oldNode.arguments.length === 1) {
                node = esprima.parse("require('synode')." + type + "(fct, arg1, templatesFile, lineNo);").body[0].expression;
                node.arguments[0] = oldNode.callee;
                node.arguments[1] = oldNode.arguments[0];
                node.arguments[2] = tempAST.arguments[0];
                node.arguments[3] = tempAST.arguments[1];
            } else if (oldNode.arguments.length === 2) {
                node = esprima.parse("require('synode')." + type + "(fct, arg1, arg2, templatesFile, lineNo);").body[0].expression;
                node.arguments[0] = oldNode.callee;
                node.arguments[1] = oldNode.arguments[0];
                node.arguments[2] = oldNode.arguments[1];
                node.arguments[3] = tempAST.arguments[0];
                node.arguments[4] = tempAST.arguments[1];
            }
            parent[key] = node;
        }
    }
}

function contains(locs, ln, col) {
    for (var i = 0; i < locs.length; i++) {
        if (locs[i].lineNo == ln && locs[i].columnNo == col) {
            return true;
        }
    }
    return false;
}

function postVisitor(node, parent) {

}

module.exports = refactor;