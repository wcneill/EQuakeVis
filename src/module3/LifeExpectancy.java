/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package module3;

//import unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Microsoft.RoadProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import java.io.IOException;

// java utils
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

// processing libraries
import processing.core.PApplet;

//parsing library
import parsing.*;

/**
 *
 * @author WNeill
 */
public class LifeExpectancy extends PApplet{
    UnfoldingMap map;
    private String year = "2017";
    private String csvPath = "data/LifeExpectancyWorldBank.csv";
    
    public void setup(){
        size(800,600, OPENGL);
        map = new UnfoldingMap(this, 50, 50, 700, 500,
                                new Microsoft.RoadProvider());
        
        MapUtils.createDefaultEventDispatcher(this, map);
        Map<String, Float> dataMap = 
                ParseLifeEx.lifeExpectancyFromCSV(csvPath, this, year);
        
        for (String key : dataMap.keySet()){
            System.out.println("Country: " + key);
            System.out.println("Life Expectancy " + dataMap.get(key));
        }

    }
    
    public void draw(){
        map.draw();
    }
    
    private Map<String, Float> loadLifeExpectancyData(String filename){
        
        Map<String, Float> dataMap = new HashMap<>();
        
        
        return dataMap;
    }
    
    public static void main(String[] args) {
        PApplet.main("module3.LifeExpectancy");
        
    }
}
