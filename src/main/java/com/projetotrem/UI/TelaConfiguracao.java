package com.projetotrem.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaConfiguracao extends JFrame {

    private JTextField txtCapacidadeDepositoM;
    private JTextField txtCapacidadeTremN;
    private JTextField txtTempoViagemTrem;
    private JTextField txtTempoEmpacotamento;

    private JButton btnIniciarSimulacao;

    public TelaConfiguracao() {

        setTitle("Configuração da Simulação - Trem de Carga");
        setSize(400, 220); // Reduzido um pouco a altura
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelFormulario = new JPanel(new GridLayout(0, 2, 5, 5));

        painelFormulario.add(new JLabel("Capacidade Depósito (M):"));
        txtCapacidadeDepositoM = new JTextField("10");
        painelFormulario.add(txtCapacidadeDepositoM);

        painelFormulario.add(new JLabel("Capacidade Trem (N):"));
        txtCapacidadeTremN = new JTextField("5");
        painelFormulario.add(txtCapacidadeTremN);

        painelFormulario.add(new JLabel("Tempo Viagem Trem (ms):"));
        txtTempoViagemTrem = new JTextField("5000");
        painelFormulario.add(txtTempoViagemTrem);

        painelFormulario.add(new JLabel("Tempo Padrão Pacote (ms):"));
        txtTempoEmpacotamento = new JTextField("2000");
        painelFormulario.add(txtTempoEmpacotamento);

        btnIniciarSimulacao = new JButton("Iniciar Simulação");

        painelPrincipal.add(painelFormulario, BorderLayout.CENTER);
        painelPrincipal.add(btnIniciarSimulacao, BorderLayout.SOUTH);

        add(painelPrincipal);

        btnIniciarSimulacao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Chama o método que vai ler e validar os dados
                iniciarSimulacao();
            }
        });
    }

    private void iniciarSimulacao() {
        try {
            int m = Integer.parseInt(txtCapacidadeDepositoM.getText());
            int n = Integer.parseInt(txtCapacidadeTremN.getText());

            long tempoViagem = Long.parseLong(txtTempoViagemTrem.getText());
            long tempoPacotePadrao = Long.parseLong(txtTempoEmpacotamento.getText());

            if (m < n) {
                // Mostra uma popup de erro (JOptionPane)
                JOptionPane.showMessageDialog(this,
                        "Erro: A Capacidade do Depósito (M) deve ser >= que a do Trem (N).",
                        "Erro de Validação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("--- Configuração Pronta ---");
            System.out.println("Depósito (M): " + m);
            System.out.println("Trem (N): " + n);
            System.out.println("Tempo Viagem: " + tempoViagem);
            System.out.println("Tempo Pacote Padrão: " + tempoPacotePadrao);
            System.out.println("---------------------------");

            TelaPrincipalOriginal telaVisual = new TelaPrincipalOriginal(m, n, tempoViagem, tempoPacotePadrao);
            telaVisual.setVisible(true);

            this.dispose();

        } catch (NumberFormatException ex) {
            // Captura erro se o usuário digitar "abc" em vez de "123"
            JOptionPane.showMessageDialog(this,
                    "Por favor, insira apenas números válidos nos campos.",
                    "Erro de Formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Método responsável por criar a interface swing

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaConfiguracao().setVisible(true);
            }
        });
    }
}