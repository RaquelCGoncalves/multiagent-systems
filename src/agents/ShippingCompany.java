package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ShippingCompany extends Agent {
	protected void setup() {
		super.setup();
		
		System.out.print("-----------------------------------------»»Starting Shipping Company»»----------------------------------------------");
		System.out.print("\n");
		System.out.print("\n");
		System.out.print("\n");
		addBehaviour(new Receiver());
	}
	
	private class Receiver extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = receive();
			if(msg!=null) {
				
		
				
				if(msg.getPerformative() == ACLMessage.PROPOSE) {
					String[] coordinates = msg.getContent().split(",");
					ACLMessage resp = msg.createReply();
					resp.setContent("Yes"); 
					resp.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					
					System.out.println(myAgent.getLocalName() + ": Receive a proposal from the pharmacy to transport the order to " + coordinates[0]);
					System.out.print("\n");
					
					
					try { 
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					AID receiver = new AID();
					receiver.setLocalName(coordinates[0]);
					ACLMessage messagem = new ACLMessage(ACLMessage.AGREE);
					messagem.addReceiver(receiver);
					messagem.setContent("Your order has been delivered!");
					myAgent.send(messagem);
					
				}
				
			}
			
			block();
		}
	}



}
