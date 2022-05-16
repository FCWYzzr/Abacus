package Abacus.Syntax.Token;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import sun.awt.geom.AreaOp;

public class Element {
    final ElementType type;
    String Name;



    private Element(char ch){
        switch(ch){
            case 's':
                this.type = ElementType.StopPoint;
                Name = "End";
                break;
            case 'e':
                this.type = ElementType.StopPoint;
                Name = "EOF";
                break;

            case 'b':
                this.type = ElementType.Block;
                Name = "Block";
                break;

            case '{':
                this.type = ElementType.Block_begin;
                Name = "{";
                break;

            case '}':
                this.type = ElementType.Block_end;
                Name = "}";
                break;
            case '?':
                this.type = ElementType.Signal;
                Name = "if";
                break;
            case 'l':
                this.type = ElementType.Signal;
                Name = "loop";
                break;

            case 'p':
                this.type = ElementType.FunctionParamBegin;
                Name = "(..";
                break;
            default:
                throw new ValueException("error char: "+ch);
        }

    }

    public Element(ElementType type, String name) {

        this.type = type;
        Name = name;
    }

    public String getName() {
        return Name;
    }
    public void ExtendName(String Name){
        this.Name += Name;
    }

    @Override
    public String toString() {
        return Name+'('+type+')';
    }

    public ElementType getType() {
        assert type != null;
        return type;
    }

    public static Element Stop = new Element('s');
    public static Element End = new Element('e');
    public static Element Block = new Element('b');
    public static Element BlockBegin = new Element('{');
    public static Element BlockEnd = new Element('}');
    public static Element Loop = new Element('l');
    public static Element IfBegin = new Element('?');
    public static Element paramBegin = new Element('p');
}