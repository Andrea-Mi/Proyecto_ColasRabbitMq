package Puntaje_de_Arreglo;

public class PuntajedeArreglo {

    public static void main(String[] args) {
        
        int[] num1 = {5, 2, 7, 4, 5};
        int[] num2 = {18, 19, 23};
        int[] num3 = {5, 5, 5};
        
        System.out.println("1: " + score(num1));
        System.out.println("2: " + score(num2));
        System.out.println("3: " + score(num3));
    }

    public static int score(int[] numbers) {
        int puntajeTotal = 0;
        
        for (int numero : numbers) {
            if (numero == 5) {
                puntajeTotal += 5;
            } else if (numero % 2 == 0) {
                puntajeTotal += 1;
            } else {
                puntajeTotal += 3;
            }
        }
        
        return puntajeTotal;
    }
}

//Complejidad temporal: El método recorre el arreglo una sola vez
// 
