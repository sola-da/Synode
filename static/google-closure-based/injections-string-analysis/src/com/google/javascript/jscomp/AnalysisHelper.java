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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.javascript.jscomp.BackwardsAnalysis.StringTemplates;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.ControlFlowAnalysis;
import com.google.javascript.jscomp.ControlFlowGraph;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SyntacticScopeCreator;
import com.google.javascript.jscomp.templates.Template;
import com.google.javascript.jscomp.templates.operations.VarDependency;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class AnalysisHelper {

	public static Set<Template> inferTemplates(SourceFile fileContent) throws IOException {
		Compiler compiler = new Compiler();

		List<SourceFile> input = new ArrayList<>();
		input.add(fileContent);
		List<SourceFile> externs = new ArrayList<>();
		compiler.init(externs, input, new CompilerOptions());
		Node root = compiler.parseInputs();
		Scope scope;
		try {
			scope = SyntacticScopeCreator.makeUntyped(compiler).createScope(root, null);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Warning: malformed input file");
			return null;
		}
		ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
		cfa.process(null, root);

		// SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
		// defFinder.process(root, root);
		SinkIdentifierAnalysis sinkAnalysis = new SinkIdentifierAnalysis(cfa.getCfg(), scope, compiler);
		sinkAnalysis.analyze();
		System.out.println(sinkAnalysis.getExitLatticeElement());
		Set<Node> functions = getEnclosingSinkFunctions(root, root,
				sinkAnalysis.getExitLatticeElement().getSinks().keySet());

		if (functions.size() == 0) {
			System.out.println("Warning: no function containing exec");
			return null;
		}
		Set<Template> templates = new HashSet<Template>();
		for (Node function : functions) {
			cfa = new ControlFlowAnalysis(compiler, false, true);
			cfa.process(null, function);

			ControlFlowGraph<Node> fcfg = cfa.getCfg();
			scope = SyntacticScopeCreator.makeUntyped(compiler).createScope(function, null);
			BackwardsAnalysis analysis = new BackwardsAnalysis(fcfg, scope, compiler,
					sinkAnalysis.getExitLatticeElement().getSinks());
			analysis.analyze();
			StringTemplates lastElement = analysis.lastElement;
			int count = 1;
			if (!function.equals(root)) {
				Node functionParameters = NodeUtil.getFunctionParameters(function);
				for (Node n : functionParameters.children()) {
					String paramName = n.getString();
					lastElement.updateTemplates(paramName, new VarDependency("param-" + (count++)));
				}
			}
			// System.out.println(lastElement.toString());
			Set<Template> templatesWithAlt = lastElement.getTemplates();
			templates.addAll(templatesWithAlt);
		}
		return templates;
	}

	public static Set<Node> getSinksCalls(String path) {

		Compiler compiler = new Compiler();

		List<SourceFile> input = new ArrayList<>();
		input.add(SourceFile.fromFile(path));
		List<SourceFile> externs = new ArrayList<>();
		compiler.init(externs, input, new CompilerOptions());
		Node root = compiler.parseInputs();
		Scope scope;
		try {
			scope = SyntacticScopeCreator.makeUntyped(compiler).createScope(root, null);
		} catch (Exception e) {
			// System.out.println("Warning: malformed input file");
			return null;
		}
		ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
		cfa.process(null, root);

		SinkIdentifierAnalysis sinkAnalysis = new SinkIdentifierAnalysis(cfa.getCfg(), scope, compiler);
		sinkAnalysis.analyze();
		// System.out.println(sinkAnalysis.getExitLatticeElement());
		Set<Node> functions = getEnclosingSinkFunctions(root, root,
				sinkAnalysis.getExitLatticeElement().getSinks().keySet());
		return functions;
	}

	private static Node getEnclosingSinkFunction(Node root, Node enclosingFunction) {
		Token type = root.getType();
		Var var = null;

		switch (type) {
		case FUNCTION:
			enclosingFunction = root;
			break;
		case CALL:
			if (BackwardsAnalysis.isExec(root) || BackwardsAnalysis.isEval(root)) {
				return enclosingFunction;
			}
			break;
		}

		Node f = null;
		for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
			f = getEnclosingSinkFunction(c, enclosingFunction);
			if (f != null)
				return f;
		}
		return f;
	}

	private static Set<Node> getEnclosingSinkFunctions(Node root, Node enclosingFunction, Set<Node> sinks) {
		Set<Node> res = new HashSet<Node>();
		Token type = root.getType();
		Var var = null;

		switch (type) {
		case FUNCTION:
			enclosingFunction = root;
			break;
		case CALL:
			if (sinks.contains(root)) {
				// if (BackwardsAnalysis.isExec(root) || BackwardsAnalysis.isEval(root)) {
				res.add(enclosingFunction);
			}
			break;
		}

		Node f = null;
		for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
			res.addAll(getEnclosingSinkFunctions(c, enclosingFunction, sinks));
		}
		return res;
	}

}
