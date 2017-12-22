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

public class MultiOperation implements TemplateEntry {

	private String operation;
	private TemplateEntry first;
	private List<TemplateEntry> rest;
	private String cachedString;

	public MultiOperation(String operation, TemplateEntry first, List<TemplateEntry> rest) {
		this.operation = operation;
		this.first = first;
		this.rest = rest;
		computeCachedString();
	}

	@Override
	public List<String> getVarNames() {
		List<String> res = new ArrayList<String>();
		if (first != null)
			res.addAll(first.getVarNames());
		for (TemplateEntry entry : rest)
			res.addAll(entry.getVarNames());
		return res;
	}

	@Override
	public TemplateEntry update(String name, TemplateEntry entryToAdd) {
		if (first != null)
			first = first.update(name, entryToAdd);
		TemplateEntry entries[] = rest.toArray(new TemplateEntry[0]);
		for (int i = 0; i < entries.length; i++) {
			TemplateEntry newE = entries[i].update(name, entryToAdd);
			if (!newE.equals(entries[i])) {
				rest.remove(i);
				rest.add(i, newE);
			}
		}
		computeCachedString();
		return this;
	}

	private void computeCachedString() {
		cachedString = operation + "(" + first;
		for (TemplateEntry e : rest)
			cachedString += "," + e;
		cachedString += ")";
	}

	@Override
	public TemplateEntry clone() {
		TemplateEntry newLeft;
		if (first != null)
			newLeft = first.clone();
		else
			newLeft = null;
		List<TemplateEntry> entries = new ArrayList<TemplateEntry>();
		for (TemplateEntry e : rest)
			entries.add(e.clone());
		return new MultiOperation(this.operation, newLeft, entries);
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

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public Set<Object> evaluate(EvaluationStrategy strategy) {
		return strategy.evaluate(this);
	}

	// private TemplateEntry getFirstParam() {
	// return rest.get(0);
	// }

	public TemplateEntry getFirst() {
		return first;
	}

	public List<TemplateEntry> getRest() {
		return rest;
	}

	public String getOperation() {
		return operation;
	}

	@Override
	public List<String> getOperations() {
		List<String> res = new ArrayList<String>();
		if (first != null)
			res.addAll(first.getOperations());
		for (TemplateEntry entry : rest)
			res.addAll(entry.getOperations());
		// if (operation == "require")
		// res.add("require " + rest.get(0));
		res.add(operation);
		return res;
	}

	@Override
	public TemplateEntry join(TemplateEntry model, TemplatesJoiningStrategy strategy) {
		return strategy.join(this, model);
	}

	public void setFirst(TemplateEntry first) {
		this.first = first;
	}

}
