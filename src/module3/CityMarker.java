package module3;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class CityMarker extends CommonMarker {
	
	// The size of the triangle marker
	// It's a good idea to use this variable in your draw method
	public static final int SIZE = 2;  
	
	public CityMarker(Location location) {
		super(location);
	}

	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}

	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void draw(PGraphics pg, float x, float y) {
		// Save previous drawing style
		pg.pushStyle();

                pg.fill(pg.color(0, 0 ,255));
                pg.ellipse(x + 0.5f*SIZE, y - 0.5f*SIZE, SIZE, SIZE);
//                pg.rect();

		// Restore previous drawing style
		pg.popStyle();
	}
        
        
        @Override
        public void showTitle(PGraphics p, float x, float y){
        
        }
        
        @Override
        public void drawMarker(PGraphics p, float x, float y){
            
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
