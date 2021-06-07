/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnr.ilc.lexo.manager.LexiconDataManager;
import it.cnr.ilc.lexo.manager.ManagerException;
import it.cnr.ilc.lexo.manager.ManagerFactory;
import it.cnr.ilc.lexo.service.data.lexicon.input.FormFilter;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryElementItem;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalEntryFilter;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalSenseFilter;
import it.cnr.ilc.lexo.service.data.lexicon.output.Counting;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.FormList;
import it.cnr.ilc.lexo.service.data.lexicon.output.HitsDataList;
import it.cnr.ilc.lexo.service.data.lexicon.output.Language;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalEntryItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalSenseCore;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalSenseItem;
import it.cnr.ilc.lexo.service.data.lexicon.output.LinkedEntity;
import it.cnr.ilc.lexo.service.helper.FormCoreHelper;
import it.cnr.ilc.lexo.service.helper.FormItemsHelper;
import it.cnr.ilc.lexo.service.helper.FormListHelper;
import it.cnr.ilc.lexo.service.helper.HelperException;
import it.cnr.ilc.lexo.service.helper.LanguageHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryCoreHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryFilterHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryElementHelper;
import it.cnr.ilc.lexo.service.helper.LexicalEntryReferenceLinkHelper;
import it.cnr.ilc.lexo.service.helper.LexicalSenseCoreHelper;
import it.cnr.ilc.lexo.service.helper.LexicalSenseFilterHelper;
import it.cnr.ilc.lexo.service.helper.LinkedEntityHelper;
import it.cnr.ilc.lexo.service.helper.PathLenghtHelper;
import it.cnr.ilc.lexo.util.EnumUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author andreabellandi
 */
@Path("lexicon/data")
@Api("Lexicon data")
public class LexiconData extends Service {

    private final LexiconDataManager lexiconManager = ManagerFactory.getManager(LexiconDataManager.class);
    private final LexicalEntryFilterHelper lexicalEntryFilterHelper = new LexicalEntryFilterHelper();
    private final LexicalSenseFilterHelper lexicalSenseFilterHelper = new LexicalSenseFilterHelper();
    private final FormItemsHelper formItemsHelper = new FormItemsHelper();
    private final FormListHelper formListHelper = new FormListHelper();
    private final LanguageHelper languageHelper = new LanguageHelper();
    private final LexicalEntryElementHelper lexicalEntryElementHelper = new LexicalEntryElementHelper();
    private final LexicalEntryCoreHelper lexicalEntryCoreHelper = new LexicalEntryCoreHelper();
    private final LexicalEntryReferenceLinkHelper lexicalEntryReferenceLinkHelper = new LexicalEntryReferenceLinkHelper();
    private final PathLenghtHelper pathLenghtHelper = new PathLenghtHelper();
    private final FormCoreHelper formCoreHelper = new FormCoreHelper();
    private final LexicalSenseCoreHelper lexicalSenseCoreHelper = new LexicalSenseCoreHelper();
    private final LinkedEntityHelper linkedEntityHelper = new LinkedEntityHelper();

