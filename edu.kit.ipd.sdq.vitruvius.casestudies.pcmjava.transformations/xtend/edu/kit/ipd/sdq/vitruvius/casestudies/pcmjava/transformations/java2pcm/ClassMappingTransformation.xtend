package edu.kit.ipd.sdq.vitruvius.casestudies.pcmjava.transformations.java2pcm

import de.uka.ipd.sdq.pcm.core.entity.InterfaceProvidingRequiringEntity
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory
import edu.kit.ipd.sdq.vitruvius.casestudies.pcmjava.transformations.JaMoPPPCMMappingTransformationBase
import edu.kit.ipd.sdq.vitruvius.casestudies.pcmjava.transformations.JaMoPPPCMNamespace
import edu.kit.ipd.sdq.vitruvius.casestudies.pcmjava.transformations.JaMoPPPCMUtils
import edu.kit.ipd.sdq.vitruvius.framework.meta.correspondence.Correspondence
import edu.kit.ipd.sdq.vitruvius.framework.meta.correspondence.CorrespondenceFactory
import edu.kit.ipd.sdq.vitruvius.framework.meta.correspondence.EObjectCorrespondence
import edu.kit.ipd.sdq.vitruvius.framework.transformationexecuter.TransformationUtils
import java.util.ArrayList
import java.util.HashSet
import java.util.Set
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.util.EcoreUtil
import org.emftext.language.java.classifiers.Class
import org.emftext.language.java.classifiers.ClassifiersFactory
import org.emftext.language.java.modifiers.Public

/**
 * Maps a JaMoPP class to a PCM Components or System. 
 * Triggered when a CUD operation on JaMoPP class is detected.
 */
class ClassMappingTransformation extends JaMoPPPCMMappingTransformationBase {
	
	private static val Logger logger = Logger.getLogger(ClassMappingTransformation.simpleName)
	
	override getClassOfMappedEObject() { 
		return Class
	}
	
	/**
	 * sets the name correspondece for JaMoPP-class names and PCM-entityName Attribut
	 */
	override setCorrespondenceForFeatures() { 
		val classNameAttribute = TransformationUtils::getAttributeByNameFromEObject(JaMoPPPCMNamespace.JAMOPP_ATTRIBUTE_NAME,  ClassifiersFactory.eINSTANCE.createClass)
		val componentNameAttribute = TransformationUtils::getAttributeByNameFromEObject(JaMoPPPCMNamespace.PCM_ATTRIBUTE_ENTITY_NAME, RepositoryFactory.eINSTANCE.createBasicComponent )
		featureCorrespondenceMap.put(classNameAttribute, componentNameAttribute)
	}
	
	/**
	 * if a class is add in java we have to check whether this is a class that implements a component or a system
	 * This is checked as follows:
	 * The class does not represents the implementing class when:
	 * 		i) the class is not public, or
	 * 		ii) the component or system corresponding to the package of the class already has an implementing class
	 * The class represents the implementing class, when
	 * 		iii) the class is public, and
	 * 		iv) the component or system corresponding to the package of the class does not has an implementing class yet, and
	 * 		v) a) the class name contians the name of the package,or 
	 * 		v) b) the user says it is the implementing class 
	 */
	override createEObject(EObject eObject) {
		val jaMoPPClass = eObject as Class
		// i) + iii)
		val hasPublicAnnotation = jaMoPPClass.annotationsAndModifiers.filter[aam|aam instanceof Public].size 
		if(0 == hasPublicAnnotation){
			//nothing to do: class is not public
			return null;
		}
		//ii) + iv)
		val jaMoPPPackage = JaMoPPPCMUtils.getContainingPackageFromCorrespondenceInstance(jaMoPPClass, correspondenceInstance)
		if(null == jaMoPPPackage){
			//nothing to do - no corresponding package found
			logger.info("jaMoPPPackage is null for class" + jaMoPPClass)
			return null
		}
		// get corresponding component or system (here we ask for InterfaceProvidingRequiringEntity cause components 
		// and systems are both InterfaceProvidingRequiringEntitys) 
		val pcmComponentOrSystem = correspondenceInstance.claimUniqueCorrespondingEObjectByType(jaMoPPPackage, InterfaceProvidingRequiringEntity)
		val correspondencesForPCMCompOrSystem = correspondenceInstance.getAllCorrespondences(pcmComponentOrSystem)
		if(null == correspondencesForPCMCompOrSystem){
			// TODO: package currently does not correspond to a PCM Component or system
			// In this case we currently do nothing 
			// TODO: implement user interaction where user is ask whether the package should correspond to a component/system 
			return null
		}
		val hasClassCorrespondence = correspondencesForPCMCompOrSystem.
				filter[correspondence| correspondence instanceof Class].size
		if( 0 < hasClassCorrespondence){
			//nothing to do --> component or system already has corresponding class
			return null
		}
		//iii) and iv) --> already checked above 
		//--> the component corresponding to the package of the class does not have a correspoding pcmComponentOrSystem yet
		//v) a)
		var isCorrespondingClass = false
		if(jaMoPPClass.name.contains(pcmComponentOrSystem.entityName)){
			//the class is the implementing class
			isCorrespondingClass =  true
		}//TODO: v) b) else if (askuser)
		
		//the corresponding instance should be created in the next method that is called by the framework
		if(isCorrespondingClass){
			return pcmComponentOrSystem.toArray
		} 
		return null
	}

