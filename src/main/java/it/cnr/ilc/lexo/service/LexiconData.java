/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnr.ilc.lexo.LexOProperties;
import it.cnr.ilc.lexo.manager.BibliographyManager;
import it.cnr.ilc.lexo.manager.LexiconDataManager;
import it.cnr.ilc.lexo.manager.ManagerException;
import it.cnr.ilc.lexo.manager.ManagerFactory;
import it.cnr.ilc.lexo.manager.SKOSManager;
import it.cnr.ilc.lexo.manager.UtilityManager;
import it.cnr.ilc.lexo.service.data.lexicon.input.FormFilter;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryElementItem;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalEntryFilter;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalSenseFilter;
import it.cnr.ilc.lexo.service.data.lexicon.output.BibliographicItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.Component;
import it.cnr.ilc.lexo.service.data.lexicon.output.ComponentItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.ConceptSetItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.EtymologicalLink;
import it.cnr.ilc.lexo.service.data.lexicon.output.Etymology;
import it.cnr.ilc.lexo.service.data.lexicon.output.EtymologyItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.EtymologyTree;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.GroupedLinkedEntity;
import it.cnr.ilc.lexo.service.data.lexicon.output.HitsDataList;
import it.cnr.ilc.lexo.service.data.lexicon.output.Language;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalConcept;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalConceptItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntityLinksItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalSenseCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalSenseItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LinkedEntity;
import it.cnr.ilc.lexo.service.helper.BibliographyHelper;
import it.cnr.ilc.lexo.service.helper.ComponentFilterHelper;
import it.cnr.ilc.lexo.service.helper.ComponentHelper;
import it.cnr.ilc.lexo.service.helper.ConceptSetHelper;
import it.cnr.ilc.lexo.service.helper.ConceptSetItemHelper;
import it.cnr.ilc.lexo.service.helper.EtymologicalLinkHelper;
import it.cnr.ilc.lexo.service.helper.EtymologyFilterHelper;
import it.cnr.ilc.lexo.service.helper.EtymologyHelper;
import it.cnr.ilc.lexo.service.helper.EtymologyTreeHelper;
import it.cnr.ilc.lexo.service.helper.FormCoreHelper;
import it.cnr.ilc.lexo.service.helper.FormItemsHelper;
import it.cnr.ilc.lexo.service.helper.HelperException;
import it.cnr.ilc.lexo.service.helper.LanguageHelper;
import it.cnr.ilc.lexo.service.helper.LexicalConceptHelper;
import it.cnr.ilc.lexo.service.helper.LexicalConceptItemHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntityLinksItemHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryCoreHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryFilterHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryElementHelper;
import it.cnr.ilc.lexo.service.helper.LexicalSenseCoreHelper;
import it.cnr.ilc.lexo.service.helper.LexicalSenseFilterHelper;
import it.cnr.ilc.lexo.service.helper.LinkedEntityHelper;
import it.cnr.ilc.lexo.sparql.SparqlVariable;
import it.cnr.ilc.lexo.util.EnumUtil;
import it.cnr.ilc.lexo.util.LogUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Level;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author andreabellandi
 */
@Path("lexicon/data")
@Api("Lexicon data")
public class LexiconData extends Service {

    private static final Logger logger = LoggerFactory.getLogger(LexiconData.class);
    Logger statLog = LoggerFactory.getLogger("statistics");

