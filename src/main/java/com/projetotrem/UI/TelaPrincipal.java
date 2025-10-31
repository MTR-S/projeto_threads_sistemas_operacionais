package com.projetotrem.UI;

import com.projetotrem.model.Empacotador;
import com.projetotrem.model.Trem;
import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class TelaPrincipal extends JFrame{
    // --- Backend ---
    private GerenteDaEstacao gerenteDaEstacao;

    // --- Configuração ---
    private final int M_CAPACIDADE_DEPOSITO;
    private final int N_CAPACIDADE_TREM;

    // --- Componentes da UI ---
    private JLabel lblStatusDeposito;
    private JLabel lblStatusTrem;
    private JPanel painelEmpacotadores; // Painel para adicionar JLabels dinamicamente

    // Mapa para guardar as JLabels de cada empacotador
    private Map<Integer, JLabel> mapaLabelsEmpacotadores;

    // Timer para atualizar a UI de forma segura
    private Timer timerAtualizacao;

    public TelaPrincipal(int m, int n, int numEmpacotadores, long tempoViagem, long tempoPacote) {
        // 1. Salvar configs
        this.M_CAPACIDADE_DEPOSITO = m;
        this.N_CAPACIDADE_TREM = n;

        // 2. Inicializar o Backend
        this.gerenteDaEstacao = new GerenteDaEstacao(n, m);
        this.mapaLabelsEmpacotadores = new HashMap<>();

        // 3. Configurar a Janela
        setTitle("Monitor da Simulação");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Sair da aplicação
        setLocationRelativeTo(null); // Centralizar

        // 4. Montar a UI
        setupUI();

        // 5. Iniciar as Threads do Backend
        iniciarThreadsBackend(numEmpacotadores, tempoViagem, tempoPacote);

        // 6. Iniciar o Timer que atualiza a UI
        iniciarTimerUI();
    }

    private void setupUI() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Painel de Status (Norte) ---
        JPanel painelStatus = new JPanel();
        // Usar BoxLayout para empilhar componentes verticalmente
        painelStatus.setLayout(new BoxLayout(painelStatus, BoxLayout.Y_AXIS));

        lblStatusDeposito = new JLabel("Depósito: 0 / " + M_CAPACIDADE_DEPOSITO);
        lblStatusDeposito.setFont(new Font("Arial", Font.BOLD, 16));

        lblStatusTrem = new JLabel("Trem: Parado");
        lblStatusTrem.setFont(new Font("Arial", Font.BOLD, 16));

        painelStatus.add(lblStatusDeposito);
        painelStatus.add(Box.createRigidArea(new Dimension(0, 5))); // Espaçamento
        painelStatus.add(lblStatusTrem);
        painelStatus.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        painelStatus.add(new JSeparator()); // Linha divisória

        // --- Painel de Empacotadores (Centro) ---
        painelEmpacotadores = new JPanel();
        painelEmpacotadores.setLayout(new BoxLayout(painelEmpacotadores, BoxLayout.Y_AXIS));

        // Adiciona um título ao painel
        painelEmpacotadores.setBorder(BorderFactory.createTitledBorder("Status dos Empacotadores"));

        // Colocamos o painel de empacotadores dentro de um JScrollPane
        // Isso cria uma barra de rolagem se houver muitos empacotadores
        JScrollPane scrollPane = new JScrollPane(painelEmpacotadores);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Adiciona os painéis principais à janela
        painelPrincipal.add(painelStatus, BorderLayout.NORTH);
        painelPrincipal.add(scrollPane, BorderLayout.CENTER);

        add(painelPrincipal);
    }

    private void iniciarThreadsBackend(int numEmpacotadores, long tempoViagem, long tempoPacote) {
        System.out.println("Iniciando threads da simulação...");

        // Inicia N empacotadores
        for (int i = 0; i < numEmpacotadores; i++) {
            // Criamos IDs únicos (ex: 1, 2, 3...)
            int id = i + 1;
            new Thread(new Empacotador(gerenteDaEstacao, id, tempoPacote)).start();
        }

        System.out.println("Iniciando a thread do Trem...");
        new Thread(new Trem(gerenteDaEstacao, tempoViagem)).start();

        System.out.println("Threads iniciadas.");
    }

    private void iniciarTimerUI() {
        // Cria um Timer que "dispara" a cada 100 milissegundos
        timerAtualizacao = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Chama o método que realmente atualiza a UI
                atualizarStatusUI();
            }
        });
        timerAtualizacao.start(); // Inicia o timer
    }

    /**
     * Este método é chamado pelo Timer a cada 100ms.
     * Ele roda na Thread de UI do Swing (EDT), sendo seguro
     * para atualizar os JLabels.
     */
    private void atualizarStatusUI() {
        // 1. Atualizar Depósito
        int caixas = gerenteDaEstacao.getQuantidadeDeCaixasDepositadas();
        lblStatusDeposito.setText("Depósito: " + caixas + " / " + M_CAPACIDADE_DEPOSITO);

        // 2. Atualizar Trem
        String statusTrem = gerenteDaEstacao.getStatusDoTrem();
        lblStatusTrem.setText("Trem: " + statusTrem);
        // 3. Atualizar Empacotadores (a parte mais dinâmica)

        // Pega uma *cópia* segura do mapa de status
        Map<Integer, String> statusMap = gerenteDaEstacao.getStatusDosEmpacotadores();

        for (Map.Entry<Integer, String> entry : statusMap.entrySet()) {
            int id = entry.getKey();
            String status = entry.getValue();

            JLabel lbl = mapaLabelsEmpacotadores.get(id);

            if (lbl == null) {
                // Se a Label não existe, a criamos!
                lbl = new JLabel();
                lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));

                // Adiciona no nosso mapa de controle
                mapaLabelsEmpacotadores.put(id, lbl);

                // Adiciona fisicamente no painel da UI
                painelEmpacotadores.add(lbl);
                painelEmpacotadores.revalidate(); // Avisa o Swing para redesenhar
            }

            // Atualiza o texto da label
            lbl.setText(String.format("Empacotador [%d]: %s", id, status));
        }
    }
}
