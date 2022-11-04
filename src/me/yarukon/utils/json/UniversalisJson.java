package me.yarukon.utils.json;

import java.util.ArrayList;

public class UniversalisJson {
	public int itemID;
	public long lastUploadTime;
	public ArrayList<ListingJson> listings;
	
	public static class ListingJson {
		public int pricePerUnit;
		public int quantity;
		public boolean hq;
		public String worldName;
		public String retainerName;
		public int total;
	}
}
