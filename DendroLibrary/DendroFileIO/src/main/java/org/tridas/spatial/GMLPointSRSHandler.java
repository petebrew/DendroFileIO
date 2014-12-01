package org.tridas.spatial;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;

/**
 * Helper class to take care of handling the srsName for coordinates.  Where
 * possible it will project points and handle axis order ambiguities.  
 * 
 * @author pwb48
 *
 */
public class GMLPointSRSHandler  {

	private static final Logger log = LoggerFactory.getLogger(GMLPointSRSHandler.class);

	
	private static final long serialVersionUID = 1L;
	private Projection proj = null;
	private PointType point = null;
	private AxisOrder axisOrder = AxisOrder.LONG_LAT;
	private Point2D.Double projectedpoint = new Point2D.Double();

	/**
	 * Whether coordinates should be presented with Latitude or Longitude
	 * first.  
	 * 
	 * @author pwb48
	 *
	 */
	public enum AxisOrder
	{
		LAT_LONG,
		LONG_LAT;
		
		/**
		 * Returns the default order - Longitude - Latitude
		 * @return
		 */
		public AxisOrder getDefault()
		{
			return AxisOrder.LONG_LAT;
		}
	}
	
	/**
	 * Standard constructor.  Takes a GML PointType.
	 * @param point
	 * @throws ProjectionException
	 */
	public GMLPointSRSHandler(PointType point) throws ProjectionException
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
		if(proj==null)
		{
			return false;
		}
		else
		{
			return true;
		}
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
			//log.debug("Raw SRSName string in GML is "+srsName);
		}
		else
		{
			// no srsName so presume WGS84
			//log.debug("no SRSName in GML so assuming WGS84");
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
			
			// Look up axis order for this EPSG code
			try
			{	
				Integer code = Integer.parseInt(urnparts[6]);
				//log.debug("Looking up srsname = "+code);
				
				CoordinateReferenceSystem crs = (CoordinateReferenceSystem) TridasIO.crsMap.get(code);
				
				if(crs==null){
					log.warn("CRS code not found");
					return;
				}

				axisOrder = crs.getAxisOrder();
				
				if(urnparts[6].equalsIgnoreCase("4326"))
				{
					// This is the standard coordinate references system so no need to look it up
					return;
				}
				else
				{
					// This means we're looking at another EPSG crs so try looking it up
					try{
						proj = crs.getAsProjection();
						if(proj==null)
						{
							throw new ProjectionException(I18n.getText("srsname.notsupported"));
						}
					} catch (ProjectionException e)
					{
						throw new ProjectionException(I18n.getText("srsname.notsupported"));
					}
					
				}
				
			} catch (NumberFormatException e){}

		}
		else if (srsName.equalsIgnoreCase("WGS84") || (srsName.equals("EPSG:4326")) || (srsName.equals("EPSG::4326")))
		{
			// Default WGS84 srsname so no need to do anything
			//log.info("Standard SRSName so no need to do anything about projecting or axis order");
			return;
		}
		
		else if (srsName.toUpperCase().startsWith("EPSG:")) 
		{
			// Some other coordinate system so have a stab at converting it
			try{
				String strcode = srsName.substring(srsName.indexOf(":")+1);
				Integer code = Integer.parseInt(strcode);
				CoordinateReferenceSystem crs = TridasIO.crsMap.get(code);
								
				proj = crs.getAsProjection();
				
				if(proj==null)
				{
					throw new ProjectionException(I18n.getText("srsname.notsupported"));
				}
				
				return;
			}catch (NumberFormatException e2)
			{
				throw new ProjectionException(I18n.getText("srsname.notsupported"));
			}
			catch (ProjectionException e)
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
	
	/**
	 * Get this point with the correct WGS84 srsName
	 * 
	 * @return
	 */
	public PointType getAsWGS84PointType()
	{
		PointType pt = new PointType();
		Pos pos = new Pos();
		
		// X,Y order for simplified WGS84 ref
		pt.setSrsName(SpatialUtils.WGS84);
		ArrayList<Double> coords = new ArrayList<Double>();
		coords.add(projectedpoint.getX());
		coords.add(projectedpoint.getY());
		
		// Y,X order for full URN ref
		//pt.setSrsName(SpatialUtils.WGS84_FULL_URN);
		//ArrayList<Double> coords = new ArrayList<Double>();
		//coords.add(projectedpoint.getY());
		//coords.add(projectedpoint.getX());
		
		pos.setValues(coords);
		pt.setPos(pos);		
		return pt;
	}


}
