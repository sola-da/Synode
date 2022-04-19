'use strict';

var Class = require('../mixin').createClass;

// use case 1: create a base class
var Bar1 = Class({
  constructor: function Bar1() {},
  t1: function() { console.log('Bar1->t1()'); },
});

// use case 2: create a class and inherit from a base class
var Bar2 = Class(Bar1, {
  constructor: function Bar2() {},
  t2: function() { console.log('Bar2->t2()'); },
});

// use case 3: create a class and inherit from multi base classes
var Bar3 = Class([Bar1, Bar2], {
  constructor: function Bar3() {},
  t3: function() { console.log('Bar3->t3()'); },
  t1: function() {
    // call same name method in super class, with apply() or call()
    this.Super('t1').apply(this, arguments);
    // this.Super('t1').call(this, 1, 2, 3);

    console.log('Bar3->t1()');
  },
});

// check an object is instance of the inherited base class
var bar3 = new Bar3();
console.log(bar3.instanceOf(Bar3)); // true
console.log(bar3.instanceOf(Bar1)); // true
console.log(bar3.instanceOf(Bar2)); // true
bar3.t1(); // Bar1->t1()   Bar3->t1()
bar3.t2(); // Bar2->t2()
bar3.t3(); // Bar3->t3()
