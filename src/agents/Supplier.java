package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Supplier extends Agent {
		protected void setup() {
			super.setup();
			System.out.print("-----------------------------------------::Starting Supplier::----------------------------------------------");
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
			addBehaviour(new Receiver());
	}

	private class Receiver extends CyclicBehaviour { 
		
		private String Pharmacy_ID;
		private String Order;
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				
				// Request from manager to restore stock
				if (msg.getPerformative() == ACLMessage.REQUEST) { 
					
					String[] req = msg.getContent().split(","); 
					Pharmacy_ID = req[0];
					Order = req[1];
					
					System.out.println(myAgent.getLocalName() + ": Receive order from the Manger and notify Interface to restore stock at " + Pharmacy_ID + " for the product " + Order);
					System.out.print("\n");
					
					// Notify interface to restore stock
					AID receiver_2 = new AID();
					receiver_2.setLocalName("Interface");
					ACLMessage messagem_2 = new ACLMessage(ACLMessage.INFORM_IF);
					messagem_2.addReceiver(receiver_2);
					messagem_2.setContent("The stock for the product" + "," + Order + "," + " for the " + "," + Pharmacy_ID + "," + " must be restore");
					myAgent.send(messagem_2);
					}
				
			
				else if (msg.getPerformative() == ACLMessage.CONFIRM) { 
			
					System.out.println(myAgent.getLocalName() + ": Receive confirmation that the stock for the procuct " + Order + " as been restore in " + Pharmacy_ID);
					System.out.print("\n");
					
					//Tells manager that the stock as been restore
					AID receiver = new AID();
					receiver.setLocalName("Manager");ACLMessage messagem = new ACLMessage(ACLMessage.CONFIRM);
					messagem.addReceiver(receiver);
					messagem.setContent("The stock for the product " + Order + " as been restore");
					myAgent.send(messagem);
					}
				}
			}
	}
}


	