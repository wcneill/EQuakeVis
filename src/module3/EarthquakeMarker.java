package module3;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author wesley.neill@gmail.com
 *
 */
public abstract class EarthquakeMarker extends SimplePointMarker
{
	
	// Did the earthquake occur on land?  This will be set by the subclasses.
	protected boolean isOnLand;

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
	
	// abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
		
	
	// constructor
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		// Add a radius property and then set the properties
                
		java.util.HashMap<String, Object> properties = feature.getProperties();
                
		float mag = Float.parseFloat(properties.get("magnitude").toString());
                
                this.radius = (float) ((20 * Math.exp(mag))/(20 + Math.exp(mag) - 1));
                
		properties.put("radius", radius);
                
		setProperties(properties);
 
	}
	

	// calls abstract method drawEarthquake 
	public void draw(PGraphics pg, float x, float y) {
            // save previous styling
            pg.pushStyle();
            
            // determine color of marker from magnitude
            colorDetermine(pg);

            // call abstract method implemented in child class to draw marker shape
            drawEarthquake(pg, x, y);

            // reset to previous styling
            pg.popStyle();
		
	}
	
        // Determine color by magnitude
	private void colorDetermine(PGraphics pg) {
        
            float mag = getMagnitude();
                       // Style markers based on earthquake magnitude
//            System.out.println("Magnitude: " + mag);
            if (mag < THRESHOLD_LIGHT){
                pg.fill(0, 255, 0);
            }
            if (mag >= THRESHOLD_LIGHT && mag < THRESHOLD_MODERATE){
                pg.fill(255, 255, 0);
            }    
            if (mag >= THRESHOLD_MODERATE){
                pg.fill(255, 0 , 0);                
            }
                
	}
	
	
	/*
	 * getters for earthquake properties
	 */
	
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
	
	public boolean isOnLand()
	{
		return isOnLand;
	}
	
	
}