package mir.responses.responses5_1ToJava.pcm2java;

import edu.kit.ipd.sdq.vitruvius.dsls.response.runtime.AbstractResponseRealization;
import edu.kit.ipd.sdq.vitruvius.framework.contracts.interfaces.UserInteracting;
import edu.kit.ipd.sdq.vitruvius.framework.meta.change.EChange;
import edu.kit.ipd.sdq.vitruvius.framework.meta.change.feature.reference.UpdateSingleValuedNonContainmentEReference;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;

@SuppressWarnings("all")
class ChangeOperationRequiredRoleInterfaceResponse extends AbstractResponseRealization {
  public ChangeOperationRequiredRoleInterfaceResponse(final UserInteracting userInteracting) {
    super(userInteracting);
  }
  
  public static Class<? extends EChange> getExpectedChangeType() {
    return UpdateSingleValuedNonContainmentEReference.class;
  }
  
  private boolean checkChangeProperties(final UpdateSingleValuedNonContainmentEReference<OperationInterface> change) {
    EObject changedElement = change.getOldAffectedEObject();
    // Check model element type
    if (!(changedElement instanceof OperationRequiredRole)) {
    	return false;
    }
    
    // Check feature
    if (!change.getAffectedFeature().getName().equals("requiredInterface__OperationRequiredRole")) {
    	return false;
    }
    return true;
  }
  
  public boolean checkPrecondition(final EChange change) {
    if (!(change instanceof UpdateSingleValuedNonContainmentEReference<?>)) {
    	return false;
    }
    UpdateSingleValuedNonContainmentEReference typedChange = (UpdateSingleValuedNonContainmentEReference)change;
    if (!checkChangeProperties(typedChange)) {
    	return false;
    }
    getLogger().debug("Passed precondition check of response " + this.getClass().getName());
    return true;
  }
  
  public void executeResponse(final EChange change) {
    UpdateSingleValuedNonContainmentEReference<OperationInterface> typedChange = (UpdateSingleValuedNonContainmentEReference<OperationInterface>)change;
    final org.palladiosimulator.pcm.repository.OperationInterface oldValue = typedChange.getOldValue();
    if (oldValue != null) {
    	typedChange.setOldValue(new mir.responses.mocks.org.palladiosimulator.pcm.repository.OperationInterfaceContainerMock(oldValue, typedChange.getOldAffectedEObject()));
    }
    mir.routines.pcm2java.ChangeOperationRequiredRoleInterfaceEffect effect = new mir.routines.pcm2java.ChangeOperationRequiredRoleInterfaceEffect(this.executionState, this, typedChange);
    effect.applyRoutine();
  }
}
