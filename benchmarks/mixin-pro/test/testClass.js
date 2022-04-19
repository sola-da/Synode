'use strict';

var Class = require('../mixin').createClass;

var A = Class({
  constructor: function A(){
    this._fa = 0;
    console.log('A');
  },
  va: 'a',
  fa: function() {
    console.log('A->fa()');
  },
});

var B = Class({
  constructor: function B(){
    this._fb = 0;
    console.log('B');
  },
  vb: 'b',
  fb: function() {
    console.log('B->fb()');
  },
});

var C = Class({
  constructor: function C(){
    this._fc = 0;
    console.log('C');
  },
  vc: 'c',
  fc: function() {
    console.log('C->fc()');
  },
});

var D = Class(C, {
  constructor: function D(){
    this._fd = 0;
    console.log('D');
  },
  vd: 'd',
  fd: function() {
    console.log('D->fd()');
  },
});

var E = Class([A, B, D], {
  constructor: function E(){
    this._fe = 0;
    console.log('E');
  },
  ve: 'e',
  fa: function() {
    this.Super('fa').apply(this, arguments);
    console.log('E->fa()');
  },
  fe: function() {
    console.log('\naccess properties')
    console.log(this.va);
    console.log(this.vb);
    console.log(this.vc);
    console.log(this.vd);
    console.log(this.ve);

    console.log('\naccess methods')
    this.fa();
    this.fb();
    this.fc();
    this.fd();
    console.log('E->fe()');
  },
});

console.log('\ninit with constructors')
var e = new E();

console.log('\ncheck initialized data')
console.log(e);

console.log('\ncheck instanceOf()')
console.log('e.instanceOf(A) -> ' + e.instanceOf(A));
console.log('e.instanceOf(B) -> ' + e.instanceOf(B));
console.log('e.instanceOf(C) -> ' + e.instanceOf(C));
console.log('e.instanceOf(D) -> ' + e.instanceOf(D));
console.log('e.instanceOf(E) -> ' + e.instanceOf(E));

e.fe();

console.log('\ncall method in base class with Super()');
e.fa(1, 2, 3);

