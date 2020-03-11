package Map;

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
import de.fhpotsdam.unfolding.marker.MarkerManager;
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
import processing.core.PGraphics;
import processing.data.Table;
import processing.data.TableRow;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * @author Wesley.neill@gmail.com
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

    
    //city border features from JSON
    private List<Feature> cityBorderFeatures;
    //city point and data features from CSV:
    private List<Feature> cityDataFeatures;
    // earthquake data features
    private List<PointFeature> earthquakeFeatures;
    
    // Markers for each city's borders
    private List<Marker> cityBorderMarkers;
    // Markers for city points
     private List<Marker> cityDataMarkers;
    // Markers for each earthquake
    private List<Marker> quakeMarkers;
    
    // fields to help handle mouse events
    private CommonMarker lastSelected;
    private CommonMarker lastClicked;
    
    private MarkerManager borderManager = new MarkerManager();
    private MarkerManager cityManager = new MarkerManager();
    private MarkerManager quakeManager = new MarkerManager();
    
    // A table object containing data about cities for creating
    // and filtering city features.
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
        
        // create initial map view
        map.zoomAndPanTo(2, UnfoldingMap.PRIME_MERIDIAN_EQUATOR_LOCATION);
        map.setZoomRange(2, 15);
//        map.setRectangularPanningRestriction(lctn, lctn1);
        MapUtils.createDefaultEventDispatcher(this, map);	

        // Load table containing city data
        cityTable = loadTable(cityCSVpath, "header");
        // Load city border data from Jason
        cityBorderFeatures = GeoJSONReader.loadData(this, cityJSONpath);
        // Load city location and population data
        cityDataFeatures = addPointFeatures();
        // Create markers for city borders
        cityBorderMarkers = MapUtils.createSimpleMarkers(cityBorderFeatures);
        // Create markers for city locations
        cityDataMarkers = citiesToMarkers(cityDataFeatures, this);
//        cityDataMarkers = MapUtils.createSimpleMarkers(cityDataFeatures);
        
        // Lists of features to be converted to markers
        earthquakeFeatures = ParseFeed.parseEarthquake(this, earthquakesURLweek);
        
        //List of quakeMarkers to be added to map
        quakeMarkers = quakesToMarkers(earthquakeFeatures, this);
        
        // Add quake and city markers to map
        quakeManager.addMarkers(quakeMarkers);
        cityManager.addMarkers(cityDataMarkers);
        filterByPopulation(1000000);
        map.addMarkerManager(borderManager); 
        map.addMarkerManager(cityManager);
        map.addMarkerManager(quakeManager);   

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
    
    @Override
    public void mouseClicked(){
//        location
    }
    
    @Override
    public void mouseMoved(){
        // clear the last selection
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;

        }
        selectMarkerIfHover(quakeMarkers);
        selectMarkerIfHover(cityDataMarkers);
    }
    
    public void mapChanged(MapEvent mapEvent){
        if (mapEvent.getType().equals(PanMapEvent.TYPE_PAN) 
                || mapEvent.getType().equals(ZoomMapEvent.TYPE_ZOOM)){
            List<Marker> toAdd = new LinkedList<>();
            List<Marker> toRemove = new LinkedList<>();
            
            borderManager.clearMarkers();
            if (map.getZoomLevel() > 6){
                for (Marker m : cityBorderMarkers){
                    if (map.isHit(map.getScreenPosition(m.getLocation()))){
                        toAdd.add(m);
                    }
                }
                borderManager.addMarkers(toAdd);
            }
        }
    }
    
    // If there is a marker under the cursor, and lastSelected is null 
    // set the lastSelected to be the first marker found under the cursor
    // Make sure you do not select two markers.
    private void selectMarkerIfHover(List<Marker> markers)
    {
        float x = mouseX;
        float y = mouseY;

        for (Marker m : markers){
            CommonMarker marker = (CommonMarker)m;
            if (marker.isInside(map, x, y)){
                lastSelected = marker;
                lastSelected.setSelected(true);
                break;
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
            Marker pointMarker = createQuakeMarker(feature, p);
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
     * A helper method to create and style quakeMarkers based on feature data
     * (magnitude, depth, etc) of an earthquake.
     * @param feature A PointFeature object representing a single earthquake.
     * @return 
     */
    private static SimplePointMarker createQuakeMarker(PointFeature feature, PApplet p){  

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

    public List<Feature> addPointFeatures(){
        
        List<Feature> cities = new LinkedList<>();
        
        for (TableRow row : cityTable.rows()){
            int population = row.getInt("population");
            if (population > 300000){
                String city = row.getString("city_ascii");
                float lat = row.getFloat("lat");
                float lng = row.getFloat("lng");
                Location loc = new Location(lat, lng);
                
                Feature f = new PointFeature(loc);
                f.addProperty("city_name", city);
                f.addProperty("population", population);
                
                cities.add(f);  
            }
        }
        
        return cities;  
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
        for (Marker marker : cityBorderMarkers){
            marker.setColor(this.color(255, 0, 0, 75));
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
     * Method filters markers by population.
     * @param threshold population 
     */
    public void filterByPopulation(int threshold){
        List<Marker> toRemove = new LinkedList<>();
        for (Object m : cityManager.getMarkers()){
            if (((Marker)m).getIntegerProperty("population") < threshold){
                toRemove.add((Marker)m);
            }
        }
        for (Marker n : toRemove){
            cityManager.removeMarker(n);
        }
    }

    public static void main(String[] args) {
        PApplet.main("module3.EarthquakeCityMap");
    }
}
