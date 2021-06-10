package agents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Customer extends Agent {
	private String name;
	int xCustomer, yCustomer, numPharmacies;
	private ArrayList<String> products;
	private String requiredProduct;
	private double start, end, total_time;
	
	protected void setup() {
		super.setup();
		start = (Calendar.getInstance()).getTimeInMillis();  //start counting the time
		
		System.out.print("-----------------------------------------||Starting Cidadão||----------------------------------------------");
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\n");
		//The coordinates are chosen at random
		Random rand = new Random();
		xCustomer = rand.nextInt(100);
		yCustomer = rand.nextInt(100);
		
		products = new ArrayList<String>();
		
		products.add("paracetamol");
		products.add("cegripe");
		products.add("bepanthene");
		products.add("dulcolax");
		products.add("aspirina");
		products.add("canesten");
		products.add("trifene200");
		products.add("actifed");
		products.add("buscopan");
		products.add("ibuprofen");
		products.add("vibrocil");
		products.add("rinialer");
		products.add("avene_eau_thermale");

		requiredProduct = "";
		
		//Behaviours
		this.addBehaviour(new CoordinateRequest());
		this.addBehaviour(new Receiver()); 
		
	}
	
	private class CoordinateRequest extends OneShotBehaviour {
		public void action() {

			try {
				//Contact all pharmacies
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Pharmacy");
				dfd.addServices(sd);

				DFAgentDescription[] result = DFService.search(this.myAgent, dfd);
				
				if (result.length > 0) {
					for (int i = 0; i < result.length; ++i) {
						// Agent Found
						DFAgentDescription dfd1 = result[i];
						AID provider = new AID();
						provider.setLocalName(dfd1.getName().getLocalName());
						numPharmacies = result.length;
					
						//Send a message to each one to ask for their coordinates
						ACLMessage mensagem = new ACLMessage(ACLMessage.SUBSCRIBE);
						mensagem.addReceiver(provider);
						myAgent.send(mensagem);
					}}}
			
				catch (FIPAException fe) {
					fe.printStackTrace();
				}}
		} 
	
	
	private class Receiver extends CyclicBehaviour {
		private int xOrigin, yOrigin;
		private int min_dist = 1000;
		private String closestPharmacy; 
		private int TotalPharmacies = 0;

		
		public void action() {
			ACLMessage msg = receive();
			name = myAgent.getLocalName();
			
			if (msg != null) {
				// Receive message from the pharmacy
				if (msg.getPerformative() == ACLMessage.INFORM) { // Receives coordinates
					String[] coordinates = msg.getContent().split(","); // Separate the coordinates and assign the value to the variables defined initially
					TotalPharmacies++; 
					xOrigin = Integer.parseInt(coordinates[0]);
					yOrigin = Integer.parseInt(coordinates[1]);  
					
					// Calculates the distance of each pharmacie and costumer
					int distance = (int) Math.sqrt(((Math.pow((xCustomer - xOrigin), 2)) + (Math.pow((yCustomer - yOrigin), 2))));
					
					System.out.println(distance);
					System.out.print("\n");
					
					if (min_dist > distance) {
						closestPharmacy = msg.getSender().getLocalName();
						min_dist = distance;
						
					}
					if (TotalPharmacies == numPharmacies) { // All pharmacies have already been processed
							
						Random randomizer = new Random(); 
						String random = products.get(randomizer.nextInt(products.size())); // Choose a random product from the list
						
						System.out.println(name + ": Choose " + closestPharmacy + " with a distance equal to " + min_dist);
						System.out.print("\n");
						
						ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST); // Send a message to the pharmacy to request the product chosen
						AID receiver = new AID();
						receiver.setLocalName(closestPharmacy);
						mensagem.addReceiver(receiver);
						mensagem.setContent("Customer:" + "," + xCustomer + "," + yCustomer + "," + random);
						myAgent.send(mensagem);
						TotalPharmacies = 0; 
						min_dist = 1000;
						closestPharmacy = null;
						}
				}
				
				//Receive pharmacy confirmation for the order
				else if (msg.getPerformative() == ACLMessage.CONFIRM) { 
					String[] order = msg.getContent().split(",");
					requiredProduct = order[1];
					name = myAgent.getLocalName();
					System.out.println(name + ": I received confirmation for the product " + requiredProduct);
					System.out.print("\n");
					
					}
				
				//Receives the product from the shipping company
				else if (msg.getPerformative() == ACLMessage.AGREE) {
						name = myAgent.getLocalName();
						System.out.println(name + ": I received my order.");
						System.out.print("\n");
						
						end = (Calendar.getInstance()).getTimeInMillis();
						total_time = end - start; // Calculate delivery time
						
						ACLMessage mensagem = new ACLMessage(ACLMessage.PROPAGATE);  // Informs interface the time of delivery
						AID receiver = new AID();
						receiver.setLocalName("Interface");
						mensagem.addReceiver(receiver);
						mensagem.setContent(total_time + "," + name);
						myAgent.send(mensagem);
						System.out.println(name + "The time between placing the order and delivering was " + total_time);
						System.out.print("\n");
						
						myAgent.doDelete(); 
				}
				
				}
			}
		}
	
	protected void takeDown() {
		super.takeDown();
		System.out.println("Ending Customer");
		
	}
	}
				