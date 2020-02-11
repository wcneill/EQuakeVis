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
import org.xml.sax.SAXException;
import org.apache.commons.io.input.BOMInputStream;


/**
 *
 * @author WNeill
 */
public class ParseLifeEx {
    
    private static XML noBOM(String filename, PApplet p) throws FileNotFoundException, IOException{

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File f = new File(filename);
        InputStream stream = new FileInputStream(f);
        BOMInputStream bomIn = new BOMInputStream(stream);
        
        int tmp = -1;
        while ((tmp = bomIn.read()) != -1){
            out.write(tmp);
        }

        String strXml = out.toString();
        return p.parseXML(strXml);
    }
    
    public static Map<String, Float> lifeExpectancyFromXML(String filename, PApplet p, 
            int year) throws FileNotFoundException, IOException{
        
        
        Map<String, Float> dataMap = new HashMap<>();

        XML xml = noBOM(filename, p);
        
        if(xml != null){

            XML[] records = xml.getChild("data").getChildren("record");
            
            for (XML record : records){
                XML[] fields = record.getChildren("field");

                String country = fields[0].getContent();
                int entryYear = fields[2].getIntContent();
                float lifeEx = fields[3].getFloatContent();
                
                if (entryYear == year){
                    System.out.println("Country: " + country);
                    System.out.println("Life Expectency: " + lifeEx);
                    dataMap.put(country, lifeEx);
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
