import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.Color;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unchecked")
public final class BuiltinsForWindow {
    // create the Dictionary for colors
    public static Map<String, Color> colors = new HashMap<String, Color>();
    static {
        // fill the Dictionary for colors
        colors.put("red", Color.RED);
        colors.put("green", Color.GREEN);
        colors.put("blue", Color.BLUE);
        colors.put("yellow", Color.YELLOW);
        colors.put("cyan", Color.CYAN);
        colors.put("magenta", Color.MAGENTA);
        colors.put("orange", Color.ORANGE);
        colors.put("pink", Color.PINK);
        colors.put("white", Color.WHITE);
        colors.put("black", Color.BLACK);
        colors.put("gray", Color.GRAY);
        colors.put("lightGray", Color.LIGHT_GRAY);
        colors.put("darkGray", Color.DARK_GRAY);                
    }

    public static void create(Interpreter interpreter) {
        // fAddButton(mProperties): create a button with the given properties and add it to the JFrame
        // stored in the "value" property of the windowEnv Environment
        interpreter.windowEnv.define("fAddButton", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2); // first is the windowEnv Environment, second is the Map Environment
            }

            public Object call(Interpreter interpreter, List<Object> arguments) {
                // get the JFrame from the first argument
                JFrame frame = (JFrame) ((Environment) arguments.get(0)).lookup("value");
                // get the Map Environment from the second argument
                Environment mapEnv = (Environment) arguments.get(1);
                // get the properties from the first argument
                Map<String, Object> properties = (Map<String, Object>)mapEnv.lookup("value");
                // create the button
                JButton button = new JButton();                
                Environment buttonEnv = interpreter.makeObject(button, interpreter.buttonEnv, "Button");
                // apply the properties to the button
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    // get the property name
                    String property = entry.getKey();
                    // get the property value
                    Object value = entry.getValue();
                    
                    // define the property in the buttonEnv Environment
                    // this is useful when the user wants to change or access the button properties
                    buttonEnv.define(property, value);

                    // sCaption
                    if (property.equals("sCaption")) {
                        button.setText((String) value);
                        continue;
                    }
                    // sBackColor
                    if (property.equals("sBackColor")) {
                        button.setBackground(GUI.decodeColor(property));
                        continue;
                    }
                    // sForeColor
                    if (property.equals("sForeColor")) {
                        button.setForeground(GUI.decodeColor(property));
                        continue;
                    }
                    // sFont
                    if (property.equals("sFont")) {
                        button.setFont((Font) value);
                        continue;
                    }
                    // sToolTip
                    if (property.equals("sToolTip")) {
                        button.setToolTipText((String) value);
                        continue;
                    }
                    // nWidth: the value is a Double, so we need to convert it to an Integer
                    if (property.equals("nWidth")) {                        
                        // we use setSize()
                        button.setSize(((Double) value).intValue(), button.getHeight());
                        continue;
                    }
                    // nHeight: the value is a Double, so we need to convert it to an Integer
                    if (property.equals("nHeight")) {                        
                        // we use setSize()
                        button.setSize(button.getWidth(), ((Double) value).intValue());
                        continue;
                    }
                    // nLeft: the value is a Double, so we need to convert it to an Integer
                    if (property.equals("nLeft")) {
                        // we use setBounds()
                        button.setBounds(((Double) value).intValue(), button.getY(), button.getWidth(), button.getHeight());
                        continue;
                    }
                    // nTop: the value is a Double, so we need to convert it to an Integer
                    if (property.equals("nTop")) {
                        // we use setBounds()
                        button.setBounds(button.getX(), ((Double) value).intValue(), button.getWidth(), button.getHeight());
                        continue;
                    }
                    // pOnClick: here we need to execute the Runtime function stored in value.                    
                    if (property.equals("pOnClick")) {
                        // the value is a Statement object, so we need to cast it
                        Runtime.Function function = (Runtime.Function) value;
                        // now when the button is clicked, we execute the code stored in the value
                        button.addActionListener(e -> {
                            // execute the code
                            List<Object> args = new ArrayList<Object>();
                            args.add(buttonEnv);
                            function.call(interpreter, args);
                        });
                        continue;
                    }                    
                }                
                // add the button to the JFrame
                frame.add(button);

                // return the button
                return interpreter.makeObject(button, interpreter.buttonEnv, "Button");
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }
}
