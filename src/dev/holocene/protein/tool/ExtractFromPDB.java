package dev.holocene.protein.tool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

public final class ExtractFromPDB {
	private ExtractFromPDB() {}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			Files.walk(Paths.get(args[0])).forEach(path -> {
				if (Files.isRegularFile(path)) {
					Supplier<BufferedReader> input = () -> {
						try {
							return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					};
					BiFunction<String, Character, BufferedWriter> output = (type, chain) -> {
						try {
							var entry = path.getFileName().toString();
							entry = entry.substring(0, entry.indexOf(".")) + "_" + chain + "." + type;
							return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
								Paths.get(type).resolve(args[0]).resolve(entry).toFile()
							)));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					};
					try {
						var fold = new HashMap<String, Structure>();
						try (var data = input.get()) {
							var structures = data.lines().filter(line -> line.startsWith("HELIX ") || line.startsWith("SHEET "));
							for (var structure : structures.toArray(String[]::new)) {
								if (structure.startsWith("HELIX "))
									fold.put(structure.charAt(19) + structure.substring(21, 26), new Structure(Structure.Type.HELIX, structure.charAt(31) + structure.substring(33, 38)));
								else
									fold.put(structure.substring(21, 27), new Structure(Structure.Type.STRAND, structure.substring(32, 38)));
							}
						}
						var chains = new HashMap<Character, Chain>();
						try (var data = input.get()) {
							var atoms = data.lines().filter(line -> line.startsWith("ATOM  ") || line.startsWith("HETATM") || line.startsWith("ENDMDL"));
							var id = "";
							var structure = Structure.EMPTY;
							for (var atom : atoms.toArray(String[]::new)) {
								if (atom.startsWith("ENDMDL"))
									break;
								if (id.equals(atom.substring(21, 27)))
									continue;
								id = atom.substring(21, 27);
								var aminoacid = AMINOACIDS.get(atom.substring(17, 20));
								if (aminoacid != null) {
									chains.putIfAbsent(id.charAt(0), new Chain());
									var chain = chains.get(id.charAt(0));
									chain.sequence.append(aminoacid);
									if (structure != Structure.EMPTY) {
										chain.structure.append(structure.type().id);
										if (id.equals(structure.term()))
											structure = Structure.EMPTY;
									} else
										chain.structure.append((structure = fold.getOrDefault(id, Structure.EMPTY)).type().id);
								}
							}
						}
						for (var chain : chains.entrySet()) {
							try (var storage = output.apply("amnacd", chain.getKey())) {
								storage.write(chain.getValue().sequence.toString());
							}
							try (var storage = output.apply("struct", chain.getKey())) {
								storage.write(chain.getValue().structure.toString());
							}
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
		} catch (UncheckedIOException | IOException e) {
			e.printStackTrace();
		}
	}

	private static final Map<String, String> AMINOACIDS = new HashMap<>() {{
		put("ALA", "A");
		put("ABA", "A"); // het
		put("AIB", "A"); // het
		put("ALN", "A"); // het
		put("DAL", "A"); // het
		put("AZH", "A"); // het
		put("CYS", "C");
		put("60F", "C"); // het
		put("CAS", "C"); // het
		put("CYQ", "C"); // het
		put("CSS", "C"); // het
		put("CCS", "C"); // het
		put("CSR", "C"); // het
		put("CSD", "C"); // het
		put("CSO", "C"); // het
		put("CSX", "C"); // het
		put("CZZ", "C"); // het
		put("CME", "C"); // het
		put("CAF", "C"); // het
		put("OCS", "C"); // het
		put("OCY", "C"); // het
		put("YCM", "C"); // het
		put("SMC", "C"); // het
		put("SNC", "C"); // het
		put("NPH", "C"); // het
		put("TQZ", "C"); // het
		put("XCN", "C"); // het
		put("2CO", "C"); // het
		put("ASP", "D");
		put("BFD", "D"); // het
		put("PHD", "D"); // het
		put("IAS", "D"); // het
		put("GLU", "E");
		put("DGL", "E"); // het
		put("CGA", "E"); // het
		put("PCA", "E"); // het
		put("FGA", "E"); // het
		put("PHE", "F");
		put("PHI", "F"); // het
		put("MEA", "F"); // het
		put("BIF", "F"); // het
		put("4AF", "F"); // het
		put("GLY", "G");
		put("SAR", "G"); // het
		put("HIS", "H");
		put("HIS", "H");
		put("DHI", "H"); // het
		put("MHS", "H"); // het
		put("NZH", "H"); // het
		put("ILE", "I");
		put("LYS", "K");
		put("DLY", "K"); // het
		put("MLY", "K"); // het
		put("MLZ", "K"); // het
		put("ILY", "K"); // het
		put("LA2", "K"); // het
		put("M3L", "K"); // het
		put("ALY", "K"); // het
		put("LEU", "L");
		put("MLU", "L"); // het
		put("MLE", "L"); // het
		put("NLE", "L"); // het
		put("DLE", "L"); // het
		put("MET", "M");
		put("MSE", "M"); // het
		put("MHO", "M"); // het
		put("FME", "M"); // het
		put("SME", "M"); // het
		put("ASN", "N");
		put("SNN", "N"); // het
		put("PYL", "O");
		put("PRO", "P");
		put("DPR", "P"); // het
		put("8LJ", "P"); // het
		put("4FB", "P"); // het
		put("FP9", "P"); // het
		put("GLN", "Q");
		put("DGN", "Q"); // het
		put("ARG", "R");
		put("SER", "S");
		put("SEP", "S"); // het
		put("SET", "S"); // het
		put("SAC", "S"); // het
		put("SDP", "S"); // het
		put("DSN", "S"); // het
		put("PYR", "S"); // het
		put("MIR", "S"); // het
		put("THR", "T");
		put("BMT", "T"); // het
		put("DTH", "T"); // het
		put("TPO", "T"); // het
		put("SEC", "U");
		put("VAL", "V");
		put("MVA", "V"); // het
		put("NVA", "V"); // het
		put("TRP", "W");
		put("DTR", "W"); // het
		put("4IN", "W"); // het
		put("TYR", "Y");
		put("BYR", "Y"); // het
		put("4PH", "Y"); // het
		put("4BF", "Y"); // het
		put("3CT", "Y"); // het
		put("3MY", "Y"); // het
		put("OMY", "Y"); // het
		put("OMZ", "Y"); // het
		put("NIY", "Y"); // het
		put("PTR", "Y"); // het
		put("IYR", "Y"); // het
		put("TYI", "Y"); // het
	}};

	private static record Structure(Type type, String term) {
		public static final Structure EMPTY = new Structure(Type.COIL, "");
		public enum Type {
			COIL('C'), HELIX('H'), STRAND('S');

			public final char id;

			Type(char id) {
				this.id = id;
			}
		}
	}

	private static class Chain {
		public final StringBuilder sequence = new StringBuilder();
		public final StringBuilder structure = new StringBuilder();
	}
}
