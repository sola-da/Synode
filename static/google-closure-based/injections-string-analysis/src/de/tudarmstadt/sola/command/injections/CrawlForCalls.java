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

import java.io.File;
import java.util.Set;

import com.google.javascript.jscomp.AnalysisHelper;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.strategies.StringStrategy;
import com.google.javascript.jscomp.templates.Template;
import com.google.javascript.rhino.Node;

public class CrawlForCalls {

	public static final String PATH_TO_PKS = "/media/cstaicu/work1/npm-study/execs2";
	public static int count = 0;

	public static void main(String[] args) {
		walk(PATH_TO_PKS, 0);
	}

	public static void walk(String path, int level) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			if (f.isDirectory()) {
				walk(f.getAbsolutePath(), level + 1);
				// System.out.println("Dir:" + f.getAbsoluteFile());
			} else {
				if (f.getName().matches(".*\\.js$") && f.length() < 2000000) {
					Set<Node> sinkCalls = getSinkCalls(f.getAbsolutePath());
					if (sinkCalls != null && sinkCalls.size() > 0) {
						try {
							Set<Template> inferTemplates = AnalysisHelper.inferTemplates(SourceFile.fromFile(f));
							System.out.println("File:" + f.getAbsoluteFile());
							for (Template t : inferTemplates)
								System.out.println(t.model.evaluate(new StringStrategy()));
						} catch (Exception e) {
							System.out.println(e);
							// e.printStackTrace();
						}
					}
				}
			}
		}
		if (level == 1) {
			count++;
			System.out.println("Finished analyzing " + path + "(" + count + ")");
		}
	}

	public final static Set<Node> getSinkCalls(String path) {
		try {
			Set<Node> sinksCalls = AnalysisHelper.getSinksCalls(path);
			return sinksCalls;
		} catch (Exception e) {

		}
		return null;
	}

}
