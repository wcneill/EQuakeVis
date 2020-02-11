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

    public void setup() {
        size(950, 600, OPENGL);
        background(99);

        if (offline) {
            map = new UnfoldingMap(this, 125, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
            earthquakesURLweek = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
        }
        else {
            map = new UnfoldingMap(this, 125, 50, 700, 500, new Microsoft.RoadProvider());
            //earthquakesURL = "2.5_week.atom";
        }

        Location center = new Location(0f,0f);
        float maxPan = 20000;
        map.zoomAndPanTo(2, center);
        map.setPanningRestriction(center, maxPan);
        MapUtils.createDefaultEventDispatcher(this, map);	

        //List of markers to be added to map
        List<Marker> markers = new ArrayList<Marker>();

        //Use parser to collect properties for each earthquake
        //PointFeatures have a getLocation method
        List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURLweek);

        // for each earthqauke feature, create a custom marker and add it
        // to the list.
        for (PointFeature quake : earthquakes){
            Marker quakeMark = createMarker(quake);
            markers.add(quakeMark);
            Map<String, Object> properties = quakeMark.getProperties();
        }

        // Add the markers to the map so that they are displayed
        map.addMarkers(markers);
    }

    /**
     * A helper method to style markers based on features (magnitude, depth,
     * etc) of an earthquake.
     * @param feature A PointFeature object representing a single earthquake.
     * @return 
     */
    private SimplePointMarker createMarker(PointFeature feature){  

        // Create a new SimplePointMarker at the location given by the PointFeature
        SimplePointMarker marker = new SimplePointMarker(feature.getLocation(), feature.getProperties());

        Object magObj = feature.getProperty("magnitude");
        Object ageObj =  marker.getProperty("days ellapsed");
        float mag = Float.parseFloat(magObj.toString());
        int age = (int) ageObj;

        //Set processing color and alpha data, for setting marker colors 
        //below.
        int alpha = 255 - (age * 255 / 7);
        int yellow = color(255, 255, 0, alpha);
        int red = color(255, 0, 0, alpha);
        int green = color(0, 255, 0, alpha);

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

        //set radius of marker based on quake magnitude
        float radius = (float) (mag * 3.5);
        marker.setStrokeColor(color(50,15));
        marker.setRadius(radius);

        return marker;
    }

    public void draw() {
        map.draw();
        addKey();
    }


    // helper method to draw key in GUI
    // TODO: Implement this method to draw the key
    private void addKey() 
    {	
            // Remember you can use Processing's graphics methods here

    }

    public static void main(String[] args) {
        PApplet.main("module3.EarthquakeCityMap");
    }
}