    private final LexiconDataManager lexiconManager = ManagerFactory.getManager(LexiconDataManager.class);
    private final LexicalEntryFilterHelper lexicalEntryFilterHelper = new LexicalEntryFilterHelper();
    private final LexicalSenseFilterHelper lexicalSenseFilterHelper = new LexicalSenseFilterHelper();
    private final FormItemsHelper formItemsHelper = new FormItemsHelper();
    private final LanguageHelper languageHelper = new LanguageHelper();
    private final LexicalEntryElementHelper lexicalEntryElementHelper = new LexicalEntryElementHelper();
    private final LexicalEntryCoreHelper lexicalEntryCoreHelper = new LexicalEntryCoreHelper();
    private final LexicalEntityLinksItemHelper lexicalEntityLinksItemHelper = new LexicalEntityLinksItemHelper();
    private final FormCoreHelper formCoreHelper = new FormCoreHelper();
    private final LexicalSenseCoreHelper lexicalSenseCoreHelper = new LexicalSenseCoreHelper();
    private final LinkedEntityHelper linkedEntityHelper = new LinkedEntityHelper();
    private final BibliographyHelper bibliographyHelper = new BibliographyHelper();
    private final BibliographyManager bibliographyManager = ManagerFactory.getManager(BibliographyManager.class);
    private final EtymologyHelper etymologyHelper = new EtymologyHelper();
    private final LexicalConceptItemHelper lexicalConceptItemHelper = new LexicalConceptItemHelper();
    private final LexicalConceptHelper lexicalConceptHelper = new LexicalConceptHelper();
    private final ConceptSetItemHelper conceptSetItemHelper = new ConceptSetItemHelper();
    private final EtymologyFilterHelper etymologyFilterHelper = new EtymologyFilterHelper();
    private final EtymologyTreeHelper etymologyTreeHelper = new EtymologyTreeHelper();
    private final EtymologicalLinkHelper etymologicalLinkHelper = new EtymologicalLinkHelper();
    private final ComponentFilterHelper componentFilterHelper = new ComponentFilterHelper();
    private final ComponentHelper componentHelper = new ComponentHelper();
    private final ConceptSetHelper conceptSetHelper = new ConceptSetHelper();
    private final SKOSManager skosManager = ManagerFactory.getManager(SKOSManager.class);

