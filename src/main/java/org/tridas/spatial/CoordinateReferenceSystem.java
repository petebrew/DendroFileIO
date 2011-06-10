package org.tridas.spatial;

import org.tridas.spatial.GMLPointSRSHandler.AxisOrder;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * 
 * 
 * @author pwb48
 */
public class CoordinateReferenceSystem {

	private final String name;
	private final Integer code;
	private final String projstr;
	private final AxisOrder axisOrder;
	
	/**
	 * Construct a new coordinate system reference
	 * 
	 * @param name
	 * @param code
	 * @param axisOrder
	 */
	public CoordinateReferenceSystem(Integer code, String name, AxisOrder axisOrder, String projstr)
	{
		this.name = name;
		this.code = code;
		this.projstr = projstr;
		this.axisOrder =axisOrder;
	}
	
	/**
	 * Get friendly name for this CRS
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get EPSG registry code for this CRS
	 * 
	 * @return
	 */
	public Integer getCode()
	{
		return code;
	}
	
	/**
	 * Get the order in which axis should be presented
	 * 
	 * @return
	 */
	public AxisOrder getAxisOrder()
	{
		return axisOrder;
	}
	
	/**
	 * Get PROJ4 specification string 
	 * 
	 * @return
	 */
	public String getProjStr()
	{
		return projstr;
	}
	
	public String toString()
	{
		return name+ " [EPSG:"+code+"]";
	}
	
	/**
	 * Get this CRS as a JMapProjLib Projection
	 * 
	 * @return
	 */
	public Projection getAsProjection()
	{
		String[] projparams = projstr.split(" ");
		return ProjectionFactory.fromPROJ4Specification(projparams);
	}
	
	
}
