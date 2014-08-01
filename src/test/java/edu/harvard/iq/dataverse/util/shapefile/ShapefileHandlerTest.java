/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dataverse.util;


//import edu.harvard.iq.dataverse.util.ZipMaker;
import edu.harvard.iq.dataverse.util.ShapefileHandler;

import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertEquals;

import java.nio.file.Files;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


// Dataverse/4.0/QA/4.0_files/ingest_4.0/dta/date_new_stata.dta
/**
 *
 * @author rmp553
 */
public class ShapefileHandlerTest {
    
        
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
   
    
    public void msg(String s){
            System.out.println(s);
    }
    
    public void msgt(String s){
        msg("------------------------------------------------------------");
        msg(s);
        msg("------------------------------------------------------------");
    }

    //@Test
    public void testWrite() throws IOException {
    
    // Create a temporary file.
    // This is guaranteed to be deleted after the test finishes.
    final File tempFile = tempFolder.newFile("myfile.txt");
    
    // Write something to it.
    FileUtils.writeStringToFile(tempFile, "hello world");
    
    // Read it.
    //final String s = FileUtils.readFileToString(tempFile);
 
    // Check that what was written is correct.
    String fcontents = FileUtils.readFileToString(tempFile);
    msgt(fcontents);
    msgt("-----------   ShapefileHandlerTest  -----------");

    assertEquals(fcontents, "hello world");
   // assertThat("hello world", is(s));
  }
    
     private File createBlankFile(String filename) throws IOException {
        if (filename == null){
            return null;
        }
        File aFile = this.tempFolder.newFile(filename);
        aFile.createNewFile();
        return aFile;
    }
    

     
     
    private void showFilesInFolder(String m, String folder_name) throws IOException{
        msgt(m);
        File folder = new File(folder_name);
        for (File f : folder.listFiles() ){
            this.msg("fname: " + f.getCanonicalPath());
        }
    } 
         
    private void showFilesInTempFolder(String m) throws IOException{
        this.showFilesInFolder(m, this.tempFolder.getRoot().getAbsolutePath());
     //   msgt(m);
       // for (File f : this.tempFolder.getRoot().listFiles() ){
         //   this.msg("fname: " + f.getCanonicalPath());
       // }
    } 
    
    private FileInputStream createZipReturnFilestream(List<String> file_names, String zipfile_name) throws IOException{
        
        File zip_file_obj = this.createAndZipFiles(file_names, zipfile_name);
        if (zip_file_obj == null){
            return null;
        }
        
        FileInputStream file_input_stream = new FileInputStream(zip_file_obj);

        return file_input_stream;
        
    }
    
    /*
        Convenience class to create .zip file and return a FileInputStream
    
        @param List<String> file_names - List of filenames to add to .zip.  These names will be used to create 0 length files
        @param String zipfile_name - Name of .zip file to create
    */
    private File createAndZipFiles(List<String> file_names, String zipfile_name) throws IOException{
        if ((file_names == null)||(zipfile_name == null)){
            return null;
        }
        
        // Create blank files based on a list of file names
        //
        Collection<File> fileCollection = new ArrayList<>();
        for (String fname : file_names) {
           File file_obj = this.createBlankFile(fname);
           fileCollection.add(file_obj);
           msg("File created: " + file_obj.getName());           
        }
        
        File zip_file_obj = this.tempFolder.newFile(zipfile_name);
        ZipOutputStream zip_stream = new ZipOutputStream(new FileOutputStream(zip_file_obj));

        // Iterate through File objects and add them to the ZipOutputStream
        for (File file_obj : fileCollection) {
             this.addToZipFile(file_obj.getName(), file_obj, zip_stream);
        }

         /* -----------------------------------
           Delete single files that were added to .zip
        ----------------------------------- */
        for (File file_obj : fileCollection) {
             file_obj.delete();
        }
        
        return zip_file_obj;
        
    } // end createAndZipFiles
    
    
    @Test
    public void testCreateZippedNonShapefile() throws IOException{
        msgt("(1) testCreateZippedNonShapefile");
                
        // Create files and put them in a .zip
        List<String> file_names = Arrays.asList("not-quite-a-shape.shp", "not-quite-a-shape.shx", "not-quite-a-shape.dbf", "not-quite-a-shape.pdf"); //, "prj");
        File zipfile_obj = createAndZipFiles(file_names, "not-quite-a-shape.zip");
        
        // Pass the .zip to the ShapefileHandler
        ShapefileHandler shp_handler = new ShapefileHandler(new FileInputStream(zipfile_obj));

        // Contains shapefile?
        assertEquals(shp_handler.containsShapefile(), false);

        // get file_groups Map
        Map<String, List<String>> file_groups = shp_handler.getFileGroups();
        
        // The dict should not be empty
        assertEquals(file_groups.isEmpty(), false);

        // Verify the key
        assertEquals(file_groups.containsKey("not-quite-a-shape"), true);
        
        // Verify the value
        assertEquals(file_groups.get("not-quite-a-shape"), Arrays.asList("shp", "shx", "dbf", "pdf"));
        
        this.showFilesInTempFolder(this.tempFolder.getRoot().getAbsolutePath());

        
        // Delete .zip
        zipfile_obj.delete();
        
        msg("Passed!");
    }
    
    
        
