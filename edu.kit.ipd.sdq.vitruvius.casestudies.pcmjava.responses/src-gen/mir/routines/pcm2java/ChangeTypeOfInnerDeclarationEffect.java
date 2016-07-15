package mir.routines.pcm2java;

import edu.kit.ipd.sdq.vitruvius.casestudies.pcmjava.responses.pcm2java.Pcm2JavaHelper;
import edu.kit.ipd.sdq.vitruvius.dsls.response.runtime.AbstractEffectRealization;
import edu.kit.ipd.sdq.vitruvius.dsls.response.runtime.ResponseExecutionState;
import edu.kit.ipd.sdq.vitruvius.dsls.response.runtime.structure.CallHierarchyHaving;
import edu.kit.ipd.sdq.vitruvius.framework.meta.change.feature.reference.UpdateSingleValuedNonContainmentEReference;
import java.io.IOException;
import mir.routines.pcm2java.RoutinesFacade;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.emftext.language.java.types.TypeReference;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.InnerDeclaration;

@SuppressWarnings("all")
public class ChangeTypeOfInnerDeclarationEffect extends AbstractEffectRealization {
  public ChangeTypeOfInnerDeclarationEffect(final ResponseExecutionState responseExecutionState, final CallHierarchyHaving calledBy, final UpdateSingleValuedNonContainmentEReference<DataType> change) {
    super(responseExecutionState, calledBy);
    				this.change = change;
  }
  
  private UpdateSingleValuedNonContainmentEReference<DataType> change;
  
  private EObject getCorrepondenceSourceNewJavaDataType(final UpdateSingleValuedNonContainmentEReference<DataType> change) {
    DataType _newValue = change.getNewValue();
    return _newValue;
  }
  
  protected void executeRoutine() throws IOException {
    getLogger().debug("Called routine ChangeTypeOfInnerDeclarationEffect with input:");
    getLogger().debug("   UpdateSingleValuedNonContainmentEReference: " + this.change);
    
    org.emftext.language.java.classifiers.Class newJavaDataType = getCorrespondingElement(
    	getCorrepondenceSourceNewJavaDataType(change), // correspondence source supplier
    	org.emftext.language.java.classifiers.Class.class,
    	(org.emftext.language.java.classifiers.Class _element) -> true, // correspondence precondition checker
    	null);
    
    preprocessElementStates();
    new mir.routines.pcm2java.ChangeTypeOfInnerDeclarationEffect.EffectUserExecution(getExecutionState(), this).executeUserOperations(
    	change, newJavaDataType);
    postprocessElementStates();
  }
  
  private static class EffectUserExecution extends AbstractEffectRealization.UserExecution {
    @Extension
    private RoutinesFacade effectFacade;
    
    public EffectUserExecution(final ResponseExecutionState responseExecutionState, final CallHierarchyHaving calledBy) {
      super(responseExecutionState);
      this.effectFacade = new mir.routines.pcm2java.RoutinesFacade(responseExecutionState, calledBy);
    }
    
    private void executeUserOperations(final UpdateSingleValuedNonContainmentEReference<DataType> change, final org.emftext.language.java.classifiers.Class newJavaDataType) {
      EObject _newAffectedEObject = change.getNewAffectedEObject();
      final InnerDeclaration innerDeclaration = ((InnerDeclaration) _newAffectedEObject);
      DataType _newValue = change.getNewValue();
      final TypeReference newDataTypeReference = Pcm2JavaHelper.createTypeReference(_newValue, newJavaDataType);
      this.effectFacade.callChangeInnerDeclarationType(innerDeclaration, newDataTypeReference);
    }
  }
}