    @GET
    @Path("lexicalEntry")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "lexicalEntry",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry",
            notes = "This method returns the data related to a specific aspect (morphology, syntax, ...) associated with a given lexical entry")
    public Response lexicalEntry(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "aspect",
                    allowableValues = "core, decomposition, variation and translation, syntax and semantics",
                    example = "core",
                    required = true)
            @QueryParam("aspect") String aspect,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/lexicalEntry <" + _id + "> with aspect: " + aspect);
            TupleQueryResult lexicalEntry = lexiconManager.getLexicalEntry(_id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                List<LexicalEntryCore> _lec = lexicalEntryCoreHelper.newDataList(lexicalEntry);
                LexicalEntryCore lec = lexiconManager.getLexicalEntityTypes(_lec);
                TupleQueryResult lexicalEntityLinks = lexiconManager.getLexicalEntityLinks(_id);
                LexicalEntityLinksItem links = lexicalEntityLinksItemHelper.newData(lexicalEntityLinks);
                lexiconManager.addLexicalEntityLink(lec, links);
                String json = lexicalEntryCoreHelper.toJson(lec);
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            log(Level.ERROR, "lexical aspect " + aspect + " not available");
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }  
    }

    @GET
    @Path("form")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "form",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Form",
            notes = "This method returns the core data related to a given form")
    public Response form(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "aspect",
                    allowableValues = "core, variation and translation",
                    example = "core",
                    required = true)
            @QueryParam("aspect") String aspect,
            @ApiParam(
                    name = "id",
                    value = "form ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/form <" + _id + "> with aspect: " + aspect);
            long start = System.currentTimeMillis();
            TupleQueryResult form = lexiconManager.getForm(_id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                List<FormCore> fc = formCoreHelper.newDataList(form);
                FormCore _fc = lexiconManager.getMorphologyInheritance(fc);
                TupleQueryResult lexicalEntityLinks = lexiconManager.getLexicalEntityLinks(_id);
                LexicalEntityLinksItem links = lexicalEntityLinksItemHelper.newData(lexicalEntityLinks);
                lexiconManager.addLexicalEntityLink(_fc, links);
                String json = formCoreHelper.toJson(_fc);
                statLog.info("response in: {}ms", System.currentTimeMillis() - start);
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("lexicalSense")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "lexicalSense",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical Sense",
            notes = "This method returns the core data related to given sense")
    public Response lexicalSense(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "aspect",
                    allowableValues = "core, variation and translation, syntax and semantics",
                    example = "core",
                    required = true)
            @QueryParam("aspect") String aspect,
            @ApiParam(
                    name = "id",
                    value = "sense ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/lexicalSense <" + _id + "> with aspect: " + aspect);
            TupleQueryResult sense = lexiconManager.getLexicalSense(_id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                LexicalSenseCore lsc = lexicalSenseCoreHelper.newData(sense);
                TupleQueryResult lexicalEntityLinks = lexiconManager.getLexicalEntityLinks(_id);
                LexicalEntityLinksItem links = lexicalEntityLinksItemHelper.newData(lexicalEntityLinks);
                lexiconManager.addLexicalEntityLink(lsc, links);
                String json = lexicalSenseCoreHelper.toJson(lsc);
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("etymology")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "etymology",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Etymology",
            notes = "This method returns the etymological data related to a given etymology")
    public Response etymology(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "etymology ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/etymology <" + _id + ">");
            String json = "";
            TupleQueryResult etymology = lexiconManager.getEtymology(_id);
            if (etymology.hasNext()) {
                TupleQueryResult etymologicalLinks = lexiconManager.getEtymologicalLinks(_id);
                Etymology e = etymologyHelper.newData(etymology);
                List<EtymologicalLink> etyLinks = etymologicalLinks.hasNext() ? etymologicalLinkHelper.newDataList(etymologicalLinks) : new ArrayList();
                TupleQueryResult lexicalEntityLinks = lexiconManager.getLexicalEntityLinks(_id);
                LexicalEntityLinksItem links = lexicalEntityLinks.hasNext() ? lexicalEntityLinksItemHelper.newData(lexicalEntityLinks) : null;
                lexiconManager.addLexicalEntityLink(e, links);
                EtymologyTree et = lexiconManager.getEtymologyTree(e, etyLinks);
                json = etymologyTreeHelper.toJson(et);
            }
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("component")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "component",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Component",
            notes = "This method returns the data of a miltiword Component")
    public Response component(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "component ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/component <" + _id + ">");
            TupleQueryResult _comp = lexiconManager.getComponent(_id);
            Component comp = componentHelper.newData(_comp);
            String json = componentHelper.toJson(comp);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("lexicalConcept")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "lexicalConcept",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical Concept",
            notes = "This method returns the data of a lexical concept")
    public Response lexicalConcept(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical concept ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/lexicalConcept <" + _id + ">");
            TupleQueryResult _lc = skosManager.getLexicalConcept(_id);
            LexicalConcept lc = lexicalConceptHelper.newData(_lc);
            lexiconManager.setDefaultLanguage(lc);
            List<LinkedEntity> le = new ArrayList();
            for (String rel : Arrays.asList("isEvokedBy", "lexicalizedSense")) {
                TupleQueryResult tqr = lexiconManager.getLexicalConceptRelation(_id, rel);
                if (tqr.hasNext()) {
                    le.addAll(linkedEntityHelper.newDataList(tqr));
                }
            }
            lc.getEntities().add(new GroupedLinkedEntity("ontolex", le));
            String json = lexicalConceptHelper.toJson(lc);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("languages")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "languages",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexicon languages list",
            notes = "This method returns a list of lexicon languages according to the input filter")
    public Response languages(@HeaderParam("Authorization") String key) throws HelperException {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            log(Level.INFO, "lexicon/data/languages");
            TupleQueryResult languages = lexiconManager.getLexiconLanguages();
            List<Language> entries = languageHelper.newDataList(languages);
            String json = languageHelper.toJson(entries);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("filteredSenses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "filteredSense",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical senses list",
            notes = "This method returns a list of lexical senses according to the input filter")
    public Response sensesList(@HeaderParam("Authorization") String key, LexicalSenseFilter lsf) throws HelperException {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            log(Level.INFO, "lexicon/data/filteredSenses\n" + LogUtil.getLogFormPayload(lsf));
            TupleQueryResult lexicalSenses = lexiconManager.getFilterdLexicalSenses(lsf);
            List<LexicalSenseItem> senses = lexicalSenseFilterHelper.newDataList(lexicalSenses);
            HitsDataList hdl = new HitsDataList(lexicalSenseFilterHelper.getTotalHits(), senses);
            String json = lexicalSenseFilterHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("lexicalEntries")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "lexicalEntries",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entries list",
            notes = "This method returns a list of lexical entries according to the input filter")
    public Response entriesList(@HeaderParam("Authorization") String key, LexicalEntryFilter lef) throws HelperException {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            log(Level.INFO, "lexicon/data/lexicalEntries\n" + LogUtil.getLogFormPayload(lef));
            TupleQueryResult lexicalEnties = lexiconManager.getFilterdLexicalEntries(lef);
            List<LexicalEntryItem> entries = lexicalEntryFilterHelper.newDataList(lexicalEnties);
            HitsDataList hdl = new HitsDataList(lexicalEntryFilterHelper.getTotalHits(), entries);
            String json = lexicalEntryFilterHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("filteredForms")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "filteredForms",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Forms list",
            notes = "This method returns a list of forms according to the input filter")
    public Response forms(@HeaderParam("Authorization") String key, FormFilter ff) throws HelperException {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            log(Level.INFO, "lexicon/data/filteredForms\n" + LogUtil.getLogFormPayload(ff));
            TupleQueryResult forms = lexiconManager.getFilterdForms(ff);
            List<FormItem> fi = formItemsHelper.newDataList(forms);
            HitsDataList hdl = new HitsDataList(formItemsHelper.getTotalHits(), fi);
            String json = formItemsHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("forms")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "forms",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry forms",
            notes = "This method returns all the forms of a lexical entry")
    public Response forms(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @QueryParam("id") String id) {

        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/forms <" + _id + ">");
            TupleQueryResult _forms = lexiconManager.getForms(_id);
            List<FormItem> forms = formItemsHelper.newDataList(_forms);
            String json = formItemsHelper.toJson(forms);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("lexicalConcepts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "lexicalConcepts",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical concept children",
            notes = "This method returns all the children of a lexical concept")
    public Response lexicalConcepts(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "concept set ID, lexical concept ID or \"root\" for root concepts",
                    required = false)
            @QueryParam("id") String id) {

        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/lexicalConcepts <" + _id + ">");
            TupleQueryResult _lc = skosManager.getLexicalConceptChildren(_id);
            List<LexicalConceptItem> lcs = lexicalConceptItemHelper.newDataList(_lc);
            HitsDataList hdl = new HitsDataList(lcs.size(), lcs);
            String json = lexicalConceptItemHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("conceptSets")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "conceptSets",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Concept Sets",
            notes = "This method returns all the concept sets")
    public Response conceptSets(
            @HeaderParam("Authorization") String key) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            log(Level.INFO, "lexicon/data/conceptSets");
            TupleQueryResult _cs = skosManager.getConceptSets();
            List<ConceptSetItem> cs = conceptSetItemHelper.newDataList(_cs);
            HitsDataList hdl = new HitsDataList(cs.size(), cs);
            String json = conceptSetItemHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("senses")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "senses",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical senses",
            notes = "This method returns all the senses of a lexical entry")
    public Response senses(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/senses <" + _id + ">");
            TupleQueryResult _forms = lexiconManager.getLexicalSenses(_id);
            List<LexicalSenseItem> senses = lexicalSenseFilterHelper.newDataList(_forms);
            String json = lexicalSenseFilterHelper.toJson(senses);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("etymologies")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "etymologies",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Etymologies",
            notes = "This method returns all the etymologies of a lexical entry")
    public Response etymologies(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/etymologies <" + _id + ">");
            TupleQueryResult _etys = lexiconManager.getEtymologies(_id);
            List<EtymologyItem> etys = etymologyFilterHelper.newDataList(_etys);
            String json = etymologyFilterHelper.toJson(etys);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("sensesByConcept")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "sensesByConcept",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical senses by concept",
            notes = "This method returns all the senses referred by a concept")
    public Response sensesByConcept(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "concept ID",
                    example = "97-Cause_Change",
                    required = true)
            @QueryParam("id") String id) {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/sensesByConcept <" + _id + ">");
            TupleQueryResult _forms = lexiconManager.getLexicalSensesByConcept(_id);
            List<LexicalSenseItem> senses = lexicalSenseFilterHelper.newDataList(_forms);
            String json = lexicalSenseFilterHelper.toJson(senses);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("elements")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "elements",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry elements",
            notes = "This method returns the counting of all the elements of a lexical entry (forms, senses, frames, etc ...)")
    public Response elements(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/elements <" + _id + ">");
            TupleQueryResult _elements = lexiconManager.getElements(_id);
            LexicalEntryElementItem elements = lexicalEntryElementHelper.newData(_elements);
            String json = lexicalEntryElementHelper.toJson(elements);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("linguisticRelation")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "linguisticRelation",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entity linguistic relation",
            notes = "This method returns the input linguistic relation with other lexical entities, as well as the inferred ones")
    public Response linguisticRelation(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "property",
                    example = "denotes",
                    required = true)
            @QueryParam("property") String property,
            @ApiParam(
                    name = "id",
                    value = "lexical entity ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/linguisticRelation <" + _id + ">");
            TupleQueryResult lingRel = lexiconManager.getLinguisticRelation(_id, property);
            if (!lingRel.hasNext()) {
                return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("There are no instances of " + property).build();
            }
            List<LinkedEntity> le = linkedEntityHelper.newDataList(lingRel);
            String json = linkedEntityHelper.toJson(le);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("genericRelation")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "genericRelation",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entity generic relation",
            notes = "This method returns the input generic relation with other lexical entities, as well as the inferred ones")
    public Response genericRelation(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "property",
                    example = "seeAlso",
                    required = true)
            @QueryParam("property") String property,
            @ApiParam(
                    name = "id",
                    value = "lexical entity ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/genericRelation <" + _id + ">");
            TupleQueryResult genRel = lexiconManager.getGenericRelation(_id, property);
            if (!genRel.hasNext()) {
                return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("There are no instances of " + property).build();
            }
            List<LinkedEntity> le = linkedEntityHelper.newDataList(genRel);
            String json = linkedEntityHelper.toJson(le);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException | UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("bibliography")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "bibliography",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entity bibliography",
            notes = "This method returns the bibliography of a given lexical entity")
    public Response bibliography(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entity ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/bilbiography <" + _id + ">");
            TupleQueryResult bib = bibliographyManager.getBibliography(_id);
            List<BibliographicItem> bibs = bibliographyHelper.newDataList(bib);
            String json = bibliographyHelper.toJson(bibs);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("subTerms")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "subTerms",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Sub term of a lexical entry",
            notes = "This method returns all the sub terms of a lexical entry")
    public Response subTerms(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
            if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/subTerms <" + _id + ">");
            String json = "";
            TupleQueryResult lexicalEnties = lexiconManager.getSubTerms(_id);
            if (lexicalEnties.hasNext()) {
                List<LexicalEntryItem> entries = lexicalEntryFilterHelper.newDataList(lexicalEnties);
                json = lexicalEntryFilterHelper.toJson(entries);
            }
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
             log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("correspondsTo")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "correspondsTo",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry corresponding to a component",
            notes = "This method returns the lexical entry corresponding to a multiword component")
    public Response correspondsTo(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "component ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/correspondsTo <" + _id + ">");
            String json = "";
            TupleQueryResult lexicalEntry = lexiconManager.getCorrespondsTo(_id);
            if (lexicalEntry.hasNext()) {
                LexicalEntryItem entry = lexicalEntryFilterHelper.newData(lexicalEntry);
                json = lexicalEntryFilterHelper.toJson(entry);
            }
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("constituents")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "constituents",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Components of a lexical entry or a component",
            notes = "This method returns all the components of a lexical entry or a component")
    public Response constituents(
            @HeaderParam("Authorization") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry or component ID",
                    required = true)
            @QueryParam("id") String id) {
        try {
             if (!LexOProperties.getProperty("keycloack.freeViewer").equals("true")) {
                checkKey(key.substring(7));
            }
            String _id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            log(Level.INFO, "lexicon/data/constituents <" + _id + ">");
            TupleQueryResult _comps = null;
            String json = "";
            UtilityManager utilityManager = ManagerFactory.getManager(UtilityManager.class);
            if (utilityManager.isLexicalEntry(_id)) {
                _comps = lexiconManager.getComponents(_id, SparqlVariable.LEXICAL_ENTRY_INDEX, "lexicalEntryLabel");
            } else if (utilityManager.isLexicalEntry(_id)) {
                _comps = lexiconManager.getComponents(_id, SparqlVariable.COMPONENT_INDEX, "componentLabel");
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("IRI " + id + " is not neither a lexical entry or a component").build();
            }
            if (_comps.hasNext()) {
                List<ComponentItem> comps = componentFilterHelper.newDataList(_comps);
                json = componentFilterHelper.toJson(comps);
            }
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (UnsupportedEncodingException | AuthorizationException ex) {
            log(Level.ERROR, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

}
