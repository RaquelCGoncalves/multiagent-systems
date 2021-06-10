package agents;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import classes.Class_Pharmacy;

public class Interface extends Agent {
	
	private ArrayList<Class_Pharmacy> Pharmacies ;
	private Map<String,Integer> quantaties_sold ;
	private Map<String,Integer> pharmacywithmostorders;
	private Map<String, String> record  ; 
	private Map<String, Double> times ; 
	private static DecimalFormat df2; 
	
	protected void setup() {
		super.setup();
		

		System.out.print("-----------------------------------------@@Starting Interface@@----------------------------------------------");
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\n");
		Pharmacies =  new ArrayList<Class_Pharmacy> ();
		quantaties_sold= new HashMap<String,Integer>();
		record  = new HashMap<String,String>();
		times = new HashMap<String,Double>();
		df2 = new DecimalFormat("#.00");
		pharmacywithmostorders = new HashMap<String, Integer>();
		
		quantaties_sold.put("paracetamol", 0);
		quantaties_sold.put("cegripe", 0);
		quantaties_sold.put("bepanthene", 0);
		quantaties_sold.put("dulcolax", 0);
		quantaties_sold.put("aspirina", 0);
		quantaties_sold.put("canesten", 0);
		quantaties_sold.put("trifene200", 0);
		quantaties_sold.put("actifed", 0);
		quantaties_sold.put("buscopan", 0);
		quantaties_sold.put("ibuprofen", 0);
		quantaties_sold.put("vibrocil", 0);
		quantaties_sold.put("rinialer", 0);
		quantaties_sold.put("avene_eau_thermale", 0);
		
		pharmacywithmostorders.put("Pharmacy0",0);
		pharmacywithmostorders.put("Pharmacy1",0);
		pharmacywithmostorders.put("Pharmacy2",0);
		pharmacywithmostorders.put("Pharmacy3",0);
		pharmacywithmostorders.put("Pharmacy4",0);
		

		//Behaviours
		
		addBehaviour(new MakeContact());
		addBehaviour(new Receiver());
		addBehaviour(new Results(this,5000));
		
	}
	
	
	private class MakeContact extends OneShotBehaviour {
		private int numPharmacies;
		
		public void action() {

			try {
				// Contact pharmacies
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Pharmacy");
				dfd.addServices(sd);

				DFAgentDescription[] result = DFService.search(this.myAgent, dfd);
				String[] Pharmacies; 
				Pharmacies = new String[result.length];
				numPharmacies = result.length;

				for (int i = 0; i < result.length; ++i) {
					Pharmacies[i] = result[i].getName().getLocalName(); 
					
					// Send message to all pharmacies request the coordinates
					ACLMessage mensagem = new ACLMessage(ACLMessage.CFP);
					AID receiver = new AID();
					receiver.setLocalName(Pharmacies[i]);
					mensagem.addReceiver(receiver);
					myAgent.send(mensagem); 
					}
				
				} catch (FIPAException e) {
				e.printStackTrace();
				}
			}
		}
	
	
	private class Receiver extends CyclicBehaviour { 
		
		private String Pharmacy_ID;
		private String Order;
		private double time;
		private String[] med,med1,med2,med3;
		private Integer[] stock_medicines2;
		private int xPharmacy, yPharmacy, xCustomer, yCustomer;
		private int min_dist = 1000;
		private String customerName;
		
