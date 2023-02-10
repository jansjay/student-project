package mscs.hms.controller.util;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import mscs.hms.model.constraints.PositiveNumberConstraint;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Dictionary;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ViewFieldUtil {
   static final Logger LOG = LoggerFactory.getLogger(ViewFieldUtil.class);
   public static List<ViewField> getPrivateFields(Class<?> classType) {
      List<ViewField> list = new ArrayList<>();
      Field[] entityFields = classType.getDeclaredFields();
      if (classType.getSuperclass() != null) {
         list.addAll(getPrivateFields(classType.getSuperclass()));
      }
      for (Field field : entityFields) {
          String modifiers = Modifier.toString(field.getModifiers());
          if(modifiers.contains("private") &&
             !modifiers.contains("static") &&
             !isTransient(field)){
              ViewField viewField = new ViewField();
              viewField.setName(field.getName());
              viewField.setType(field.getType().getSimpleName());
              viewField.setViewType(getViewType(field));
              viewField.setPlaceHolder(getPlaceHolder(field));
              viewField.setValidationMessage(getValidationMessage(field));
              viewField.setRequired(!viewField.getValidationMessage().isBlank());
              viewField.setIdColumn(isIdColumn(field));
              viewField.setGeneratedColumn(isGeneratedColumn(field));
              viewField.setAssociationField(isAssociationField(field));
              viewField.setManyAssociation(isManyToManyField(field));
              list.add(viewField);
          }
      }
      return list;
  }

  private static String getViewType(Field field){
      return switch (field.getType().getSimpleName()) {
          case "Integer", "Long", "Double" -> "number";
          case "Date" -> "date";
          default -> "text";
      };
  }

  private static String getPlaceHolder(Field field){
     String displayText = "";
      try{
         displayText = getDisplayName(field.getName());            
      }
      catch(Exception ex){
      }
      return displayText;
  }

  private static String getValidationMessage(Field field){
   StringBuilder placeHolder = new StringBuilder();
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       try{
         if(annotation.annotationType() == NotNull.class)
         {
            placeHolder.append(field.getAnnotation(NotEmpty.class).message()).append(" ");
         }
         if(annotation.annotationType() == NotEmpty.class)
         {
            placeHolder.append(field.getAnnotation(NotEmpty.class).message()).append(" ");
         }
         if(annotation.annotationType() == PositiveNumberConstraint.class)
         {
            placeHolder.append(field.getAnnotation(PositiveNumberConstraint.class).message()).append(" ");
         }          
       }
       catch(Exception ex){ }
   }
   return placeHolder.toString();
}

private static boolean isIdColumn(Field field){
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       if(annotation.annotationType() == Id.class)
         {
            return true;            
         }            
   }
   return false;
}

private static boolean isTransient(Field field){
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       if(annotation.annotationType() == Transient.class)
         {
            return true;            
         }            
   }
   return false;
}

private static boolean isGeneratedColumn(Field field){
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       if(annotation.annotationType() == GeneratedValue.class)
         {
            return true;            
         }            
   }
   return false;
}

private static boolean isAssociationField(Field field){
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       if(annotation.annotationType() == ManyToMany.class ||
          annotation.annotationType() == OneToOne.class ||
          annotation.annotationType() == ManyToOne.class ||
          annotation.annotationType() == OneToMany.class)
         {
            return true;            
         }            
   }
   return false;
}

private static boolean isManyToManyField(Field field){
   Annotation[] annotations = field.getAnnotations();
   for(Annotation annotation: annotations){
       if(annotation.annotationType() == ManyToMany.class ||
          annotation.annotationType() == OneToMany.class)
         {
            return true;            
         }            
   }
   return false;
}

   public static Object getFieldValue(Object ob, String fieldName) throws Exception{
      try{
         PropertyDescriptor pd = new PropertyDescriptor(fieldName, ob.getClass());
         Method getter = pd.getReadMethod();
         return getter.invoke(ob);
      }
      catch(Exception ex){
         LOG.error(String.format("Error fetching value for field %s",fieldName), ex);
         return null;
      }
   }   

   public Object getFieldValueFromSelectList(Object ob, String fieldName, Dictionary<String, Iterable<?>> lists) throws Exception{
      Object fieldValue = getFieldValue(ob, fieldName);
      if(fieldValue == null)
         return null;
      if(lists == null || lists.isEmpty() )
         return fieldValue;
      Enumeration<String> names = lists.keys();
      while(names.hasMoreElements()){
         if(fieldName.equals(names.nextElement())){
            List<?> values = (List<?>)lists.get(fieldName);
            for (Object object : values) {
               if(object.equals(fieldValue)){
                  return object;
               }
            }
         }
      }
      return fieldValue;
   }
   
   public static List<Object> getFieldSelectedList(Object ob, String fieldName, Dictionary<String, Iterable<?>> lists) throws Exception{
      List<Object> values = new ArrayList<>();
      Object fieldValue = getFieldValue(ob, fieldName);
      if(fieldValue == null)
         return values;
      if(lists == null || lists.isEmpty() )
         return values;
      Enumeration<String> names = lists.keys();
      while(names.hasMoreElements()){
         if(fieldName.equals(names.nextElement())){
            List<?> listValues = (List<?>)lists.get(fieldName);
            for (Object object : listValues) {
               for(Object selectedEntityObject : (List<?>)fieldValue)
               {
                  if(object.equals(selectedEntityObject)){
                     values.add(object);
                  }
               }
            }
         }
      }
      return values;
   }

   public static String getDisplayName(String fieldName) throws Exception{
      String displayName = fieldName.replaceAll("\\d+", "").replaceAll("(.)([A-Z])", "$1 $2");
      displayName = fieldName.substring(0, 1).toUpperCase() + displayName.substring(1);      
      return displayName;
   }

   public static String getDeleteCrudPath(String crudPathMain, String idFieldName, Object ob) throws Exception{
      return crudPathMain + "delete?" + idFieldName + "=" + getFieldValue(ob, idFieldName);
   }

   public static boolean isAssociationFieldAndListAvailableAndNotNullOrEmpty(ViewField field, Dictionary<String, Iterable<?>> lists, Object mappedObject) throws Exception{
      if( field.isAssociationField() && mappedObject != null && lists != null && lists.size() > 0){
         Enumeration<String> names = lists.keys();
         while(names.hasMoreElements()){
            if(field.getName().equals(names.nextElement())){
               return true;
            }
         }
      }
      return false;
   }
}
