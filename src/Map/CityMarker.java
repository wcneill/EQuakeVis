package Map;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import java.util.HashMap;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class CityMarker extends CommonMarker {
	
	// The size of the triangle marker
	public static final int SIZE = 2;  
	
	public CityMarker(Location location) {
            super(location);
	}

	public CityMarker(Feature city) {
            super(((PointFeature)city).getLocation(), city.getProperties());       
	}

	/**
	 * Implementation of abstract method in CommonMarker class  to draw 
         * marker on the map.
	 */
        @Override
	public void drawMarker(PGraphics pg, float x, float y) {

            pg.pushStyle();
            pg.fill(pg.color(0, 0 ,255));
            pg.ellipse(x + 0.5f*SIZE, y - 0.5f*SIZE, SIZE, SIZE);
            pg.popStyle();
	}
        
        @Override
        public void showTitle(PGraphics p, float x, float y){
            // for now, just trying to test the hover by popping up a blank 
            // rectangle
            p.rect(x, y, 50, 50);
            String city = getCity();
            Float population = getPopulation();
        }
	
	public String getCity()
	{
            return getStringProperty("name");
	}
	
	public String getCountry()
	{
            return getStringProperty("country");
	}
	
	public float getPopulation()
	{
            return Float.parseFloat(getStringProperty("population"));
	}	
}
