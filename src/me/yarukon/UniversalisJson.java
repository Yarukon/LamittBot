package me.yarukon;

import java.util.ArrayList;

public class UniversalisJson {
	public int itemID;
	public long lastUploadTime;
	public ArrayList<UniversalisListingJson> listings;
	
	public static class UniversalisListingJson {
		public int pricePerUnit;
		public int quantity;
		public boolean hq;
		public String worldName;
		public String retainerName;
		public int total;
	}
}
