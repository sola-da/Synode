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
package com.google.javascript.jscomp.templates;

import com.google.javascript.jscomp.strategies.SqueezeTemplates;

public class Template {

	public TemplateEntry model;
	public String type;
	public final int codeLoc;
	public final int codeCol;

	public Template(TemplateEntry model, int codeLoc, int codeCol, String type) {
		this.codeLoc = codeLoc;
		this.codeCol = codeCol;
		this.model = model;
		this.type = type;
	}

	@Override
	public int hashCode() {
		return this.model.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.model.equals(obj);
	}

	public void join(Template tempA) {
		model = model.join(tempA.model, new SqueezeTemplates());
	}

}
