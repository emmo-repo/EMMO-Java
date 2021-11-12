package emmo.manager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public class ReasonerUtils {

	private static final String WORKING_DIRECTORY = ""; //"C:\\Dropbox (Personal)\\Goldbeck\\EMMO\\1.0.0-alpha2\\";
	
	private static void core(String[] args) {
		String libraryPath = init();
		
//		System.setProperty("factpp.jni.path", "C:\\GitRepositories\\factplusplus\\FaCT++.Java\\src\\main\\resources\\lib\\native\\64bit\\FaCTPlusPlusJNI.dll");
//		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
//		config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.);
	
		OWLOntologyManager mergedOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		String iri = "EMMO"; //"https://emmo.info/emmo/1.0.0-alpha2";
		String outputFormat = "rdf";
		String destinationPath = WORKING_DIRECTORY+"output";
		String options = "secpodj";
		
		Boolean useReasoner = true;
		Boolean remapURIs = true;
		
		if(args!=null) {
			int i = 0;
			while(i<args.length) {
				String a = args[i];
				switch(a) {
					case "-s":
						try {
							iri = args[i+1];
						} catch(Exception e) {
							
						}
						break;
					case "-d":
						try {
							destinationPath = args[i+1];
						} catch(Exception e) {
							
						}
						break;
					case "-o":
						try {
							options = args[i+1];
						} catch(Exception e) {
							
						}
						break;
					case "-f":
						try {
							outputFormat = args[i+1];
						} catch(Exception e) {
							
						}
						break;
					case "-r":
						try {
							useReasoner = Boolean.parseBoolean(args[i+1]);
						} catch(Exception e) {
							
						}
						break;
					case "-m":
						try {
							remapURIs = Boolean.parseBoolean(args[i+1]);
						} catch(Exception e) {
							
						}
						break;	
				}
				i = i + 2;
			}
		}
		
		System.out.println("*** Fact++ JNI library path: "+libraryPath);	
		System.out.println("*** Input ontology IRI/file path: "+iri);
		System.out.println("*** Destination file/folder path: "+destinationPath);
		System.out.println("*** Options: "+options);
		System.out.println("*** Output format: "+outputFormat);
		System.out.println("*** Generate inferred ontology: "+useReasoner);
		System.out.println("*** Map hexadecimal URIs to verbose URIs: "+remapURIs);
		
		processOntology(manager, mergedOntologyManager, iri, destinationPath, options, outputFormat, useReasoner, remapURIs);
	}
	
	private static void processOntology(
			OWLOntologyManager manager,
			OWLOntologyManager mergedOntologyManager, 
			String iri,
			String destinationPath, 
			String options, 
			String outputFormat, 
			Boolean useReasoner, 
			Boolean remapURIs) {
		boolean folder = false;
		try {
			File outputFile = null;
			List<String> destinationFilePaths = new ArrayList<String>();
			//			ont = mngr.loadOntologyFromOntologyDocument(IRI.cre)
			OWLOntology emmo = null;
			OWLOntology emmoMerged = null;
			OWLReasoner reasoner = null;
			if(iri.contains("http")) {
				System.out.println("*** Remote ontology detected with URI: "+iri);
				emmo = manager.loadOntology(IRI.create(iri)); 
			} else if(!iri.endsWith(".rdf") && !iri.endsWith(".ttl") && !iri.endsWith(".owl")) {
				System.out.println("*** Directory detected; processing and converting ontology files found within it...");
				folder = true;
				
				File[] files = new File(iri).listFiles();
				for(File f: files) {
					if(!f.isDirectory() && (f.getName().endsWith(".rdf") || f.getName().endsWith(".owl") || f.getName().endsWith(".ttl"))) {
						convertFile(manager, f, destinationPath, destinationFilePaths);
					} else if(f.isDirectory()) {
						
//						if(!f.getName().equals("application")) {
						
	//						System.err.println("Found a directory: "+f.getAbsolutePath());
							File[] innerFiles = f.listFiles();
							for(File inf: innerFiles) {
	//							System.err.println("Found file: "+inf.getAbsolutePath());
								if(inf.getName().endsWith(".rdf") || inf.getName().endsWith(".owl") || inf.getName().endsWith(".ttl")) {
									convertFile(manager, inf, destinationPath, destinationFilePaths);
								}
							}
//						}
					}
//					System.out.println("------- Saving merged ontology file...");
//					saveOntologyToFile(manager, destinationPath+File.separator+"merged.rdf", emmoMerged, new RDFXMLDocumentFormat());
				}
				
			} else {	
				System.out.println("*** Single ontology file detected.");
				emmo = manager.loadOntologyFromOntologyDocument(new File(iri));
			
			}

			/* 
			 * Merge ontologies
			 * 
			 */
			
			if(!folder) {
				OWLOntologyMerger merger = new OWLOntologyMerger(manager);
				Set<OWLOntology> imports = emmo.getImports();
				for(OWLOntology i: imports) {
					System.out.println("Imported: "+i.getOntologyID().getOntologyIRI().get());
					mergeOntology(mergedOntologyManager, i);
				}

				emmoMerged = merger.createMergedOntology(mergedOntologyManager, IRI.create(iri+"merged.owl")); 
			} else {
//				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				OWLOntologyMerger merger = new OWLOntologyMerger(mergedOntologyManager);
				for(String destinationFilePath: destinationFilePaths) {
//					OWLOntology i = man.loadOntologyFromOntologyDocument(new File(destinationFilePath));
//					System.out.println("Loaded: "+i.getOntologyID().getOntologyIRI().get());
					mergeOntologyFromDocuments(mergedOntologyManager, destinationFilePath);
				}
				emmoMerged = merger.createMergedOntology(mergedOntologyManager, IRI.create(iri+"merged.owl"));
			}
			
			if(useReasoner) {

				
								
				OWLReasonerFactory fac = new FaCTPlusPlusReasonerFactory();
				reasoner = fac.createReasoner(emmoMerged);
				//			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);


				//			gens.add(new InferredSubClassAxiomGenerator());
				//			gens.add(new InferredEquivalentClassAxiomGenerator());
				//			gens.add(new InferredClassAssertionAxiomGenerator());
				//			gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
				//			gens.add(new InferredDisjointClassesAxiomGenerator());

				// Put the inferred axioms into a fresh empty ontology.
				//			OWLOntology inferredOntology = outputOntologyManager.createOntology();
				InferredOntologyGenerator iog = null;
				if(options==null) {
					iog = new InferredOntologyGenerator(reasoner);
				} else {
					List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
					if(options.toLowerCase().contains("s")) {
						gens.add(new InferredSubClassAxiomGenerator());
					}
					if(options.toLowerCase().contains("e")) {
						gens.add(new InferredEquivalentClassAxiomGenerator());
					}
					if(options.toLowerCase().contains("c")) {
						gens.add(new InferredClassAssertionAxiomGenerator());
					}
					if(options.toLowerCase().contains("p")) {
						gens.add(new InferredPropertyAssertionGenerator());
					}
					if(options.toLowerCase().contains("o")) {
						gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
					}
					if(options.toLowerCase().contains("d")) {
						gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
					}
					if(options.toLowerCase().contains("j")) {
						gens.add(new InferredDisjointClassesAxiomGenerator());
					}

					iog = new InferredOntologyGenerator(reasoner, gens);
				}


				//			InferredOntologyGenerator 

				iog.fillOntology(mergedOntologyManager.getOWLDataFactory(), emmoMerged);

			}
			OWLDocumentFormat format = null;
			switch(outputFormat) {
				case "rdf":
					format = new RDFXMLDocumentFormat();
					break;
				case "ttl":
					format = new TurtleDocumentFormat();
					break;
				case "owl":
					format = new OWLXMLDocumentFormat(); 
					break;
			}
			
			if(emmoMerged==null) {
				emmoMerged = emmo;
			}
			String asserted = "_inferred";
			if(!useReasoner) {
				asserted = "_asserted";
			}
			
			if(emmoMerged!=null) {
				System.out.println("*** Saving merged ontology...");
				outputFile = saveOntologyToFile(manager, destinationPath+File.separator+"full_ontology"+asserted+"."+outputFormat, emmoMerged, format);
				
				/* Fixing errors */
				fixErrors(outputFile);
				
				/* Remapping hexadecimal URIs to verbose URIs: */
				if(remapURIs) {
					System.out.println("*** Remapping hexadecimal URIs to verbose URIs...");
					StringBuilder sb = new StringBuilder("");
					try {
						for(String destinationFilePath: destinationFilePaths) {
							System.out.println("*** *** * Generating renaming map for "+destinationFilePath);
							sb.append(EMMOUtils.generateRenamingMap(destinationFilePath));
						}
						System.out.println("*** Renaming map generated.");
						CommonUtils.printFileUsingPrintWriter(sb.toString(), "files/renamingMap.txt", "UTF-8");
						
						EMMOUtils.replaceHexadecimalUrisWithLabels(outputFile.getAbsolutePath(), false);
						System.out.println("*** Renaming completed.");
					} catch(Exception e) {
						System.err.println("Could not rename the ontology's URIs - "+e.getMessage());
					}
				}
			}

			// Terminate the worker threads used by the reasoner.
			if(reasoner!=null) {
				reasoner.dispose();
			}
			
			if(outputFile!=null) {
				System.out.println("Ontology produced within: "+outputFile.getAbsolutePath());
			}
			
			System.out.println("Done."); 
			
		} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	private static OWLOntology convertFile(OWLOntologyManager manager, File sourceFile, String destinationPath, List<String> destinationFilePaths) throws OWLOntologyCreationException, OWLOntologyStorageException {
		OWLOntology emmo;
		emmo = manager.loadOntologyFromOntologyDocument(sourceFile);
		if(sourceFile.getName().endsWith(".ttl")) {
			System.out.println("--- Converting file: "+sourceFile.getAbsolutePath()+" to RDF/XML format...");
			String destinationFilePath = destinationPath+File.separator+sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("."))+".rdf";
			saveOntologyToFile(manager, destinationFilePath, emmo, new RDFXMLDocumentFormat());
			destinationFilePaths.add(destinationFilePath);
		}
		return emmo;
	}

	private static void mergeOntology(OWLOntologyManager mergedOntologyManager, OWLOntology i) {
		try {
			mergedOntologyManager.loadOntology(i.getOntologyID().getOntologyIRI().get());
		} catch(Exception e) {
			System.err.println("Could not merge: "+i.getOntologyID().getOntologyIRI().get()+" - "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void mergeOntologyFromDocuments(OWLOntologyManager mergedOntologyManager, String filePath) {
		try {
			mergedOntologyManager.loadOntologyFromOntologyDocument(new File(filePath));
		} catch(Exception e) {
			System.err.println("Could not merge: "+filePath+" - "+e.getMessage());
			e.printStackTrace();
		}
	}

	protected static File saveOntologyToFile(OWLOntologyManager manager,
			String destinationPath, OWLOntology emmoMerged,
			OWLDocumentFormat format) throws OWLOntologyStorageException {
		File outputFile = new File(destinationPath);
		// Save the inferred ontology.
		manager.saveOntology(emmoMerged,
				format,
				IRI.create((outputFile.toURI())));
		return outputFile;
	}

	protected static void fixErrors(File outputFile) {
		String f = CommonUtils.readTextFile(outputFile.getAbsolutePath(), "UTF-8");
		if(f.contains("#decimal")) {
			f = f.replace("#decimal", "#double");
			CommonUtils.printFileUsingPrintWriter(f, outputFile.getAbsolutePath(), "UTF-8");
		}
	}

	private static String init() {
		Properties prop = new Properties();
		try {
			InputStream is = new FileInputStream("properties/reasoner.properties");
			prop.load(is);
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		String libraryPath = System.getProperty("user.dir")+File.separator+prop.getProperty("factpp.jni.path");
		System.setProperty("factpp.jni.path", libraryPath);
		return libraryPath;
	}
	
	private static void test(String baseEMMOdir, String destDir) {
		String libraryPath = init();
		OWLOntologyManager mergedOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		
		
		String sourceDirApplication = "application";
		String sourceDirDomain = "domain";
		String sourceDirMiddle = "middle";
		String sourceDirTop = "top";
		
		List<String> sourceDirs = Arrays.asList(sourceDirTop, sourceDirMiddle, sourceDirDomain, sourceDirApplication);
		for(String sourceDir: sourceDirs) {
			String dir = baseEMMOdir+File.separator+sourceDir;
			System.out.println("Source directory: "+dir);
			File[] files = new File(dir).listFiles(); 
			for(File f: files) {
			
				if(!f.isDirectory() && f.getName().endsWith(".ttl")) {
					System.out.println("--- Converting file: "+f.getAbsolutePath()+" to RDF/XML format...");
					processOntology(manager, mergedOntologyManager, f.getAbsolutePath(), destDir+f.getName().substring(0, f.getName().lastIndexOf("."))+".rdf", null, "rdf", false, false);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		core(args);
//		String baseEMMOdir = "C:\\GitRepositories\\EMMO";
//		String destDir = "C:\\Dropbox (Personal)\\Goldbeck\\EMMO\\refactoring\\original\\";
		
//		test(baseEMMOdir, destDir);
	}
}