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
package com.google.javascript.jscomp.templates.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.javascript.jscomp.strategies.EvaluationStrategy;
import com.google.javascript.jscomp.strategies.TemplatesJoiningStrategy;
import com.google.javascript.jscomp.templates.TemplateEntry;

public class BinaryOperation implements TemplateEntry {

	private String operation;
	private TemplateEntry left;
	private TemplateEntry right;
	private String cachedString;

	public BinaryOperation(String operation, TemplateEntry left, TemplateEntry right) {
		this.operation = operation;
		this.left = left;
		this.right = right;
		cachedString = operation + "(" + left + "," + right + ")";
	}

	@Override
	public List<String> getVarNames() {
		List<String> res = new ArrayList<String>();
		res.addAll(left.getVarNames());
		res.addAll(right.getVarNames());
		return res;
	}

	@Override
	public String toString() {
		return cachedString;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}

	@Override
	public TemplateEntry update(String name, TemplateEntry entryToAdd) {
		left = left.update(name, entryToAdd);
		right = right.update(name, entryToAdd);
		cachedString = operation + "(" + left + "," + right + ")";
		return this;
	}

	@Override
	public TemplateEntry clone() {
		TemplateEntry newLeft = left.clone();
		TemplateEntry newright = right.clone();
		return new BinaryOperation(this.operation, newLeft, newright);
	}

	public TemplateEntry getLeft() {
		return left;
	}

	public TemplateEntry getRight() {
		return right;
	}

	public String getOperation() {
		return operation;
	}

	@Override
	public Set<Object> evaluate(EvaluationStrategy strategy) {
		return strategy.evaluate(this);
	}

	public static String getNameOfProp(TemplateEntry entry) {
		String prop = "";
		if (entry instanceof BinaryOperation) {
			TemplateEntry entryRight = ((BinaryOperation) entry).right;
			if (entryRight instanceof ConstantString)
				return entryRight.toString();
		}
		return prop;
	}

	@Override
	public List<String> getOperations() {
		List<String> res = new ArrayList<String>();
		res.addAll(left.getOperations());
		res.addAll(right.getOperations());
		res.add(operation);
		return res;
	}

	@Override
	public TemplateEntry join(TemplateEntry model, TemplatesJoiningStrategy strategy) {
		return strategy.join(this, model);
	}

	public void setRight(TemplateEntry right) {
		this.right = right;
	}

	public void setLeft(TemplateEntry left) {
		this.left = left;
	}

}
