package org.tridas.io.util;

import java.awt.geom.Point2D;

import org.tridas.io.I18n;

import net.opengis.gml.schema.PointType;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * Helper class to take care of projecting coordinates and handling axis order ambiguities.  
 * 
 * @author pwb48
 *
 */
public class TridasPointProjectionHandler  {

	private static final long serialVersionUID = 1L;
	private Projection proj = null;
	private PointType point = null;
	private AxisOrder axisOrder = AxisOrder.LONG_LAT;
	private Point2D.Double projectedpoint = new Point2D.Double();

	
	enum AxisOrder{
		LAT_LONG,
		LONG_LAT;
	}
	
	/**
	 * Standard constructor.  Takes a GML PointType.
	 * @param point
	 * @throws ProjectionException
	 */
	public TridasPointProjectionHandler(PointType point) throws ProjectionException
	{
		this.point = point;
		
		// Parse the srsName for the point at set projection info if possible
		parseSRSName();
			
		// Actually project the point
		doProjection();
	}
	
	public String getProjectionName()
	{
		if(proj==null) return null;
		return proj.toString();
	}
	
	public String getProjectionDescription()
	{
		if(proj==null) return null;
		return proj.getProjectionDescription();
	}
	
	
	public String getEPSGCode()
	{
		if(proj==null) return null;
		
		int code = proj.getEPSGCode();
		if(code==0)
		{
			return null;
		}
		else
		{
			return code+"";
		}
	}
	
	/**
	 * Does this location have a known and useable projection?
	 *  
	 * @return
	 */
	public Boolean hasSpecificProjection()
	{
		return proj!=null;
	}
	
	/**
	 * Get the X (longitude) coordinate for this location
	 * 
	 * @return
	 */
	public Double getWGS84LongCoord()
	{
		if(projectedpoint!=null)
		{
			return projectedpoint.getX();
		}
		return null;
	}
	
	/**
	 * Get the Y (latitude) coordinate for this location
	 * 
	 * @return
	 */
	public Double getWGS84LatCoord()
	{
		if(projectedpoint!=null)
		{
			return projectedpoint.getY();
		}
		return null;
	}
	
	/**
	 * The correct order in which the axis should appear in gml:Pos 
	 * 
	 * @return
	 */
	public AxisOrder getAxisOrder()
	{
		return axisOrder;
	}
		
	/**
	 * Try to extract projection information from this location
	 * @throws ProjectionException 
	 */
	private void parseSRSName() throws ProjectionException
	{
				
		// Grab the srsName from the geometry if it exists.  Trim and removed spaces.
		String srsName = null;
		if(point.isSetSrsName())
		{
			srsName = point.getSrsName().trim().replace(" ", "");
		}
		else
		{
			// no srsName so presume WGS84
			return;
		}
			
		
		if(srsName.startsWith("urn:"))
		{
			// Srsname is a URN
			
			String[] urnparts = srsName.split(":");
			if(urnparts.length!=7)
			{
				throw new ProjectionException("Invalid srsName URN");
			}
			
			if(!(urnparts[1].equalsIgnoreCase("ogc")) && !(urnparts[1].equalsIgnoreCase("x-ogc")))
			{
				if(urnparts[1].equals(""))
				{
					throw new ProjectionException("The URN for the coordinate reference system contains no organisation description");
				}
				throw new ProjectionException("The URN for the coordinate reference system is from the unsupported organisation '" +urnparts[1]+"'");
			}
			
			if(!urnparts[2].equalsIgnoreCase("def"))
			{
				throw new ProjectionException("Invalid srsName URN");
			}
			
			if(!urnparts[3].equalsIgnoreCase("crs"))
			{
				throw new ProjectionException("The URN supplied to describe this coordinate reference system is for object type other than 'crs'");
			}
			
			if(!urnparts[4].equalsIgnoreCase("EPSG"))
			{
				throw new ProjectionException("The URN supplied does not refer to the EPSG database.  The EPSG is the only authority currently supported");
			}
			
			//TODO Need to look this up in the EPSG database
			if(urnparts[4].equalsIgnoreCase("EPSG") && urnparts[6].equalsIgnoreCase("4326"))
			{
				axisOrder = AxisOrder.LAT_LONG;
			}
			
			
			if(urnparts[4].equalsIgnoreCase("EPSG") && urnparts[6].equalsIgnoreCase("4326"))
			{
				// This is the standard coordinate references system so no need to look it up
				return;
			}
			else
			{
				// This means we're looking at another EPSG crs so try looking it up
				try{
					proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(urnparts[4]+":"+urnparts[6]);
					return;
				} catch (ProjectionException e)
				{
					throw new ProjectionException(I18n.getText("srsname.notsupported"));
				}
				
			}
			
			
		}
		else if (srsName.equalsIgnoreCase("WGS84") || (srsName.equals("EPSG:4326")))
		{
			// Default WGS84 srsname so no need to do anything
			return;
		}
		
		else 
		{
			// Some other coordinate system so have a stab at converting it
			try{
				proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(srsName);
				
				if(proj==null)
				{
					throw new ProjectionException(I18n.getText("srsname.notsupported"));
				}
				
				return;
			} catch (ProjectionException e)
			{
				throw new ProjectionException(I18n.getText("srsname.notsupported"));
			}
		}

	}
	
	/**
	 * Project the point.  If the projection is null then just return the point unprojected
	 * but only once we've taken care of the hideous axis order mess.
	 */
	private void doProjection()
	{
		
		Point2D.Double pnt = new Point2D.Double();
		
		// Basic error checking
		if(!point.isSetPos()) return;
		if(!point.getPos().isSetValues()) return;
		if(point.getPos().getValues().size()!=2) return;
		
		// Grab the coordinates from the GML based upon axis order
		switch (axisOrder){
		case LAT_LONG:
			pnt.setLocation(
					point.getPos().getValues().get(1), 
					point.getPos().getValues().get(0));
			break;
		case LONG_LAT:
			pnt.setLocation(
					point.getPos().getValues().get(0), 
					point.getPos().getValues().get(1));
			break;
		}
		
		if(proj==null)
		{
			// No need to project just grab coordinates
			projectedpoint = pnt;
		}
		else
		{
			// Need to actually project coordinates
			proj.inverseTransform(pnt, projectedpoint);
		}
		
		
	}
	


}
