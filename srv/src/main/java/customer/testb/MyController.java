package customer.testb;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.Struct;
import com.sap.cds.feature.xsuaa.XsuaaUserInfo;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Upsert;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpsert;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationOptions;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationOptionsAugmenter;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationRetrievalStrategy;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.connectivity.DefaultErpHttpDestination;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;
import com.sap.cloud.sdk.services.scp.workflow.cf.api.WorkflowDefinitionsApi;
import com.sap.cloud.sdk.services.scp.workflow.cf.model.WorkflowDefinition;
import com.sap.cloud.security.xsuaa.token.Token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.util.calendar.LocalGregorianCalendar.Date;
import z.sap.com.vdm.namespaces.northwindmetadata.Customer;
import z.sap.com.vdm.namespaces.northwindmetadata.CustomerFluentHelper;
import z.sap.com.vdm.services.DefaultNorthwindMetadataService;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.tomcat.jni.Time;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
// @PreAuthorize("isAuthenticated()")
// @RequestMapping("/test-api")
@RequestMapping(value = "/rest")
public class MyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyController.class);
     private final HttpDestination httpDestination_S4 = DestinationAccessor.getDestination("s4hanaonprem_ODataAPI").asHttp().decorate(DefaultErpHttpDestination::new);;
    private final HttpDestination httpDestination_NorthwiDestination = DefaultHttpDestination.builder("https://services.odata.org").build();
   
    
    
    @Autowired
    JwtDecoder jwtDecoder;
    @Autowired
    PersistenceService db;
    @Autowired
    XsuaaUserInfo xsuaaUserInfo;


    @PreAuthorize("permitAll()")
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {

        return "hello from rest";

    }

    @PreAuthorize("permitAll()")
    @GetMapping("/getBP")
    @ResponseBody
    public List<BusinessPartner> getBP() {

    List<BusinessPartner> rtn = new ArrayList();
    try {
    List<BusinessPartner> businessPartners = new
    DefaultBusinessPartnerService().getAllBusinessPartner().top(10).execute(httpDestination_S4);

    return businessPartners;

    //return businessPartners;
    } catch (ODataException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    }
    return rtn;

    }
    @PreAuthorize("permitAll()")
    @GetMapping("/getCustomers")
    @ResponseBody
    public List<Customer> getCustomers() {

        List<Customer> rtn = new ArrayList();
        try {

            DefaultNorthwindMetadataService tmp_DefaultNorthwindMetadataService = new DefaultNorthwindMetadataService();
            CustomerFluentHelper tmp_CustomerFluentHelper = tmp_DefaultNorthwindMetadataService.withServicePath("V2/Northwind/Northwind.svc").getAllCustomer();
           
            LOGGER.error("getCustomers: DEFAULT_SERVICE_PATH:"+tmp_DefaultNorthwindMetadataService.DEFAULT_SERVICE_PATH+" httpDestination_NorthwiDestination:"+httpDestination_NorthwiDestination.toString());
               List<Customer> tmp = tmp_CustomerFluentHelper.executeRequest(httpDestination_NorthwiDestination);
               return tmp;
               
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rtn;

    }
    @PreAuthorize("permitAll()")
    @GetMapping("/getWorkflowDefinitions")
    @ResponseBody
    public List<WorkflowDefinition> getWorkflowDefinitions(@AuthenticationPrincipal Token token) {
        HttpDestination httpDestination_workflow = DestinationAccessor.getDestination("dest_workflow_runtime2").asHttp();
        List<WorkflowDefinition> rtn = new ArrayList();
        try {
                List<WorkflowDefinition> workflowDefinitions = new WorkflowDefinitionsApi(httpDestination_workflow).queryDefinitions();
                return workflowDefinitions;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rtn;

    }



   }