		public void action() {
			
			ACLMessage msg = receive();
			if (msg != null) {
				
				//Receive coordinates from each pharmacy
				if (msg.getPerformative() == ACLMessage.INFORM) {
					Pharmacy_ID = msg.getSender().getLocalName();
					String[] coordinates = msg.getContent().split(","); 
					xPharmacy = Integer.parseInt(coordinates[0]);
					yPharmacy = Integer.parseInt(coordinates[1]);
						
					Class_Pharmacy a = new Class_Pharmacy(Pharmacy_ID,xPharmacy,yPharmacy);
					Pharmacies.add(0,a);
					
					}
				
				// Receives a request from the manager to check the stock
				else if (msg.getPerformative() == ACLMessage.PROPOSE) {
					int stock_medicine = 0;
					Integer[] stock_medicines = null;
					String[] req = msg.getContent().split(","); 
					Pharmacy_ID = req[1];
					Order = req[2];
					customerName = req[3];
					
					System.out.println(myAgent.getLocalName() + ": Receive a requeste from manager to verify the sotck on " + Pharmacy_ID + " for the product " + Order );
					System.out.print("\n");
					
					for (Class_Pharmacy f :Pharmacies) {
						if (f.getAID().equals(Pharmacy_ID)) {
						
							med1=f.getMedicines();
							
							for(int i=0; i<med1.length; i++) {
								
								if(med1[i].equals(Order)) {
									
									stock_medicines = f.getStock_medicines();
									stock_medicine = stock_medicines[i]; 
									
									}
								}
							}
						}
					
					// Send a message to the manager
					ACLMessage response = msg.createReply(); 
				    response.setPerformative(ACLMessage.ACCEPT_PROPOSAL); 
					
				    // If stock exist
					if (stock_medicine>0) {
						response.setContent("Yes" + "," + Pharmacy_ID + "," + Order);
						
						
						// Put in the record
						record.put(customerName, Order);
						
						// Add a unit for this product
						for (String product : quantaties_sold.keySet()) {
		
							if(product.equals(Order)) {
								quantaties_sold.put(product, quantaties_sold.get(product)+1);
								}
							}
						
						//E incrementa um valor a esta farmacia no arraylist farmaciaComMaisOrders
						for (String pharmacy : pharmacywithmostorders.keySet()) {
							
							if(pharmacy.equals(Pharmacy_ID)) {
								
								pharmacywithmostorders.put(pharmacy, pharmacywithmostorders.get(pharmacy)+1);
								}
							}
						
						System.out.println(myAgent.getLocalName() + ": Inform manager that there is  sotck in " + Pharmacy_ID + " for the product " + Order + " so it can be sold");
						System.out.print("\n");
						}
					
					
					// If there is no stock
					else if(stock_medicine==0) {
						response.setContent("No" + "," + Pharmacy_ID + "," + Order);
						
						System.out.println(myAgent.getLocalName() + ": Inform manager that is no stock" + Pharmacy_ID + " for the product " + Order);
						System.out.print("\n");
						
						}
					
					myAgent.send(response);
					}
				
				
				//Receive request from supplier to restock 
				else if (msg.getPerformative() == ACLMessage.INFORM_IF) {
					
					String[] rest = msg.getContent().split(","); 
					Order = rest[1];
					Pharmacy_ID = rest[3];
					
					System.out.println(myAgent.getLocalName() + ": Receive a information from supplier to restable the stock in " + Pharmacy_ID + " for the product " + Order);
					System.out.print("\n");
				
					for (Class_Pharmacy f : Pharmacies) {
						if (f.getAID().equals(Pharmacy_ID)) {
							med=f.getMedicines();
							for(int i=0; i<med.length; i++) {
								if(med[i].equals(Order)) {
									
									//altera a quantidade em stock (aumenta uma unidade) e altera o nº de vezes de reestabelecimento (aumenta uma unidade)
									f.changeStock(f.getStock_medicines()[i]+1,i); 
									f.changeNumberRestock(f.getRestock()[i]+1,i);
									
									//Confirma ao Fornecedor que reestabeleceu o stock
									ACLMessage resp = msg.createReply();
									resp.setPerformative(ACLMessage.CONFIRM); 
										
									System.out.println(myAgent.getLocalName() + ": Confirms to the Supplier that the stock as been restore in " + Pharmacy_ID + " for the product " + Order);
									System.out.print("\n");
									
									myAgent.send(resp);
									}
								}
							}
						}
					}
				
				// Receives a request from manager to reduce the stock in the inventory 
				else if (msg.getPerformative() == ACLMessage.AGREE) {
					String[] reduce_stock = msg.getContent().split(","); 
					Pharmacy_ID = reduce_stock[0];
					Order = reduce_stock[1];
					
					System.out.println(myAgent.getLocalName() + ": Reduce stock in " + Pharmacy_ID + " for the product product " + Order);
					System.out.print("\n");
					
					
					for (Class_Pharmacy f :Pharmacies) {
						if (f.getAID().equals(Pharmacy_ID)) {
							med3=f.getMedicines();
							for(int i=0; i<med3.length; i++) {
								if(med3[i].equals(Order)){
									
									//altera a quantidade em stock (diminui uma unidade) 
									f.changeStock(f.getStock_medicines()[i]-1,i);
									
									System.out.println(" Sotck atualizado: " + f.getStock_medicines()[i]);
									System.out.print("\n");
									}
								}
							}
						}
					}
				
				// Receive a request from manager to find a second pharmacy that has the product in stock
				else if (msg.getPerformative() == ACLMessage.REQUEST) {
					int stock_medicine2 = 0;
					String secondPharmacy = null ;
					String[] order = msg.getContent().split(","); 
					Pharmacy_ID = order[0];
					Order = order[1];
					xCustomer = Integer.parseInt(order[2]);
					yCustomer = Integer.parseInt(order[3]);
					customerName = order[4];
					
					System.out.println(myAgent.getLocalName() + ": Receive a request from manager to searh for the second closest pharmacy with stock for the product" + Order ); 
					System.out.print("\n");
					
					
					for (Class_Pharmacy f :Pharmacies) {
						if (!f.getAID().equals(Pharmacy_ID)) {
							med2=f.getMedicines(); 
							xPharmacy = f.getxPharmacy(); 
						    yPharmacy = f.getyPharmacy();
						    
						 
							for(int i=0; i<med2.length; i++) {
								if(med2[i].equals(Order)) {
									stock_medicines2 = f.getStock_medicines();
									stock_medicine2= stock_medicines2[i]; 
									
									// If ther is stock calculate the distance
									if (stock_medicine2>0) {
										int distance = (int) Math.sqrt(((Math.pow((xCustomer - xPharmacy), 2)) + (Math.pow((yCustomer - yPharmacy), 2))));
										if (distance < min_dist) {
											min_dist = distance;
											secondPharmacy = f.getAID();
											}
										}
									}
								}
							}
						}
					
					
					
					//If there is any stock in the other pharmacies
					if (secondPharmacy == null) {
						
						System.out.println(myAgent.getLocalName() + ": There is no stock in any pharmacy!!");
						System.out.print("\n");
						
						// Restock all pharmacies
						for (Class_Pharmacy f :Pharmacies) {
							med=f.getMedicines();
							for(int i=0; i<med.length; i++) {
								if(med[i].equals(Order)) {
									//altera a quantidade em stock (aumenta uma unidade) e altera o nº de vezes de reestabelecimento (aumenta uma unidade)
									f.changeStock(f.getStock_medicines()[i]+1,i); 
									f.changeNumberRestock(f.getRestock()[i]+1,i);
										
								}
							}
						}
						
						ACLMessage m = msg.createReply(); 
						m.setPerformative(ACLMessage.PROPAGATE); 
						
						System.out.println(myAgent.getLocalName() + ": Notify manager that the stock in all pharmacies as been restore so " + Pharmacy_ID + " can proceed with the order");
						System.out.print("\n");
								
						m.setContent("The closest pharmacy is" + "," + Pharmacy_ID);
						myAgent.send(m);
						

						// Put in record
						record.put(customerName, Order);
						
						
						
						for (String product : quantaties_sold.keySet()) {
							if(product.equals(Order)) {
								quantaties_sold.put(product, quantaties_sold.get(product)+1);}
							}
						
						
						for (String pharmacy : pharmacywithmostorders.keySet()) {
							
							if(pharmacy.equals(Pharmacy_ID)) {
								pharmacywithmostorders.put(pharmacy, pharmacywithmostorders.get(pharmacy)+1);}
							}
						}
					else {
						//Adiciona ao record 
						record.put(customerName, Order);
						
						// Add a unit for this product
						for (String product : quantaties_sold.keySet()) {
							if(product.equals(Order)) {
								quantaties_sold.put(product, quantaties_sold.get(product)+1);}
							}
						
						// Add a unit for this product in the pharmacy
						for (String pharmacy : pharmacywithmostorders.keySet()) {
							
							if(pharmacy.equals(secondPharmacy)) {
								pharmacywithmostorders.put(pharmacy, pharmacywithmostorders.get(pharmacy)+1);}
							}
						
						// Inform manager the closest pharmacy
						ACLMessage m = msg.createReply(); 
						m.setPerformative(ACLMessage.INFORM); 
						
						System.out.println(myAgent.getLocalName() + ": Informs manager that the second pharmacy with stock is " + secondPharmacy);
						System.out.print("\n");
								
						m.setContent("The second pharmacy is:" + "," + secondPharmacy);
						myAgent.send(m);
						min_dist = 1000;
						secondPharmacy = null;}
					}
				
				// Receive information from customer of the time 
				else { 
					
					String[] duration = msg.getContent().split(","); 
					customerName = msg.getSender().getLocalName();
					time = Double.parseDouble(duration[0]);
					times.put(customerName, time);
					}
				}
			}
		}
	
private class Results extends TickerBehaviour {
		private Integer[] stock_medicines4;
		public Results(Agent a, long timeout)
		{
			super(a, timeout);
		}
		protected void onTick() {
			
			ArrayList<String> most_sold_product = new ArrayList<String>();
			ArrayList<String> pharmacy_most_orders = new ArrayList<String>();
			int max = 0;
			int max1 = 0;
			
	
			
			for (String product : quantaties_sold.keySet()) {
				
				if(quantaties_sold.get(product)>max) {
					max = quantaties_sold.get(product);
					most_sold_product.clear();
					most_sold_product.add(product);}
				else if(quantaties_sold.get(product)==max){
					most_sold_product.add(product);}
				}
			
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
	
			System.out.print("┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                                        MOST SOLD PRODUCT                                                                                  │\n");
			System.out.print("├───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");


			for (int i = 0; i<most_sold_product.size();i++) {
				
				System.out.print("┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
				System.out.print("│                                                    "+ most_sold_product.get(i) + " " + " with a total equals to " + " " + max + "                                     │\n");
				System.out.print("├───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");
			
			}
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
			
		
			for (String pharmacy : pharmacywithmostorders.keySet()) {
				
				if(pharmacywithmostorders.get(pharmacy)>max1) {
					max1 = pharmacywithmostorders.get(pharmacy);
					pharmacy_most_orders.clear();
					pharmacy_most_orders.add(pharmacy);
					
				}
				else if(pharmacywithmostorders.get(pharmacy)==max1){
					pharmacy_most_orders.add(pharmacy);
					}
				}
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                                        PAHRAMCY WITH MOST SELLS                                                                                  │\n");
			System.out.print("├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");
			
			for (int i = 0; i<pharmacy_most_orders.size();i++) {
				
				System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
				System.out.print("│                                                    "+ pharmacy_most_orders.get(i) + " " + " with a total equals to " + " " + max1 + "                                        │\n");
				System.out.print("├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");
			
			}
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
		
			
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                                         Nº OF SELLS                                                                                              │\n");
			System.out.print("├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");
			
				System.out.print("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			
			
			System.out.print(
					"| FARMÁCIA | Total Vendas | Paracetamol | Cegripe | Bepanthene | Ducolax | Aspirina | Canestan | Trifene200 | Actifed | Buscopan | Ibuprofen | Vibrocil | Rinialer | Avene_eau_thermal|\n");
		
			pharmacysells(Pharmacies, "Pharmacy0");
			pharmacysells(Pharmacies, "Pharmacy1");
			pharmacysells(Pharmacies, "Pharmacy2");
			pharmacysells(Pharmacies, "Pharmacy3");
			pharmacysells(Pharmacies, "Pharmacy4");
			
				System.out.print("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
		
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
			// record 
			
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                              RECORD OF EACH COSTUMER                                                             │\n");
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                         COSTUMER                                         │                               PRODUCT                                 │\n");
			System.out.print("├──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┤\n");
				
						for (String customer : record.keySet()) {
							
			System.out.print("├──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┤\n");
			System.out.print("│                        "+customer+"                                      │                          " + record.get(customer) + "                  │\n");
			System.out.print("└──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┘\n");
							
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
			
						}
		
			// times
			
			
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                        TIME OF DELIREVY OF EACH COSTUMER                                                         │\n");
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                         COSTUMER                                         │                                TIME OF DELIVERY                       │\n");
			System.out.print("├──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┤\n");
			
			for (String customer : times.keySet()) {
				
			System.out.print("├──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┤\n");
			System.out.print("│                        "+customer+"                                      │                          " + times.get(customer) + "                  │\n");
			System.out.print("└──────────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────┘\n");
			}
			
			
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
		
			double soma=0;
			for (String customer : times.keySet()) {
				soma += times.get(customer);
				
			}
			String average_time = df2.format(soma/times.size());
			
			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                                                                                          │\n");
			System.out.print("                                            AVEREGE TIME OF DELIVERY "  + average_time +                                 " \n");
			System.out.print("│                                                                                                                          │\n");
			System.out.print("└──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘\n");


			
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
		
			

			System.out.print("┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			System.out.print("│                                                              STOCK IN PHARMACIES                                                                                                 │\n");
			System.out.print("├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤\n");
			
			
			System.out.print(
					"|  PHARMACY  |  Paracetamol | Cegripe |  Bepanthene |  Dulcolax  |  Aspirina  | Canesten | Trifene200 |  Actifed  | Buscopan | Ibuprofen |  Vibrocil | Rinialer| Avene_eau_thermale|\n");
		
			for (int i=0;i<	Pharmacies.size(); i++) {
				stock_medicines4 = Pharmacies.get(i).getStock_medicines(); 				
				System.out.print(
						"┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
				System.out.print("|  "+Pharmacies.get(i).getAID().toUpperCase()+" |    " + stock_medicines4[0] + "     |     "
						+ stock_medicines4[1] + "     |     "
						+ stock_medicines4[2] + "      |    " + stock_medicines4[3] 
						+ "    |    " + stock_medicines4[4] + "     |     " + stock_medicines4[5] + "    |    " 
						+ stock_medicines4[6]+ "     |     " + stock_medicines4[7] 
						+ "      |     " + stock_medicines4[8] + "     |     " + stock_medicines4[9] + "     |     " 
						+ stock_medicines4[10] + "    |     " + stock_medicines4[11] + "     |     " 
						+ stock_medicines4[12] + "    |     " 
						+ " \n");
				}
			System.out.print(
					"┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
			
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			
			
		}
}
	



private void pharmacysells(ArrayList<Class_Pharmacy> pharmacies2, String Pharmacy_ID) {
	
	
	int total_paracetamol = 0;
	int total_cegripe = 0;
	int total_bepanthene = 0;
	int total_ducolax= 0;
	int total_aspirina = 0;
	int total_canesten = 0;
	int total_trifene200 = 0;
	int total_actifed = 0;
	int total_buscopan = 0;
	int total_ibuprofen = 0;
	int total_vibrocil = 0;
	int total_rinialer = 0;
	int total_avene = 0;
	int total_num = 0;
	String name = null;
	
	
	for (Class_Pharmacy f : pharmacies2) {
		if(f.getAID().equals(Pharmacy_ID)) {
			
			name = f.getAID(); 
			Integer[] stock_pharmacy = f.getStock_medicines(); 
			Integer[] num_restock = f.getRestock();  
			
			total_paracetamol = 5 - stock_pharmacy[0] + num_restock[0];
			total_cegripe = 5 - stock_pharmacy[1] + num_restock[1];
			total_bepanthene = 1 - stock_pharmacy[2] + num_restock[2];
			total_ducolax = 1 - stock_pharmacy[3] + num_restock[3];
			total_aspirina = 5 - stock_pharmacy[4] + num_restock[4];
			total_canesten = 5 - stock_pharmacy[5] + num_restock[5];
			total_trifene200 = 1 - stock_pharmacy[6] + num_restock[6];
			total_actifed = 5 - stock_pharmacy[7] + num_restock[7];
			total_buscopan = 1 - stock_pharmacy[8] + num_restock[8];
			total_ibuprofen = 5 - stock_pharmacy[9] + num_restock[9];
			total_vibrocil = 5 - stock_pharmacy[10] + num_restock[10];
			total_rinialer = 5 - stock_pharmacy[11] + num_restock[11];
			total_avene = 1 - stock_pharmacy[12] + num_restock[12];
			
		
			total_num = total_paracetamol + total_cegripe + total_bepanthene + total_ducolax + total_aspirina
					+ total_canesten + total_trifene200 + total_actifed + total_buscopan + total_ibuprofen
					+ total_vibrocil + total_rinialer + total_avene;
			}
		
		
		}
	
	
				
		
	System.out.print(
			"┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐\n");
				
		System.out.print("| "+name.toUpperCase()+ " |     " + total_num + "      |   "
				+ total_paracetamol + "    |     " + total_cegripe + "     |     " + total_bepanthene 
				+ "    |   " + total_ducolax + "   |   " + total_aspirina + "    |    " 
				+ total_canesten+ "     |    " + total_trifene200 
				+ "     |    " + total_actifed + "     |     " + total_buscopan + "     |     " 
				+ total_ibuprofen + "     |    " + total_vibrocil + "   |     " 
				+ total_rinialer + "     |     " + total_avene + "   |   " 
				+ " \n");
		}
		}
	