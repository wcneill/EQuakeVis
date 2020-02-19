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
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Microsoft.RoadProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;
import processing.core.PFont;

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
    private PFont f;

    public void setup() {
        size(1250, 850, OPENGL);
        background(50);

        if (offline) {
            map = new UnfoldingMap(this, 225, 25, 1000, 800, new MBTilesMapProvider(mbTilesString));
            earthquakesURLweek = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
        }
        else {
            map = new UnfoldingMap(this, 225, 25, 1000, 800, new Microsoft.RoadProvider());
            //earthquakesURL = "2.5_week.atom";
        }
        
        //set panning restrictions
        Location center = new Location(0, 0);
        float maxPan = 20000;
        map.zoomAndPanTo(2, center);
        map.setPanningRestriction(center, maxPan);
        MapUtils.createDefaultEventDispatcher(this, map);	

        //Use parser to collect properties for each earthquake
        //PointFeatures have a getLocation method
        List<PointFeature> earthquakes = 
                ParseFeed.parseEarthquake(this, earthquakesURLweek);
        
        //List of markers to be added to map
        List<Marker> markers = featuresToPoints(earthquakes, this);

        // Add the markers to the map so that they are displayed
        map.addMarkers(markers);
    }
    
    public void draw() {
        map.draw();
        addKey();
    }
    
    /**
     * Helper method to convert a list of PointFeatures to a list of Markers.
     * @param features
     * @param p
     * @return 
     */
    private static List<Marker> featuresToPoints(List<PointFeature> features, PApplet p){
        List<Marker> markers = new ArrayList<>();
        for (PointFeature feature : features){
            Marker pointMarker = createMarker(feature, p);
            markers.add(pointMarker);
        }
        return markers;
    }
    
    /**
     * A helper method to style markers based on features (magnitude, depth,
     * etc) of an earthquake.
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
        
        // Style markers based on earthquake magnitude
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
        
        fill(color(150, 10));
        rect(25, 25, 175, 800);
//        
//        f = createFont("Sans Serif", 16, true);
//
//        textFont(f, 16);
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

    public static void main(String[] args) {
        PApplet.main("module3.EarthquakeCityMap");
    }
}
