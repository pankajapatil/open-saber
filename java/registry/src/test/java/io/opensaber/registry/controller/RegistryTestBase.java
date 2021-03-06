package io.opensaber.registry.controller;

import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import io.opensaber.converters.JenaRDF4J;
import io.opensaber.registry.middleware.util.Constants;
import io.opensaber.utils.converters.RDF2Graph;
import io.opensaber.validators.shex.shaclex.ShaclexValidator;


public class RegistryTestBase {
	
	@Autowired
	private Environment environment;
	
	public String jsonld;
	public static final String FORMAT = "JSON-LD";
	private static final String INVALID_SUBJECT_LABEL = "ex:Picasso";
	private static final String REPLACING_SUBJECT_LABEL = "!samp131d";
	private static final String VALID_JSONLD = "school.jsonld";
	private static final String CONTEXT_CONSTANT = "sample:";
	private Graph graph;
	
	public void setJsonld(String filename){

		try {
			String file = Paths.get(getPath(filename)).toString();
	    	jsonld = readFromFile(file);
		} catch (Exception e) {
			jsonld = StringUtils.EMPTY;
		}

	}

	public UUID getLabel() {
		UUID label = UUID.randomUUID();
		return label;
	}
	public String readFromFile(String file) throws IOException,FileNotFoundException{
		BufferedReader reader = new BufferedReader(new FileReader (file));
		StringBuilder sb = new StringBuilder();
		try{
			String line = null;
			while((line = reader.readLine()) !=null){
				sb.append(line);
			}
		}catch(Exception e){
			return StringUtils.EMPTY;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
		return sb.toString();
	}
	
	public URI getPath(String file) throws URISyntaxException {
		return this.getClass().getClassLoader().getResource(file).toURI();
	}

	public String generateBaseUrl(){
		return Constants.INTEGRATION_TEST_BASE_URL;
	}
	
	public Model getNewValidRdf(String fileName, String contextConstant){
		setJsonld(fileName);
		setJsonldWithNewRootLabel(contextConstant+generateRandomId());
		Model model = ShaclexValidator.parse(jsonld, FORMAT);
		return model;
	}

	public Model getNewValidRdf(String fileName, String contextConstant, String rootNodeLabel){
		setJsonld(fileName);
		setJsonldWithNewRootLabel(rootNodeLabel);
		return ShaclexValidator.parse(jsonld, FORMAT);
	}
	
	public Model getRdfWithInvalidTpe(){
		Resource resource = ResourceFactory.createResource(INVALID_SUBJECT_LABEL);
		Model model = ModelFactory.createDefaultModel();
		model.add(resource, FOAF.name, "Pablo");
		model.add(resource,RDF.type, "ex:Artist");
		model.add(resource,FOAF.depiction, "ex:Image");
		return model;
	}
	
	public Model getNewValidRdf(){
		return getNewValidRdf(VALID_JSONLD, CONTEXT_CONSTANT);
		
	}
	

	
	public void setJsonldWithNewRootLabel(String label){
		jsonld = jsonld.replace(REPLACING_SUBJECT_LABEL, label);
	}
	
	public static String generateRandomId(){
		return UUID.randomUUID().toString();
	}

	public String getSubjectType(){
		return environment.getProperty(Constants.SUBJECT_LABEL_TYPE);
	}
}
