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
import java.util.Set;

import com.google.javascript.jscomp.templates.operations.BinaryOperation;
import com.google.javascript.jscomp.templates.operations.ConstantString;
import com.google.javascript.jscomp.templates.operations.MultiOperation;
import com.google.javascript.jscomp.templates.operations.VarDependency;

public class GrammarStrategy implements EvaluationStrategy {

	@Override
	public Set<Object> evaluate(ConstantString templateEntry) {
		if (templateEntry.getStrVal() == "[]") {
			return set(new ArrayList<Object>());
		}
		if (templateEntry.getStrVal() == "{}") {
			return set(new ObjectModel("{}"));
		}
		return set("'" + templateEntry.getStrVal() + "'");
	}

	@Override
	public Set<Object> evaluate(VarDependency templateEntry) {
		return set("HOLE");
	}

	@Override
	public Set<Object> evaluate(BinaryOperation templateEntry) {
		return null;
	}

	@Override
	public Set<Object> evaluate(MultiOperation templateEntry) {
		return null;
	}

	@Override
	public String prettyPrint(Object evalTmp) {
		return evalTmp.toString();
	}

	private Set<Object> set(Object obj) {
		HashSet<Object> res = new HashSet<Object>();
		res.add(obj);
		return res;
	}

}
