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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class SinkIdentifierAnalysis extends DataFlowAnalysis<Node, SinkIdentifierAnalysis.SinksCollection> {

	// The scope of the function that we are analyzing.
	private final Scope jsScope;
	private final AbstractCompiler compiler;
	private final Set<Var> escaped;
	private final HashMap<String, String> myScope = new HashMap<String, String>();

	SinkIdentifierAnalysis(ControlFlowGraph<Node> cfg, Scope jsScope, AbstractCompiler compiler) {
		super(cfg, new SinksJoin());
		this.jsScope = jsScope;
		this.compiler = compiler;
		this.escaped = new HashSet<>();
		computeEscaped(jsScope, escaped, compiler);
	}

	static final class SinksCollection implements LatticeElement {

		private HashMap<Node, String> sinks = new HashMap<Node, String>();

		public SinksCollection() {
		}

		public SinksCollection(SinksCollection s) {
			addSinks(s);
		}

		public void addSinks(SinksCollection sink) {
			Set<Node> keySet = sink.sinks.keySet();
			for (Node node : keySet)
				this.sinks.put(node, sink.sinks.get(node));
		}

		public Map<Node, String> getSinks() {
			return sinks;
		}

		@Override
		public String toString() {
			Set<Node> keySet = sinks.keySet();
			String res = "{\n";
			for (Node n : keySet)
				res += n.getLineno() + "\n";
			res += "}\n";
			return res;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj.hashCode() == hashCode();
		}

	}

	public static class SinksJoin extends JoinOp.BinaryJoinOp<SinksCollection> {

		@Override
		SinksCollection apply(SinksCollection latticeA, SinksCollection latticeB) {
			SinksCollection output = new SinksCollection(latticeA);
			output.addSinks(latticeB);
			return output;
		}
	}

	@Override
	boolean isForward() {
		return true;
	}

	@Override
	SinksCollection flowThrough(Node node, SinksCollection input) {
		// System.out.println(node.getType());
		SinksCollection output = new SinksCollection(input);
		computeSinks(node, output);
		return output;
	}

	@Override
	SinksCollection createInitialEstimateLattice() {
		return new SinksCollection();
	}

	@Override
	SinksCollection createEntryLattice() {
		return new SinksCollection();
	}

	private String computeSinks(Node n, SinksCollection out) {
		String result = "";
		String name = "";
		switch (n.getType()) {
		case GETPROP:
			if (n.getFirstChild().getType().equals(Token.NAME))
				if (myScope.containsKey(n.getFirstChild().getString()))
					name = myScope.get(n.getFirstChild().getString());
				else
					name = computeSinks(n.getFirstChild(), out);
			else
				name = computeSinks(n.getFirstChild(), out);
			if (name != null && name.equals("child_process")) {
				String method = n.getSecondChild().getString();
				if (method.equals("exec")) {
					result = "child_process-exec";
				}
			}
			if (result.equals(""))
				result = "other-prop";
			break;
		case CALL:
			name = "";
			String firstParam = "";
			try {
				if (n.getFirstChild() != null && (n.getFirstChild().getType().equals(Token.GETPROP)
						|| n.getFirstChild().getType().equals(Token.FUNCTION))) {
					name = computeSinks(n.getFirstChild(), out);
				} else {
					name = n.getFirstChild().getString();
					if (myScope.containsKey(name))
						name = myScope.get(name);
				}
				if (n.getSecondChild() != null)
					firstParam = n.getSecondChild().getString();

			} catch (Exception e) {
				// e.printStackTrace();
			}
			if (name.equals("child_process-exec")) {
				out.sinks.put(n, "exec");
			}
			if (name.equals("eval")) {
				out.sinks.put(n, "eval");
			}
			if (name.equals("require") && firstParam.equals("child_process")) {
				result = "child_process";
			}
			if (result.equals(""))
				result = "no-name";
			break;
		case ASSIGN:
			name = "";
			String what = "";
			try {
				name = n.getFirstChild().getString();
				what = computeSinks(n.getSecondChild(), out);
				myScope.put(name, what);
			} catch (Exception e) {

			}
			break;
		case EXPR_RESULT:
			// result = computeSinks(n.getFirstChild(), out);
			break;
		case VAR:
			for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
				name = "";
				what = "";
				if (c.hasChildren()) {
					Node assignedValue = NodeUtil.getAssignedValue(c);
					try {
						name = c.getString();
						what = computeSinks(assignedValue, out);
						myScope.put(name, what);
					} catch (Exception e) {

					}
				}
			}
			break;
		case FUNCTION:
			result = "anonymous-fct";
			break;
		default:

			break;
		}
		for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
			computeSinks(c, out);
		}
		return result;
	}

}
