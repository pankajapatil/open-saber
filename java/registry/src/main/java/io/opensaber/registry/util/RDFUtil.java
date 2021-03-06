package io.opensaber.registry.util;

import io.opensaber.converters.JenaRDF4J;
import org.apache.jena.ext.com.google.common.io.ByteStreams;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RDFUtil {

    private static Logger logger = LoggerFactory.getLogger(RDFUtil.class);

    @Value("${registry.context.base}")
    private static String registryContext;

    /**
     * This method returns a List of RDFNodes which are blank nodes from a Jena Model
     * @param model
     * @return
     */
    public static List<RDFNode> getBlankNodes(Model model) {
        StmtIterator it = model.listStatements();
        List<RDFNode> blankNodes = new ArrayList<>();
        while (it.hasNext()) {
            Statement stmt = it.next();
            if (stmt.getSubject().isAnon() && !stmt.getObject().isLiteral() && stmt.getSubject().getURI() == null) {
                blankNodes.add(stmt.getObject());
            }
        }
        return blankNodes;
    }

    /**
     * This method updates the Blank node's URI with a random generated UUID. Also, the
     * Subject of the children nodes of the blank node will be updated with the same URI.
     * @param model
     */
    public static void updateIdForBlankNode(Model model) {
        StmtIterator stmtIterator = model.listStatements();

        ArrayList<Statement> updatedStatements = new ArrayList<>();
        while (stmtIterator.hasNext()) {
            Statement node = stmtIterator.next();

            if (node.getSubject().isAnon() && !node.getObject().isLiteral() && node.getSubject().getURI() == null) {

                StmtIterator parent = model.listStatements(null, null, node.getSubject());

                String label = UUID.randomUUID().toString();
                StmtIterator nodeProperties = node.getSubject().listProperties();
                // String namespace = node.getObject().asResource().getNameSpace();
                registryContext = "http://example.com/voc/teacher/1.0.0/";

                /*
                 * Update the child node labels
                 */
                while (nodeProperties.hasNext()) {
                    Statement childNode = nodeProperties.next();
                    Statement updated = updateSubjectLabel(childNode, label);
                    nodeProperties.remove();
                    updatedStatements.add(updated);
                }

                // Update the parent node label
                // updateSubjectLabel(node, label, namespace);

                while(parent.hasNext()) {
                    Statement parentSt = parent.next();
                    Statement updatedParent = updateResource(parentSt, label);
                    parent.remove();
                    updatedStatements.add(updatedParent);
                }
            }
        }
        model.add(updatedStatements.toArray(new Statement[0]));
    }

    public static Statement updateSubjectLabel(Statement statement, String label) {
        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode object = statement.getObject();

        Resource subjectCopy = ResourceFactory.createResource(label);
        BeanWrapper bw = new BeanWrapperImpl(subject);
        BeanUtils.copyProperties(bw.getWrappedInstance(), subjectCopy);

        return ResourceFactory.createStatement(subjectCopy, predicate, object);
    }

    public static Statement updateResource(Statement statement, String label) {

        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode object = statement.getObject();

        Resource res = object.asResource();
        Resource objectCopy = ResourceFactory.createResource(label);
        BeanWrapper bw = new BeanWrapperImpl(object);
        BeanUtils.copyProperties(bw.getWrappedInstance(), objectCopy);

        return ResourceFactory.createStatement(subject, predicate, objectCopy);

    }

    public static String frameEntity(org.eclipse.rdf4j.model.Model entityModel) throws IOException {
        Model jenaEntityModel = JenaRDF4J.asJenaModel(entityModel);
        DatasetGraph g = DatasetFactory.create(jenaEntityModel).asDatasetGraph();
        JsonLDWriteContext ctx = new JsonLDWriteContext();
        InputStream is = RDFUtil.class.getClassLoader().getResourceAsStream("frame.json");
        String fileString = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
        ctx.setFrame(fileString);
        WriterDatasetRIOT w = RDFDataMgr.createDatasetWriter(org.apache.jena.riot.RDFFormat.JSONLD_FRAME_FLAT) ;
        PrefixMap pm = RiotLib.prefixMap(g);
        String base = null;
        StringWriter sWriterJena = new StringWriter();
        w.write(sWriterJena, g, pm, base, ctx) ;
        String jenaJSON = sWriterJena.toString();
        return jenaJSON;
    }

    public static void updateRdfModelNodeId(Model rdfModel, RDFNode object, String label) {
        StmtIterator stmtIterator = rdfModel.listStatements();
        ArrayList<Statement> updatedStatements = new ArrayList<>();

        while(stmtIterator.hasNext()) {
            Statement node = stmtIterator.next();
            if(!node.getObject().isLiteral() && node.getObject().asResource().equals(object)) {
                StmtIterator parentItr = rdfModel.listStatements(null, null, node.getSubject());
                StmtIterator nodeProperties = node.getSubject().listProperties();

                while (nodeProperties.hasNext()) {
                    Statement childNode = nodeProperties.next();
                    Statement updated = RDFUtil.updateSubjectLabel(childNode, label);
                    nodeProperties.remove();
                    updatedStatements.add(updated);
                }

                while (parentItr.hasNext()) {
                    Statement parent = parentItr.next();
                    Statement updatedParent = RDFUtil.updateResource(parent, label);
                    parentItr.remove();
                    updatedStatements.add(updatedParent);
                }
            }
        }
        rdfModel.add(updatedStatements.toArray(new Statement[0]));
    }

}
