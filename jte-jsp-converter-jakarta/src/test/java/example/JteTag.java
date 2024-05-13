package example;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import jakarta.servlet.jsp.tagext.TagSupport;


public class JteTag extends TagSupport implements DynamicAttributes {

   @Override
   public void setDynamicAttribute( String s, String s1, Object o ) throws JspException {

   }

   public void setJte( String name ) {

   }
}
