package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Manager extends Agent {
	protected void setup() {
		super.setup();
		
		System.out.print("-----------------------------------------==Starting Manager==----------------------------------------------");
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\n");
		
		addBehaviour(new Receiver());
}

private class Receiver extends CyclicBehaviour { 
		
		private int xCustomer, yCustomer, xPharmacy, yPharmacy;
		private String Pharmacy_ID, secondPharmacy, Pharmacy;
		private String Order;
		private String customerName;
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				
				// Receive a request from pharmacy to verify stock 
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					
					String[] request = msg.getContent().split(",");
					Pharmacy_ID = msg.getSender().getLocalName();
					customerName = request[1];
					xCustomer = Integer.parseInt(request[2]);
					yCustomer = Integer.parseInt(request[3]);
					xPharmacy = Integer.parseInt(request[4]);
					yPharmacy = Integer.parseInt(request[5]);
					Order = request[6];
					
				
					System.out.println(myAgent.getLocalName() + ": Receive a request from " + Pharmacy_ID + " to check the stock for the product " + Order );
					System.out.print("\n");
					
					System.out.println(myAgent.getLocalName() + ": Ask Interface to check if there is stock in " + Pharmacy_ID + " for the product " + Order );
					System.out.print("\n");
					
					
					//  Ask Interface to check if there is stock
					
					AID receiver = new AID();
					receiver.setLocalName("Interface");
					ACLMessage messagem = new ACLMessage(ACLMessage.PROPOSE);
					messagem.addReceiver(receiver);
					messagem.setContent(" Is there stock? " + "," + Pharmacy_ID + "," + Order + "," + customerName);
					myAgent.send(messagem);
					
				}
				
				// Receive a menssage with the asnwer of Interface about stock 
				else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					String[] answer_stock = msg.getContent().split(","); 
					Pharmacy_ID = answer_stock[1];
					Order = answer_stock[2];
					
					// If there is stock
					if(answer_stock[0].equals("Yes")) {
						
						System.out.println(myAgent.getLocalName() + ": " + Pharmacy_ID + " There is stock for the product " + Order );
						System.out.print("\n");
						
						// Confirms to the pharmacy that exists stcok for the product and can proceed with the sale
						AID receiver = getAID(Pharmacy_ID);
						ACLMessage sms = new ACLMessage(ACLMessage.CONFIRM);
						sms.addReceiver(receiver);
						sms.setContent("There is stock, the order can be sent to the customer" + "," + customerName);
						myAgent.send(sms);
					
						
					
					}
					
					// If there is no stock
					else if(answer_stock[0].equals("No")) {
						System.out.println(myAgent.getLocalName() + ": Ask the supplier to restore stock at Pharmacy " + Pharmacy_ID + " the product " + Order);
						System.out.print("\n");
						
						
						// Request the supplier to restore stock at Pharmacy 
						AID receiver = new AID();
						receiver.setLocalName("Supplier");
						ACLMessage sms = new ACLMessage(ACLMessage.REQUEST); 
						sms.addReceiver(receiver);
						sms.setContent(Pharmacy_ID + "," + Order);
						myAgent.send(sms);
						
						System.out.println(myAgent.getLocalName() + ": Ask the interface for the second closest pharmacy with stock for the product " + Order );
						System.out.print("\n");
						
						
						// Gives the data to the interface so it can calculate the closest pharmacy
						AID receiver_2 = new AID();
						receiver_2.setLocalName("Interface");
						ACLMessage messagem_2 = new ACLMessage(ACLMessage.REQUEST); 
						messagem_2.addReceiver(receiver_2);
						messagem_2.setContent(Pharmacy_ID + "," + Order + "," + xCustomer + "," + yCustomer + "," + customerName);
						myAgent.send(messagem_2);
						}
					}
				//Recebe da interface a 2ª farmácia mais próxima
				else if (msg.getPerformative() == ACLMessage.INFORM) {	
					String[] pharmacy2 = msg.getContent().split(","); 
					secondPharmacy = pharmacy2[1];
					
					System.out.print("\n");
					System.out.println(myAgent.getLocalName() + ": Informs the second closest pharmacy " + secondPharmacy + " to send the order");
					System.out.print("\n");
					
					// Informs the second farmaci to send the order
					AID receiver = new AID();
					receiver.setLocalName(secondPharmacy);
					ACLMessage messagem = new ACLMessage(ACLMessage.INFORM);
					messagem.addReceiver(receiver);
					messagem.setContent(Order + "," + xCustomer + "," + yCustomer + "," + customerName);
					myAgent.send(messagem);
					}
				
				// Receive confirmation from the supplier that stock has been restored
				else if (msg.getPerformative() == ACLMessage.CONFIRM) { 
					
					System.out.println(myAgent.getLocalName() + ": Notify " + Pharmacy_ID + " the the stock for the product " + Order + " as been restored");
					System.out.print("\n");
					
					// Notify pharmacy 
					AID receiver = new AID();
					receiver.setLocalName(Pharmacy_ID);
					ACLMessage sms = new ACLMessage(ACLMessage.AGREE);
					sms.addReceiver(receiver);
					sms.setContent("Your stock as been restored");
					myAgent.send(sms);
					}
				
				
				// Receive a request from the pharmacy to reduce its stock after it has made the sale
				else if (msg.getPerformative() == ACLMessage.AGREE) { 
					String[] req = msg.getContent().split(","); 
					Pharmacy_ID = req[1];
					Order = req[3];
					
					System.out.println(myAgent.getLocalName() + ": Asks Interface to reduce stock in the " + Pharmacy_ID + " for the product " + Order);
					System.out.print("\n");
					
					// Asks Interface
					AID receiver = new AID();
					receiver.setLocalName("Interface");
					ACLMessage sms = new ACLMessage(ACLMessage.AGREE); 
					sms.addReceiver(receiver);
					sms.setContent(Pharmacy_ID + "," + Order);
					myAgent.send(sms);
					}
			
				else {
					String[] pharmacy = msg.getContent().split(","); 
					Pharmacy = pharmacy[1];
					
					System.out.print("\n");
					System.out.println(myAgent.getLocalName() + ": Inform closest pahramcy " + Pharmacy + " to send the order");
					System.out.print("\n");
					
					
					AID receiver = new AID();
					receiver.setLocalName(Pharmacy);
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					message.addReceiver(receiver);
					message.setContent(Order + "," + xCustomer + "," + yCustomer + "," + customerName);
					myAgent.send(message);
					}
				
					}
		
		else {
			block();
			}
			}
}
} 

	