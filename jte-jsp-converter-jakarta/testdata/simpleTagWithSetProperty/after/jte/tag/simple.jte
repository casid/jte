@import static example.JteContext.*
@import example.SimpleBean

@param SimpleBean bean
!{bean.setStringProperty("hello");}
${bean.stringProperty}