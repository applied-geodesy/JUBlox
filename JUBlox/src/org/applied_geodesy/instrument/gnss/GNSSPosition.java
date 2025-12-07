/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.instrument.gnss;

import java.util.Date;

public class GNSSPosition {
	public enum GeographicDirectionLatitude {
		NORTH, SOUTH;
		
		public static GeographicDirectionLatitude getGeographicDirectionLatitudeByChar(char c) {
			return c == 'N' || c == 'n' ? NORTH : SOUTH;
		}
		
		public static GeographicDirectionLatitude getGeographicDirectionLatitudeByChar(String s) {
			return getGeographicDirectionLatitudeByChar(s.charAt(0));
		}
	}
	
	public enum GeographicDirectionLongitude {
		EAST, WEST;
		
		public static GeographicDirectionLongitude getGeographicDirectionLongitudeByChar(char c) {
			return c == 'E' || c == 'e' ? EAST : WEST;
		}
		
		public static GeographicDirectionLongitude getGeographicDirectionLongitudeByChar(String s) {
			return getGeographicDirectionLongitudeByChar(s.charAt(0));
		}
	}
	
	private double longitude, latitude, altitude, geoidalSeparation, hdop;
	private int numberOfSatellites;
	private GeographicDirectionLatitude  oriLat;
	private GeographicDirectionLongitude oriLon;
	private Date receivedDate, systemDate;
	public GNSSPosition(GeographicDirectionLatitude oriLat, double latitude, GNSSPosition.GeographicDirectionLongitude oriLon, double longitude, double altitude, double geoidalSeparation, double hdop, int numberOfSatellites, Date receivedDate, Date systemDate) {
		this.longitude = longitude;
		this.latitude  = latitude;
		this.altitude  = altitude;
		this.geoidalSeparation = geoidalSeparation;
		this.hdop = hdop;
		this.numberOfSatellites = numberOfSatellites;
		this.oriLat = oriLat;
		this.oriLon = oriLon;
		this.receivedDate = receivedDate;
		this.systemDate = systemDate;
	}
	public double getLongitude() {
		return longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public double getGeoidalSeparation() {
		return geoidalSeparation;
	}
	public double getHeight() {
		return geoidalSeparation + altitude;
	}
	public double getHorizontalDilutionOfPrecision() {
		return hdop;
	}
	public int numberOfSatellites() {
		return this.numberOfSatellites;
	}
	public Date getReceivedDate() {
		return receivedDate;
	}
	public Date getSystemDate() {
		return systemDate;
	}
	@Override
	public String toString() {
		return "GNSSPosition [longitude=" + longitude + "(" + oriLon + "), latitude="
				+ latitude + "(" + oriLat + "), receivedDate=" + receivedDate + ", HDOP="
				+ hdop + "]";
	}
}
