'use strict';

function A() {
  this._va = 0;
  console.log('A');
}
A.prototype = {
  va: 1,
  fa: function() {
    console.log('A->fa()');
  }
};

function B() {
  this._vb = 0;
  console.log('B');
}
B.prototype = {
  vb: 1,
  fb: function() {
    console.log('B->fb()');
  }
};

function C() {
  this._vc = 0;
  console.log('C');
}
C.prototype = {
  vc: 1,
  fc: function() {
    console.log('C->fc()');
  }
};

function D(){
  this._vd = 0;
  console.log('D');
}
D.prototype = {
  vd: 1,
  fd: function() {
  this.fa();
  this.fb();
  this.fc();
  console.log('D->fd()');
  }
};

var mixin = require('../mixin');

D = mixin(D, A);
D = mixin(D, B);
D = mixin(D, C);

var d = new D();

console.log(d);
console.log(d.constructor.name);

d.fd();

var a = new A();

console.log(a);
console.log(a.__proto__);
console.log(a.va);
