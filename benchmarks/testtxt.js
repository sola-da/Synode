const path = require("path");
const fs = require("fs");
const pathmodule = path.resolve("./node_modules/mixin-pro");
const outFile = path.resolve(pathmodule + "/.synode/0.txt");

console.log(outFile);
const output = JSON.parse(fs.readFileSync(outFile).toString());
console.log(output);