	/**
	 * called after createEObject is called
	 * creates the correspondences and returns the TransformationChangeResult object containing the PCM element that should be saved
	 */	
	override createNonRootEObjectInList(EObject affectedEObject, EReference affectedReference, EObject newValue, int index, EObject[] newCorrespondingEObjects) {
		val newClass = newValue as Class
		val compilationUnit = newClass.containingCompilationUnit
		if(newCorrespondingEObjects.nullOrEmpty){
			//nothing todo --> return emtpy TransformationChangeResult
			return TransformationUtils.createEmptyTransformationChangeResult
		}
		val parentCorrespondences = correspondenceInstance.getAllCorrespondences(newValue)
		var Correspondence parentCorrespondence = null
		if(null != parentCorrespondences){
			parentCorrespondence = parentCorrespondences.iterator.next
		}
		val EObjectCorrespondence class2Component = CorrespondenceFactory.eINSTANCE.createEObjectCorrespondence
		class2Component.setElementA(newCorrespondingEObjects.get(0))
		class2Component.setElementB(newClass) 
		class2Component.setParent(parentCorrespondence)
		val EObjectCorrespondence compilationUnit2Component = CorrespondenceFactory.eINSTANCE.createEObjectCorrespondence
		compilationUnit2Component.setElementA(newCorrespondingEObjects.get(0))
		compilationUnit2Component.setElementB(compilationUnit)
		compilationUnit2Component.setParent(parentCorrespondence)
		compilationUnit2Component.dependentCorrespondences.add(class2Component)
		class2Component.dependentCorrespondences.add(compilationUnit2Component)
		correspondenceInstance.addSameTypeCorrespondence(class2Component)
		correspondenceInstance.addSameTypeCorrespondence(compilationUnit2Component)
		return TransformationUtils.createTransformationChangeResultForEObjectsToSave(newCorrespondingEObjects)
	}

	
	/**
	 * Remove class: 
	 * Check if class has corresponding elements and
	 * Remove CorrespondingInstance for class and compilation unit
	 * Also removes basicComponent on PCM.
	 * If the class is not the only class in the package ask the user whether to remove the component
	 * and package and all classes 
	 */
	override removeEObject(EObject eObject) {
		val jaMoPPClass = eObject as Class
		val correspondences = correspondenceInstance.getAllCorrespondences(jaMoPPClass);
		var eObjectsToDelete = new ArrayList<EObject>()
		if(null != correspondences && 0 < correspondences.size ){
			val classifiersInSamePackage = jaMoPPClass.containingCompilationUnit.classifiersInSamePackage
			if(null != classifiersInSamePackage && 1 < classifiersInSamePackage.size){
				//TODO: ask user whether to remove also this classifiers
				var boolean removeAllClassifiers = false;
				if(removeAllClassifiers){
					eObjectsToDelete.addAll(classifiersInSamePackage)
				}
				eObjectsToDelete.add(jaMoPPClass.containingCompilationUnit)
				correspondences.forEach[correspondingObj|EcoreUtil.remove(correspondingObj)]
				eObjectsToDelete.addAll(correspondences)				
			}
			correspondenceInstance.removeAllCorrespondences(jaMoPPClass)
		}	
		return null
	}

	/**
	 * we do not really need the method deleteNonRootEObjectInList in InterfaceMappingTransformation because the deletion of the 
	 * object has already be done in removeEObject.
	 * We just return an empty TransformationChangeResult 
	 */
	override deleteNonRootEObjectInList(EObject affectedEObject, EReference affectedReference, EObject oldValue, int index, EObject[] oldCorrespondingEObjectsToDelete) {
		return TransformationUtils.createEmptyTransformationChangeResult
	}
	
	/**
	 * if the class is renamed rename the corresponding objects on PCM side 
	 */
	override updateSingleValuedEAttribute(EObject affectedEObject, EAttribute affectedAttribute, Object oldValue, Object newValue) {
		val EStructuralFeature affectedPCMFeature = featureCorrespondenceMap.claimValueForKey(affectedAttribute)
		var Set<EObject> ret = new HashSet<EObject>()
		try{
			var correspondingPCMObjects = correspondenceInstance.claimCorrespondingEObjectsByType(affectedEObject, RepositoryComponent)
			for(correspondingPCMObject : correspondingPCMObjects){
				correspondingPCMObject.eSet(affectedPCMFeature, newValue)
				ret.add(correspondingPCMObject)				
			}
		}catch(RuntimeException rt){
			logger.trace(rt)
		}
		return TransformationUtils.createTransformationChangeResultForEObjectsToSave(ret)
	}
	
	override createNonRootEObjectSingle(EObject affectedEObject, EReference affectedReference, EObject newValue, EObject[] newCorrespondingEObjects) {
		logger.warn("method should not be called for ClassMappingTransformation transformation")
		return null
	}
	
}