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

package org.applied_geodesy.instrument.gnss.ublox;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.applied_geodesy.instrument.gnss.GNSSLogger;
import org.applied_geodesy.instrument.gnss.GNSSPosition;
import org.applied_geodesy.io.rxtx.ReceiveDataType;
import org.applied_geodesy.io.rxtx.ReceiverExchangeable;
import org.applied_geodesy.io.rxtx.RxTx;
import org.applied_geodesy.io.rxtx.RxTxReturnable;

public class TEPUB353 implements GNSSLogger, RxTxReturnable, ReceiverExchangeable {
	private RxTx connRxTx;
	private GNSSPosition lastPosition;
	private final static String GGA_REG_EXP = "(?s).*(\\$G[N|P]GGA\\S+\\s).*$";
	private final static Pattern GGA_PATTERN = Pattern.compile(GGA_REG_EXP);
	
	public TEPUB353(RxTx connRxTx) {
		this.connRxTx = connRxTx;
		this.connRxTx.setReceiveDataType(ReceiveDataType.BYTE_ARRAY);
	}

	@Override
	public GNSSPosition getLastPosition() {
		return this.lastPosition;
	}

	// http://aprs.gids.nl/nmea/
	private StringBuffer responseMessageGGA = new StringBuffer();
	private Calendar systemDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	
	@Override
	public void receive(byte[] bytesRX) throws IOException {
		this.responseMessageGGA.append(new String(bytesRX));
		this.systemDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		Matcher matcher = GGA_PATTERN.matcher(this.responseMessageGGA);
		if (matcher != null && matcher.find() && matcher.groupCount() > 0) {
			this.lastPosition = exploidGGAResponse(matcher.group(1));
			this.responseMessageGGA.setLength(0); // clearing
		}
	}
	
	@Override
	public void receive(int intRX) throws IOException {
		throw new IOException("Error, unsupported method call. Use receive(byte[] bytesRX) for data transfer.");
	}


	private boolean checkMessage(String msg) {
		int dollarToken = msg.indexOf("$");
		int starToken = msg.indexOf('*');

		if (dollarToken < 0 || starToken < 0 || starToken + 1 == msg.length())
			return false;

		int checksum = 0;
		for (int i = dollarToken + 1; i < starToken; i++)
			checksum ^= msg.charAt(i);

		try {
			int refSum = Integer.parseInt(msg.substring(starToken + 1).trim(), 16);
			return checksum == refSum;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// $GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a
	// $GPGGA,153933.00,5011.28964,N,00844.51181,E,1,08,1.08,103.7,M,47.5,M,,*50
	private GNSSPosition exploidGGAResponse(String ggaMsg) {
		GNSSPosition gnssPosition = null;

		if (this.checkMessage(ggaMsg)) {
			Calendar receivedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

			String data[] = ggaMsg.split(",");

			if (data.length >= 15) {
				// Aufnahmezeit
				int hh    = Integer.parseInt(data[1].substring(0, 2), 10);
				int mm    = Integer.parseInt(data[1].substring(2, 4), 10);
				int ss    = Integer.parseInt(data[1].substring(4, 6), 10);
				int SS    = Integer.parseInt(data[1].substring(7, data[1].length()), 10);

				receivedDate.set(Calendar.HOUR_OF_DAY, hh);
				receivedDate.set(Calendar.MINUTE, mm);
				receivedDate.set(Calendar.SECOND, ss);
				receivedDate.set(Calendar.MILLISECOND, SS);

				// geogr. Koordinaten
				double latitude = Integer.parseInt(data[2].substring(0, 2), 10) + Double.parseDouble(data[2].substring(2, data[2].length()))/60.0;
				char ns =  data[3].charAt(0);
				double longitude = Integer.parseInt(data[4].substring(0, 3), 10) + Double.parseDouble(data[4].substring(3, data[4].length()))/60.0;
				char ew =  data[5].charAt(0);

				// Statistik
				int numberOfSatellites = Integer.parseInt(data[7], 10);
				double hdop = Double.parseDouble(data[8]);

				// Hoehe
				double altitude          = Double.parseDouble(data[9]);
				double geoidalSeparation = Double.parseDouble(data[11]);

				gnssPosition = new GNSSPosition(
						GNSSPosition.GeographicDirectionLatitude.getGeographicDirectionLatitudeByChar(ns),
						latitude, 
						GNSSPosition.GeographicDirectionLongitude.getGeographicDirectionLongitudeByChar(ew), 
						longitude, 
						altitude, 
						geoidalSeparation, 
						hdop, 
						numberOfSatellites, 
						new Date(receivedDate.getTimeInMillis()), 
						new Date(this.systemDate.getTimeInMillis())
						);
			}
		}
		return gnssPosition;
	}

	@Override
	public RxTx getRxTx() {
		return this.connRxTx;
	}
}