    @GET
    @Path("{id}/lexicalEntry")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/lexicalEntry",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry",
            notes = "This method returns the data related to a specific aspect (morphology, syntax, ...) associated with a given lexical entry")
    public Response lexicalEntry(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
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
            @PathParam("id") String id) {
        try {
            TupleQueryResult lexicalEntry = lexiconManager.getLexicalEntry(id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                LexicalEntryCore lec = lexicalEntryCoreHelper.newData(lexicalEntry);
                TupleQueryResult lexicalEntryReferenceLinks = lexiconManager.getLexicalEntryReferenceLinks(id);
                LexicalEntryElementItem referenceLinks = lexicalEntryReferenceLinkHelper.newData(lexicalEntryReferenceLinks);
                lexiconManager.addLexicalEntryLinks(lec, referenceLinks,
                        new LexicalEntryElementItem("Multimedia", new ArrayList()),
                        new LexicalEntryElementItem("Attestation", new ArrayList()),
                        new LexicalEntryElementItem("Other", new ArrayList()));
                String json = lexicalEntryCoreHelper.toJson(lec);
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}/lexicalEntryLinguisticRelation")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/lexicalEntryLinguisticRelation",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry links",
            notes = "This method returns the relations with other lexical entities according to the input type")
    public Response lexicalEntryLinguisticRelation(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "property",
                    allowableValues = "denotes, evokes",
                    example = "denotes",
                    required = true)
            @QueryParam("property") String property,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @PathParam("id") String id) {
        try {
            TupleQueryResult propertyLinks = lexiconManager.getLexicalEntryPropertyLinks(id, property);
            List<LinkedEntity> le = linkedEntityHelper.newDataList(propertyLinks);
            String json = linkedEntityHelper.toJson(le);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}/form")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/form",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Form",
            notes = "This method returns the core data related to given form")
    public Response form(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "aspect",
                    allowableValues = "core, variation and translation",
                    example = "core",
                    required = true)
            @QueryParam("aspect") String aspect,
            @ApiParam(
                    name = "id",
                    value = "form ID",
                    example = "MUSabbacchiareVERB_PHUabbacchiammo_P1IR",
                    required = true)
            @PathParam("id") String id) {
        try {
            TupleQueryResult form = lexiconManager.getForm(id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                List<FormCore> fc = formCoreHelper.newDataList(form);
//                TupleQueryResult lexicalEntryReferenceLinks = lexiconManager.getLexicalEntryReferenceLinks(id);
//                LexicalEntryElementItem referenceLinks = lexicalEntryReferenceLinkHelper.newData(lexicalEntryReferenceLinks);
//                lexiconManager.addLexicalEntryLinks(lec, referenceLinks,
//                        new LexicalEntryElementItem("Multimedia", new ArrayList()),
//                        new LexicalEntryElementItem("Attestation", new ArrayList()),
//                        new LexicalEntryElementItem("Other", new ArrayList()));
                String json = formCoreHelper.toJson(lexiconManager.getMorphologyInheritance(fc));
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}/lexicalSense")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/lexicalSense",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical Sense",
            notes = "This method returns the core data related to given sense")
    public Response lexicalSense(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "aspect",
                    allowableValues = "core, variation and translation, syntax and semantics",
                    example = "core",
                    required = true)
            @QueryParam("aspect") String aspect,
            @ApiParam(
                    name = "id",
                    value = "sense ID",
                    example = "USem73621abolizione",
                    required = true)
            @PathParam("id") String id) {
        try {
            TupleQueryResult sense = lexiconManager.getLexicalSense(id, aspect);
            if (aspect.equals(EnumUtil.LexicalAspects.Core.toString())) {
                LexicalSenseCore lsc = lexicalSenseCoreHelper.newData(sense);
//                TupleQueryResult lexicalEntryReferenceLinks = lexiconManager.getLexicalEntryReferenceLinks(id);
//                LexicalEntryElementItem referenceLinks = lexicalEntryReferenceLinkHelper.newData(lexicalEntryReferenceLinks);
//                lexiconManager.addLexicalEntryLinks(lec, referenceLinks,
//                        new LexicalEntryElementItem("Multimedia", new ArrayList()),
//                        new LexicalEntryElementItem("Attestation", new ArrayList()),
//                        new LexicalEntryElementItem("Other", new ArrayList()));
                String json = lexicalSenseCoreHelper.toJson(lsc);
                return Response.ok(json)
                        .type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Headers", "content-type")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("lexical aspect not available").build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("languages")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/languages",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexicon languages list",
            notes = "This method returns a list of lexicon languages according to the input filter")
    public Response languages(@QueryParam("key") String key) throws HelperException {
        try {
            TupleQueryResult languages = lexiconManager.getLexiconLanguages();
            List<Language> entries = languageHelper.newDataList(languages);
            String json = languageHelper.toJson(entries);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("lexicalSenses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/lexicalSenses",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical senses list",
            notes = "This method returns a list of lexical senses according to the input filter")
    public Response sensesList(@QueryParam("key") String key, LexicalSenseFilter lsf) throws HelperException {
        try {
            TupleQueryResult lexicalSenses = lexiconManager.getFilterdLexicalSenses(lsf);
            List<LexicalSenseItem> entries = lexicalSenseFilterHelper.newDataList(lexicalSenses);
            String json = lexicalSenseFilterHelper.toJson(entries);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("lexicalEntries")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/lexicalEntries",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entries list",
            notes = "This method returns a list of lexical entries according to the input filter")
    public Response entriesList(@QueryParam("key") String key, LexicalEntryFilter lef) throws HelperException {
        try {
            TupleQueryResult lexicalEnties = lexiconManager.getFilterdLexicalEntries(lef);
            List<LexicalEntryItem> entries = lexicalEntryFilterHelper.newDataList(lexicalEnties);
            HitsDataList hdl = new HitsDataList(lexicalEntryFilterHelper.getTotalHits(), entries);
            String json = lexicalEntryFilterHelper.toJson(hdl);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("forms")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/forms",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Forms list",
            notes = "This method returns a list of forms according to the input filter")
    public Response formsList(@QueryParam("key") String key, FormFilter ff) throws HelperException {
        try {
            List<FormList> list = new ArrayList();
            TupleQueryResult res = lexiconManager.getFilterdForms(ff);
            list.add(new FormList("", 0, "", "", formItemsHelper.newDataList(res)));
            if (!ff.getExtendTo().equals(EnumUtil.AcceptedSearchFormExtendTo.None.toString())) {
                for (String sense : ff.getSenseUris()) {
                    if (ff.getExtendTo().equals(EnumUtil.AcceptedSearchFormExtendTo.Hypernym.toString())
                            || ff.getExtendTo().equals(EnumUtil.AcceptedSearchFormExtendTo.Hyponym.toString())) {
                        int lenght = 1;
                        List<FormItem> forms = new ArrayList();
                        for (Counting c : pathLenghtHelper.newDataList(lexiconManager.getRelationByLenght(ff.getExtendTo(), sense))) {
                            if (ff.getExtensionDegree() >= c.getCount()) {
                                if (lenght == c.getCount()) {
                                    TupleQueryResult _res = lexiconManager.getFilterdForms(c.getLabel());
                                    forms.addAll(formItemsHelper.newDataList(_res));
                                } else {
                                    list.add(new FormList(ff.getExtendTo(), lenght, lexiconManager.getNamespace() + sense, sense,
                                            lexiconManager.getFormItemListCopy(forms)));
                                    forms.clear();
                                    lenght++;
                                    TupleQueryResult _res = lexiconManager.getFilterdForms(c.getLabel());
                                    forms.addAll(formItemsHelper.newDataList(_res));
                                }
                            }
                        }
                        list.add(new FormList(ff.getExtendTo(), lenght, lexiconManager.getNamespace() + sense, sense,
                                lexiconManager.getFormItemListCopy(forms)));
                    } else if (ff.getExtendTo().equals(EnumUtil.AcceptedSearchFormExtendTo.Synonym.toString())) {
                        TupleQueryResult _resForms = lexiconManager.getFormsBySenseRelation(ff, sense);
                        if (_resForms.hasNext()) {
                            list.add(new FormList("synonym", 1, lexiconManager.getNamespace() + sense, sense, formItemsHelper.newDataList(_resForms)));
                        }
                    }
                }
            }
            String json = formListHelper.toJson(list);
            return Response.ok(json)
                    .type(MediaType.TEXT_PLAIN)
                    .header("Access-Control-Allow-Headers", "content-type")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                    .build();
        } catch (ManagerException ex) {
            Logger.getLogger(LexiconData.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}/forms")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/forms",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry forms",
            notes = "This method returns all the forms of a lexical entry")
    public Response forms(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @PathParam("id") String id) {
//        log(Level.INFO, "get lexicon entries types");
        TupleQueryResult _forms = lexiconManager.getForms(id);
        List<FormItem> forms = formItemsHelper.newDataList(_forms);
        String json = formItemsHelper.toJson(forms);
        return Response.ok(json)
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Headers", "content-type")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                .build();
    }

    @GET
    @Path("{id}/senses")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/senses",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical senses",
            notes = "This method returns all the senses of a lexical entry")
    public Response senses(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @PathParam("id") String id) {
//        log(Level.INFO, "get lexicon entries types");
        TupleQueryResult _forms = lexiconManager.getLexicalSenses(id);
        List<LexicalSenseItem> senses = lexicalSenseFilterHelper.newDataList(_forms);
        String json = lexicalSenseFilterHelper.toJson(senses);
        return Response.ok(json)
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Headers", "content-type")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                .build();
    }

    @GET
    @Path("{id}/elements")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}/elements",
            produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "Lexical entry elements",
            notes = "This method returns the counting of all the elements of a lexical entry (forms, senses, frames, etc ...)")
    public Response elements(
            @ApiParam(
                    name = "key",
                    value = "authentication token",
                    example = "lexodemo",
                    required = true)
            @QueryParam("key") String key,
            @ApiParam(
                    name = "id",
                    value = "lexical entry ID",
                    example = "MUSaccedereVERB",
                    required = true)
            @PathParam("id") String id) {
//        log(Level.INFO, "get lexicon entries types");
        TupleQueryResult _elements = lexiconManager.getElements(id);
        LexicalEntryElementItem elements = lexicalEntryElementHelper.newData(_elements);
        String json = lexicalEntryElementHelper.toJson(elements);
        return Response.ok(json)
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Headers", "content-type")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                .build();
    }

}
