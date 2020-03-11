package Map;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author wesley.neill@gmail.com
 *
 */
public class EarthquakeMarker extends CommonMarker
{

	// SimplePointMarker has a field "radius" which is inherited
	// by Earthquake marker:
	 protected float radius;


	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;
		
        
	// constructor
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
                
		// Get Properties from feature, then set them to this marker.
		java.util.HashMap<String, Object> properties = feature.getProperties();
                
		float mag = Float.parseFloat(properties.get("magnitude").toString());
                
                this.radius = (float) ((30 * Math.exp(mag))/(30 + Math.exp(mag) - 1));
                
		properties.put("radius", radius);

		setProperties(properties);
 
	}
	

	// calls abstract method drawEarthquake 
	public void drawMarker(PGraphics pg, float x, float y) {
            // save previous styling
            pg.pushStyle();
            
            pg.noStroke();
            
            // determine color of marker from magnitude
            colorDetermine(pg);

            // draw ellipse marker 
            pg.ellipse(x, y, radius, radius);

            // reset to previous styling
            pg.popStyle();
		
	}
	
        // Determine color by magnitude and transparency by age
	private void colorDetermine(PGraphics pg) {
        
            float mag = getMagnitude();
            int age = getAge();
            int alpha = 110*(1-(age/7));
            
            
            // Style markers based on earthquake magnitude
            if (mag < THRESHOLD_LIGHT){
                pg.fill(0, 255, 0, alpha);
            }
            if (mag >= THRESHOLD_LIGHT && mag < THRESHOLD_MODERATE){
                pg.fill(255, 255, 0, alpha);
            }    
            if (mag >= THRESHOLD_MODERATE){
                pg.fill(255, 0 , 0, alpha);                
            }  
	}
	
	
	/*
	 * getters for earthquake properties
	 */
        public int getAge(){
            return Integer.parseInt(getProperty("days ellapsed").toString());
        }
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	public String getTitle() {
		return (String) getProperty("title");	
		
	}
	
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}

    @Override
    public void showTitle(PGraphics pg, float x, float y) {
//          pg.rect(x, y, 50, 50);
        
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
	
}
