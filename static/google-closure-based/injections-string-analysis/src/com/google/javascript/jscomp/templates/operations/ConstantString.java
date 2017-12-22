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

public class ConstantString implements TemplateEntry {

	private String strVal;

	public ConstantString(String strVal) {
		this.strVal = strVal;
	}

	@Override
	public String toString() {
		return strVal;
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
	public List<String> getVarNames() {
		return new ArrayList<String>();
	}

	@Override
	public TemplateEntry update(String name, TemplateEntry entryToAdd) {
		return this;
	}

	@Override
	public TemplateEntry clone() {
		return new ConstantString(strVal);
	}

	@Override
	public Set<Object> evaluate(EvaluationStrategy strategy) {
		return strategy.evaluate(this);
	}

	@Override
	public List<String> getOperations() {
		List<String> res = new ArrayList<String>();
		return res;
	}

	public String getStrVal() {
		return strVal;
	}

	@Override
	public TemplateEntry join(TemplateEntry model, TemplatesJoiningStrategy strategy) {
		return strategy.join(this, model);
	}

}