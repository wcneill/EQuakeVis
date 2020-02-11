package utils;

import java.net.URL;
import java.io.File;
import java.io.FileWriter;

// map provider imports
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a helper class that updates the offline tiles and quake data
 * XML files. These updates enable working in offline mode with the most recent
 * data. This allows for a single call to the Google map provider, which limits
 * the number of calls to its service per day. If working in online mode, it
 * is easy to exceed that limit, meaning a 24 hour wait period to access
 * tiles needed to render a map. 
 * 
 * @author WNeill
 */
public class OfflineUpdate {
    
    private static String xmlSource = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
    private static String xmlWriteLoc = "data/2.5_week.atom";
    private static File xmlFile;
    private static String tileSource;
    private static String tileWriteLoc;
    
    public static void writeXML() throws MalformedURLException, IOException{
        
        xmlFile = new File(xmlWriteLoc);
        
        try {
            URL xmlURL = new URL(xmlSource);
            //get InputStreamReader from URL.openStream()
            InputStreamReader isr = new InputStreamReader(xmlURL.openStream());
            
            //Wrape InputStreamReader in Buffered Reader
            try (BufferedReader br = new BufferedReader(isr)){
                //Create FileWriter 
                FileWriter fw = new FileWriter(xmlFile);
                // Wrap FileWriter in BufferedWriter
                BufferedWriter bw = new BufferedWriter(fw);
                
                String input;
                // Line by line: get line from URL via buffered reader,
                // Then write that line to file via buffered writer
                while ((input = br.readLine()) != null){
                    bw.write(input);
                    bw.newLine();
                }
                //close the writer when file reading is complete.
                bw.close();
            } catch (IOException ioe){
                System.err.println(ioe);
            }
        } catch (MalformedURLException mue) {
            System.err.println(mue);
        }       
    }
    
    public static void main(String[] args) {
        try {
            writeXML();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
