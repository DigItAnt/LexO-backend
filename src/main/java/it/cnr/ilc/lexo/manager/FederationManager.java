/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.manager;

import it.cnr.ilc.lexo.LexOProperties;
import it.cnr.ilc.lexo.service.data.lexicon.input.FormBySenseFilter;
import it.cnr.ilc.lexo.service.data.lexicon.input.FormFilter;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalEntryFilter;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalSenseFilter;
import it.cnr.ilc.lexo.service.data.lexicon.output.Counting;
import it.cnr.ilc.lexo.service.data.lexicon.output.EtymologicalLink;
import it.cnr.ilc.lexo.service.data.lexicon.output.Etymology;
import it.cnr.ilc.lexo.service.data.lexicon.output.EtymologyTree;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.HitsDataList;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntityLinksItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntityLinksItem.Link;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryElementItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalSenseCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.Morphology;
import it.cnr.ilc.lexo.service.data.lexicon.output.federation.FederatedObject;
import it.cnr.ilc.lexo.sparql.SparqlSelectData;
import it.cnr.ilc.lexo.sparql.SparqlVariable;
import it.cnr.ilc.lexo.util.EnumUtil;
import it.cnr.ilc.lexo.util.EnumUtil.AcceptedSearchFormExtendTo;
import it.cnr.ilc.lexo.util.EnumUtil.AcceptedSearchFormExtensionDegree;
import it.cnr.ilc.lexo.util.EnumUtil.FormTypes;
import it.cnr.ilc.lexo.util.EnumUtil.LexicalEntryStatus;
import it.cnr.ilc.lexo.util.EnumUtil.LexicalEntryTypes;
import it.cnr.ilc.lexo.util.EnumUtil.LexicalSenseSearchFilter;
import it.cnr.ilc.lexo.util.EnumUtil.SearchFormTypes;
import it.cnr.ilc.lexo.util.RDFQueryUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.federated.FedXFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andreabellandi
 */
public class FederationManager implements Manager, Cached {

    static final Logger logger = LoggerFactory.getLogger(FederationManager.class.getName());

    private final String namespace = LexOProperties.getProperty("repository.lexicon.namespace");

    public String getNamespace() {
        return namespace;
    }

    @Override
    public void reloadCache() {

    }

    public HitsDataList getFederatedResult() throws ManagerException {
        ArrayList<FederatedObject> fedList = new ArrayList();
        Repository repository = FedXFactory.newFederation()
                .withSparqlEndpoint("https://lila-erc.eu/sparql/lila_knowledge_base/sparql")
                .create();
        try ( RepositoryConnection conn = repository.getConnection()) {
            String query
                    = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "prefix ontolex: <http://www.w3.org/ns/lemon/ontolex#>\n"
                    + "prefix lila: <http://lila-erc.eu/ontologies/lila/>\n"
                    + "\n"
                    + "SELECT ?lemma ?pos\n"
                    + "WHERE {\n"
                    + "  ?lemma a lila:Lemma ;\n"
                    + "  ontolex:writtenRep \"donator\" ;\n"
                    + "  lila:hasPOS ?pos\n"
                    + "}";
            TupleQuery tq = conn.prepareTupleQuery(query);
            try ( TupleQueryResult tqRes = tq.evaluate()) {
                while (tqRes.hasNext()) {
                    FederatedObject fo = new FederatedObject();
                    BindingSet b = tqRes.next();
                    for (String var : b.getBindingNames()) {
                        fo.setKey(var);
                        fo.setValue(b.getValue(var).stringValue());
                    }
                    fedList.add(fo);
                }
            }
        } catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
            throw new ManagerException(e.getMessage());
        }
        repository.shutDown();
        return new HitsDataList(fedList.size(), fedList);
    }

}
