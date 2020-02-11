/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import processing.core.PApplet;
import processing.data.*;


import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//apache commons libraries for dealing with Byte Order Marks
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.xml.sax.SAXException;


/**
 *
 * @author WNeill
 */
public class ParseLifeEx {
    
    /**
     * A helper method for parsing data from the WorldBank XML document 
     * containing life expectancy data. This method reads the file and strips 
     * it of any Byte Order Mark which might prohibit the Processing library 
     * from loading the XML document. 
     * @param filename - location of XML file
     * @param p
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static XML noBOM(String path, PApplet p) throws 
            FileNotFoundException, UnsupportedEncodingException, IOException{
        
        //set default encoding
        String defaultEncoding = "UTF-8";

        //create BOMInputStream to get rid of any Byte Order Mark
        BOMInputStream bomIn = new BOMInputStream(new FileInputStream(path));
        
        //If BOM is present, determine encoding. If not, use UTF-8
        ByteOrderMark bom = bomIn.getBOM();
        String charSet = bom == null ? defaultEncoding : bom.getCharsetName();
      
        //get buffered reader for speed
        InputStreamReader reader = new InputStreamReader(bomIn, charSet);
        BufferedReader breader = new BufferedReader(reader);
        
        //Build string to parse into XML using Processing's PApplet.parsXML
        StringBuilder buildXML = new StringBuilder();
        int c;
        while((c = breader.read()) != -1){
            buildXML.append((char) c);
        }
        reader.close();
        return p.parseXML(buildXML.toString());
    }
    
    public static Map<String, Float> lifeExpectancyFromXML(String filename, PApplet p, 
            String year) throws UnsupportedEncodingException, IOException{
        
        Map<String, Float> dataMap = new HashMap<>();

        XML xml = noBOM(filename, p);
        
        if(xml != null){

            XML[] records = xml.getChild("data").getChildren("record");
            
            for (XML record : records){
                XML[] fields = record.getChildren("field");
                
                String code = fields[0].getString("key");
                String entryYear = fields[2].getContent();
                float lifeEx = fields[3].getFloatContent();
                
                if (entryYear.equals(year)){
                    dataMap.put(code, lifeEx); 
                }
            }
        } 
        else {
            System.out.println("String could not be parsed.");
        }
        
        return dataMap;
    } 
    
    public static Map<String, Float> 
        lifeExpectancyFromCSV(String filename, PApplet p, String year){
        
        Map<String, Float> dataMap = new HashMap<>();
        Table table = p.loadTable(filename, "header");
        for (TableRow country : table.rows()){
            String c = country.getString("Country Name");
            Float le = country.getFloat(year);
            dataMap.put(c, le);
        }
        return dataMap;
        
    }
    
    
}
