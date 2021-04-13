package emmo.manager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
	
	private static void convertOntologicalFormats(String outputFormat, OWLOntologyManager manager) {
		
	}
	
	public static void main(String[] args) {
		String libraryPath = init();
		
//		System.setProperty("factpp.jni.path", "C:\\GitRepositories\\factplusplus\\FaCT++.Java\\src\\main\\resources\\lib\\native\\64bit\\FaCTPlusPlusJNI.dll");
//		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
//		config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.);
	
		OWLOntologyManager mergedOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
//		OWLOntologyIRIMapper autoIRIMapper = new AutoIRIMapper(new File(WORKING_DIRECTORY), true);
//        manager.addIRIMapper(autoIRIMapper);
		
//		manager.setOntologyLoaderConfiguration(config);
		
		String iri = "https://emmo.info/emmo/1.0.0-alpha2";
		String outputFormat = "rdf";
		String destinationPath = WORKING_DIRECTORY+"emmo-inferred."+outputFormat;
		String options = null;
		
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
		
//		int position = 0;
//		
//		try {
//			iri = args[position];
//			position++;
//		} catch(Exception e) {
//			
//		}
//		if(iri==null) {
//			iri = "https://emmo.info/emmo/1.0.0-alpha2";
//			//iri = "https://raw.githubusercontent.com/emmo-repo/EMMO/1.0.0-alpha2/emmo.owl";
//		}
//		
//		String destinationPath = null;
//		try {
//			destinationPath = args[position];
//			position++;
//		} catch(Exception e) {
//			
//		}
//		if(destinationPath==null) {
//			destinationPath = WORKING_DIRECTORY+"export-inferred-.owl";
//		}
//		
//		String options = null;
//		try {
//			options = args[position];
//			position++;
//		} catch(Exception e) {
//			
//		}
//		String outputFormat = null;
//		try {
//			outputFormat = args[position];
//		} catch(Exception e) {
//			
//		}
//		if(outputFormat==null) {
//			outputFormat = "rdf";
//		}
		System.out.println("Fact++ JNI library path: "+libraryPath);	
		System.out.println("*** IRI: "+iri);
		System.out.println("*** Destination file path: "+destinationPath);
		System.out.println("*** Options: "+options);
		System.out.println("*** Output format: "+outputFormat);
		System.out.println("*** Generate inferred ontology: "+useReasoner);
		System.out.println("*** Map hexadecimal URIs to verbose URIs: "+remapURIs);
		
		generateInferredOntology(manager, mergedOntologyManager, iri, destinationPath, options, outputFormat, useReasoner, remapURIs);
	}

	private static void generateInferredOntology(OWLOntologyManager manager,
			OWLOntologyManager mergedOntologyManager, String iri,
			String destinationPath, String options, String outputFormat, Boolean useReasoner, Boolean remapURIs) {
		try {
			//			ont = mngr.loadOntologyFromOntologyDocument(IRI.cre)
			OWLOntology emmo = null;
			OWLOntology emmoMerged = null;
			OWLReasoner reasoner = null;
			if(iri.contains("http")) {
				emmo = manager.loadOntology(IRI.create(iri)); 
			} else {
				emmo = manager.loadOntologyFromOntologyDocument(new File(iri));
			}



			if(useReasoner) {

				OWLOntologyMerger merger = new OWLOntologyMerger(manager);
				Set<OWLOntology> imports = emmo.getImports();
				for(OWLOntology i: imports) {
					System.out.println("Imported: "+i.getOntologyID().getOntologyIRI().get());
					try {
						mergedOntologyManager.loadOntology(i.getOntologyID().getOntologyIRI().get());
					} catch(Exception e) {
						System.err.println("Could not merge: "+i.getOntologyID().getOntologyIRI().get()+" - "+e.getMessage());
						e.printStackTrace();
					}
				}



				emmoMerged = merger.createMergedOntology(mergedOntologyManager, IRI.create(iri+"merged.owl")); 

				OWLReasonerFactory fac = new FaCTPlusPlusReasonerFactory();
				reasoner = fac.createReasoner(emmo);
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
			File outputFile = new File(destinationPath);
			// Save the inferred ontology.
			manager.saveOntology(emmoMerged,
					format,
					IRI.create((outputFile.toURI())));

			// Terminate the worker threads used by the reasoner.
			if(reasoner!=null) {
				reasoner.dispose();
			}
			
			/* Fixing errors */
			String f = CommonUtils.readTextFile(outputFile.getAbsolutePath(), "UTF-8");
			if(f.contains("#decimal")) {
				f = f.replace("#decimal", "#double");
				CommonUtils.printFileUsingPrintWriter(f, outputFile.getAbsolutePath(), "UTF-8");
			}
			
			/* Remapping hexadecimal URIs to verbose URIs: */
			if(remapURIs) {
				EMMOUtils.replaceHexadecimalUrisWithLabels(outputFile.getAbsolutePath());
			}
			
		} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
			e.printStackTrace();
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
}
