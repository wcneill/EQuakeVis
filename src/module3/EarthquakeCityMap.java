package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Microsoft.RoadProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import java.util.HashMap;
import java.util.LinkedList;

//Parsing library
import parsing.ParseFeed;
import processing.core.PFont;
import processing.data.Table;
import processing.data.TableRow;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

    // IF YOU ARE WORKING OFFLINE, change the value of this variable to true
    private static final boolean offline = false;
    
    // Less than this threshold is a light earthquake
    public static final float THRESHOLD_MODERATE = 5;
    // Less than this threshold is a minor earthquake
    public static final float THRESHOLD_LIGHT = 4;

    /** This is where to find the local tiles, for working without an Internet connection */
    public static String mbTilesString = "data/blankLight-1-3.mbtiles";

    // The map
    private UnfoldingMap map;

    //feed with magnitude 2.5+ Earthquakes
    private String earthquakesURLweek = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
    private String earthquakesURLday = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.atom";
    
    // The files containing city names and info and country names and info
    private String cityJSONpath = "cities.geo.json";
    private String cityCSVpath = "worldcities.csv";
    private String countryFile = "countries.geo.json";

    
    // Country features from JSON
    private List<Feature> cityFeatures;
    // Markers for each city
    private List<Marker> cityMarkers;
    // Markers for each earthquake
    private List<Marker> quakeMarkers;
    // A List of country quakeMarkers
    private List<Marker> countryMarkers;
    
    // A table object containing data about cities for filtering features.
    private Table cityTable;

    public void setup() {
        size(1275, 900, OPENGL);
        background(255,255,255);

        if (offline) {
            map = new UnfoldingMap(this, 225, 25, 1020, 850, new MBTilesMapProvider(mbTilesString));
            earthquakesURLweek = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
        }
        else {
            map = new UnfoldingMap(this, 225, 25, 1020, 850);
            //earthquakesURL = "2.5_week.atom";
        }
        
        //set panning restrictions
        Location center = new Location(0, 0);
        float maxPan = 20;
        map.zoomAndPanTo(2, center);
//        map.setPanningRestriction(center, maxPan);
        map.setZoomRange(2, 15);
        MapUtils.createDefaultEventDispatcher(this, map);	

        // List of features to be converted to markers
        List<PointFeature> earthquakes = 
                ParseFeed.parseEarthquake(this, earthquakesURLweek);
        List<Feature> cities = GeoJSONReader.loadData(this, cityJSONpath);
        
        
        cityTable = loadTable(cityCSVpath, "header");
        cityFeatures = GeoJSONReader.loadData(this, cityJSONpath);
        cityMarkers = MapUtils.createSimpleMarkers(cityFeatures);
        //List of quakeMarkers to be added to map
        quakeMarkers = quakesToMarkers(earthquakes, this);
//        cityMarkers = citiesToMarkers(cities, this);
        

        // Add the quakeMarkers to the map so that they are displayed
        map.addMarkers(quakeMarkers);
        map.addMarkers(filterByPopulation(cityMarkers, 3000000));
        shadeBoundaries();
        
        addKey();
    }
    
    public void draw() {
        map.draw();
    }
     
    @Override
    public void mouseReleased(){
        if(mouseX > 50 && mouseX < 100
                && mouseY > 800 && mouseY < 850){
            background(255,255,255);
            addKey();
        }
        
        if (mouseX > 125 && mouseX < 175
                && mouseY > 800 && mouseY < 850){
            background(100, 100, 100);
            addKey();
        }
    }
    
    public void mapChanged(MapEvent mapEvent){
        System.out.println("Event Detected");
        if (mapEvent.getType().equals(PanMapEvent.TYPE_PAN) 
                || mapEvent.getType().equals(ZoomMapEvent.TYPE_ZOOM)){
            System.out.println("Event is Pan or Zoom");
            List<Marker> toAdd = new LinkedList<>();
            
            if (map.getZoomLevel() >= 6 && map.getZoomLevel() < 8){
                for (Marker m : cityMarkers){
                    if (map.isHit(map.getScreenPosition(m.getLocation()))){
                        toAdd.add(m);
                        System.out.println("Marker to add: " + m.getStringProperty("NAME"));
                    }
                }
                
                map.addMarkers(filterByPopulation(toAdd, 1000000));
                System.out.println("Markers Added");
            }
            
            if (map.getZoomLevel() >= 8){
                for (Marker m : cityMarkers){
                    if (map.isHit(map.getScreenPosition(m.getLocation()))){
                        toAdd.add(m);
                        System.out.println("Marker to add: " + m.getStringProperty("NAME"));
                    }
                }
                
                map.addMarkers(filterByPopulation(toAdd, 300000));
                System.out.println("Markers Added");
            }
            
        }
    }
  
    
    /**
     * Helper method to convert a list of PointFeatures to a list of Markers.
     * @param features
     * @param p
     * @return 
     */
    private static List<Marker> quakesToMarkers(List<PointFeature> features, PApplet p){
        List<Marker> markers = new ArrayList<>();
        for (PointFeature feature : features){
            Marker pointMarker = createMarker(feature, p);
            markers.add(pointMarker);
        }
        return markers;
    }
    
    private static List<Marker> citiesToMarkers(List<Feature> cities, PApplet p){
        ArrayList<Marker> markers = new ArrayList<>();
        for(Feature city : cities) {
            markers.add(new CityMarker(city));
        }
        return markers;
    }
    
    
    /**
     * A helper method to style quakeMarkers based on features (magnitude, depth,
 etc) of an earthquake.
     * @param feature A PointFeature object representing a single earthquake.
     * @return 
     */
    private static SimplePointMarker createMarker(PointFeature feature, PApplet p){  

        // Create a new SimplePointMarker at the location given by the PointFeature
        SimplePointMarker marker = 
                new SimplePointMarker(feature.getLocation(), 
                                      feature.getProperties());

        Object magObj = feature.getProperty("magnitude");
        Object ageObj =  marker.getProperty("days ellapsed");
        float mag = Float.parseFloat(magObj.toString());
        int age = (int) ageObj;

        //Set processing color and alpha data, for setting marker colors 
        //below.
        int alpha = 255 - (age * 255 / 7);
        int yellow = p.color(255, 255, 0, alpha);
        int red = p.color(255, 0, 0, alpha);
        int green = p.color(0, 255, 0, alpha);
        
        // Style quakeMarkers based on earthquake magnitude
        if (mag < THRESHOLD_LIGHT){
            marker.setColor(green);
        }
        if (mag >= THRESHOLD_LIGHT && mag < THRESHOLD_MODERATE){
            marker.setColor(yellow);
        }
        if (mag >= THRESHOLD_MODERATE){
            marker.setColor(red);
        }

        //set radius of marker based on quake magnitude (logistic growth)
        float radius = (float) ((30 * Math.exp(mag))/(30 + Math.exp(mag) - 1));
        marker.setStrokeColor(p.color(50,15));
        marker.setRadius(radius);

        return marker;
    }
    
    /**
     * Helper method to add a legend to the map.
     */
    private void addKey() {
        
        int yellow = color(255, 255, 0);
        int red = color(255, 0, 0);
        int green = color(0, 255, 0);
        
        fill(color(200, 100));
        rect(25, 25, 175, 850);

        fill(0);
        text("Legend", 87.5f, 50);
        
//        textFont(f, 10);
        text("Magnitude", 80, 90);
        
        fill(255);
        stroke(0);
        addCircleSeries(60, 120, 10, 2, 10, this);
        
        fill(0);
        text("Mag < " + THRESHOLD_LIGHT, 60, 170);
        fill(green);
        ellipse(150, 165, 25, 25);
        
        fill(0);
        text("Mag > " + THRESHOLD_LIGHT, 60, 220);
        fill(yellow);
        ellipse(150, 215, 25, 25);

        fill(0);
        text("Mag > " + THRESHOLD_MODERATE, 60, 270);
        fill(red);
        ellipse(150, 265, 25, 25);
        
        //Draw buttons for backtround color change:
        fill(255,255,255);
        rect(50,800, 50, 50);
        
        fill(100,100,100);
        rect(125,800, 50, 50);
        
        
    }
    
    /**
     * Create a horizontal series of circles that are evenly spaced 
     * and increasing in size.
     * 
     * @param x x-coord of first circle
     * @param y y-coord of first circle
     * @param init size of first circle
     * @param incr size increment of circles
     * @param spacing space between circles
     */
    private void addCircleSeries(int x, int y, int init,
                                 int incr, int spacing, PApplet p){
        
        for (int i = 0; i < 5; i ++){
            p.ellipse(x, y, init, init);
            x = x + init/2 + spacing + (init + incr)/2;
            init += incr;
        }
    }   
    
    private void shadeBoundaries(){
        for (Marker marker : cityMarkers){
            marker.setColor(this.color(255, 0, 0, 150));
        }
    }
    
    /**
     * Create a Map of key value pairs that are two associated column values. 
     * @param table
     * @param keyCol
     * @param valueCol
     * @return 
     */
    private Map<String, String> getRowMap(Table table, 
                                            String keyCol, String valueCol){
        Map<String, String> tableMap = new HashMap<>();
        int numRows = table.getRowCount();
        
        for (int i = 0; i < numRows; i++){
            TableRow row = table.getRow(i);
            String key = row.getString(keyCol).toUpperCase();
            String value = row.getString(valueCol).toUpperCase();
            tableMap.put(key, value);
        }
        
        return tableMap;

    }
    
    /**
     * Method takes a list of city features with property "NAME" but no population.
     * The method is then filtered by population based on a hashmap that contains
     * both city names and population to return. The filtered list of features
     * is returned. This is a work around to filter data from a JSON file containing
     * the border geometry for most of the cities of the world, but does not have 
     * population data as a feature. 
     * 
     * @param popMap
     * @param toFilter
     * @param threshold
     * @return 
     */
    private List<Marker> filterByPopulation(List<Marker> toFilter, int threshold){
        

        List<Marker> filtered = new LinkedList<>();
        Map<String, String> popMap = 
                getRowMap(cityTable, "city_ascii", "population");
        
        for (Marker f : toFilter){
            String cityName = f.getStringProperty("NAME");
            String pop = popMap.get(cityName);
            if ((cityName != null) && (pop != null) && (!pop.isBlank())){
                if (Double.parseDouble(pop) > threshold){
                    filtered.add(f);
                }
            }   
        }
        return filtered;
    }

    public static void main(String[] args) {
        PApplet.main("module3.EarthquakeCityMap");
    }
}
