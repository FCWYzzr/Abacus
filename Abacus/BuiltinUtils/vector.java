package Abacus.BuiltinUtils;

import java.util.Scanner;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class vector {
    protected final Vector<Double> actual;
    public vector(){
        actual = new Vector<>();
    }
    public vector(Vector<Double> act){
        actual = act;
    }
    public void input(String vec){
        actual.clear();
        Scanner sc = new Scanner(vec);
        sc.useDelimiter("[,| ]");
        while (sc.hasNextDouble())
            actual.add(Double.valueOf(sc.next()));
    }
    protected void push(double d){
        actual.add(d);
    }
    protected static void sameDim(vector v1,vector v2) throws Exception {
        if (v1.dim() == v2.dim()) return;
        throw new Exception("Vector dim not same");
    }

    public double at(int i){
        return actual.get(i);
    }

    public double sum(){
        double a=0;
        for (double b:actual)
            a += b;
        return a;
    }
    public double ave(){
        return sum()/ dim();
    }
    public double dim(){
        return actual.size();
    }
    public double var(){
        double a = ave(), s=0;
        for (double b:actual)
            s += (b-a)*(b-a);
        return s;
    }

    @Override
    public String toString() {
        return actual.toString().replace("[","(").replace("]",")");
    }

    public vector Multiply(double x){
        vector v = new vector();
        for (double d :actual)
            v.actual.add(d * x);
        return v;
    }

    public vector add(vector vec) throws Exception {
        sameDim(this, vec);
        vector v = new vector();
        for (int i = 0; i < dim(); i++)
            v.push(at(i) + vec.at(i));
        return v;
    }

    public vector subtract(vector vec) throws Exception {
        sameDim(this, vec);
        vector v = new vector();
        for (int i = 0; i < dim(); i++)
            v.push(at(i) - vec.at(i));
        return v;
    }

    public double innerMultiply(vector b) throws Exception {
        sameDim(this, b);
        double s = 0;
        for (int i = 0; i < b.dim(); i++)
            s += actual.get(i) * b.actual.get(i);
        return s;
    }

    public vector outerMultiply(vector b) throws Exception {
        sameDim(this, b);
        if (dim()!= 2 && dim()!= 3)
            throw new Exception("Outer Multiply only support dim 3 vector (dim 2 will be convert implicitly)");
        if (dim() == 2){
            Vector<Double> vec = new Vector<>();
            vec.add(0D);vec.add(0D);
            vec.add(at(0)*b.at(1) - b.at(0)*at(1));
            return new vector(vec);
        }
        else{
            Vector<Double> vec = new Vector<>();
            vec.add(at(1)*b.at(2) - at(2)*b.at(1));
            vec.add(at(2)*b.at(0) - at(0)*b.at(2));
            vec.add(at(0)*b.at(1) - at(1)*b.at(0));
            return new vector(vec);
        }
    }
    public Matrix matrixMultiply(vector b){
        Matrix matrix = new Matrix();
        Vector<Double> line;
        for (int i = 0; i < dim(); i++) {
            line = new Vector<>();
            for (int j = 0; j < dim(); j++)
                line.add(at(i)*b.at(j));
            matrix.push(line);
        }
        return matrix;
    }
    public double norm(int p){
        double s=0;
        for (double b:actual)
            s += pow(b, p);
        return pow(s, 1D/p);
    }

    public vector negative() {
        vector vec = new vector();
        for (Double d :actual)
            vec.push(-d);

        return vec;
    }

    @Override
    public boolean equals(Object b){
        if (!(b instanceof vector)) return false;
        if (this == b) return true;
        return this.actual.equals(((vector)b).actual);
    }

    public vector div(Double d) throws Exception {
        if (abs(d) < Double.MIN_VALUE)
            throw new Exception("zero divide");
        Vector<Double> vec = new Vector<>();
        for (Double dd :actual)
            vec.add(dd/d);
        return new vector(vec);
    }
}
