package agents;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Pharmacy extends Agent {
	
	int xPharmacy, yPharmacy;
	

	protected void setup() {
		super.setup();
		
		
		System.out.print("-----------------------------------------##Starting Pharmacy##----------------------------------------------");
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\n");
		

		
		// The coordinates are chosen at random
		Random rand = new Random();
		xPharmacy = rand.nextInt(100);
		yPharmacy = rand.nextInt(100);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Pharmacy");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		
		// Behaviour
		addBehaviour(new Receiver());
	}
	
	private class Receiver extends CyclicBehaviour { 
		
		private int xCustomer, yCustomer;
		private String Order;
		private String customerName;
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				// Receive message from customer and send the coordinates
				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
				
					System.out.println(myAgent.getLocalName() + ": I received a request from " + msg.getSender().getLocalName() + " to send my coordinates. ");
					System.out.print("\n");
					
					
					AID provider = msg.getSender();
					ACLMessage answer = new ACLMessage(ACLMessage.INFORM); // Respond to costumer
					answer.addReceiver(provider);
					answer.setContent(xPharmacy + "," + yPharmacy); // Send the coordinates
					myAgent.send(answer);
				}
				
				// Receive message from interface and send the coordinates
				else if (msg.getPerformative() == ACLMessage.CFP) {
					
					System.out.println(myAgent.getLocalName() + ": I received a request from Interface to send my coordinates. ");
					System.out.print("\n");
					
					ACLMessage response = msg.createReply(); 
					response.setPerformative(ACLMessage.INFORM); //Respond to interface
					response.setContent(xPharmacy + "," + yPharmacy); // Send the coordinates
					myAgent.send(response);
				}
				
				// Receive message from the costumer with the order
				else if (msg.getPerformative() == ACLMessage.REQUEST) {
					String[] OrderAndCoordinates = msg.getContent().split(","); 
					xCustomer = Integer.parseInt(OrderAndCoordinates[1]);
					yCustomer = Integer.parseInt(OrderAndCoordinates[2]);
					Order = OrderAndCoordinates[3];
					customerName = msg.getSender().getLocalName();
		
					
					// Confirm order to customer
					System.out.println(myAgent.getLocalName() + ": Confirms the order to " + customerName + " for the product " + Order);
					System.out.print("\n");
					
					ACLMessage resp = msg.createReply();
					resp.setPerformative(ACLMessage.CONFIRM);
					resp.setContent("Pharmacy: Confirms to the customer their order of the product" + "," + Order);
					myAgent.send(resp);
					
					
					
					//Envia pedido ao Gestor para ele verificar o stock
					AID receiver = new AID();
					receiver.setLocalName("Manager");
					ACLMessage messagem = new ACLMessage(ACLMessage.REQUEST);
					messagem.setContent("Send order to manager:" + "," + customerName + "," + xCustomer + "," + yCustomer + "," + xPharmacy + "," + yPharmacy + "," + Order);
					messagem.addReceiver(receiver);

					System.out.println(myAgent.getLocalName() + ": Sends the order to the manager to see if it has stock");
					System.out.print("\n");
					
					myAgent.send(messagem);
				}
				// Receive confirmation that stock exists
				else if (msg.getPerformative() == ACLMessage.CONFIRM){
					
					System.out.println(myAgent.getLocalName() + ": Receive confirmation from the Manager that there is stock and  will contact the shipping company");
					System.out.print("\n");
					
					// Contact the shipping company to deliver to the customer
					System.out.println(myAgent.getLocalName() + ": Shipping company please send the product " + Order + " to " + customerName + " to the following coordinates " +  xCustomer + " and " +  yCustomer);
					System.out.print("\n");
					
					AID receiver = new AID();
					receiver.setLocalName("ShippingCompany");
					ACLMessage messagem = new ACLMessage(ACLMessage.PROPOSE);
					messagem.addReceiver(receiver);
					messagem.setContent(customerName + "," + xCustomer + "," + yCustomer);
					myAgent.send(messagem);
					
					
					// Warns the manager to reduce the product on the inventory
					
					System.out.println(myAgent.getLocalName() + ": Manager please reduce the product " + Order + " in the inventory");
					System.out.print("\n");
					
					ACLMessage resp = msg.createReply();
					resp.setPerformative(ACLMessage.AGREE); 
					resp.setContent("Reduce stock in the Pharmacy" + "," + myAgent.getLocalName() + "," + "for the product" + "," + Order);
					myAgent.send(resp);
					
				}

				// Receive message from the manager asking to send the order to another pharmacy
				else if (msg.getPerformative() == ACLMessage.INFORM){
						
						String[] Order_second = msg.getContent().split(","); 
						Order = Order_second[0];
						xCustomer = Integer.parseInt(Order_second[1]);
						yCustomer = Integer.parseInt(Order_second[2]);
						customerName = Order_second[3];
	
						System.out.println(myAgent.getLocalName() + ": Receive a request from the Manager to send the product " + Order + " to " + customerName);
						System.out.print("\n");
						
						
						AID receiver = new AID();
						receiver.setLocalName("Shipping_Company");
						ACLMessage messagem = new ACLMessage(ACLMessage.PROPOSE);
						messagem.addReceiver(receiver);
						messagem.setContent(customerName + "," + xCustomer + "," + yCustomer);
						myAgent.send(messagem);
						
						System.out.println(myAgent.getLocalName() + ": Manager please reduce the product "+ Order + " in the inventory");
						System.out.print("\n");
						
						
						ACLMessage resp = msg.createReply();
						resp.setPerformative(ACLMessage.AGREE); 
						resp.setContent("Reduce stock in the pharmacy" + "," + myAgent.getLocalName() + "," + " for the product" + "," + Order);
						myAgent.send(resp);
						}
				
				
				else if (msg.getPerformative() == ACLMessage.AGREE){
			
					System.out.println(myAgent.getLocalName() + ": The stock has been restored");
					System.out.print("\n");
						}
			
				
				else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
					
					System.out.println(myAgent.getLocalName() + ": Receive confirmation from the shipping company to delivery the order to" + customerName);
					System.out.print("\n");
					}
			
			
				}
			
			else {
				block();
			}
			}
		}
	}


				
				
					