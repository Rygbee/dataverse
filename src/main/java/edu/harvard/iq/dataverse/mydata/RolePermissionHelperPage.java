package edu.harvard.iq.dataverse.mydata;

import edu.harvard.iq.dataverse.DatasetPage;
import edu.harvard.iq.dataverse.DataverseRoleServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.authorization.DataverseRole;
import edu.harvard.iq.dataverse.authorization.DataverseRolePermissionHelper;
import java.io.IOException;
import static java.lang.Math.max;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rmp553
 */
@ViewScoped
@Named("RolePermissionHelperPage")
public class RolePermissionHelperPage implements java.io.Serializable {
    
    private static final Logger logger = Logger.getLogger(DatasetPage.class.getCanonicalName());

    @Inject DataverseSession session;    

    @EJB
    DataverseRoleServiceBean roleService;
    
    private String testName = "blah";
    private DataverseRolePermissionHelper rolePermissionHelper;// = new DataverseRolePermissionHelper();
    private Pager pager;
     
    private void msg(String s){
        System.out.println(s);
    }
    
    private void msgt(String s){
        msg("-------------------------------");
        msg(s);
        msg("-------------------------------");
    }
    
    public String init() {
        msgt("----------- init() -------------");
        List<DataverseRole> roleList = roleService.findAll();
        msgt("roles: " + roleList.toString());
        rolePermissionHelper = new DataverseRolePermissionHelper(roleList);

        
        pager = new Pager(111, 10, 3);
        return null;
    }

    public Pager getPager(){
        return this.pager;
    }
    
    private int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
    
    
    public String getRandomPagerJSON() throws JSONException{
        
        int itemsPerPage = 10;
        int numResults = randInt(1,200);
        int numPages =  numResults / itemsPerPage;
        if ((numResults % itemsPerPage) > 0){
            numPages++;
        }
        int chosenPage = max(randInt(0, numPages), 1);
        return new Pager(numResults, itemsPerPage, chosenPage).asJSONString();
                
    }
    
    public DataverseRolePermissionHelper getRolePermissionHelper(){
        return this.rolePermissionHelper;
    }
    
    public String getRoleInfo() throws IOException{
        return "test";     
    }
    
    public String getTestName(){
        return this.testName;//"blah";
    }

    public void setTestName(String name){
        this.testName = name;
    }
    
    public String getSomeJSON() throws JSONException{
        
      JSONObject obj = new JSONObject();

      obj.put("name", "foo");
      obj.put("num", new Integer(100));
      obj.put("balance", new Double(1000.21));
      obj.put("is_vip", new Boolean(true));

      return obj.toString();
    }

    public String getSomeText(){
        //System.out.println(this.rolePermissionHelper.getRoleNameListString());;
        return "pigletz";
        //return this.rolePermissionHelper.getRoleNameListString();
    }
}