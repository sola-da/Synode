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

import java.util.ArrayList;
import java.util.List;

import com.google.javascript.jscomp.SinkIdentifierAnalysis.SinksCollection;
import com.google.javascript.rhino.Node;

import junit.framework.Assert;

public class SinksIdentificationTest {

	public static void main(String[] args) {
		SourceFile fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/tps/simpleExecTest.js");
		SinksCollection analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n3\n}\n");
		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/tps/simpleExecTest2.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n1\n}\n");
		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/tps/simpleExecTest3.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n2\n}\n");
		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/tps/simpleExecTest4.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n5\n}\n");
		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/tps/simpleExecTest5.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n14\n6\n}\n");

		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/fps/simpleExecTest.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n}\n");
		fileContent = SourceFile
				.fromFile("./test/com/google/javascript/jscomp/sinks/identification/fps/simpleExecTest2.js");
		analyzeFileContent = analyzeFileContent(fileContent);
		Assert.assertEquals(analyzeFileContent.toString(), "{\n}\n");
	}

	private static SinksCollection analyzeFileContent(SourceFile fileContent) {
		Compiler compiler = new Compiler();

		List<SourceFile> input = new ArrayList<>();
		input.add(fileContent);
		List<SourceFile> externs = new ArrayList<>();
		compiler.init(externs, input, new CompilerOptions());
		Node root = compiler.parseInputs();
		TypedScope scope = null;
		try {
			scope = SyntacticScopeCreator.makeTyped(compiler).createScope(root, null);
		} catch (Exception e) {
			System.out.println("Warning: malformed input file");
		}
		ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
		cfa.process(null, root);

		cfa = new ControlFlowAnalysis(compiler, false, true);
		cfa.process(null, root);

		ControlFlowGraph<Node> fcfg = cfa.getCfg();
		scope = SyntacticScopeCreator.makeTyped(compiler).createScope(root, null);
		SinkIdentifierAnalysis analysis = new SinkIdentifierAnalysis(fcfg, scope, compiler);
		analysis.analyze();
		SinksCollection exitLatticeElement = analysis.getExitLatticeElement();
		return exitLatticeElement;
	}

}
