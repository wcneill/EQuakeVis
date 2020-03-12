package Map;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import java.util.HashMap;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author wesley.neill@gmail.com
 *
 */
public class CityMarker extends CommonMarker {
	
	// The size of the triangle marker
	public static int SIZE = 2;  
	
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
//            System.out.println("Is Hidden? " + this.isHidden());
            pg.pushStyle();
            pg.fill(pg.color(0, 0 ,255));
            pg.ellipse(x + 0.5f*SIZE, y - 0.5f*SIZE, SIZE, SIZE);
            pg.popStyle();
	}
        
        @Override
        public void showTitle(PGraphics p, float x, float y){
            
            String city = getCity();
            int population = getPopulation();
            
            p.fill(250);
            p.rect(x, y, 150, 35);
            p.fill(0);
            p.text(city, x+10, y+15);
            p.text("Population: " + population, x+10, y+30);

        }
	
	public String getCity()
	{
            return getStringProperty("city_name");
	}
	
	public String getCountry()
	{
            return getStringProperty("country").toString();
	}
	
	public int getPopulation()
	{  
            return Integer.parseInt(getProperty("population").toString());
	}	
}