    @Test
    public void testZippedTwoShapefiles() throws IOException{
        msgt("(2) testZippedTwoShapefiles");
                
        // Create files and put them in a .zip
        List<String> file_names = Arrays.asList("shape1.shp", "shape1.shx", "shape1.dbf", "shape1.prj", "shape2.shp", "shape2.shx", "shape2.dbf", "shape2.prj"); //, "prj");
        File zipfile_obj = createAndZipFiles(file_names, "two-shapes.zip");
        
        // Pass the .zip to the ShapefileHandler
        ShapefileHandler shp_handler = new ShapefileHandler(new FileInputStream(zipfile_obj));
        
        
        // Contains shapefile?
        assertEquals(shp_handler.containsShapefile(), true);

        // get file_groups Map
        Map<String, List<String>> file_groups = shp_handler.getFileGroups();
        
        // The dict should not be empty
        assertEquals(file_groups.isEmpty(), false);

        // Verify the keys
        assertEquals(file_groups.containsKey("shape1"), true);      
        assertEquals(file_groups.containsKey("shape2"), true);

        // Verify the values
        assertEquals(file_groups.get("shape1"), Arrays.asList("shp", "shx", "dbf", "prj"));
        assertEquals(file_groups.get("shape2"), Arrays.asList("shp", "shx", "dbf", "prj"));
        
        this.showFilesInTempFolder(this.tempFolder.getRoot().getAbsolutePath());

        //shp_handler.rezipShapefileSets(new FileInputStream(zipfile_obj), this.tempFolder.newFolder("test_unzip").getAbsoluteFile());
        shp_handler.rezipShapefileSets(new FileInputStream(zipfile_obj), new File("/Users/rmp553/Desktop/blah"));
        
        if (shp_handler.errorFound){
            msgt("Error found!");
            msg(shp_handler.errorMessage);
        }
        
        // Delete .zip
        zipfile_obj.delete();
        
        msg("Passed!");
    }
    
    
    //@Test
    public void testZippedShapefileWithExtraFiles() throws IOException{
        msgt("(3) testZippedShapefileWithExtraFiles");
                
        // Create files and put them in a .zip
        List<String> file_names = Arrays.asList("shape1.shp", "shape1.shx", "shape1.dbf", "shape1.prj", "shape1.pdf", "README.md", "shape_notes.txt"); 
        File zipfile_obj = createAndZipFiles(file_names, "shape-plus.zip");

        // Pass the .zip to the ShapefileHandler
        ShapefileHandler shp_handler = new ShapefileHandler(new FileInputStream(zipfile_obj));
        
        
        // Contains shapefile?
        assertEquals(shp_handler.containsShapefile(), true);

        // get file_groups Map
        Map<String, List<String>> file_groups = shp_handler.getFileGroups();
        
        // The dict should not be empty
        assertEquals(file_groups.isEmpty(), false);

        // Verify the keys
        assertEquals(file_groups.containsKey("shape1"), true);      

        // Verify the values
        assertEquals(file_groups.get("shape1"), Arrays.asList("shp", "shx", "dbf", "prj", "pdf"));
        assertEquals(file_groups.get("README"), Arrays.asList("md"));
        assertEquals(file_groups.get("shape_notes"), Arrays.asList("txt"));
        
        //this.showFilesInTempFolder(this.tempFolder.getRoot().getAbsolutePath());
        
        // Delete .zip
        zipfile_obj.delete();
        
        msg("Passed!");
    }
    
    
    //@Test
    public void testCreateZippedShapefile() throws IOException {
    
        msgt("(4) testCreateZippedShapefile");

        // Create four files 
        String file_basename = "income_areas";
        List<String> file_extensions = Arrays.asList("shp", "shx", "dbf", "prj");
        
        Collection<File> fileCollection = new ArrayList<File>();
        for (String ext_name : file_extensions) {
           //System.out.println("ext: " + ext_name);
           File shpPart = this.createBlankFile(file_basename + "." +  ext_name);
           fileCollection.add(shpPart);
           msg("File created: " + shpPart.getName());
           
        }
        
        // debug
        showFilesInTempFolder("Show files in temp folder 1");
        
        
        //ArrayList<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
        
        /* -----------------------------------
           Add the files to a .zip
        ----------------------------------- */
        // create a  ZipOutputStream
        String zippedShapefileName = file_basename + ".zip";
        File zip_file_obj = this.tempFolder.newFile(zippedShapefileName);
        ZipOutputStream zip_stream = new ZipOutputStream(new FileOutputStream(zip_file_obj));
        msg("\nCreate zipped shapefile: " + zippedShapefileName);
        // Iterate through File objects and add them to the ZipOutputStream
        for (File file_obj : fileCollection) {
             this.addToZipFile(file_obj.getName(), file_obj, zip_stream);
        }

        // debug
        showFilesInTempFolder("Show files in temp folder 2");

         /* -----------------------------------
           Delete single files that were added to .zip
        ----------------------------------- */
        for (File file_obj : fileCollection) {
             file_obj.delete();
        }
        showFilesInTempFolder("Show files in temp folder 3");

        
         /* -----------------------------------
           Check this .zipped shapefile
        ----------------------------------- */
        //String tmp_output_folder_for_unzipping = this.tempFolder.newFolder("scratch-space").getAbsolutePath();
        //String output_folder_for_new_zip = this.tempFolder.newFolder("newly-zipped").getAbsolutePath();
        File output_folder_to_unzip = this.tempFolder.newFolder("folder_to_unzip");
        File output_folder_to_rezip = this.tempFolder.newFolder("rezip");

        msg("Temp folder location: " + this.tempFolder.getRoot().getAbsolutePath());
        
        msg("output_folder_to_rezip: " + output_folder_to_rezip.getAbsolutePath());
        ShapefileHandler shp_handler = new ShapefileHandler(new FileInputStream(zip_file_obj)
                                            , output_folder_to_unzip.getAbsolutePath()
                                            , output_folder_to_rezip.getAbsolutePath());
        
        msg("Contains shapefile?: " + shp_handler.containsShapefile());

        
        assertEquals(shp_handler.containsShapefile(), true);
        
        /*
            Get a dict with the following contents
                  key: "income_areas"
                  value: Arrays.asList("shp", "shx", "dbf", "prj")
        */
        Map<String, List<String>> file_groups = shp_handler.getFileGroups();
        
        // The dict should not be empty
        assertEquals(file_groups.isEmpty(), false);

        // Verify the key
        assertEquals(file_groups.containsKey("income_areas"), true);
        
        // Verify the value
        assertEquals(file_groups.get("income_areas"), Arrays.asList("shp", "shx", "dbf", "prj"));
      
  }
    
    
    
    
       private boolean addToZipFile(String fileName, File fileToZip, ZipOutputStream zip_output_stream) throws FileNotFoundException, IOException {

            //File file = new File(fullFilepath);
            FileInputStream file_input_stream = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zip_output_stream.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = file_input_stream.read(bytes)) >= 0) {
                    zip_output_stream.write(bytes, 0, length);
            }

            zip_output_stream.closeEntry();
            file_input_stream.close();

            msg("File [" + fileName + "] added to .zip");

            return true;
	} // end: addToZipFile

}
    

