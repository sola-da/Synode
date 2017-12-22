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

import java.util.List;

import com.google.javascript.jscomp.templates.TemplateEntry;
import com.google.javascript.jscomp.templates.operations.BinaryOperation;
import com.google.javascript.jscomp.templates.operations.ConstantString;
import com.google.javascript.jscomp.templates.operations.MultiOperation;
import com.google.javascript.jscomp.templates.operations.VarDependency;

public class SqueezeTemplates implements TemplatesJoiningStrategy {

	@Override
	public TemplateEntry join(ConstantString entryA, TemplateEntry entryB) {
		if (entryA.equals(entryB) || isOneAlt(entryA, entryB))
			return entryB;
		else
			return new BinaryOperation("alternative", entryA, entryB);
	}

	@Override
	public TemplateEntry join(VarDependency entryA, TemplateEntry entryB) {
		if (entryA.equals(entryB) || isOneAlt(entryA, entryB))
			return entryB;
		else
			return new BinaryOperation("alternative", entryA, entryB);
	}

	@Override
	public TemplateEntry join(BinaryOperation entryA, TemplateEntry entryB) {
		if (entryA.equals(entryB) || isOneAlt(entryA, entryB))
			return entryB;
		if (isOneAlt(entryB, entryA))
			return entryA;
		if ((entryB instanceof BinaryOperation)) {
			BinaryOperation modelOp = (BinaryOperation) entryB;
			if (entryA.getOperation() == modelOp.getOperation() && oneDiff(entryA, modelOp)) {
				entryA.setLeft(entryA.getLeft().join(modelOp.getLeft(), this));
				entryA.setRight(entryA.getRight().join(modelOp.getRight(), this));
				return entryA;
			}
		}
		return new BinaryOperation("alternative", entryA, entryB);
	}

	@Override
	public TemplateEntry join(MultiOperation entryA, TemplateEntry entryB) {
		if (entryA.equals(entryB) || isOneAlt(entryA, entryB))
			return entryB;
		if ((entryB instanceof MultiOperation)) {
			MultiOperation modelOp = (MultiOperation) entryB;
			if (entryA.getOperation() == modelOp.getOperation() && entryA.getRest().size() == modelOp.getRest().size()
					&& oneDiff(entryA, modelOp)) {
				if (entryA.getFirst() != null)
					entryA.setFirst(entryA.getFirst().join(modelOp.getFirst(), this));
				TemplateEntry restArr[] = entryA.getRest().toArray(new TemplateEntry[0]);
				List<TemplateEntry> rest = entryA.getRest();
				for (int i = 0; i < restArr.length; i++) {
					TemplateEntry res = restArr[i].join(modelOp.getRest().get(i), this);
					rest.remove(i);
					rest.add(i, res);
				}
				return entryA;
			}
		}
		return new BinaryOperation("alternative", entryA, entryB);
	}

	private boolean isOneAlt(TemplateEntry a, TemplateEntry b) {
		if (b instanceof BinaryOperation) {
			BinaryOperation bop = (BinaryOperation) b;
			if (bop.getOperation() == "alternative") {
				if (bop.getLeft().equals(a) || bop.getRight().equals(a))
					return true;
				if (bop.getLeft() instanceof BinaryOperation)
					if (isOneAlt(a, bop.getLeft()))
						return true;
				if (bop.getRight() instanceof BinaryOperation)
					if (isOneAlt(a, bop.getRight()))
						return true;
			}
		}
		return false;
	}

	private boolean oneDiff(BinaryOperation a, BinaryOperation b) {
		int count = 0;
		if (!a.getLeft().equals(b.getLeft()))
			count++;
		if (!a.getRight().equals(b.getRight()))
			count++;
		return count == 1;
	}

	private boolean oneDiff(MultiOperation a, MultiOperation b) {
		int count = 0;
		if (a.getFirst() != null && (!a.getFirst().equals(b.getFirst())))
			count++;
		List<TemplateEntry> restA = a.getRest();
		List<TemplateEntry> restB = b.getRest();
		for (int i = 0; i < restA.size(); i++)
			if (!restA.get(i).equals(restB.get(i)))
				count++;
		return count == 1;
	}

}
