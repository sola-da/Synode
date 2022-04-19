# Mixin-Pro, for javascript multi-inheritance

Mixin is an easy way to repeatedly mix functionality into a prototypical
JavaScript class. It automatically takes care of the combination of overriding
prototype methods and invoking constructors. Moreover, it will notify a mixed
in constructor that is has been mixed into another class. This can be used to
construct dependent mixin hierarchies.

## Installation

Using npm:
```bash
    npm install mixin-pro
```

## Usage
```javascript
var Class = require("mixin-pro").createClass;

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
```

Of course, the mixin-pro can also be used in traditional style:

```javascript
    var mixin = require("mixin-pro");

    // traditional: create base class Foo, Foo1, Foo2, ...
    function Foo() {}
    Foo.prototype = {
       t0: function() { console.log('Foo->t0()'); }
    };

    // normal mixin: add features to existing classes
    Foo1 = mixin(Foo1, Foo2);
```

# Usage

## mixin.createClass()

* mixin.createClass(definition)
* mixin.createClass(base, definition)
* mixin.createClass([base1, base2, ...], definition)

```javascript
var definition = {
  constructor: function Bar3() {},
  t3: function() { return 't3'; },
};
```

`constructor` is required, and the function `Bar3` must be named, it will be used 
as your new class name.

When a new class object constructed, the constructors of the inherited base classes 
will be called in order.

## obj.instanceOf(BaseClass)

Check whether obj is an instance of BaseClass or derived class.

## obj.Super(methodName)

Get method with given name defined in base class, will return a function object, which 
can be called with call(obj, arg1, arg2, ...) or apply(obj, arguments).

Will throw error if the method is not defined in base class.

## mixin(base, mixed)

The call `mixin(base, mixed)` returns a new constructor that adds the
prototype for `mixed` at the back of the prototype chain for `base` and
invokes the constructors for both `base` and `mixed` in reverse order. If
`mixed` has function property `included`, then this function will be invoked
with `this = mixed` and the new constructor class as the single argument.

A constructor to be mixed in cannot have a prototype chain of its own
(i.e. it can't itself be the product of a mixin), however this functionality
can be achieved by calling mixin again inside the `included` callback.
Moreover, the same constructor can not be mixed in twice to the same
prototypical inheritance chain.

## ctor.included(base)

If `ctor` is mixed in to another constructor (e.g. by invoking `mixin(Base, ctor)`) then `ctor.included(base)` is called with the `this` set to the mixed in constructor (e.g. `ctor`) and is passed a single argument, the new constructor created by `mixin()`.  This is extremely useful for creating dependent chains of mixins (i.e. `Mixin1` requires `Mixin2`) since `mixin` itself can be invoked from within the `included` call.

If `included` returns a value, then it is used as the constructor for the call to `mixin` that invoked this function.  This behavior leads to the useful idiom:
```javascript
    Foo.included = function(ctor) {
        return mixin(ctor, EventEmitter);
    }
```
to mix the functionality of EventEmitter into any constructor that mixes in `Foo`.

## mixin.alias(obj, name, suffix, fun)

Can be used to override a method already defined on `obj`. It assigns `fun` to
`obj[name+'_with_"+suffix]` and reassigns the current value of `obj[name]` to
`obj[name+'_without_'+suffix]`. Finally it sets `obj[name]` to `fun`.  This is
similar to Rails' 'alias_method_chain`.
