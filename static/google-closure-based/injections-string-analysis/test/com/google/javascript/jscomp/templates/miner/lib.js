function f(param) { 
	var objLit = {y: "abba"};
	objLit.x = +23;
	var cstArr = ["test"]	
	var f = "txt";
	var g = f;
	var x = (objLit.x === 23)?"tst":"2x2";
	x = x + param;
	x = x + (objLit.x || "ana");
	x += objLit.y;
	var y = "secondPart"
	if (true)
		x = x + y + "?" + 12;
	eval(x);
	var arr = [25, param];
	arr.push(23);
	arr.push(" test");
	arr.push(x);
	arr.push(cstArr[0]);
	arr.push(null);
	this.x = 25;
	arr.push(this.x);
	arr.push(new String(23));
	eval(arr.join(" "));
	var newStuff = "myTest"
	if (true)
		newStuff += param;
	else 
		newStuff += objLit.y;
	eval(newStuff);
	var flag;
	flag = flag || cond && "--help"
	eval("exec " + flag); 
}
module.exports.f = f;