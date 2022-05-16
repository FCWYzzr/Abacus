package Abacus.Syntax.Token;


import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;


public class TokenArray {
    Vector<Element> arr;
    public TokenArray(String filename) {
        arr = new Vector<>();
        StringBuilder sb = new StringBuilder(255);
        String name;
        int ch;
        boolean f=true;
        try (FileReader FR = new FileReader(filename)) {
            ch = FR.read();
            while(ch != -1){
                if (f)
                 switch (ch){
                     case '\n':
                        arr.add(Element.Stop);
                        ch = FR.read();
                     break;
                     case '{':
                         arr.add(Element.BlockBegin);
                         ch = FR.read();
                         break;
                     case '}':
                         arr.add(Element.BlockEnd);
                         ch = FR.read();
                         break;
                     case '?':
                         arr.add(Element.IfBegin);
                         ch = FR.read();
                         break;
                     case '(':
                         if (arr.lastElement().getType() == ElementType.BIF)
                         arr.add(Element.paramBegin);
                         break;
                     default:
                        if(Rule.initial(ch)){
                            if (Rule.type != '\'' && Rule.type != '\"')sb.append((char)ch);
                            ch = FR.read();
                            f = false;
                        }
                        else
                            ch = FR.read();
                }
                else if (Rule.next(ch)) {
                    sb.append((char) ch);
                    ch = FR.read();
                }
                else{
                    name = sb.toString();
                    sb.delete(0,sb.length());
                    switch (Rule.type){
                        case 'i':
                            if (Rule.isReserved(name))
                                switch (name){
                                    case "is":
                                        arr.add(new Element(
                                                ElementType.Operation,
                                                "is"
                                        ));
                                        break;
                                    case "Y":
                                        arr.add(new Element(
                                                ElementType.Signal,
                                                "if true"
                                        ));
                                        break;
                                    case "N":
                                        arr.add(new Element(
                                                ElementType.Signal,
                                                "if false"
                                        ));
                                        break;

                                    default:
                                        arr.add(new Element(
                                                ElementType.Command,
                                                name
                                        ));
                                }
                            else if (Rule.isType(name))
                                arr.add(new Element(
                                        ElementType.Type,
                                        name
                                ));
                            else if (Rule.isBIF(name))
                                arr.add(new Element(
                                        ElementType.BIF,
                                        name
                                ));
                            else
                                arr.add(new Element(
                                        ElementType.Identifier,
                                        name
                                ));
                            break;
                        case 'n':
                            arr.add(new Element(
                                    ElementType.Number,
                                    name
                            ));
                            break;
                        case 'o':
                            arr.add(new Element(
                                    ElementType.Operation,
                                    name
                            ));
                            break;
                        case '\'':
                        case '\"':
                            if (arr.lastElement().type.equals(ElementType.Literal))
                                arr.lastElement().ExtendName(name);
                            else
                                arr.add(new Element(
                                        ElementType.Literal,
                                        name
                                ));
                            ch = FR.read();
                            break;
                    }
                    f=true;
                }
            }
            if (sb.length() > 0){
                name = sb.toString();
                switch (Rule.type){
                    case 'i':
                        if (Rule.isReserved(name))
                        switch (name){
                            case "is":
                                arr.add(new Element(
                                        ElementType.Operation,
                                        "is"
                                ));
                                break;
                            case "Y":
                                arr.add(new Element(
                                        ElementType.Signal,
                                        "if true"
                                ));
                                break;
                            case "N":
                                arr.add(new Element(
                                        ElementType.Signal,
                                        "if false"
                                ));
                                break;
                            default:
                                arr.add(new Element(
                                        ElementType.Command,
                                        name
                                ));
                        }
                        else if (Rule.isType(name))
                            arr.add(new Element(
                                    ElementType.Type,
                                    name
                            ));
                        else
                            arr.add(new Element(
                                    ElementType.Identifier,
                                    name
                            ));
                        break;
                    case 'n':
                        arr.add(new Element(
                                ElementType.Number,
                                name
                        ));
                        break;
                    case 'o':
                        arr.add(new Element(
                                ElementType.Operation,
                                name
                        ));
                        break;
                    case '\'':
                    case '\"':
                        if (arr.lastElement().type.equals(ElementType.Literal))
                            arr.lastElement().ExtendName(name);
                        else
                            arr.add(new Element(
                                    ElementType.Literal,
                                    name.substring(1)
                            ));
                        break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
        arr.add(Element.End);
    }
    public Vector<Element> getArray(){
        return arr;
    }
}
