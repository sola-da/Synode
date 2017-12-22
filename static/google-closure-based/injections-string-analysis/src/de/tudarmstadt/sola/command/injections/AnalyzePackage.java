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
package de.tudarmstadt.sola.command.injections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.javascript.jscomp.AnalysisHelper;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.strategies.EvaluationStrategy;
import com.google.javascript.jscomp.strategies.StringStrategy;
import com.google.javascript.jscomp.templates.Template;
import com.google.javascript.jscomp.templates.operations.ConstantString;

public class AnalyzePackage {

	public static final String PATH_TO_PKS = "/media/cstaicu/work1/npm-study/execs2";
	public static int count = 0;

	public static void main(String[] args) throws IOException {
		String inputFileName = args[0];
		String outputFileName = args[1];
		String tempsFileName = args[2];
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName)));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)));
		BufferedWriter writerTemps = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempsFileName)));
		EvaluationStrategy str = new StringStrategy();
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println("Analyzing " + line);
			Set<Template> inferTemplates = AnalysisHelper.inferTemplates(SourceFile.fromFile(new File(line)));
			if (inferTemplates != null)
				for (Template temp : inferTemplates) {
					writerTemps.write("****TEMPLATE START****\n");
					writerTemps.write(temp.type + ":" + line + ":" + temp.codeLoc + "\n");
					writerTemps.write("****VARIABLES****\n");
					List<String> varNamesL = temp.model.getVarNames();
					Map<String, Integer> varsHash = new HashMap<String, Integer>();
					for (String var : varNamesL) {
						if (varsHash.containsKey(var)) {
							varsHash.put(var, varsHash.get(var) + 1);
						} else {
							varsHash.put(var, 1);
						}
					}
					Set<String> varNames = varsHash.keySet();
					for (String varName : varNames) {
						writerTemps.write(varName + "," + varsHash.get(varName) + "\n");
					}
					writerTemps.write("****OPERATIONS****\n");
					List<String> operationsL = temp.model.getOperations();
					Map<String, Integer> opsHash = new HashMap<String, Integer>();
					for (String op : operationsL) {
						if (opsHash.containsKey(op)) {
							opsHash.put(op, opsHash.get(op) + 1);
						} else {
							opsHash.put(op, 1);
						}
					}
					Set<String> operations = opsHash.keySet();
					for (String opName : operations) {
						writerTemps.write(opName + "," + opsHash.get(opName) + "\n");
					}
					Set<Object> evaluated = temp.model.evaluate(str);
					for (Object obj : evaluated) {
						writerTemps.write("****INSTANCE START****\n");
						writerTemps.write(obj.toString() + "\n");

					}
					if (temp.model instanceof ConstantString) {
						writer.write(temp.type + ":" + line + ":" + temp.codeLoc + ", 1, 1, 0, 0\n");
					} else {
						int strings = 0;
						int modeled = 0;
						for (Object obj : evaluated) {
							if (obj.toString().indexOf(StringStrategy.CALL_H_PLACEHOLDER) == -1) {
								if (obj.toString().indexOf(StringStrategy.VAR_H_PLACEHOLDER) == -1) {
									strings++;
								} else {
									modeled++;
								}
							}
						}
						writer.write(temp.type + ":" + line + ":" + temp.codeLoc + ", " + evaluated.size() + ", 0, "
								+ strings + ", " + modeled + "\n");
					}
				}
		}
		reader.close();
		writer.close();
		writerTemps.close();
	}

}
