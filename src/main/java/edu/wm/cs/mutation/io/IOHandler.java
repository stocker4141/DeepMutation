package edu.wm.cs.mutation.io;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import edu.wm.cs.mutation.tester.ChangeExtractor;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.pair.MethodPair;
import org.apache.commons.io.FileUtils;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IOHandler {

	public static final String METHODS = "methods";
	private static final String MUTANTS = "mutants";

	private static final String KEY_SUFFIX = ".key";
	private static final String SRC_SUFFIX = ".src";
	private static final String MAP_SUFFIX = ".map";
	public static final String ABS_SUFFIX = ".abs";

	private static final String RESULTS_COMP = "results.comp";
	private static final String RESULTS_TEST = "results.test";
	private static final String PASSED = "PASSED";
	private static final String FAILED = "FAILED";

	private static final String MUTANT_LOG_SUFFIX = ".log";
	private static final String COMPILE_LOG_SUFFIX = "_compile.log";
	private static final String TEST_LOG_SUFFIX = "_test.log";

	private static final String MUTANT_DIR = "mutants/";
	private static final String LOG_DIR = "logs/";

	public static HashSet<String> readInputMethods(String methodPath) {
		System.out.println("Reading specified methods from input file... ");

		List<String> methods = null;
		try {
			methods = Files.readAllLines(Paths.get(methodPath));
		} catch (IOException e) {
			System.err.println("  ERROR: could not load specified methods from files: " + e.getMessage());
		}

		if (methods == null) {
			System.err.println("  ERROR: could not load specified methods from files");
			return null;
		}
		return new HashSet<String>(methods);
	}

	public static void writeMethods(String outPath, LinkedHashMap<String, String> map, boolean abstracted) {
		if (abstracted) {
			System.out.println("Writing abstracted methods... ");
		} else {
			System.out.println("Writing methods... ");
		}

		if (map == null) {
			System.err.println("  ERROR: cannot write null input map");
			return;
		}

		List<String> signatures = new ArrayList<>(map.keySet());
		List<String> bodies = new ArrayList<>(map.values());

		try {
			Files.createDirectories(Paths.get(outPath));
			Files.write(Paths.get(outPath + METHODS + KEY_SUFFIX), signatures);
			if (abstracted) {
				Files.write(Paths.get(outPath + METHODS + ABS_SUFFIX), bodies);
			} else {
				Files.write(Paths.get(outPath + METHODS + SRC_SUFFIX), bodies);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	public static LinkedHashMap<String, String> readMethods(String outPath, boolean abstracted) {
		if (abstracted) {
			System.out.println("Reading abstracted methods from file... ");
		} else {
			System.out.println("Reading methods from file... ");
		}

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		List<String> signatures = null;
		List<String> bodies = null;
		try {
			signatures = Files.readAllLines(Paths.get(outPath + METHODS + KEY_SUFFIX));
			if (abstracted) {
				bodies = Files.readAllLines(Paths.get(outPath + METHODS + ABS_SUFFIX));
			} else {
				bodies = Files.readAllLines(Paths.get(outPath + METHODS + SRC_SUFFIX));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (signatures == null || bodies == null) {
			System.err.println("  ERROR: could not load map from files");
			return null;
		}

		for (int i = 0; i < signatures.size(); i++) {
			map.put(signatures.get(i), bodies.get(i));
		}

		System.out.println("done.");
		return map;
	}

	public static void writeMutants(String outPath, Map<String, LinkedHashMap<String, String>> modelsMap,
			List<String> modelPaths, boolean abstracted) {
		if (abstracted) {
			System.out.println("Writing abstracted mutants... ");
		} else {
			System.out.println("Writing mutants... ");
		}

		if (modelsMap == null) {
			System.err.println("ERROR: cannot write null input map");
			return;
		}

		for (String modelPath : modelPaths) {
			File modelFile = new File(modelPath);
			String modelName = modelFile.getName();
			System.out.println("  Processing model " + modelName + "... ");

			LinkedHashMap<String, String> mutantsMap = modelsMap.get(modelName);
			if (mutantsMap == null) {
				System.err.println("    WARNING: cannot write null map for model " + modelName);
				continue;
			}

			List<String> signatures = new ArrayList<>(mutantsMap.keySet());
			List<String> bodies = new ArrayList<>(mutantsMap.values());

			try {
				String modelOutPath = outPath + modelName + "/";
				Files.createDirectories(Paths.get(modelOutPath));
				Files.write(Paths.get(modelOutPath + MUTANTS + KEY_SUFFIX), signatures);
				if (abstracted) {
					Files.write(Paths.get(modelOutPath + MUTANTS + ABS_SUFFIX), bodies);
				} else {
					Files.write(Paths.get(modelOutPath + MUTANTS + SRC_SUFFIX), bodies);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("  done.");
		}
		System.out.println("done.");
	}

	public static Map<String, LinkedHashMap<String, String>> readMutants(String outPath, List<String> modelPaths,
			boolean abstracted) {
		if (abstracted) {
			System.out.println("Reading abstracted mutants from files... ");
		} else {
			System.out.println("Reading mutants from files... ");
		}

		Map<String, LinkedHashMap<String, String>> modelsMap = new HashMap<>();

		for (String modelPath : modelPaths) {
			File modelFile = new File(modelPath);
			String modelName = modelFile.getName();
			System.out.println("  Processing model " + modelName + "... ");

			String modelOutPath = outPath + modelName + "/";
			List<String> signatures = null;
			List<String> bodies = null;

			try {
				signatures = Files.readAllLines(Paths.get(modelOutPath + MUTANTS + KEY_SUFFIX));
				if (abstracted) {
					bodies = Files.readAllLines(Paths.get(modelOutPath + MUTANTS + ABS_SUFFIX));
				} else {
					bodies = Files.readAllLines(Paths.get(modelOutPath + MUTANTS + SRC_SUFFIX));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (signatures == null || bodies == null) {
				System.err.println("  ERROR: could not load map from files");
				return null;
			}

			LinkedHashMap<String, String> mutantsMap = new LinkedHashMap<>();
			for (int i = 0; i < signatures.size(); i++) {
				mutantsMap.put(signatures.get(i), bodies.get(i));
			}

			modelsMap.put(modelName, mutantsMap);
			System.out.println("  done.");
		}
		System.out.println("done.");
		return modelsMap;
	}

	/**
	 * Create mutants for each mutated method. Output the changes from original
	 * class to mutant class
	 * 
	 */
	public static void createMutantFiles(String outPath, Map<String, LinkedHashMap<String, String>> modelsMap,
			List<CtMethod> methods, List<String> modelPaths) {
		System.out.println("Creating mutant files... ");

		if (modelsMap == null) {
			System.err.println("  ERROR: cannot write null input map");
			return;
		}

		for (String modelPath : modelPaths) {
			File modelFile = new File(modelPath);
			String modelName = modelFile.getName();
			System.out.println("  Processing model " + modelName + "... ");

			LinkedHashMap<String, String> mutantsMap = modelsMap.get(modelName);
			List<String> logs = new ArrayList<>();

			if (mutantsMap == null) {
				System.err.println("    WARNING: cannot write null map for model " + modelName);
				continue;
			}

			// create directory
			String mutantPath = outPath + modelName + "/" + MUTANT_DIR;
			if (!Files.exists(Paths.get(mutantPath))) {
				try {
					Files.createDirectories(Paths.get(mutantPath));
				} catch (IOException e) {
					System.out.println("    ERROR: could not create mutant directory: " + e.getMessage());
					e.printStackTrace();
					continue;
				}
			}

			// create format for padded mutantIDs
			int num_digits = Integer.toString(mutantsMap.keySet().size()).length();
			StringBuilder format = new StringBuilder();
			format.append("%0").append(num_digits).append("d");

			// replace original methods with mutants
			int counter = 1; // counter for mutated file
			for (CtMethod method : methods) {
				String signature = method.getParent(CtType.class).getQualifiedName() + "#" + method.getSignature();
				if (!mutantsMap.containsKey(signature)) {
					continue;
				}

				SourcePosition sp = method.getPosition();
				String original = sp.getCompilationUnit().getOriginalSourceCode();

				// XXXX_relative-path-to-file.java
				String mutantID = String.format(format.toString(), counter);
				String fileName = mutantID + "_" + sp.getCompilationUnit().getFile().getPath()
						.replaceFirst(System.getProperty("user.dir") + "/", "").replace("/", "-");

				// construct and format mutated class

				// find start position of original source code
				int srcStart = sp.getNameSourceStart();
				while (original.charAt(srcStart) != '\n')
					srcStart--;
				srcStart++;

				StringBuilder sb = new StringBuilder();
				sb.append(original.substring(0, srcStart));
				sb.append(mutantsMap.get(signature));
				sb.append(original.substring(sp.getSourceEnd() + 1));

				try {
					String formattedSrc = new Formatter().formatSource(sb.toString());
					Files.write(Paths.get(mutantPath + fileName), formattedSrc.getBytes());

					// Extract the changes from original src code to mutanted src code
					ChangeExtractor changeTester = new ChangeExtractor();
					Map<MethodPair, List<Operation>> changesMap = changeTester.extractChanges(original, formattedSrc);

					if (changesMap == null || changesMap.size() == 0) {
						logs.add(fileName + "_" + method.getSignature() + "_un-mutated");
					} else {
						// Output the changes to folder mutantPath/counter_mutatedMethodName/
						String mutatedMethod = String.format(format.toString(), counter) + "_" + method.getSimpleName();

						// Output the changes to folder mutantPath/mutantID_change_ID
						String outDir = mutantPath + "/" + mutantID;
						ChangeExporter exporter = new ChangeExporter(changesMap);
						exporter.exportChanges(outDir);

						// Create log for mutated method
						logs.add(fileName + "_" + method.getSignature());
					}

					// Write mutant log to /modelpath/mutants.log
					String logPath = outPath + modelName + "/";
					Files.write(Paths.get(logPath + MUTANTS + MUTANT_LOG_SUFFIX), logs);

				} catch (FormatterException e) {
					System.err.println("    ERROR: could not format mutant " + counter + ": " + e.getMessage());
				} catch (IOException e) {
					System.err.println("    ERROR: could not write mutant " + counter + ": " + e.getMessage());
				}

				counter++;
			}
			System.out.println("  done.");
		}
		System.out.println("done.");
	}

	public static void writeLogs(String outPath, Map<String, Map<String, String>> modelsMap, List<String> modelPaths,
			String type) {
		if (type.equals("test")) {
			System.out.println("Writing test logs... ");
		} else {
			System.out.println("Writing compile logs... ");
		}

		if (modelsMap == null) {
			System.err.println("  ERROR: cannot write null input map");
			return;
		}

		for (String modelPath : modelPaths) {
			File modelFile = new File(modelPath);
			String modelName = modelFile.getName();
			System.out.println("  Processing model " + modelName + "... ");

			Map<String, String> mutantsMap = modelsMap.get(modelName);

			if (mutantsMap == null) {
				System.err.println("    WARNING: cannot write null map for model " + modelName);
				continue;
			}

			// Create log directory
			String logPath = outPath + modelName + "/" + LOG_DIR;
			try {
				Files.createDirectories(Paths.get(logPath));
			} catch (IOException e) {
				System.err.println("    ERROR: could not create log directory");
				e.printStackTrace();
				continue;
			}

			String suffix = (type.equals("test")) ? TEST_LOG_SUFFIX : COMPILE_LOG_SUFFIX;

			for (String mutantID : mutantsMap.keySet()) {
				try {
					Files.write(Paths.get(logPath + mutantID + suffix), mutantsMap.get(mutantID).getBytes());
				} catch (IOException e) {
					System.err.println("    Error in writing mutant " + mutantID + ": " + e.getMessage());
				}
			}
			System.out.println("  done.");
		}
		System.out.println("done.");
	}

	public static void writeBaseline(String outPath, String baseline, String type) {
		if (baseline == null) {
			System.err.println("ERROR: cannot write null input string");
			return;
		}

		String baselineID = "baseline";
		String suffix = (type.equals("test")) ? TEST_LOG_SUFFIX : COMPILE_LOG_SUFFIX;

		try {
			Files.write(Paths.get(outPath + baselineID + suffix), baseline.getBytes());
		} catch (IOException e) {
			System.err.println("    Error in writing baseline: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void writeResults(String outPath, Map<String, Map<String, Boolean>> modelsMap,
			List<String> modelPaths, String type) {
		System.out.println("Writing results... ");
		if (modelsMap == null) {
			System.err.println("ERROR: cannot write null input map");
			return;
		}

		for (String modelPath : modelPaths) {
			File modelFile = new File(modelPath);
			String modelName = modelFile.getName();
			System.out.println("  Processing model " + modelName + "... ");

			Map<String, Boolean> mutantsMap = modelsMap.get(modelName);

			if (mutantsMap == null) {
				System.err.println("    WARNING: cannot write null map for model " + modelName);
				continue;
			}

			String name = (type.equals("test")) ? RESULTS_TEST : RESULTS_COMP;
			String path = outPath + modelName + "/";

			StringBuilder sb = new StringBuilder();

			for (String mutantID : mutantsMap.keySet()) {
				sb.append(mutantID).append(" ");
				if (mutantsMap.get(mutantID)) {
					sb.append(PASSED);
				} else {
					sb.append(FAILED);
				}
				sb.append(System.lineSeparator());
			}
			try {
				Files.write(Paths.get(path + name), sb.toString().getBytes());
			} catch (IOException e) {
				System.err.println("    Error in writing results");
			}
			System.out.println("  done.");
		}
		System.out.println("done.");
	}

	public static void exportMutants(String projPath, String outPath, String mutantsPath) {
		System.out.println("Exporting mutants... ");

		File origProj = new File(projPath);
		File dir = new File(mutantsPath);
		File[] mutants = dir.listFiles();

		if (mutants == null) {
			System.err.println("  ERROR: " + mutantsPath + " is not a directory");
			return;
		}
		if (mutants.length == 0) {
			System.out.println("  ERROR: could not find any mutants");
			return;
		}

		for (File mutant : mutants) {
			if (!mutant.isFile() || !mutant.getName().endsWith(".java")) {
				continue;
			}
			String mutantID = mutant.getName().split("_")[0];
			File mutantProj = new File(outPath + origProj.getName() + mutantID);
			String mutantPath = mutant.getName().split("_")[1].replace("-", "/").replaceFirst(projPath,
					mutantProj.getPath() + "/");

			try {
				FileUtils.copyDirectory(origProj, mutantProj);
				System.out.println("  Created " + mutantProj.getPath() + ".");
			} catch (IOException e) {
				System.err.println("  ERROR: could not copy project for mutant " + mutantID);
				e.printStackTrace();
				return;
			}

			try {
				Files.copy(mutant.toPath(), Paths.get(mutantPath), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				System.err.println("  ERROR: could not export mutant " + mutantID);
				try {
					FileUtils.deleteDirectory(mutantProj);
				} catch (IOException ee) {
					System.err.println("  ERROR: could not clean up failed project " + mutantProj.getPath());
					ee.printStackTrace();
				}
				e.printStackTrace();
			}
		}
		System.out.println("done.");
	}

	public static void writeMappings(String outPath, Map<String, String> dictMap) {
		System.out.println("Writing mappings... ");
		if (dictMap == null) {
			System.err.println("ERROR: cannot write null input mappings");
			return;
		}
		List<String> mappings = new ArrayList<>(dictMap.values());
		try {
			Files.write(Paths.get(outPath + METHODS + MAP_SUFFIX), mappings);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	public static LinkedHashMap<String, String> readMappings(String outPath) {
		System.out.println("Reading mappings from file... ");
		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		List<String> signatures = null;
		List<String> mappings = null;
		try {
			signatures = Files.readAllLines(Paths.get(outPath + METHODS + KEY_SUFFIX));
			mappings = Files.readAllLines(Paths.get(outPath + METHODS + MAP_SUFFIX));

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (signatures == null || mappings == null) {
			System.err.println("  ERROR: could not load map from files");
			return null;
		}

		for (int i = 0; i < signatures.size(); i++) {
			map.put(signatures.get(i), mappings.get(i));
		}

		System.out.println("done.");
		return map;
	}

	public static Set<String> readIdioms(String filePath) {
		try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
			return stream.collect(Collectors.toSet());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
