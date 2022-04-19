var sprintf = require("sprintf-js").sprintf,
  vsprintf = require("sprintf-js").vsprintf;

console.log(sprintf("%2$s %3$s a %1$s", "cracker", "Polly", "wants"));
vsprintf("The first 4 letters of the english alphabet are: %s, %s, %s and %s", [
  "a",
  "b",
  "c",
  "d",
]);

console.log("in test.js");
