@import static example.JteContext.*

@param example.SimpleBean bean
!{bean.setStringProperty("hello");}
${bean.stringProperty}