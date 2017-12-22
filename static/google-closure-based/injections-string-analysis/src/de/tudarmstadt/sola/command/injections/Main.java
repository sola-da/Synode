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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.javascript.jscomp.AnalysisHelper;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.strategies.EvaluationStrategy;
import com.google.javascript.jscomp.strategies.RegexStrategy;
import com.google.javascript.jscomp.strategies.StringStrategy;
import com.google.javascript.jscomp.templates.Template;

public class Main {

	public static void main(String[] args) throws IOException {
		String filePath = new File(args[0]).getCanonicalPath();
		String outPutFile = args[1];
		EvaluationStrategy strategy = new StringStrategy();
		Set<Template> inferTemplates = AnalysisHelper.inferTemplates(SourceFile.fromFile(filePath));
		Set<Object> evaluatedTemplates = new HashSet<Object>();
		if (inferTemplates != null) {
			for (Template te : inferTemplates) {
				Set<Object> evalTmps = te.model.evaluate(strategy);				
				for (Object obj : evalTmps) {
					String refactored = strategy.prettyPrint(obj);
					evaluatedTemplates.add(new EvTemplatesDetails(refactored, filePath, te.codeLoc, te.codeCol, te.type));
				}
			}
			Gson gson = new Gson();
			String json = gson.toJson(evaluatedTemplates);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPutFile)));
			writer.write(json);
			writer.close();
			System.out.println("Saved " + inferTemplates.size() + " templates in " + outPutFile);
		}
	}

}
