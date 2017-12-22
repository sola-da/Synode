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
 * @author Cristian-Alexandru Staicu on 12.12.17.
 */
package com.google.javascript.jscomp.strategies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.javascript.jscomp.templates.TemplateEntry;
import com.google.javascript.jscomp.templates.operations.BinaryOperation;
import com.google.javascript.jscomp.templates.operations.ConstantString;
import com.google.javascript.jscomp.templates.operations.MultiOperation;
import com.google.javascript.jscomp.templates.operations.VarDependency;

public class RegexStrategy implements EvaluationStrategy {

	public final static String ALL_CHARS_HOLE = "(.*)";
	public final static String ALL_CHARS_HOLE_ESCAPED = "\\(\\.\\*\\)";

	public String escapeRegExp(String str) {
		return str.replaceAll("([\\-\\[\\]\\/\\{\\}\\(\\)\\*\\+\\?\\.\\\\^\\$\\|])", "\\\\$0");
	}

	@Override
	public Set<Object> evaluate(ConstantString templateEntry) {
		if (templateEntry.getStrVal() == "[]") {
			return createSet(new ArrayList<Object>());
		}
		if (templateEntry.getStrVal() == "{}") {
			return createSet(new ObjectModel("{}"));
		}
		return createSet(escapeRegExp(templateEntry.getStrVal()));
	}

	@Override
	public Set<Object> evaluate(VarDependency templateEntry) {
		return createSet(new ObjectModel(RegexStrategy.ALL_CHARS_HOLE));
	}

	@Override
	public Set<Object> evaluate(BinaryOperation templateEntry) {
		Object lObj = first(templateEntry.getLeft().evaluate(this));
		Object rObj = first(templateEntry.getRight().evaluate(this));
		if (templateEntry.getOperation() == "+" && isStringOrObj(lObj) && isStringOrObj(rObj)) {
			return createSet(lObj.toString() + rObj.toString());
		}
		if (templateEntry.getOperation() == "alternative") {
			return createSet("((" + lObj.toString() + ")|(" + rObj.toString() + "))");
		}
		if (templateEntry.getOperation() == "append" && lObj != null && (lObj instanceof ObjectModel)) {
			ObjectModel oModel = (ObjectModel) lObj;
			String propName = BinaryOperation.getNameOfProp(templateEntry.getLeft());
			if (propName != null)
				oModel.setValueForProperty(propName, rObj);
			return createSet(oModel);
		}
		if (templateEntry.getOperation() == "access" && lObj != null && (lObj instanceof ObjectModel)) {
			ObjectModel oModel = (ObjectModel) lObj;
			String property = BinaryOperation.getNameOfProp(templateEntry);
			if (property != null && oModel.hasProperty(property)) {
				return createSet(oModel.getValueForProperty(property));
			} else {
				return createSet(lObj);
			}
		}
		return createSet(RegexStrategy.ALL_CHARS_HOLE);
	}

	private boolean isStringOrObj(Object obj) {
		return (obj != null) && ((obj instanceof String) || (obj instanceof Object));
	}

	@Override
	public Set<Object> evaluate(MultiOperation templateEntry) {
		Object fObj = null;
		if (templateEntry.getFirst() != null)
			fObj = first(templateEntry.getFirst().evaluate(this));
		List<Object> pObjs = new ArrayList<Object>();
		for (TemplateEntry te : templateEntry.getRest())
			pObjs.add(first(te.evaluate(this)));
		if (templateEntry.getOperation() == "array") {
			if (templateEntry.getFirst() != null)
				pObjs.add(0, fObj);
			return createSet(pObjs);
		}
		if (templateEntry.getOperation() == "push" && fObj != null && (fObj instanceof ArrayList)) {
			((ArrayList) fObj).add(pObjs.get(0));
			return createSet(fObj);
		}
		if (templateEntry.getOperation() == "join" && fObj != null && (fObj instanceof ArrayList)) {
			ArrayList list = (ArrayList) fObj;
			String res = "";
			for (Object o : list) {
				String oStringRep = "";
				if (o instanceof TemplateEntry)
					oStringRep = first(((TemplateEntry) o).evaluate(this)).toString();
				else
					oStringRep = o.toString();
				res += (res.equals("") ? "" : pObjs.get(0).toString()) + oStringRep;
			}
			return createSet(res);
		}
		return createSet(RegexStrategy.ALL_CHARS_HOLE);
	}

	Set<Object> createSet(Object obj) {
		HashSet<Object> res = new HashSet<Object>();
		res.add(obj);
		return res;
	}

	Object first(Set<Object> set) {
		return set.iterator().next();
	}

	public String prettyPrint(Object evalTmp) {
		return "^" + evalTmp + "$";
	}
}
