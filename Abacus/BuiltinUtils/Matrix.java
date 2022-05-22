package Abacus.BuiltinUtils;


import java.util.Scanner;
import java.util.Vector;

import static java.lang.Math.*;

public class Matrix {

    int rows,cols;
    Vector<Vector<Double>> actual;
    public Matrix(){
        actual = new Vector<>();
    }

    public Matrix(int p){
        Vector<Double> v;
        actual = new Vector<>();
        rows = p;
        cols = p;
        for (int i = 0; i < p; i++) {
            v = new Vector<>();
            for (int j = 0; j < p; j++)
                v.add(j == i?1D:0D);
            actual.add(v);
        }
    }

    public Matrix(Matrix matrix) {
        actual = new Vector<>(matrix.actual);
        rows = matrix.rows;
        cols = matrix.cols;
    }

    public final Vector<Integer> dim(){
        Vector<Integer> v = new Vector<>();
        v.add(rows);
        v.add(cols);
        return v;
    }
    private static void sameDim(Matrix m1, Matrix m2) throws Exception {
        if(m1.dim().equals(m2.dim())) return;
        throw new Exception("Matrix dim not fit");
    }

    public Matrix add(Matrix m2) throws Exception {
        sameDim(this,m2);
        Matrix m3 = new Matrix();
        for (int i = 0; i < rows; i++)
            m3.push(rowAt(i).add(m2.rowAt(i)).actual);
        return m3;
    }

    public Matrix subtract(Matrix m2) throws Exception{
        sameDim(this,m2);
        Matrix m3 = new Matrix();
        for (int i = 0; i < rows; i++)
            m3.push(rowAt(i).subtract(m2.rowAt(i)).actual);
        return m3;
    }

    protected void push(Vector<Double> vec){
        cols = vec.size();
        actual.add(vec);
        ++ rows;
    }
    public void input(Scanner scanner) throws Exception {
        actual.clear();
        int tmp=0;
        rows=cols=0;
        actual = new Vector<>();
        Scanner sc;
        Vector<Double> vec;
        while (scanner.hasNextLine()){
            vec = new Vector<>();
            sc = new Scanner(scanner.nextLine());
            if (!sc.hasNext())
                break;
            sc.useDelimiter("[,| ]");
            while (sc.hasNext()) {
                vec.add(Double.valueOf(sc.next()));
                ++ tmp;
            }
            if(cols == 0) cols = tmp;
            else if (cols != tmp)
                throw new Exception("Not a valid matrix: columns' count not same: "+cols +" -> "+tmp);
            actual.add(vec);
            ++ rows;
            tmp=0;
        }
    }
    public vector rowAt(int i){
        return new vector(new Vector<>(actual.get(i)));
    }
    public vector colAt(int i){
        Vector<Double> vec = new Vector<>();
        for (int j = 0; j < rows; j++)
            vec.add(actual.get(j).get(i));
        return new vector(vec);
    }
    public double At(int i,int j){
        return actual.get(i).get(j);
    }

