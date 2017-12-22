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
package com.google.javascript.jscomp;

import java.io.IOException;
import java.util.Set;

import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.strategies.EvaluationStrategy;
import com.google.javascript.jscomp.strategies.StringStrategy;
import com.google.javascript.jscomp.templates.Template;

public class BackwardsAnalysisTest {

	public static final EvaluationStrategy strategy = new StringStrategy();

	public static void main(String[] args) throws IOException {
		System.out.println("Backwards Analysis");
		System.out.println("===");
		Set<Template> inferTemplates = AnalysisHelper
				.inferTemplates(SourceFile.fromFile("./test/com/google/javascript/jscomp/templates/miner/lib.js"));
		for (Template te : inferTemplates) {
			System.out.println(te.model);
			Set<Object> evalTmps = te.model.evaluate(strategy);
			for (Object evalTmp : evalTmps)
				System.out.println(strategy.prettyPrint(evalTmp));
			System.out.println("====");
		}
	}
}
