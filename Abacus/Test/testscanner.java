package Abacus.Test;

import Abacus.BuiltinUtils.Matrix;
import Abacus.BuiltinUtils.vector;

import java.util.Scanner;

public class testscanner {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        Matrix matrix = new Matrix();
        try {
            matrix.input(scanner);
            System.out.println(matrix.determinant());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