    public double norm(int p){
        double s=0;
        for (Vector<Double> v:actual)
            for (Double d:v)
                s += Math.pow(abs(d), p);
        return Math.pow(s, 1D/p);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Vector<Double> v:actual) {
            for (Double d : v)
                sb.append(String.format("%1.2f ", d));
            sb.append('\n');
        }
        return sb.toString();
    }

    public Matrix toUpTriangle(){
        Matrix mat = new Matrix(this);
        for (int i = 0; i <rows-1; i++){
            for (int j = i+1; j <rows; j++) {
                //System.out.printf("proceed %d with %d%n", j,i);
                mat.actual.set(j, proceed1(mat.rowAt(i), mat.rowAt(j)).actual);
            }
        }
        return mat;
    }

    public Matrix toDownTriangle(){
        Matrix mat = new Matrix(this);
        for (int i = rows-1; i > 0; --i){
            for (int j = i-1; j >= 0; --j) {
                //System.out.println(String.format("proceed %d with %d", i,j));
                mat.actual.set(j, proceed2(mat.rowAt(j), mat.rowAt(i)).actual);
            }
        }
        return mat;
    }

    public Matrix toDiagonal(){
        return toUpTriangle().toDownTriangle();
    }

    public Double determinant(){
        Matrix UD = toUpTriangle();
        double x=1;
        int i=0;
        while (i != UD.rows && i != UD.cols)
            x *= UD.At(i,i++);
        return x;
    }
    public Matrix negative(){
        Matrix mat = new Matrix();
        for (int i = 0; i < rows; i++)
            mat.push(rowAt(i).negative().actual);
        return mat;
    }



    private static vector proceed1(vector template, vector toBeProceed){
        int pos = 0;
        while(pos < template.dim() && template.at(pos) == 0 && toBeProceed.at(pos) == 0)
            ++pos;
        if(pos < template.dim()) {
            try {
                return toBeProceed.subtract(template.Multiply(toBeProceed.at(pos)/template.at(pos)));
            } catch (Exception ignored) {return toBeProceed;}
        }
        else
            return toBeProceed;
    }
    private static vector proceed2(vector template, vector toBeProceed){
        int pos = (int) (template.dim()-1);
        while(pos >= 0 && template.at(pos) == 0 && toBeProceed.at(pos) == 0)
            -- pos;
        if(pos >= 0) {
            try {
                return toBeProceed.subtract(template.Multiply(toBeProceed.at(pos)/template.at(pos)));
            } catch (Exception ignored) {return toBeProceed;}
        }
        else
            return toBeProceed;
    }


    public Matrix Multiply(Matrix m2) throws Exception {
        if (cols != m2.rows)
            throw new Exception("cols != rows, can not multiply");
        Matrix matrix = new Matrix();
        Vector<Double> line;
        for (int i = 0; i < rows; i++) {
            line = new Vector<>();
            for (int j = 0; j < m2.cols; j++) {
                line.add(rowAt(i).innerMultiply(m2.colAt(j)));
            }
            matrix.push(line);
        }
        return matrix;
    }

    public double Minor(int I, int J)throws Exception{
        if (!(0 <= I && I < rows) && !(0 <= J && J < cols))
            throw new Exception("Invalid Index");
        Matrix matrix = new Matrix();
        Vector<Double> line;
        for (int i = 0; i < rows; i++)
            if (i != I) {
                line = new Vector<>();
                for (int j = 0; j < cols; j++)
                    if (j != J)
                        line.add(At(i,j));
                matrix.push(line);
            }
        return matrix.determinant();
    }
    public double complementalMinor(int i, int j) throws Exception {
        return Minor(i,j) * ((i+j)%2==0?1:-1);
    }

    public Matrix adjointMatrix() throws Exception{
        Matrix matrix = new Matrix();
        Vector<Double> line;
        for (int i = 0; i < rows; i++) {
            line = new Vector<>();
            for (int j = 0; j < cols; j++)
                line.add(complementalMinor(i,j));
            matrix.push(line);
        }
        return matrix;
    }
    public Matrix inverseMatrix() throws Exception {
        if (determinant() == 0)
            throw new Exception("singular matrix");
        return adjointMatrix().div(determinant());
    }
    public Matrix div(double d) throws Exception {
        Matrix m = new Matrix();
        for (int i = 0; i < rows; i++) {
            m.push(rowAt(i).div(d).actual);
        }
        return m;
    }

    public Matrix pow(int p)throws Exception{
        if (cols != rows)
            throw new Exception("cols != rows, can not power");
        Matrix mat = new Matrix(rows),
               tmp = new Matrix(this);
        for (; p != 0; p >>= 1) {
            if ((p&1) != 0)
                mat = mat.Multiply(tmp);
            tmp.Multiply(tmp);
        }
        return mat;
    }
}
