/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Map;

//import unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoDataReader;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import java.io.IOException;

// java utils
import java.util.List;
import java.util.Map;

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
    private String xmlPath = "data/life_expectancy.xml";
    private String jsonPath = "data/countries.geo.json";
    private List<Feature> countryFeatures;
    private List<Marker> countryMarkers;
    private Map<String, Float> dataMap;
    
    
    public void setup(){

        size(800,600, OPENGL);
        map = new UnfoldingMap(this, 50, 50, 700, 500,
                new Microsoft.RoadProvider());

        MapUtils.createDefaultEventDispatcher(this, map);
        
        try {
            dataMap = loadLifeExpectancyData();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
        countryFeatures = GeoJSONReader.loadData(this, jsonPath);
        countryMarkers = MapUtils.createSimpleMarkers(countryFeatures);

        map.addMarkers(countryMarkers);
        shadeMarkers();
    }
    
    public void draw(){
        map.draw();
    }
    
    /**
     * Helper method to set country shades by life expectancy.
     */
    private void shadeMarkers(){
        for (Marker marker : countryMarkers){
            String ID = marker.getId();
            if (dataMap.containsKey(ID)){
                float lifeExpectancy = dataMap.get(ID);
                int blueLevel = (int) map(lifeExpectancy, 40, 90, 255, 5);
                int c = color(0, 0, 255 - blueLevel, 200);
                marker.setColor(c);
                marker.setStrokeColor(color(0, 0, 0));
                marker.setStrokeWeight(1);
            }
            else{
                marker.setColor(color(150,150,150,50));
                marker.setStrokeColor(color(0, 0, 0));
                marker.setStrokeWeight(1);
            }
            
        }
    }
    
    private Map<String, Float> loadLifeExpectancyData() throws IOException{
        return ParseLifeEx.lifeExpectancyFromXML(xmlPath, this, year);
    }
    
    public static void main(String[] args) {
        PApplet.main("module3.LifeExpectancy");
        
    }
}
