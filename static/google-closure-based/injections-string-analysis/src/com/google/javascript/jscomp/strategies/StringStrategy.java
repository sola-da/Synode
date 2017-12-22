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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.javascript.jscomp.templates.TemplateEntry;
import com.google.javascript.jscomp.templates.operations.BinaryOperation;
import com.google.javascript.jscomp.templates.operations.ConstantString;
import com.google.javascript.jscomp.templates.operations.MultiOperation;
import com.google.javascript.jscomp.templates.operations.VarDependency;

public class StringStrategy implements EvaluationStrategy {

	public final static char HOLE_CHAR = ((char) 166);
	public final static String CALL_H_PLACEHOLDER = HOLE_CHAR + "F-HOLE" + HOLE_CHAR;
	public final static String VAR_H_PLACEHOLDER = HOLE_CHAR + "V-HOLE" + HOLE_CHAR;

	@Override
	public Set<Object> evaluate(ConstantString templateEntry) {
		if (templateEntry.getStrVal() == "[]") {
			return set(new ArrayList<Object>());
		}
		if (templateEntry.getStrVal() == "{}") {
			return set(new ObjectModel("{}"));
		}
		if (templateEntry.getStrVal() == "undefined") {
			return set("");
		}
		return set(templateEntry.getStrVal());
	}

	@Override
	public Set<Object> evaluate(VarDependency templateEntry) {
		return set(new ObjectModel(StringStrategy.VAR_H_PLACEHOLDER));
	}

	@Override
	public Set<Object> evaluate(BinaryOperation templateEntry) {
		Set<Object> lObjSet = templateEntry.getLeft().evaluate(this);
		Set<Object> rObjSet = templateEntry.getRight().evaluate(this);
		Set<Object> res = new HashSet<Object>();
		if (templateEntry.getOperation() == "+") {
			for (Object objLeft : lObjSet)
				for (Object objRight : rObjSet)
					res.add(objLeft.toString() + objRight.toString());
			return res;
		}
		if (templateEntry.getOperation() == "append") {
			for (Object lObj : lObjSet)
				if (lObj != null && (lObj instanceof ObjectModel)) {
					ObjectModel oModel = (ObjectModel) lObj;
					for (Object rObj : rObjSet) {
						ObjectModel newOModel = oModel.clone();
						String propName = BinaryOperation.getNameOfProp(templateEntry.getLeft());
						if (propName != null)
							newOModel.setValueForProperty(propName, rObj);
						res.add(newOModel);
					}
				} else {
					res.add(StringStrategy.VAR_H_PLACEHOLDER);
				}
			return res;
		}
		if (templateEntry.getOperation() == "alternative") {
			res.addAll(lObjSet);
			res.addAll(rObjSet);
			return res;
		}
		if (templateEntry.getOperation() == "access") {
			for (Object lObj : lObjSet)
				if (lObj != null && (lObj instanceof ObjectModel)) {
					ObjectModel oModel = (ObjectModel) lObj;
					String property = BinaryOperation.getNameOfProp(templateEntry);
					if (property != null && oModel.hasProperty(property)) {
						res.add(oModel.getValueForProperty(property));
					} else {
						// res.add(lObj);
						res.add(StringStrategy.VAR_H_PLACEHOLDER);
					}
				} else {
					res.add(StringStrategy.VAR_H_PLACEHOLDER);
				}
			return res;
		}
		return set(StringStrategy.CALL_H_PLACEHOLDER);
	}

	@Override
	public Set<Object> evaluate(MultiOperation templateEntry) {
		Set<Object> res = new HashSet<Object>();
		Set<Object> fObjSet = null;
		if (templateEntry.getFirst() != null)
			fObjSet = templateEntry.getFirst().evaluate(this);
		List<Set<Object>> pObjs = new ArrayList<Set<Object>>();
		for (TemplateEntry te : templateEntry.getRest())
			pObjs.add(te.evaluate(this));
		if (templateEntry.getOperation() == "replace") {
			// hacky model for replace when base is string and first argument is string
			try {
				if (fObjSet.size() == 1) {
					Object base = fObjSet.iterator().next();
					if (base instanceof String) {
						Set<Object> firstSet = pObjs.get(0);
						Set<Object> secondSet = pObjs.get(1);
						if (firstSet.size() == 1 && secondSet.size() == 1) {
							Object firstParam = firstSet.iterator().next();
							Object secondParam = secondSet.iterator().next();
							if (firstParam instanceof String) {

								if (((String) firstParam).matches("/.*/g")) {
									String fp = ((String) firstParam).replaceAll("^/", "").replaceAll("/g$", "");

									res.add(((String) base).replaceAll(fp, secondParam.toString()));
									return res;
								}

							}
						}

					}
				}
			} catch (Exception e) {
				return set(StringStrategy.CALL_H_PLACEHOLDER);
			}
		}
		if (templateEntry.getOperation() == "array") {
			if (templateEntry.getFirst() != null)
				pObjs.add(0, fObjSet);
			return factor(pObjs);
		}
		if (templateEntry.getOperation() == "push") {
			for (Object fObj : fObjSet) {
				if (fObj != null && (fObj instanceof ArrayList)) {
					ArrayList<Object> oldF = ((ArrayList<Object>) fObj);
					Set<Object> rOpts = pObjs.get(0);
					for (Object opt : rOpts) {
						ArrayList<Object> newF = (ArrayList<Object>) oldF.clone();
						newF.add(opt);
						res.add(newF);
					}
				} else {
					res.add(StringStrategy.CALL_H_PLACEHOLDER);
				}
			}
			return res;
		}
		if (templateEntry.getOperation() == "join" && fObjSet != null) {
			for (Object fObj : fObjSet) {
				if (fObj != null && (fObj instanceof ArrayList)) {
					ArrayList list = (ArrayList) fObj;
					String resStr = "";
					for (Object joinOp : pObjs.get(0)) {
						for (Object o : list) {
							String oStringRep = "";
							if (o instanceof TemplateEntry)
								oStringRep = ((TemplateEntry) o).evaluate(this).toString();
							else
								oStringRep = o.toString();
							resStr += (resStr.equals("") ? "" : joinOp.toString()) + oStringRep;
						}
						res.add(resStr);
					}
				} else {
					res.add(StringStrategy.CALL_H_PLACEHOLDER);
				}
			}
			return res;
		}
		return set(StringStrategy.CALL_H_PLACEHOLDER);
	}

	private Set<Object> set(Object obj) {
		HashSet<Object> res = new HashSet<Object>();
		res.add(obj);
		return res;
	}

	@Override
	public String prettyPrint(Object evalTmp) {
		return evalTmp.toString();
	}

	private Set<Object> factor(List<Set<Object>> input) {
		Set<Object> res = new HashSet<Object>();
		int total = 1;
		for (Set<Object> currSet : input)
			total *= currSet.size();
		for (int i = 0; i < total; i++)
			res.add(getCurrParam(input, i));
		return res;
	}

	private List<Object> getCurrParam(List<Set<Object>> restAlternatives, int i) {
		List<Object> res = new ArrayList<Object>();
		for (int j = 0; j < restAlternatives.size(); j++) {
			int index = i % restAlternatives.get(j).size();
			Iterator<Object> it = restAlternatives.get(j).iterator();
			Object entry = null;
			for (int k = 0; k <= index; k++)
				entry = it.next();
			res.add(entry);
			i = i / restAlternatives.get(j).size();
		}
		return res;
	}

}
