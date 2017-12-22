/*
 * Copyright 2008 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law. or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.ControlFlowGraph;
import com.google.javascript.jscomp.DataFlowAnalysis;
import com.google.javascript.jscomp.JoinOp;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.Var;
import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.jscomp.templates.Template;
import com.google.javascript.jscomp.templates.TemplateEntry;
import com.google.javascript.jscomp.templates.operations.BinaryOperation;
import com.google.javascript.jscomp.templates.operations.ConstantString;
import com.google.javascript.jscomp.templates.operations.MultiOperation;
import com.google.javascript.jscomp.templates.operations.VarDependency;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.security.KeyRep.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;


final class BackwardsAnalysis extends DataFlowAnalysis<Node, BackwardsAnalysis.StringTemplates> {

	// The scope of the function that we are analyzing.
	private final Scope jsScope;
	private final AbstractCompiler compiler;
	private final Set<Var> escaped;
	public StringTemplates lastElement;
	public static final int MAX_IT = 10;
	private Set<Node> sinks;
	private Map<Node, String> sinksTypes;

	BackwardsAnalysis(ControlFlowGraph<Node> cfg, Scope jsScope, AbstractCompiler compiler, Map<Node, String> sinks) {
		super(cfg, new StringTemplatesJoin());
		this.jsScope = jsScope;
		this.compiler = compiler;
		this.escaped = new HashSet<>();
		this.sinks = sinks.keySet();
		this.sinksTypes = sinks;
		computeEscaped(jsScope, escaped, compiler);		
	}		

	static final class StringTemplates implements LatticeElement {

		private Set<Template> templates;		
		private Map<Node, Integer> history = new HashMap<Node, Integer>();

		public StringTemplates() {
			templates = new HashSet<Template>();
		}
		
		public StringTemplates(StringTemplates model) {			
			this.templates = new HashSet<Template>();			
			for (Node n : model.history.keySet())
				this.history.put(n, model.history.get(n));
			for (Template t : model.templates) {				
				this.templates.add(new Template(t.model.clone(), t.codeLoc, t.codeCol, t.type));
			}
		}
		
		public void addTemplate(Template t) {
			templates.add(t);
		}
		
		public Set<Template> getTemplates() {
			return templates;
		}
		
		public void updateTemplates(String varName, TemplateEntry entry) {			
			Template entries[] = templates.toArray(new Template[0]);			
			for (Template t : entries) {
				TemplateEntry newT = t.model.update(varName, entry);			
				if (!newT.equals(t.model)) {
					templates.remove(t);
					templates.add(new Template(newT, t.codeLoc, t.codeCol, t.type));
				}
			}
		}
		
		@Override
		public String toString() {
			String res = "{\n";
			for (Template t : templates)
				res+= "[" + t.codeLoc + "]" + t.model.toString() + "\n";
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

		public Set<String> getVarNames() {
			Set<String> res = new HashSet<String>();			
			for (Template t : templates) {
				res.addAll(t.model.getVarNames());
			}
			return res;
		}

	}

	private static class StringTemplatesJoin extends JoinOp.BinaryJoinOp<StringTemplates> {
		
		@Override
		public StringTemplates apply(StringTemplates a, StringTemplates b) {
			Set<Node> keySet = a.history.keySet();
//			for (Node n : keySet) {				
//				if (b.history.containsKey(n)) {
//					if (a.history.get(n) + b.history.get(n) > BackwardsAnalysis.MAX_IT) {
//						System.out.println("Skipped " + n.getType() + " " + n.getLineno());
//						if (a.history.get(n) > b.history.get(n)) {							
//							return a;
//						} else 
//							return b;
//					}
//				}
//			}
			Set<Template> res = new HashSet<Template>();
			res.addAll(a.templates);
			/* Version 1
			res.addAll(b.templates);
			*/
			/* start of Version 2 */
			boolean joined = false;
			for (Template tempB : b.templates) {
				joined = false;
				Template toTrav[] = res.toArray(new Template[0]);
				for (Template tempRes : toTrav) {
					if(tempB.codeLoc == tempRes.codeLoc) {						
						tempRes.join(tempB);
						joined = true;
					}
				}
				if (!joined) {
					res.add(tempB);
				}
			}
			/* end of version 2 */
			StringTemplates result = new StringTemplates();
			result.templates = res;	
			for (Node n : a.history.keySet()) {
				result.history.put(n, a.history.get(n));
			}
			for (Node n : b.history.keySet()) {
				if (result.history.containsKey(n))
					result.history.put(n, result.history.get(n) + b.history.get(n));
				else 
					result.history.put(n,  b.history.get(n));
			}
			return result;
		}
			
	}

	@Override
	boolean isForward() {
		return false;
	}

	@Override
	StringTemplates createEntryLattice() {
		return new StringTemplates();
	}

	@Override
	StringTemplates createInitialEstimateLattice() {
		return new StringTemplates();
	}
	
	@Override
	StringTemplates flowThrough(Node n, StringTemplates input) {		
		if (input.history.containsKey(n)) {
			input.history.put(n, input.history.get(n) + 1);
			if (input.history.get(n) > BackwardsAnalysis.MAX_IT)
				return input;
		} else 
			input.history.put(n, 1);

		StringTemplates output = new StringTemplates(input);
		if (n.getType() != Token.SCRIPT) // Google Closure bug? Why joining with the whole script at the end?
			computeTemplates(n, n, output, false);
		lastElement = output;
		return output;		
	}
	
	private void recurse(Node n, Node cfgNode, StringTemplates output, boolean conditional) {		
		for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
			computeTemplates(c, cfgNode, output, conditional);
		}
	}

	private void computeTemplates(Node n, Node cfgNode, StringTemplates output, boolean conditional) {		
		Set<String> names;
		String aValue;
		switch (n.getType()) {
		case BLOCK:
		case FUNCTION:
			return;
		case ASSIGN:
			if (n.getFirstChild().getType() == Token.NAME) {
				names = output.getVarNames();			
				aValue = NodeUtil.getBestLValue(n.getFirstChild()).getString();
				if (names.contains(aValue)) {
					Node assignedValue = NodeUtil.getAssignedValue(n.getFirstChild());
					TemplateEntry tmp = computeTemplate(assignedValue);
					output.updateTemplates(aValue, tmp);
				}
			} else {			
				TemplateEntry tmp = computeTemplate(n.getFirstChild());
				Node assignedValue = n.getSecondChild();				
				tmp = new BinaryOperation("append", tmp, computeTemplate(assignedValue));								
				try { 
					String name = getLeftMostName(tmp);
					names = output.getVarNames();			
					if (names.contains(name)) {
						output.updateTemplates(name, tmp);
					}
				} catch(RuntimeException e) {
					
				}
			}
			recurse(n, cfgNode, output, conditional);
			return; 
		case ASSIGN_ADD:
			if (n.getFirstChild().getType() == Token.NAME) {
				names = output.getVarNames();			
				aValue = n.getFirstChild().getString();
				if (names.contains(aValue)) {
					Node assignedValue = NodeUtil.getRValueOfLValue(n.getFirstChild());
					TemplateEntry tmp = computeTemplate(assignedValue);
					tmp = new BinaryOperation("+", new VarDependency(aValue), tmp);				
					output.updateTemplates(aValue, tmp);	
				}
				recurse(n, cfgNode, output, conditional);
				return;
			}
		case CALL:
			if (n.getFirstChild().getType() == Token.GETPROP && n.getFirstChild().getChildCount() > 0) {
				names = output.getVarNames();
				if (n.getFirstChild().getFirstChild().getType() == Token.NAME
					&& n.getFirstChild().getSecondChild().getString() == "push"){
					String baseObj = n.getFirstChild().getFirstChild().getString();
					if (names.contains(baseObj)) {
						List<TemplateEntry> params = new ArrayList<TemplateEntry>();
						for (int i = 1; i < n.getChildCount(); i++) {					
							TemplateEntry e = computeTemplate(n.getChildAtIndex(i));
							if (e != null)
								params.add(e);
						}
						TemplateEntry tmp = new MultiOperation(
								n.getFirstChild().getSecondChild().getString(), 
								new VarDependency(baseObj), params);
						output.updateTemplates(baseObj, tmp);
					}				
				}
			}			
			if (n.getChildCount() > 1) {
//				if (isExec(n)){
//					TemplateEntry tmp = computeTemplate(n.getSecondChild());				
//					output.addTemplate(new Template(tmp, n.getLineno(), "exec"));																
//				} else if (isEval(n)) {
//					TemplateEntry tmp = computeTemplate(n.getSecondChild());			
//					output.addTemplate(new Template(tmp, n.getLineno(), "eval"));							
//				}
				if (sinks.contains(n)) {					
					TemplateEntry tmp = computeTemplate(n.getSecondChild());
					output.addTemplate(new Template(tmp, n.getLineno(), n.getCharno(), sinksTypes.get(n)));
				}
			}			
			return;
		case WHILE:
		case DO:
		case IF:
			computeTemplates(NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
			return;

		case FOR:
			if (!NodeUtil.isForIn(n)) {
				computeTemplates(NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
			} else {
				// for(x in y) {...}
				Node lhs = n.getFirstChild();
				Node rhs = lhs.getNext();
				if (lhs.isVar()) {
					lhs = lhs.getLastChild(); // for(var x in y) {...}
				}
				if (lhs.isName()) {
					// addToDefIfLocal(lhs.getString(), cfgNode, rhs, output);
				}
			}
			return;

		case AND:
		case OR:
			computeTemplates(n.getFirstChild(), cfgNode, output, conditional);
			computeTemplates(n.getLastChild(), cfgNode, output, true);
			return;

		case HOOK:
			computeTemplates(n.getFirstChild(), cfgNode, output, conditional);
			computeTemplates(n.getSecondChild(), cfgNode, output, true);
			computeTemplates(n.getLastChild(), cfgNode, output, true);
			return;

		case VAR:
			for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {				
				if (c.hasChildren()) {					
					aValue = c.getString();					
					names = output.getVarNames();			
					if (names.contains(aValue)) {
						Node assignedValue = NodeUtil.getAssignedValue(c);
						TemplateEntry tmp = computeTemplate(assignedValue);
						output.updateTemplates(aValue, tmp);
					}
				} else {
					aValue = c.getString();
					output.updateTemplates(aValue, new ConstantString("undefined"));					
				}
			}
			recurse(n, cfgNode, output, conditional);
			return;
		default:
			if (NodeUtil.isAssignmentOp(n)) {
				if (n.getFirstChild().isName()) {
					Node name = n.getFirstChild();
					computeTemplates(name.getNext(), cfgNode, output, conditional);

					return;
				} else if (NodeUtil.isGet(n.getFirstChild())) {
					// Treat all assignments to arguments as redefining the
					// parameters itself.
					Node obj = n.getFirstFirstChild();
					if (obj.isName() && "arguments".equals(obj.getString())) {
						// TODO(user): More accuracy can be introduced
						// i.e. We know exactly what arguments[x] is if x is a
						// constant
						// number.
					}
				}
			}

			// DEC and INC actually defines the variable.
			if (n.isDec() || n.isInc()) {
				Node target = n.getFirstChild();
				if (target.isName()) {
					return;
				}
			}

			recurse(n, cfgNode, output, conditional);
		}
	}

	private String getLeftMostName(TemplateEntry tmp) {
		while (tmp instanceof BinaryOperation)
			tmp = ((BinaryOperation)tmp).getLeft();		
		if (tmp instanceof VarDependency)
			return ((VarDependency)tmp).getVarName();
		throw new RuntimeException("Expectation fail at getLeftMostName");
	}

	private TemplateEntry computeTemplate(Node n) {				
		TemplateEntry res = null;
		switch (n.getType()) {
			case STRING:
				res = new ConstantString(n.getString());
				break;
			case OR:			
				TemplateEntry firstAlt = computeTemplate(n.getFirstChild());
				TemplateEntry secondAlt = computeTemplate(n.getSecondChild());
				res = new BinaryOperation("alternative", firstAlt, secondAlt);
				break;
			case NOT: 
				res = computeTemplate(n.getFirstChild());
				break;
			case EQ:
			case MUL:
			case SUB:
			case AND:
				firstAlt = new ConstantString("undefined");
				secondAlt = computeTemplate(n.getSecondChild());
				res = new BinaryOperation("alternative", firstAlt, secondAlt);
//				res = firstAlt;
				break;
			case ADD:											
				TemplateEntry left = computeTemplate(n.getFirstChild());
				TemplateEntry right = computeTemplate(n.getSecondChild());
				res = new BinaryOperation("+", left, right);
				break;
			case NAME:
				res = new VarDependency(n.getString());				
				break;
			case REGEXP:
				res = new ConstantString("/" + 
						((n.getChildCount() > 0)? n.getFirstChild().getString() : "") + "/" + 
						((n.getChildCount() > 1)? n.getSecondChild().getString() : ""));
				break;			
			case CALL:			
				List<TemplateEntry> params = new ArrayList<TemplateEntry>();

				if (n.getFirstChild().getType() == Token.GETPROP) {
					for (int i = 1; i < n.getChildCount(); i++) {					
						TemplateEntry e = computeTemplate(n.getChildAtIndex(i));
						if (e != null)
							params.add(e);
					}
					TemplateEntry first = computeTemplate(n.getFirstChild().getFirstChild());					
					res = new MultiOperation(n.getFirstChild().getSecondChild().getString(), first, params);
				} else { 
					for (int i = 1; i < n.getChildCount(); i++) {					
						params.add(computeTemplate(n.getChildAtIndex(i)));
					}					
					String name = "";
					if (n.getFirstChild().getType() == Token.FUNCTION)
						name = "custom-function";
					else 
						name = n.getFirstChild().getString();
					res = new MultiOperation(name, null, params);
				}				
				break;
			case GETPROP:
				TemplateEntry base = computeTemplate(n.getFirstChild());
				TemplateEntry offset = computeTemplate(n.getSecondChild());
				res = new BinaryOperation("access", base, offset);				
				break;
			case NUMBER: 
				res = new ConstantString(NodeUtil.getNumberValue(n) + "");
				break;
			case ARRAYLIT:
				params = new ArrayList<TemplateEntry>();
				TemplateEntry first = null;
				if (n.getChildCount() > 0) {
					first = computeTemplate(n.getChildAtIndex(0));
					for (int i = 1; i < n.getChildCount(); i++) {					
						TemplateEntry e = computeTemplate(n.getChildAtIndex(i));
						params.add(e);
					}					
				}
				res = new MultiOperation("array", first, params); 
				break;
			case GETELEM:
				base = computeTemplate(n.getFirstChild());
				offset = computeTemplate(n.getSecondChild());							
				res = new BinaryOperation("access", base, offset);
				break;
			case ASSIGN: 
				res = computeTemplate(n.getSecondChild());
				break;
			case NULL:
				res = new ConstantString("null");
				break;
			case TRUE:
				res = new ConstantString("true");
				break;
			case FALSE:
				res = new ConstantString("true");
				break;
			case HOOK:
				firstAlt = computeTemplate(n.getSecondChild());
				secondAlt = computeTemplate(n.getChildAtIndex(2));
				res = new BinaryOperation("alternative", firstAlt, secondAlt);
				break;
			case THIS:
				res = new VarDependency("this");
				break;
			case FUNCTION:
				res = new BinaryOperation("function-def", new ConstantString(""), new ConstantString(""));
				break;
			case OBJECTLIT:
				if (n.getChildCount() == 0)
					return new ConstantString("{}");
				else {					
					TemplateEntry newObj = new ConstantString("{}");
					for (int i = 0; i < n.getChildCount(); i++) {
						String key = n.getChildAtIndex(i).getString();
						TemplateEntry lSide = computeTemplate(n.getChildAtIndex(i).getFirstChild());
						newObj = new BinaryOperation("append", new BinaryOperation("access", newObj, new ConstantString(key)), lSide);
					}
					return newObj;
				}				
			case NEW:
				params = new ArrayList<TemplateEntry>();
				for (int i = 1; i < n.getChildCount(); i++) {					
					TemplateEntry e = computeTemplate(n.getChildAtIndex(i));
					if (e != null)
						params.add(e);
				}
				first = computeTemplate(n.getFirstChild());					
				res = new MultiOperation("new", first , params);				
				break;
			case POS:				
				res = computeTemplate(n.getFirstChild());
				break;
			default:
				res = new MultiOperation("unknown", null, new ArrayList<TemplateEntry>());
//				System.out.println("Unknown type:" + n.getType() + " " + n.getLineno());
//				throw new RuntimeException();
//				break;
		}		
		return res;
	}
	
	public static final boolean isExec(Node n) {		
		if (n.getFirstChild().getType() == Token.NAME
				&& (n.getFirstChild().getString().equals("exec")))
			return true;
		return false;
	}
	
	public static final boolean isEval(Node n) {		
		if (n.getFirstChild().getType() == Token.NAME
				&& (n.getFirstChild().getString().equals("eval")))
			return true;
		return false;
	}

}
