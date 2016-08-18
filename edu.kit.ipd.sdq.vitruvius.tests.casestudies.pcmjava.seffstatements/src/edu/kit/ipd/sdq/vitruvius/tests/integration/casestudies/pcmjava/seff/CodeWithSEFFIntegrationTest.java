package edu.kit.ipd.sdq.vitruvius.tests.integration.casestudies.pcmjava.seff;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.text.edits.InsertEdit;
import org.emftext.language.java.members.Method;
import org.junit.Assert;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.InternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

import edu.kit.ipd.sdq.vitruvius.codeintegration.tests.CodeIntegrationTest;
import edu.kit.ipd.sdq.vitruvius.framework.contracts.datatypes.CorrespondenceInstance;
import edu.kit.ipd.sdq.vitruvius.framework.contracts.meta.correspondence.Correspondence;
import edu.kit.ipd.sdq.vitruvius.framework.contracts.util.datatypes.CorrespondenceInstanceUtil;
import edu.kit.ipd.sdq.vitruvius.tests.casestudies.pcmjava.transformations.util.CompilationUnitManipulatorHelper;

public class CodeWithSEFFIntegrationTest extends CodeIntegrationTest {

    private static final String TEST_BUNDLE_NAME_JSCIENCE = "edu.kit.ipd.sdq.vitruvius.tests.casestudies.pcmjava.seffstatements";
    private static final String TEST_PROJECT_NAME_JSCIENCE = "Calculator-JScience";
    private static final String SRC_AND_MODEL_FOLDER_JSCIENCE = "exampleCode/Calculator-JScience";

    @Override
    protected String getTestProjectName() {
        return CodeWithSEFFIntegrationTest.TEST_PROJECT_NAME_JSCIENCE;
    }

    @Override
    protected String getTestBundleName() {
        return CodeWithSEFFIntegrationTest.TEST_BUNDLE_NAME_JSCIENCE;
    }

    @Override
    protected String getTestSourceAndModelFolder() {
        return CodeWithSEFFIntegrationTest.SRC_AND_MODEL_FOLDER_JSCIENCE;
    }

    @Override
    protected void assertStandardCodeIntegrationTest() throws Throwable {
        final CorrespondenceInstance ci = this.getCorrespondenceInstance();
        // check that we have created correspondences to method for all AbstractActions
        final Repository repo = this.getRepository(ci);
        final Iterable<EObject> iterable = () -> repo.eAllContents();
        final List<AbstractAction> abstractActions = StreamSupport.stream(iterable.spliterator(), false)
                .filter(eObject -> eObject instanceof AbstractAction).map(eObject -> (AbstractAction) eObject)
                .collect(Collectors.toList());
        Assert.assertNotEquals("No abstract action found", 0, abstractActions.size());
        abstractActions.forEach(abstractAction -> this.assertCorrespondenceToMethod(abstractAction, ci));

    }

    @Test
    public void testCodeIntegrationAndChangesOnSEFF() throws Throwable {
        super.testStandardCodeIntegration();

        // create new method (testMethod) with some dummy internal statements in CalculatorTool
        final String dummyStatements = "int j = 0;\nfor(int i =0; i < 10; i++)\n{\nj +=i;\n}\n";
        final String methodCode = "private void testMethod(){ \n" + dummyStatements + "\n}";
        CompilationUnitManipulatorHelper.addMethodToCompilationUnit("CalculatorTool", methodCode,
                this.getTestProject());

        // call the new method from the main method
        final ICompilationUnit compUnit = CompilationUnitManipulatorHelper
                .findICompilationUnitWithClassName("CalculatorTool", this.getTestProject());
        final int offset = CompilationUnitManipulatorHelper.getOffsetToInsertInMethodInCompilationUnit(compUnit,
                "main");
        final String code = "testMethod();";
        final InsertEdit insertEdit = new InsertEdit(offset, code);
        CompilationUnitManipulatorHelper.editCompilationUnit(compUnit, insertEdit);
        Thread.sleep(2500);

        // assert that we got one new InternalCallAction and the remainder stayed the same
        final CorrespondenceInstance<Correspondence> ci = this.getCorrespondenceInstance();
        final Repository repo = this.getRepository(ci);
        final BasicComponent bc = (BasicComponent) repo.getComponents__Repository().stream()
                .filter(comp -> comp.getEntityName().contains("CalculatorTool")).findAny().get();
        final List<ResourceDemandingSEFF> seffs = bc.getServiceEffectSpecifications__BasicComponent().stream()
                .map(seff -> (ResourceDemandingSEFF) seff).collect(Collectors.toList());
        final ResourceDemandingSEFF resourceDemandingSEFF = seffs.stream()
                .filter(seff -> seff.getDescribedService__SEFF().getEntityName().equals("main")).findAny().get();
        final AbstractAction newAbstractAction = resourceDemandingSEFF.getSteps_Behaviour().get(1);
        Assert.assertTrue("the newly created abstract action needs to be an instance of InternalCallAction",
                newAbstractAction instanceof InternalCallAction);
        final InternalCallAction ica = (InternalCallAction) newAbstractAction;
        final List<AbstractAction> behaviourOfNewInternalBehavior = ica.getCalledResourceDemandingInternalBehaviour()
                .getSteps_Behaviour();
        Assert.assertEquals("the new internal behaviour needs to have thre actions", 3,
                behaviourOfNewInternalBehavior.size());
        Assert.assertTrue("The first action needs to be a startAction",
                behaviourOfNewInternalBehavior.get(0) instanceof StartAction);
        Assert.assertTrue("The second action needs to be a InternalAction",
                behaviourOfNewInternalBehavior.get(1) instanceof InternalAction);
        Assert.assertTrue("The third action needs to be a StopAction",
                behaviourOfNewInternalBehavior.get(2) instanceof StopAction);

    }

    private Repository getRepository(final CorrespondenceInstance<Correspondence> ci) {
        return CorrespondenceInstanceUtil.getAllEObjectsOfTypeInCorrespondences(ci, Repository.class).iterator().next();
    }

    private void assertCorrespondenceToMethod(final AbstractAction abstractAction,
            final CorrespondenceInstance<Correspondence> ci) {
        final Set<EObject> correspondingEObjects = CorrespondenceInstanceUtil.getCorrespondingEObjects(ci,
                abstractAction);
        if (abstractAction instanceof StartAction || abstractAction instanceof StopAction) {
            // assert empty correspondence for start and stop action
            Assert.assertEquals("StartAction or StopAction " + abstractAction + " should not have a correspondence", 0,
                    correspondingEObjects.size());
            return;
        }
        // assert unique correspondence for all other abstract actions
        Assert.assertEquals("expected unique Correspondene for AbstractAction " + abstractAction, 1,
                correspondingEObjects.size());
        // unique correspondence to method
        final EObject eObject = correspondingEObjects.iterator().next();
        Assert.assertTrue("unique corresponding object should be a method", eObject instanceof Method);

    }

}
