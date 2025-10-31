package com.projetotrem;

import com.projetotrem.model.Empacotador;
import com.projetotrem.sync.GerenteDaEstacao;
import java.util.Scanner;

public class Main {
    static int idNovoEmpacotador = 0;

    public static void main(String[] args) {
        /*Scanner sc = new Scanner(System.in);

        System.out.println("Digite a capacidade máxima do trem");
        int capacidadeTrem = sc.nextInt();

        System.out.println("Digite a capacidade máxima do deposito");
        int capacidadeDeposito = sc.nextInt();

        GerenteDaEstacao gerente = new GerenteDaEstacao(capacidadeTrem, capacidadeDeposito);

        Thread thread = new Thread(gerente::getStatusSistema);
        gerente.createTrem(0, sc);

        System.out.println("Quer criar um empacotador? \n Digite sim");

        int op = sc.nextInt();

        thread.start();

        while(true){
            if(op == 1){
                gerente.createEmpacotador(idNovoEmpacotador, sc);
                incrementEmpacotadorId();
            }
            System.out.println("Quer criar um empacotador? 1=sim");
            op = sc.nextInt();
        }
    }

    public static void incrementEmpacotadorId(){
        idNovoEmpacotador++;*/
    }
}