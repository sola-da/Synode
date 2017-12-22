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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.strategies.EvaluationStrategy;
import com.google.javascript.jscomp.strategies.RegexStrategy;
import com.google.javascript.jscomp.strategies.StringStrategy;
import com.google.javascript.jscomp.templates.Template;
import com.google.javascript.jscomp.templates.TemplateEntry;

public class TemplatesMinner {

	public static final Class evalStr = StringStrategy.class;
	public static final String FOLDER = "/home/cstaicu/Desktop/runtime/benchmarks-runtime/";
	public static final String OUTPUT_FOLDER = "/home/cstaicu/Desktop/runtime/benchmarks-analysis/";
	// public static final String FOLDER =
	// "/media/cstaicu/work1/npm-study/eval-call-sites/";
	// public static final String OUTPUT_FOLDER =
	// "/home/cstaicu/Desktop/runtime/static-analysis/";
	// public static final String OUTPUT_FILE =
	// "/home/cstaicu/Desktop/runtime/static-analysis/templates.txt";
	public static final String OUTPUT_FILE = "/home/cstaicu/work/ec-spride-sola-projects-npm-vulnerabilities-study/dynamic/templates.txt";

	public static void main(String[] args) throws IOException {
		ArrayList<Integer> toSkip = new ArrayList<Integer>();
		// toSkip.add(2109);
		// toSkip.add(2110);
		// toSkip.add(4292);
		// toSkip.add(4298);
		// toSkip.add(4710);
		// toSkip.add(4345);
		// toSkip.add(4347);
		// toSkip.add(4348);
		// toSkip.add(4349);
		// toSkip.add(4350);
		// toSkip.add(4351);
		// toSkip.add(4352);
		// toSkip.add(4353);
		// toSkip.add(5751);
		// toSkip.add(6711);
		// toSkip.add(7815);
		//
		// toSkip.add(743);
		// toSkip.add(1426);
		// toSkip.add(1609);
		// toSkip.add(1746);
		// toSkip.add(3440);
		// toSkip.add(4282);
		// toSkip.add(4289);
		// toSkip.add(4295);
		// toSkip.add(4307);
		// toSkip.add(4314);
		// toSkip.add(4321);
		// toSkip.add(4343);
		/* eval */
		// toSkip.add(2);
		// toSkip.add(3);
		// toSkip.add(4);
		// toSkip.add(28);
		// toSkip.add(62);
		// toSkip.add(114);
		// toSkip.add(115);
		// toSkip.add(137);
		// toSkip.add(138);
		// toSkip.add(169);
		// toSkip.add(170);
		// toSkip.add(254);
		// toSkip.add(255);
		// toSkip.add(256);
		// toSkip.add(257);
		// toSkip.add(258);
		// toSkip.add(259);
		// toSkip.add(260);
		// toSkip.add(261);
		// toSkip.add(262);
		// toSkip.add(263);
		// toSkip.add(264);
		// toSkip.add(265);
		// toSkip.add(266);
		// toSkip.add(676);
		// toSkip.add(695);
		// toSkip.add(750);
		// toSkip.add(751);
		// toSkip.add(752);
		// toSkip.add(753);
		// toSkip.add(754);
		// toSkip.add(755);
		// toSkip.add(756);
		// toSkip.add(757);
		// toSkip.add(801);
		// toSkip.add(802);
		// toSkip.add(3262);
		// toSkip.add(3349);
		// toSkip.add(3376);
		// toSkip.add(3389);
		// toSkip.add(3390);
		// toSkip.add(3399);
		// toSkip.add(3400);
		// toSkip.add(3401);
		// toSkip.add(3402);
		// toSkip.add(3436);
		// toSkip.add(3442);
		// toSkip.add(3554);
		// toSkip.add(3567);
		// toSkip.add(3578);
		// toSkip.add(3579);
		// toSkip.add(3580);
		// toSkip.add(3581);
		// toSkip.add(3582);
		// toSkip.add(3583);
		// toSkip.add(3596);
		// toSkip.add(3597);
		// toSkip.add(3620);
		// toSkip.add(3733);
		// toSkip.add(3734);
		// toSkip.add(3826);
		// toSkip.add(3863);
		// toSkip.add(3864);
		// toSkip.add(3865);
		// toSkip.add(3869);
		// toSkip.add(3870);
		// toSkip.add(3929);
		// toSkip.add(3930);
		// toSkip.add(3979);
		// toSkip.add(3980);
		// toSkip.add(3981);
		// toSkip.add(4055);

		int numberOfErrors = 0;
		int warnings = 0;
		int unresolvedRefs = 0;
		int noCtChars = 0;
		int constantStrings = 0;
		int noParams = 0;
		int fullyEvaluated = 0;
		int noHolesV = 0;
		int noHolesC = 0;
		List<TemplateEntry> templates = new ArrayList<TemplateEntry>();
		// int START = 0;
		// int END = 8620;
		// int END = 5383;
		int START = 1;
		int END = 27;
		Map<String, Set<String>> templatesPerCallSite = new HashMap<String, Set<String>>();
		EvaluationStrategy strategy = getEvaluationStrategy();
		BufferedWriter writerNonEv = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE)));
		for (int i = START; i <= END; i++) {
			if (!toSkip.contains(i))
				try {
					System.out.println("Analyzing " + i);
					Set<Template> temps = inferTemplates(i);
					if (temps == null) {
						warnings++;
						continue;
					}
					int count = 0;
					for (Template t : temps) {
						templates.add(t.model);
						// System.out.println(t.model);
						String id = i + "-" + t.codeLoc;
						if (templatesPerCallSite.containsKey(id)) {
							Set<String> currSet = templatesPerCallSite.get(id);
							Set<Object> evaluatedTmps = t.model.evaluate(strategy);
							for (Object evTmp : evaluatedTmps)
								currSet.add(evTmp.toString());
						} else {
							HashSet<String> currSet = new HashSet<String>();
							Set<Object> evaluatedTmps = t.model.evaluate(strategy);
							count += evaluatedTmps.size();
							for (Object evTmp : evaluatedTmps) {
								currSet.add(evTmp.toString());
								writerNonEv.write("*****\n");
								writerNonEv.write(t.type + "\n");
								writerNonEv.write(i + "\n");
								writerNonEv.write(t.codeLoc + "\n");
								writerNonEv.write(evTmp.toString() + "\n");
							}
							templatesPerCallSite.put(id, currSet);
						}
					}
					System.out.println("Inferred " + count + " for " + i);
				} catch (Exception e) {
					numberOfErrors++;
					e.printStackTrace();
				}
		}
		writerNonEv.close();
		Map<String, Integer> refsFreq = new HashMap<String, Integer>();
		Map<String, Integer> opsFreq = new HashMap<String, Integer>();
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(OUTPUT_FOLDER + "evaluated-templates.txt")));
		BufferedWriter writerCS = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(OUTPUT_FOLDER + "templates-per-csite.csv")));

		for (TemplateEntry t : templates) {
			System.out.println("=======");

			// System.out.println("Non-evaluated template:" + t);
			Set<Object> evaluatedSet = t.evaluate(strategy);
			System.out.println("Evaluated template:");
			for (Object evObj : evaluatedSet) {
				String evaluated = evObj.toString();

				// System.out.println(evaluated);
				List<String> refs = t.getVarNames();
				if (refs.size() == 0) {
					constantStrings++;
				} else {
					if (evaluated.indexOf(StringStrategy.CALL_H_PLACEHOLDER) == -1)
						fullyEvaluated++;
					if (evalStr == StringStrategy.class) {
						noHolesC += StringUtils.countMatches(evaluated, StringStrategy.CALL_H_PLACEHOLDER);
						noHolesV += StringUtils.countMatches(evaluated, StringStrategy.VAR_H_PLACEHOLDER);
					} else if (evalStr == RegexStrategy.class) {
						noHolesC += StringUtils.countMatches(evaluated, RegexStrategy.ALL_CHARS_HOLE_ESCAPED);
					}
					String ctString = removeHoles(evaluated);
					noCtChars += ctString.length();
					List<String> operations = t.getOperations();
					for (String op : operations) {
						if (opsFreq.containsKey(op)) {
							opsFreq.put(op, opsFreq.get(op) + 1);
						} else {
							opsFreq.put(op, 1);
						}
					}
				}

				for (String hole : refs) {
					if (hole.matches("param-.*"))
						noParams++;
					else {
						if (refsFreq.containsKey(hole)) {
							refsFreq.put(hole, refsFreq.get(hole) + 1);
						} else {
							refsFreq.put(hole, 1);
						}
					}
				}
				unresolvedRefs += refs.size();
			}
		}
		System.out.println("Number of analyzed files: " + (END - START));
		System.out.println("Number of errors: " + numberOfErrors);
		System.out.println("Number of warnings: " + warnings);
		System.out.println("Number of skipped: " + toSkip.size());
		System.out.println("Number of call sites: " + templatesPerCallSite.keySet().size());
		System.out.println("Number of inferred templates: " + templates.size());
		System.out.println("Number of constant strings: " + constantStrings);
		System.out.println("Number of fully evaluated templates: " + fullyEvaluated);
		System.out.println("No. of constant chars / non-constant template: "
				+ (noCtChars / ((double) templates.size() - constantStrings)));
		System.out.println("Number of holes: " + (noHolesC + noHolesV) + " (" + noHolesC + ", " + noHolesV + ")");
		System.out.println("No. of holes / non-constant template: "
				+ (noHolesC + noHolesV) / ((double) templates.size() - constantStrings));
		System.out.println("Number of unresolved references: " + unresolvedRefs);
		System.out.println("Number of parameters: " + noParams);
		Map<String, Integer> sortByValue = sortByValue(refsFreq);
		Set<String> keySet = sortByValue.keySet();
		System.out.println("Top references:");
		int max = 0;
		for (String key : keySet) {
			if (max++ > keySet.size() - 10) {
				System.out.println(key + " " + sortByValue.get(key));
			}
		}
		System.out.println("Top operations:");
		sortByValue = sortByValue(opsFreq);
		keySet = sortByValue.keySet();
		max = 0;
		int sanitizeCount = 0;
		int escapeCount = 0;
		for (String key : keySet) {
			if (max++ > keySet.size() - 11) {
				System.out.println(key + " " + sortByValue.get(key));
			}
			if (key.matches(".*escape.*"))
				escapeCount += sortByValue.get(key);
			if (key.matches(".*sanitize.*"))
				sanitizeCount += sortByValue.get(key);
		}

		keySet = templatesPerCallSite.keySet();
		writerCS.write(
				"Call Site, Unique Inferred Templates, Avg Constant Chars, No Constant Templates, No Fully Evaluated, Unresolved Templates\n");
		int tB = 0, tD = 0, tE = 0, tF = 0;
		double tC = 0;
		int countOnlyConstants = 0;
		int countAtLeastOneNonModeled = 0;
		int interestingCS = 0;
		for (String key : keySet) {
			Set<String> uniqueTemplates = templatesPerCallSite.get(key);
			int total = uniqueTemplates.size();
			noCtChars = 0;
			constantStrings = 0;
			fullyEvaluated = 0;
			int unmodeled = 0;
			for (String uniqueT : uniqueTemplates) {
				writer.write(uniqueT + "\n");
				writer.write("========\n");
				if (evalStr == StringStrategy.class) {
					if (uniqueT.indexOf(StringStrategy.CALL_H_PLACEHOLDER) == -1
							&& uniqueT.indexOf(StringStrategy.VAR_H_PLACEHOLDER) == -1)
						constantStrings++;
					else if (uniqueT.indexOf(StringStrategy.CALL_H_PLACEHOLDER) == -1)
						fullyEvaluated++;
					else
						unmodeled++;
				} else if (evalStr == RegexStrategy.class) {
					if (uniqueT.indexOf(RegexStrategy.ALL_CHARS_HOLE) == -1)
						constantStrings++;
					else
						fullyEvaluated++;
				}
				String ctString = removeHoles(uniqueT);
				noCtChars += ctString.length();
			}
			if (unmodeled > 0)
				countAtLeastOneNonModeled++;
			if (unmodeled == 0 && fullyEvaluated == 0)
				countOnlyConstants++;
			if (fullyEvaluated > 0 && unmodeled == 0)
				interestingCS++;
			tB += total;
			tC += (noCtChars / ((double) total));
			tD += constantStrings;
			tE += fullyEvaluated;
			tF += unmodeled;
			writerCS.write(key + "," + total + "," + (noCtChars / ((double) total)) + "," + constantStrings + ","
					+ fullyEvaluated + "," + unmodeled + "\n");
		}
		writerCS.write("," + tB + "," + (tC / keySet.size()) + "," + tD + "," + tE + "," + tF + "\n");
		writerCS.write("Call sites with only constant templates: ," + countOnlyConstants + "\n");
		writerCS.write("Call sites with non-modeled templates: ," + countAtLeastOneNonModeled + "\n");
		writerCS.write("Call sites with only constants + fully evaluatted: ," + interestingCS + "\n");
		writer.close();
		writerCS.close();
		System.out.println();
		System.out.println(".*escape.* " + escapeCount);
		System.out.println(".*sanitize.* " + sanitizeCount);
	}

	public static String removeHoles(String txt) {
		if (evalStr == RegexStrategy.class) {
			return txt.replaceAll(RegexStrategy.ALL_CHARS_HOLE_ESCAPED, "");
		}
		if (evalStr == StringStrategy.class) {
			return txt.replaceAll(StringStrategy.CALL_H_PLACEHOLDER, "").replaceAll(StringStrategy.VAR_H_PLACEHOLDER,
					"");
		}
		return txt;
	}

	public static EvaluationStrategy getEvaluationStrategy() {
		if (evalStr == RegexStrategy.class)
			return new RegexStrategy();
		if (evalStr == StringStrategy.class)
			return new StringStrategy();
		return null;
	}

	public static Set<Template> inferTemplates(int index) throws IOException {
		return AnalysisHelper.inferTemplates(SourceFile.fromFile(FOLDER + index + ".js"));
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry>() {
			public int compare(Map.Entry o1, Map.Entry o2) {
				return ((Integer) o1.getValue()).compareTo((Integer) o2.getValue());
			}

		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
