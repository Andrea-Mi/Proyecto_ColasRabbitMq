package MinyMax;

public class MinyMax {

    public static void main(String[] args) {
       
        int[] caso1 = {7, 2, 9, 4, 1, 8};
        int[] caso2 = {5, 3, 9, 1, 10, 1};
        int[] caso3 = {10, 20, 30, 40, 50};
        
        int[] resultado1 = secondMinMax(caso1);
        System.out.println("1: [" + resultado1[0] + ", " + resultado1[1] + "]");
        
        int[] resultado2 = secondMinMax(caso2);
        System.out.println("2: [" + resultado2[0] + ", " + resultado2[1] + "]");
        
        int[] resultado3 = secondMinMax(caso3);
        System.out.println("3: [" + resultado3[0] + ", " + resultado3[1] + "]");
    }
    
    public static int[] secondMinMax(int[] numbers) {
        
        int menor = Integer.MAX_VALUE;
        int segundoMenor = Integer.MAX_VALUE;
        int mayor = Integer.MIN_VALUE;
        int segundoMayor = Integer.MIN_VALUE;
        
        
        for (int numero : numbers) {
            
            
            if (numero < menor) {
                segundoMenor = menor;
                menor = numero;
            } else if (numero < segundoMenor && numero != menor) {
                segundoMenor = numero;
            }
            
            if (numero > mayor) {
                segundoMayor = mayor;
                mayor = numero;
            } else if (numero > segundoMayor && numero != mayor) {
                segundoMayor = numero;
            }
        }
        
        return new int[] {segundoMenor, segundoMayor};
    }
}