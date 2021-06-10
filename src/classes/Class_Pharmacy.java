package classes;

import java.util.Arrays;
import jade.core.AID;

public class Class_Pharmacy {
	private String pharmacy;
	private int xPharmacy;
	private int yPharmacy;
	private String[] Medicines;
	private Integer[]Stock_medicines;
	private Integer[] Restock;
	

	public Class_Pharmacy(String x, int y, int z) {
		
		pharmacy = x;
		xPharmacy= y;
		yPharmacy = z;
		
		Medicines = new String[] {"paracetamol","cegripe","bepanthene","dulcolax","aspirina","canesten","trifene200",
				"actifed","buscopan","ibuprofen","vibrocil","rinialer","avene_eau_thermale"};
		
		Stock_medicines = new Integer[] {5,5,1,3,5,1,5,1,5,5,5,1,5};
		
		Restock = new Integer[] {0,0,0,0,0,0,0,0,0,0,0,0,0};
	
		
	}

	
	public int getxPharmacy() {
		return xPharmacy;
	}
	
	public int getyPharmacy() {
		return yPharmacy;
	}
	
	public String getAID() {
		return pharmacy;
	}
	public String[] getMedicines() {
		return Medicines;
	}

	public void setMedicines(String[] Medicines) {
		this.Medicines = Medicines;
	}

	public Integer[] getStock_medicines() {
		return Stock_medicines;
	}

	public void setStock_medicines(Integer[]Stock_medicines) {
		this.Stock_medicines =Stock_medicines;
	}
	
	public Integer[] getRestock() {
		return Restock;
	}
	
	public void setRestock(Integer[] Restock) {
		this.Restock = Restock;
	}
	
	public void changeStock(int stock,int indice){
		Stock_medicines[indice]=stock;
		 }
	
	public void changeNumberRestock(int vezes,int indice){
		Restock[indice]=vezes;
		 }
	
	
}