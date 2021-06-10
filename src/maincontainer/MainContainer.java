package maincontainer;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
	
	Runtime rt;
	ContainerController container;
	
	public static void main(String[] args) {
		MainContainer a = new MainContainer();
		
		a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
		
		int n;
		int limit_pharmacies = 5;
		int limit_customers = 5;
		
		
		//Start Agents Farmacias
		for (n = 0; n < limit_pharmacies; n++) {
			try {
				a.startAgentInPlatform("Pharmacy" + n, "agents.Pharmacy");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		
		a.startAgentInPlatform("Interface", "agents.Interface");
		a.startAgentInPlatform("Manager", "agents.Manager");
		a.startAgentInPlatform("ShippingCompany", "agents.ShippingCompany");
		a.startAgentInPlatform("Supplier", "agents.Supplier");
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//Start Agents Cidadaos
		for (n = 0; n < limit_customers; n++) {
			try {
				a.startAgentInPlatform("Customer" + n, "agents.Customer");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
		
	}
	
	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		
		this.rt = Runtime.instance();
		
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		
		ContainerController container = rt.createAgentContainer(profile);
		return container;
		
	}
	
	public void initMainContainerInPlatform(String host, String port, String containerName) {
		
		this.rt = Runtime.instance();
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");
		
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);
	}
	
	public void startAgentInPlatform(String name, String classpath) {
		try {
			AgentController ac = container.createNewAgent( name, classpath, new Object[0]);
			
			ac.start();
			
		} catch (Exception e)  {
			e.printStackTrace();
		}
	}
	

	

}