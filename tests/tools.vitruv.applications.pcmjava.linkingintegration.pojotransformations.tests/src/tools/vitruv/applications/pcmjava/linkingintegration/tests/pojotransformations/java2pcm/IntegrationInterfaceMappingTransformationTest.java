package tools.vitruv.applications.pcmjava.linkingintegration.tests.pojotransformations.java2pcm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.palladiosimulator.pcm.repository.OperationInterface;

import tools.vitruv.applications.pcmjava.linkingintegration.tests.CodeIntegrationTestCBSNamespace;

public class IntegrationInterfaceMappingTransformationTest extends Java2PcmPackageIntegrationMappingTransformationTest{

	private static final String INTEGRATED_INTERFACE_NAME = "IntegratedInterface";
	private static final String NAME_OF_NOT_INTEGRATED_INTERFACE = "NotIntegratedInterface";

	@Test
	public void addInterfaceInIntegratedArea() throws Throwable{
		OperationInterface opInterface = this.createInterfaceInPackage(CodeIntegrationTestCBSNamespace.PACKAGE_NAME_OF_DISPLAY_COMPONENT, INTEGRATED_INTERFACE_NAME, false);
		
		assertEquals(opInterface, null);
		assertMessage(1, "Created class or interface in area with integrated object");
	}
	
	@Test
	public void addInterfaceInNonIntegratedArea() throws Throwable{
		this.testUserInteractor.addNextSelections(SELECT_NOTHING_DECIDE_LATER);
		createPackageWithPackageInfo(NAME_OF_NOT_INTEGRATED_PACKAGE);
		
		this.testUserInteractor.addNextSelections(0);
		OperationInterface opInterface = createInterfaceInPackage(NAME_OF_NOT_INTEGRATED_PACKAGE,NAME_OF_NOT_INTEGRATED_INTERFACE, true);
		
		this.assertOperationInterface(opInterface.getRepository__Interface(), opInterface, NAME_OF_NOT_INTEGRATED_INTERFACE);
		this.assertNoUserInteractingMessage();
	}
}
